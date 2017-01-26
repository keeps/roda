package org.roda.core.common.dips;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.DIP;

public class DIPUtils {

  public static OptionalWithCause<String> getCompleteOpenExternalURL(DIP dip) {
    OptionalWithCause<String> ret;
    Optional<String> dipProperty = getDIPProperty(dip, "dip", dip.getType(), "openExternalURL");
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
    Optional<String> dipProperty = getDIPProperty(dip, "dip", dip.getType(), "deleteExternalURL");
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
    return getDIPProperty(dip, "dip", dip.getType(), "deleteExternalURL", "method");
  }

  public static Optional<Pair<String, String>> getDeleteCredentials(DIP dip) {
    Optional<Pair<String, String>> credentials = Optional.empty();

    Optional<String> username = getDIPProperty(dip, "dip", dip.getType(), "credentials", "username");
    Optional<String> password = getDIPProperty(dip, "dip", dip.getType(), "credentials", "password");
    if (username.isPresent() && password.isPresent()) {
      credentials = Optional.of(new Pair<>(username.get(), password.get()));
    }
    return credentials;
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
