package io.mybatis.rui;

import io.mybatis.rui.model.Name;
import org.junit.Assert;
import org.junit.Test;

public class NameTest {

  @Test
  public void test() {
    Name id = Name.of("ID");
    Assert.assertEquals("id", id.getFieldName().toString());
    Assert.assertEquals("Id", id.getClassName().toString());

    Name userName = Name.of("USER_NAME");
    Assert.assertEquals("userName", userName.getFieldName().toString());
    Assert.assertEquals("UserName", userName.getClassName().toString());
  }

}
