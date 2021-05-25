package org.roda.core.storage.utils;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.YamlUtils;
import org.roda.core.data.v2.distributedInstance.LocalInstance;
import org.roda.core.data.v2.distributedInstance.LocalInstanceIdentifierState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Date;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class LocalInstanceUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalInstanceUtils.class);

  private LocalInstanceUtils() {
    // do nothing
  }

  public static String getLocalInstanceIdentifier() {
    try {
      Path configFile = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE_PATH);
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      if (localInstance.getInstanceIdentifierState().equals(LocalInstanceIdentifierState.INACTIVE)) {
        return null;
      } else {
        return localInstance.getId();
      }
    } catch (GenericException e) {
      return null;
    }
  }

  public static String retrieveLocalInstanceIdentifierToPlugin() {
    try {
      Path configFile = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE_PATH);
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      return localInstance.getId();
    } catch (GenericException e) {
      return null;
    }
  }

  public static LocalInstanceIdentifierState getLocalInstanceState() {
    try {
      Path configFile = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE_PATH);
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      return localInstance.getInstanceIdentifierState();
    } catch (GenericException e) {
      return null;
    }
  }
}
