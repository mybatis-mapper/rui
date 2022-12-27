package ${package};

import org.springframework.stereotype.Service;

import java.util.List;
import tk.mybatis.mapper.entity.Example;

/**
 * 通用接口
 */
@Service
public interface IService<T> {

  T selectByKey(Object key);

  List<T> selectPage(T t, int page, int rows);

  int save(T entity);

  int delete(Object key);

  int updateAll(T entity);

  int updateNotNull(T entity);

  List<T> selectByExample(Example example);

}