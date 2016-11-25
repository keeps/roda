package org.roda.core.common.dips;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.data.v2.ip.DIP;

public class DIPUtils {
  public static Optional<String> getCompleteOpenExternalURL(DIP dip) {
    if (StringUtils.isNotBlank(dip.getType())) {
      String config = RodaCoreFactory.getRodaConfigurationAsString("dip", dip.getType(), "openExternalURL");
      if (StringUtils.isNotBlank(config)) {
        return Optional.of(HandlebarsUtility.executeHandlebars(config, dip.getProperties()));
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

  public static Optional<String> getCompleteDeleteExternalURL(DIP dip) {
    if (StringUtils.isNotBlank(dip.getType())) {
      String config = RodaCoreFactory.getRodaConfigurationAsString("dip", dip.getType(), "deleteExternalURL");
      if (StringUtils.isNotBlank(config)) {
        return Optional.of(HandlebarsUtility.executeHandlebars(config, dip.getProperties()));
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }
}
