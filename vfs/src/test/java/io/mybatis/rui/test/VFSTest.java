package io.mybatis.rui.test;

import cn.hutool.core.io.FileUtil;
import io.mybatis.rui.VFS;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VFSTest {

  String userDir = System.getProperty("user.dir");

  @Test
  public void test() {
    String userDir = System.getProperty("user.dir");
    //绝对路径
    VFS vfs = VFS.of(userDir);
    //绝对路径
    vfs.mkdirs(new File(userDir + File.separator + "doc"));
    //创建相对路径的目录
    vfs.mkdirs("src/main/java");
    //写入文件
    vfs.write("README.md", "# Hello VFS");
    //写入绝对路径文件(相对userDir)
    vfs.write(new File(userDir + File.separator + "pom.xml"),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    //输出文件结构
    System.out.println(vfs.print());
    //写入到指定磁盘目录
    vfs.syncDisk(new File(userDir, "target/Hello"));
    //输出到压缩文件
    vfs.syncDisk(new File(userDir, "target/Hello.zip"));
  }

  @Test
  public void testPath() {
    System.out.println(Paths.get("").toAbsolutePath());
    System.out.println(Paths.get("abc").toAbsolutePath());
    System.out.println(Paths.get("/abc").toAbsolutePath());
    System.out.println(Paths.get("d:/").toAbsolutePath());
    System.out.println(Paths.get("d:/").toAbsolutePath());
    System.out.println(Paths.get("1234").toAbsolutePath());
    System.out.println(Paths.get("asdf-123").toAbsolutePath());
    System.out.println("-------------------------------");
    Path a = Paths.get("/a");
    Path abc = Paths.get("/a/b/c");
    System.out.println(a);
    System.out.println(a.toAbsolutePath());
    System.out.println(abc);
    System.out.println(abc.toAbsolutePath());
    System.out.println(a.relativize(abc));
    System.out.println(a.relativize(abc).toAbsolutePath());
    System.out.println("-------------------------------");
    System.out.println(Paths.get("/").relativize(Paths.get("/a/c")));
    System.out.println(Paths.get("/a").relativize(Paths.get("/a/c")));
    System.out.println(Paths.get("/a/").relativize(Paths.get("/a/c")));
    System.out.println(Paths.get("/c").relativize(Paths.get("/a/c")));
    System.out.println(Paths.get("/a/c").relativize(Paths.get("/a/c")));
    System.out.println(Paths.get("/a/c/").relativize(Paths.get("/a/c")));
    System.out.println(Paths.get("/a/b").relativize(Paths.get("/a/c")));
    System.out.println("-------------------------------");
    //这里会出现 ../../ 父目录的操作，目前VFS的根是固定的，不能超出范围
    Path relativePath = Paths.get("/a/b/c/d/e/f").relativize(Paths.get("/a/b/h/i/j"));
    System.out.println(relativePath);
    for (int i = 0; i < relativePath.getNameCount(); i++) {
      System.out.println(relativePath.getName(i));
    }
    System.out.println("-------------------------------");
    relativePath = Paths.get("/a/c").relativize(Paths.get("/a/c/e"));
    System.out.println(relativePath);
    for (int i = 0; i < relativePath.getNameCount(); i++) {
      System.out.println(relativePath.getName(i));
    }
  }

  @Test
  public void testVFSPath() {
    VFS vfs = VFS.of("/");
    vfs.mkdirs("/a");
    vfs.mkdirs("/a/b");
    vfs.mkdirs("/a/c");
    vfs.mkdirs("/a/c");
    vfs.mkdirs("/a/d/e.txt");
    vfs.write("/a/help.txt", "帮助文档");
    vfs.delelte("/a/c");
    System.out.println(vfs.print());
    vfs.syncDisk(new File("vfs-syncdisk.zip"));
    System.out.println(vfs.print());
    vfs.mkdirs("/a/d/f");
    vfs.mkdirs("/a2/b2/c2");
    System.out.println(vfs.print());
    vfs.delelte("/a/c");
    System.out.println(vfs.print());
    vfs.delelte("/a/d");
    System.out.println(vfs.print());
    vfs.write("/a/README.md", "# Hello\nWorld!");
    System.out.println(vfs.print());
    File file = new File(userDir + "/target/testPath");
    System.out.println("写入目录: " + file.getAbsolutePath());
    vfs.syncDisk(file);
  }

  @Test
  public void testV() {
    String basePath = "test123";
    VFS vfs = VFS.of(basePath);
    vfs.write(new File(basePath, "/b/c/d/e/f/h.txt"), "Hello");
    System.out.println(vfs.print());
    vfs.syncDisk(new File(userDir, "target/basePath"));
  }

  @Test
  public void testLoad() {
    VFS vfs = VFS.load(new File(userDir));
    System.out.println(vfs.print());
    vfs.syncDisk(new File(userDir, "target/loadFile.zip"));
  }

  @Test
  public void testLoadZip() throws IOException {
    VFS vfs = VFS.load(FileUtil.file("tk-mapper.zip"));
    System.out.println(vfs.print());
    vfs.mkdirs("hello");
    vfs.syncDisk();
    vfs.syncDisk(new File(userDir + "/target/loadZip"));
    vfs.syncDisk(new File(userDir + "/target/tk-mapper-zip.zip"));
  }

}
