/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.dips;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.DIP;

public class DIPUtils {

  private DIPUtils() {
    // do nothing
  }

  public static OptionalWithCause<String> getCompleteOpenExternalURL(DIP dip) {
    OptionalWithCause<String> ret;
    Optional<String> dipProperty = getDIPProperty(dip, RodaConstants.RODA_OBJECT_DIP, dip.getType(), "openExternalURL");

    if (dipProperty.isPresent()) {
      try {
        String externalUrl = HandlebarsUtility.executeHandlebars(dipProperty.get(), dip.getProperties());
        ret = OptionalWithCause.of(externalUrl);
      } catch (GenericException e) {
        ret = OptionalWithCause.empty(e);
      }
    } else {
      ret = OptionalWithCause.of(Optional.empty());
    }

    return ret;
  }

  public static OptionalWithCause<String> getCompleteDeleteExternalURL(DIP dip) {
    OptionalWithCause<String> ret;
    Optional<String> dipProperty = getDIPProperty(dip, RodaConstants.RODA_OBJECT_DIP, dip.getType(),
      "deleteExternalURL");

    if (dipProperty.isPresent()) {
      try {
        String externalUrl = HandlebarsUtility.executeHandlebars(dipProperty.get(), dip.getProperties());
        ret = OptionalWithCause.of(externalUrl);
      } catch (GenericException e) {
        ret = OptionalWithCause.empty(e);
      }
    } else {
      ret = OptionalWithCause.of(Optional.empty());
    }

    return ret;
  }

  public static Optional<String> getDeleteMethod(DIP dip) {
    return getDIPProperty(dip, RodaConstants.RODA_OBJECT_DIP, dip.getType(), "deleteExternalURL", "method");
  }

  public static Optional<Pair<String, String>> getDeleteCredentials(DIP dip) {
    Optional<String> username = getDIPProperty(dip, RodaConstants.RODA_OBJECT_DIP, dip.getType(), "credentials",
      "username");
    Optional<String> password = getDIPProperty(dip, RodaConstants.RODA_OBJECT_DIP, dip.getType(), "credentials",
      "password");

    if (username.isPresent() && password.isPresent()) {
      return Optional.of(Pair.of(username.get(), password.get()));
    } else {
      return Optional.empty();
    }
  }

  public static Optional<String> getDeletePlugin(DIP dip) {
    return getDIPProperty(dip, RodaConstants.RODA_OBJECT_DIP, dip.getType(), "deleteExternalURL", "plugin");
  }

  private static Optional<String> getDIPProperty(DIP dip, String... property) {
    Optional<String> ret = Optional.empty();
    if (StringUtils.isNotBlank(dip.getType())) {
      String value = RodaCoreFactory.getRodaConfigurationAsString(property);
      if (StringUtils.isNotBlank(value)) {
        ret = Optional.of(value);
      }
    }
    return ret;
  }
}
