/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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