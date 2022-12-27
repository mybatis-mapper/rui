package io.mybatis.rui;

import org.junit.Test;
import org.mvel2.MVEL;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Mvel2Test {

  @Test
  public void test() {
    Map map = new HashMap<>();
    map.put("name", "user_role");
    System.out.println(MVEL.evalToBoolean("name.startsWith(\"user1_\") || name.endsWith(\"le\")", map));
  }

}
