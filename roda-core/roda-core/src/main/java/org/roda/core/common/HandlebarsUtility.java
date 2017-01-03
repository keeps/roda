package org.roda.core.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.GenericException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class HandlebarsUtility {
  private static final Handlebars HANDLEBARS = new Handlebars();
  private static final String HELPER_FIELD = "field";
  private static final String HELPER_IF = "ifCond";
  private static final String CONDITION_AND = "&&";
  private static final String CONDITION_OR = "||";

  private HandlebarsUtility() {
    super();
  }

  static {
    HANDLEBARS.registerHelper(HELPER_FIELD, (o, options) -> options.fn());
    HANDLEBARS.registerHelper(HELPER_IF, (context, options) -> {
      // the first parameter of ifCond is placed in the context field by the
      // parser
      String condition = (context == null) ? CONDITION_OR : context.toString();
      List<Object> values = Arrays.asList(options.params);
      boolean display;
      if (condition.equals(CONDITION_OR)) {
        display = false;
        for (Object value : values) {
          if (value != null) {
            display = true;
            break;
          }
        }
      } else if (condition.equals(CONDITION_AND)) {
        display = true;
        for (Object value : values) {
          if (value == null) {
            display = false;
            break;
          }
        }
      } else {
        display = false;
      }
      return display ? options.fn() : options.inverse();
    });
  }

  public static final Handlebars getHandlebars() {
    return HANDLEBARS;
  }

  public static String executeHandlebars(String template, Map<String, ?> scopes) throws GenericException {
    try {
      Template templ = getHandlebars().compileInline(template);
      return templ.apply(scopes);
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

}
