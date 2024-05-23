package org.roda.core.security;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
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
      boolean enabled = RodaCoreFactory.getProperty("core.plugins.security.enable", false);
      List<String> list = RodaCoreFactory.getRodaConfigurationAsList("core.plugins.security.list");
      return enabled && list.contains(oClassname.get());
    }
    return false;
  }
}
