package org.roda.core.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class SpringContext implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext ctx) {
    context = ctx;
  }

  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

  public static boolean isContextInitialized() {
    return context != null;
  }
}