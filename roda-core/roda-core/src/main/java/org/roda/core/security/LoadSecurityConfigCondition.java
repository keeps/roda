package org.roda.core.security;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
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
      boolean enabled = RodaCoreFactory.getProperty(RodaConstants.SECURITY_PLUGINS_ENABLE_PROPERTY, false);
      List<String> list = RodaCoreFactory
        .getRodaConfigurationAsList(RodaConstants.SECURITY_PLUGINS_CONFIGURATIONS_PROPERTY);
      return enabled && list.contains(oClassname.get());
    }
    return false;
  }
}
