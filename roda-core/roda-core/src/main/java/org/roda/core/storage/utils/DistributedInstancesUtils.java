package org.roda.core.storage.utils;

import java.nio.file.Path;
import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.YamlUtils;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstanceIdentifierState;

import java.nio.file.Path;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class DistributedInstancesUtils {

  private DistributedInstancesUtils() {
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


}
