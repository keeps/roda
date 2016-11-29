package org.roda.core.common.dips;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.data.v2.ip.DIP;

public class DIPUtils {

  public static Optional<String> getCompleteOpenExternalURL(DIP dip) {
    return getDIPProperty(dip, "dip", dip.getType(), "openExternalURL")
      .map(value -> HandlebarsUtility.executeHandlebars(value, dip.getProperties()));
  }

  public static Optional<String> getCompleteDeleteExternalURL(DIP dip) {
    return getDIPProperty(dip, "dip", dip.getType(), "deleteExternalURL")
      .map(value -> HandlebarsUtility.executeHandlebars(value, dip.getProperties()));
  }

  public static Optional<String> getDeleteMethod(DIP dip) {
    return getDIPProperty(dip, "dip", dip.getType(), "deleteExternalURL", "method");
  }

  private static Optional<String> getDIPProperty(DIP dip, String... property) {
    Optional<String> ret = Optional.empty();
    if (StringUtils.isNotBlank(dip.getType())) {
      String method = RodaCoreFactory.getRodaConfigurationAsString(property);
      if (StringUtils.isNotBlank(method)) {
        ret = Optional.of(method);
      }
    }
    return ret;
  }
}
