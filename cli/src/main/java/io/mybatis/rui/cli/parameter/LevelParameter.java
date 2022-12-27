package io.mybatis.rui.cli.parameter;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import org.slf4j.event.Level;

public class LevelParameter implements IParameterValidator, IStringConverter<Level> {

  @Override
  public Level convert(String value) {
    return Level.valueOf(value.toUpperCase());
  }

  @Override
  public void validate(String name, String value) throws ParameterException {
    Level.valueOf(value.toUpperCase());
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", value);
  }

}
