package io.mybatis.rui;

import io.mybatis.rui.template.struct.Merge;
import io.mybatis.rui.template.struct.Structure;
import io.mybatis.rui.template.struct.UserCodesMerge;
import org.junit.Assert;
import org.junit.Test;

public class MergeTest {

  @Test
  public void test() {
    String before = "" +
        "public class Hello {\r\n" +
        "//USER CODES START\r\n" +
        "自己写的内容不会丢1\r\n" +
        "<!-- USER CODES END -->\r\n" +
        "Hello World\r\n" +
        "//USER CODES START\r\n" +
        "自己写的内容不会丢2\r\n" +
        "<!-- USER CODES END -->\r\n" +
        "}";
    String after = "public class World {\r\n" +
        "//USER CODES START\r\n" +
        "\r\n" +
        "<!-- USER CODES END -->\r\n" +
        "Hello World 2\r\n" +
        "//USER CODES START\r\n" +
        "\r\n" +
        "<!-- USER CODES END -->\r\n" +
        "}";

    String result = "" +
        "public class World {\r\n" +
        "//USER CODES START\r\n" +
        "自己写的内容不会丢1\r\n" +
        "<!-- USER CODES END -->\r\n" +
        "Hello World 2\r\n" +
        "//USER CODES START\r\n" +
        "自己写的内容不会丢2\r\n" +
        "<!-- USER CODES END -->\r\n" +
        "}";

    System.out.println("------------------------before--------------------");
    System.out.println(before);
    System.out.println("------------------------before--------------------");

    System.out.println("------------------------after--------------------");
    System.out.println(after);
    System.out.println("------------------------after--------------------");

    Merge userCodesMerge = new UserCodesMerge();
    String merge = userCodesMerge.merge(null, new Structure(), null, "fileName", before, after);
    System.out.println("------------------------merge--------------------");
    System.out.println(merge);
    System.out.println("------------------------merge--------------------");

    Assert.assertEquals(result, merge);
  }
}
