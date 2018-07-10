package com.bdp.auth.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.bdp.framework.biz.UserBiz;
import com.bdp.framework.entity.User;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

	/**
	 * 要用与用户名密码登录同一个PasswordEncoder
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserBiz userBiz;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory().withClient("bdp").secret(passwordEncoder.encode("bdp"))
				.authorizedGrantTypes("authorization_code", "refresh_token").scopes("all").autoApprove(true)// 很有用，自动授权，跳过认证服务器的授权页面，提升用户体验
				.and().withClient("third").secret("third").authorizedGrantTypes("authorization_code", "refresh_token")
				.scopes("all").autoApprove(true);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(jwtTokenStore()).accessTokenConverter(jwtAccessTokenConverter());
	}

	/**
	 * 认证服务器的安全配置
	 * 
	 * @param security
	 * @throws Exception
	 */
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		// 该配置表示在使用JWT时客户端或资源服务器在获取token_key时需要认证，即访问oauth/token_key路径。
		// 此时客户端会把clientId和clientSecret发到认证服务器上做认证。
		security.tokenKeyAccess("isAuthenticated()");
	}

	@Bean
	public TokenStore jwtTokenStore() {
		return new JwtTokenStore(jwtAccessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		// 客户端或资源服务器通过oauth/token_key路径获取该值
		converter.setSigningKey("bdp-auth-token-key");
		return converter;
	}

	@Bean
	public TokenEnhancer jwtTokenEnhancer() {
		return new JwtTokenEnhancer();
	}

	class JwtTokenEnhancer implements TokenEnhancer {
		@Override
		public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
			String username = (String) authentication.getPrincipal();
			User user = userBiz.readByName(username);
			if (user != null) {
				Map<String, Object> infos = new HashMap<>();
				infos.put("UserID", user.getId());
				infos.put("Username", user.getUsername());
				infos.put("RealName", user.getRealname());
				infos.put("Sex", user.getSex());
				infos.put("Birthday", user.getBirthday());
				infos.put("Address", user.getAddress());
				((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(infos);
			}
			return accessToken;
		}
	}
}