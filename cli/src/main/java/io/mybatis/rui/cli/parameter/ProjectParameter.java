package io.mybatis.rui.cli.parameter;

import com.beust.jcommander.IStringConverter;
import io.mybatis.rui.template.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectParameter implements IStringConverter<Project> {

  @Override
  public Project convert(String s) {
    Path path = Paths.get(s);
    File file = path.toFile();
    return Project.load(file.getAbsolutePath());
  }

}
