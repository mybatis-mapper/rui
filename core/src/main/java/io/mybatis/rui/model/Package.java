package io.mybatis.rui.model;

import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 封装包名，方便在模板中找上级
 */
@Getter
public class Package {
  public static final Map<String, Package> PACKAGE_MAP       = new ConcurrentHashMap<>();
  public static final String               PACKAGE_SEPARATOR = ".";
  public static final String               PACKAGE_NAME      = "package";
  private final       String               name;
  private             Package              parent;

  private Package(String name) {
    this.name = name;
    //找 parent
    if (this.name.contains(PACKAGE_SEPARATOR)) {
      String parent = this.name.substring(0, this.name.lastIndexOf(PACKAGE_SEPARATOR));
      this.parent = of(parent);
    }
  }

  public static Package of(@NonNull String name) {
    if (!PACKAGE_MAP.containsKey(name)) {
      PACKAGE_MAP.put(name, new Package(name));
    }
    return PACKAGE_MAP.get(name);
  }

  /**
   * 初始化包参数
   *
   * @param params
   * @param name
   */
  public static void initPackage(Map<String, Object> params, String name) {
    //将当前包记录下面，作为子级目录的上级供子级使用
    Package pck = (Package) params.get(PACKAGE_NAME);
    if (pck != null) {
      pck = pck.subPackage(name);
    } else {
      pck = of(name);
    }
    params.put(PACKAGE_NAME, pck);
  }

  /**
   * 获取子包
   *
   * @param name
   * @return
   */
  public Package subPackage(String name) {
    return of(this.name + "." + name);
  }

  @Override
  public String toString() {
    return name;
  }
}
