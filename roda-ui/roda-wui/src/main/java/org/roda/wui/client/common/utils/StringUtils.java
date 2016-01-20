package org.roda.wui.client.common.utils;

public class StringUtils {

  public static final boolean isNotBlank(String s) {
    return s != null && s.trim().length() > 0;
  }
}
