package io.mybatis.rui.template;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.Objects;

/**
 * 引用文件，通过复用的方式引用其他 YAML
 *
 * @param <R>
 */
@Slf4j(topic = "Ref")
@SuppressWarnings("unchecked")
public abstract class Ref<R extends Ref> {
  @Getter
  private           String ref;
  private transient R      r;

  /**
   * 从资源文件加载
   *
   * @param resource
   * @param clazz
   * @param <R>
   * @return
   */
  public static <R> R load(String resource, Class<R> clazz) {
    Yaml yaml = new Yaml(new Constructor(clazz));
    InputStream inputStream = null;
    try {
      inputStream = ResourceUtil.getStream(resource);
      R load = yaml.load(inputStream);
      ((Ref) load).ref = resource;
      ((Ref) load).r = (Ref) load;
      return load;
    } finally {
      IoUtil.close(inputStream);
    }
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  /**
   * 获取引用的对象
   *
   * @return
   */
  public R getR() {
    if (r != null) {
      return r;
    } else if (Objects.nonNull(ref)) {
      log.info("读取引用配置文件: " + ref);
      Class<?> clazz = (Class<?>) GenericTypeResolver.resolveType(Ref.class.getTypeParameters()[0], getClass(), Ref.class);
      r = (R) load(ref, clazz);
      return r;
    } else {
      r = (R) this;
      return r;
    }
  }
}
