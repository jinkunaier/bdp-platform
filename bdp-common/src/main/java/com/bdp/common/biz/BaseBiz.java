package com.bdp.common.biz;

import com.bdp.common.msg.TableResultResponse;
import com.bdp.common.util.EntityUtils;
import com.bdp.common.util.Query;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * 业务逻辑基类
 * 
 * @author jack
 *
 * @param <M>
 * @param <T>
 */
public abstract class BaseBiz<M extends Mapper<T>, T> {
	@Autowired
	protected M mapper;

	public void setMapper(M mapper) {
		this.mapper = mapper;
	}

	public T selectOne(T entity) {
		return mapper.selectOne(entity);
	}

	public T selectById(Object id) {
		return mapper.selectByPrimaryKey(id);
	}

	public List<T> selectList(T entity) {
		return mapper.select(entity);
	}

	public List<T> selectListAll() {
		return mapper.selectAll();
	}

	public Long selectCount(T entity) {
		return new Long(mapper.selectCount(entity));
	}

	public void insert(T entity) {
		EntityUtils.setCreatAndUpdatInfo(entity);
		mapper.insert(entity);
	}

	public void insertSelective(T entity) {
		EntityUtils.setCreatAndUpdatInfo(entity);
		mapper.insertSelective(entity);
	}

	public void delete(T entity) {
		mapper.delete(entity);
	}

	public void deleteById(Object id) {
		mapper.deleteByPrimaryKey(id);
	}

	public void updateById(T entity) {
		EntityUtils.setUpdatedInfo(entity);
		mapper.updateByPrimaryKey(entity);
	}

	public void updateSelectiveById(T entity) {
		EntityUtils.setUpdatedInfo(entity);
		mapper.updateByPrimaryKeySelective(entity);

	}

	public List<T> selectByExample(Example example) {
		return mapper.selectByExample(example);
	}

	public T selectOneByExample(Example example) {
		List<T> list = mapper.selectByExample(example);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	public int deleteByExample(Example example) {
		return mapper.deleteByExample(example);
	}

	public int selectCountByExample(Example example) {
		return mapper.selectCountByExample(example);
	}

	@SuppressWarnings("unchecked")
	public TableResultResponse<T> selectByQuery(Query query) {
		Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
		Example example = new Example(clazz);
		Example.Criteria criteria = example.createCriteria();
		for (Map.Entry<String, Object> entry : query.entrySet()) {
			criteria.andLike(entry.getKey(), "%" + entry.getValue().toString() + "%");
		}
		Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
		List<T> list = mapper.selectByExample(example);
		return new TableResultResponse<T>(result.getTotal(), list);
	}

}