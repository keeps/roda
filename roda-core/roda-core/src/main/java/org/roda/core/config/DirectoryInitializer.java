package org.roda.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DirectoryInitializer {
  private final Logger LOGGER = LoggerFactory.getLogger(DirectoryInitializer.class);
  private static DirectoryInitializer instance;
  private boolean instantiatedWithoutErrors = true;
  private final ConfigurationManager configurationManager;

  public static DirectoryInitializer getInstance(ConfigurationManager configurationManager) {
    if (instance == null) {
      instance = new DirectoryInitializer(configurationManager);
      instance.instantiateEssentialDirectories();
    }
    return instance;
  }

  private DirectoryInitializer(ConfigurationManager configurationManager) {
    this.configurationManager = configurationManager;
  }

  public boolean isInstantiatedWithoutErrors() {
    return instantiatedWithoutErrors;
  }

  private void instantiateEssentialDirectories() {
    List<Path> essentialDirectories = new ArrayList<>();
    essentialDirectories.add(configurationManager.getConfigPath());
    essentialDirectories.add(configurationManager.getRodaHomePath().resolve(RodaConstants.CORE_LOG_FOLDER));
    essentialDirectories.add(configurationManager.getDataPath());
    essentialDirectories.add(configurationManager.getLogPath());
    essentialDirectories.add(configurationManager.getStoragePath());
    essentialDirectories.add(configurationManager.getIndexDataPath());
    essentialDirectories.add(configurationManager.getExampleConfigPath());
    essentialDirectories.add(configurationManager.getReportPath());

    for (Path path : essentialDirectories) {
      try {
        if (!FSUtils.exists(path)) {
          Files.createDirectories(path);
        }
      } catch (IOException e) {
        LOGGER.error("Unable to create " + path, e);
        instantiatedWithoutErrors = false;
      }
    }
  }

  public void instantiateExampleResources() {
    // copy configs folder from classpath to example folder
    try {
      FSUtils.deletePathQuietly(configurationManager.getExampleConfigPath());
      Files.createDirectories(configurationManager.getExampleConfigPath());
      RodaUtils.copyFilesFromClasspath(RodaConstants.CORE_CONFIG_FOLDER + "/",
        configurationManager.getExampleConfigPath(), true,
        Arrays.asList(RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_LDAP_FOLDER,
          RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_I18N_FOLDER + "/"
            + RodaConstants.CORE_I18N_CLIENT_FOLDER,
          RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_I18N_FOLDER + "/"
            + RodaConstants.CORE_I18_GWT_XML_FILE));
    } catch (IOException e) {
      LOGGER.error("Unable to create " + configurationManager.getExampleConfigPath(), e);
      instantiatedWithoutErrors = false;
    }
  }

}
