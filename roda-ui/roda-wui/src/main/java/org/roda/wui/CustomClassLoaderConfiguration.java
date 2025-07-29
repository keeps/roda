/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class CustomClassLoaderConfiguration {
  @Bean
  public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
    return beanFactory -> {
      beanFactory.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
    };
  }
}
