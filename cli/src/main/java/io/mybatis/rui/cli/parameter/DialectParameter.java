package io.mybatis.rui.cli.parameter;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import io.mybatis.rui.template.database.Dialect;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DialectParameter implements IParameterValidator, IStringConverter<Dialect> {

  @Override
  public Dialect convert(String value) {
    return Dialect.valueOf(value);
  }

  @Override
  public void validate(String name, String value) throws ParameterException {
    try {
      Dialect.valueOf(value);
    } catch (Exception e) {
      throw new ParameterException(name + " 的参数值 " + value + " 无效，可选值为: "
          + Arrays.stream(Dialect.values()).map(Dialect::name).collect(Collectors.joining(",")));
    }
  }

}
