package com.bdp.framework.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("frameworkWebConfig")
public class WebConfig implements WebMvcConfigurer {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 注意通配符是**号，addResourceLocations中使用classpath且以/结尾表示路径
		registry.addResourceHandler("/framework/**").addResourceLocations("classpath:/static/framework/",
				"classpath:/templates/framework/");

		// 映射一个static目录，因为其它项目的静态资源文件放在static目录，这里统一映射
		if (!registry.hasMappingForPattern("/static/**")) {
			registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
		}
	}

	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new ProxyHeaderInterceptor());
	}

	/**
	 * 设置Zuul Proxy 映射的路径，通过这种方式，可以同时直接访问和Zuul代理访问或者
	 * 
	 * @author jack
	 *
	 */
	class ProxyHeaderInterceptor implements HandlerInterceptor {
		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {
			String zuulProxy = request.getHeader("zuulProxy");
			if (StringUtils.isNotEmpty(zuulProxy)) {
				// 设置在request中的属性可以直接被thymeleaf通过${}引用
				request.setAttribute("zuulProxy", "/" + zuulProxy);
			} else {
				request.setAttribute("zuulProxy", "");
			}
			return true;
		}
	}
}
