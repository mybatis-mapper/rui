package io.mybatis.rui.cli.parameter;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class DriverParameter implements IParameterValidator, IStringConverter<Class> {

  @Override
  public Class convert(String value) {
    try {
      return Class.forName(value);
    } catch (ClassNotFoundException e) {
      throw new ParameterException(e);
    }
  }

  @Override
  public void validate(String name, String value) throws ParameterException {
    try {
      Class.forName(value);
    } catch (Exception e) {
      throw new ParameterException(name + " 指定的驱动类 " + value + " 不存在，请确保在当前 classpath 下可以找到该驱动");
    }
  }

}
