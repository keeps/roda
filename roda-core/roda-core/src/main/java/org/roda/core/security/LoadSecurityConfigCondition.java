/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.security;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.config.ConfigurationManager;
import org.roda.core.data.common.RodaConstants;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class LoadSecurityConfigCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Optional<String> oClassname = metadata.getAnnotations().stream()
      .map(a -> Objects.requireNonNull(a.getSource()).toString()).distinct().findAny();

    if (oClassname.isPresent()) {
      boolean enabled = ConfigurationManager.getInstance().getProperty(RodaConstants.SECURITY_PLUGINS_ENABLE_PROPERTY,
        false);
      List<String> list = RodaCoreFactory
        .getRodaConfigurationAsList(RodaConstants.SECURITY_PLUGINS_CONFIGURATIONS_PROPERTY);
      return enabled && list.contains(oClassname.get());
    }
    return false;
  }
}
