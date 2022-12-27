package io.mybatis.rui;

import cn.hutool.core.io.FileUtil;
import io.mybatis.rui.template.Project;
import io.mybatis.rui.template.vfs.VFSGenFileSystem;
import io.mybatis.rui.template.vfs.VFSTemplateFileSystem;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTest {

  @Test
  public void testSimple() {
    Project.load("simple-demo/demo.yaml").generate();
  }

  @Test
  public void testMyBatisSpring() {
    Project.load("mybatis-spring-demo/generator.yaml").generate();
  }

  @Test
  public void testMyBatisMapper() {
    Project.load("mybatis-mapper-demo/generator-demo.yaml").generate();
  }

  @Test
  public void testMyBatisMapperSimple() {
    Project project = Project.load("mybatis-mapper-demo/generator-simple-demo.yaml");
    VFS vfs = project.preview();
    System.out.println(vfs.print());
    //真正生成
    project.generate();
  }

  @Test
  public void testCommonMapper() {
    Project project = Project.load("tk-mapper/generator-demo.yaml");
    project.preview().syncDisk(new File("tk-mapper-demo.zip"));
    project.generate();
  }

  @Test
  public void testVFS() {
    Project project = Project.load("tk-mapper/generator-demo.yaml");
    VFSGenFileSystem genFileSystem = new VFSGenFileSystem();

    VFS vfs = VFS.load(FileUtil.file("tk-mapper"));
    VFSTemplateFileSystem templateFileSystem = new VFSTemplateFileSystem(vfs);

    project.getContext().setGenFileSystem(genFileSystem);
    project.getContext().setTemplateFileSystem(templateFileSystem);
    project.generate();
    VFS vfsWrite = genFileSystem.getVfs();
    System.out.println(vfsWrite.print());
    //模板存为压缩包
    vfs.syncDisk(FileUtil.file("testCommonMapperTemplate.zip"));
    vfsWrite.syncDisk(FileUtil.file("testCommonMapperCode.zip"));
  }

  @Test
  public void testIter() {
    Project project = Project.load("iter.yaml");

    List<Map<String, Object>> pprev = null;
    List<Map<String, Object>> prev = null;
    List<Map<String, Object>> curr = null;
    Map<String, Object> map;
    for (int i = 7; i > 0; i--) {
      curr = new ArrayList<>();
      map = new HashMap<>();
      curr.add(map);
      map.put("name", "iter" + i);
      if (prev != null) {
        map.put("iters", prev);
        pprev = prev;
      }
      prev = curr;
    }
    curr.add(pprev.get(0));
    project.addDataSource("iters", curr);
    project.generate();
  }

}
