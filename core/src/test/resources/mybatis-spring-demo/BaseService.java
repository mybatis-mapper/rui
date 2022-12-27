package ${package};

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.github.pagehelper.PageHelper;

import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.common.Mapper;

public abstract class BaseService<T> implements IService<T> {

  @Autowired
  protected Mapper<T> mapper;

  public Mapper<T> getMapper() {
    return mapper;
  }

  @Override
  public T selectByKey(Object key) {
    return mapper.selectByPrimaryKey(key);
  }

  @Override
  public List<T> selectPage(T t, int page, int rows) {
    PageHelper.startPage(page, rows);
    return mapper.select(t);
  }

  @Override
  public int save(T entity) {
    return mapper.insert(entity);
  }

  @Override
  public int delete(Object key) {
    return mapper.deleteByPrimaryKey(key);
  }

  @Override
  public int updateAll(T entity) {
    return mapper.updateByPrimaryKey(entity);
  }

  @Override
  public int updateNotNull(T entity) {
    return mapper.updateByPrimaryKeySelective(entity);
  }

  @Override
  public List<T> selectByExample(Example example) {
    return mapper.selectByExample(example);
  }

}
