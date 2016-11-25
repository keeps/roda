package org.roda.core.common;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class HandlebarsUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(HandlebarsUtility.class);

  public static String executeHandlebars(String template, Map<String, ?> scopes) {
    Handlebars handlebars = new Handlebars();
    String result = "";
    try {
      Template templ = handlebars.compileInline(template);
      result = templ.apply(scopes);
    } catch (IOException e) {
      LOGGER.error("Error executing handlebars", e);
    }
    return result;
  }
}
