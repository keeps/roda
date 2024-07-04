/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public final class PekkoUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(PekkoUtils.class);

  private PekkoUtils() {
    // do nothing
  }

  public static Config getPekkoConfiguration(String configFilename) {
    Config pekkoConfig = null;

    try (InputStream originStream = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.CORE_ORCHESTRATOR_FOLDER + "/" + configFilename)) {
      String configAsString = IOUtils.toString(originStream, StandardCharsets.UTF_8);
      pekkoConfig = ConfigFactory.parseString(configAsString);
    } catch (IOException e) {
      LOGGER.error("Could not load Pekko configuration '{}'", configFilename, e);
    }

    return pekkoConfig;
  }
}
