package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SyncBundleHelper {
  private final static String STATE_FILE = "state.json";

  public static SelectedItemsFilter getSelectItems(Class<?> bundleClass, Date initialDate, Date finalDate)
    throws NotFoundException {
    Filter filter = new Filter();
    if (bundleClass.equals(AIP.class)) {
      if (initialDate != null) {
        filter.add(new DateIntervalFilterParameter(RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON,
          initialDate, finalDate));
      }
      return new SelectedItemsFilter(filter, IndexedAIP.class.getName(), false);
    } else if (bundleClass.equals(DIP.class)) {
      if (initialDate != null) {
        filter.add(new DateIntervalFilterParameter(RodaConstants.DIP_LAST_MODIFIED, RodaConstants.DIP_LAST_MODIFIED,
          initialDate, finalDate));
      }
      return new SelectedItemsFilter(filter, IndexedDIP.class.getName(), false);
    } else if (bundleClass.equals(RiskIncidence.class)) {
      if (initialDate != null) {
        filter.add(new DateIntervalFilterParameter(RodaConstants.RISK_INCIDENCE_DETECTED_ON,
          RodaConstants.RISK_INCIDENCE_DETECTED_ON, initialDate, finalDate));
      }
      return new SelectedItemsFilter(filter, RiskIncidence.class.getName(), false);
    } else if (bundleClass.equals(Job.class)) {
      List<FilterParameter> parameters = new ArrayList<>();
      parameters.add(new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INTERNAL.toString()));
      if (initialDate != null) {
        parameters.add(new DateIntervalFilterParameter(RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE,
          initialDate, finalDate));
      }
      filter.add(parameters);
      return new SelectedItemsFilter(filter, Job.class.getName(), false);
    } else if (bundleClass.equals(IndexedPreservationEvent.class)) {
      List<FilterParameter> parameters = new ArrayList<>();
      parameters.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
      if (initialDate != null) {
        parameters.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
          RodaConstants.PRESERVATION_EVENT_DATETIME, initialDate, finalDate));
      }
      filter.add(parameters);
      return new SelectedItemsFilter(filter, IndexedPreservationEvent.class.getName(), false);
    } else if (bundleClass.equals(IndexedPreservationAgent.class)) {
      return new SelectedItemsFilter(filter, IndexedPreservationAgent.class.getName(), false);
    } else {
      throw new NotFoundException("No Bundle plugin available");
    }
  }

  public static BundleState getBundleStateFile(LocalInstance localInstance) throws GenericException {
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    return JsonUtils.readObjectFromFile(bundleStateFilePath, BundleState.class);
  }

  public static void updateBundleStateFile(LocalInstance localInstance, BundleState bundleState)
    throws GenericException {
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);
  }

  public static PackageState getPackageState(LocalInstance localInstance, String entity) throws GenericException {
    return getBundleStateFile(localInstance).getPackageState(entity);
  }

  public static void updatePackageState(LocalInstance localInstance, String entity, PackageState packageState)
    throws GenericException {
    BundleState bundleState = getBundleStateFile(localInstance);
    bundleState.setPackageState(entity, packageState);
    updateBundleStateFile(localInstance, bundleState);
  }

  public static void updatePackageStateStatus(LocalInstance localInstance, String entity, PackageState.Status status)
    throws GenericException {
    PackageState packageState = getPackageState(localInstance, entity);
    packageState.setStatus(status);
    updatePackageState(localInstance, entity, packageState);
  }

  public static BundleState createBundleStateFile(LocalInstance localInstance) throws GenericException {
    BundleState bundleState = new BundleState();
    Path bundlePath = Paths.get(localInstance.getBundlePath());
    try {
      if (Files.exists(bundlePath)) {
        FSUtils.deletePath(bundlePath);
      }

      Files.createDirectories(bundlePath);
      bundleState.setDestinationPath(localInstance.getBundlePath());
      bundleState.setFromDate(localInstance.getLastSynchronizationDate());
      bundleState.setToDate(new Date());
      Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
      JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);

    } catch (IOException | NotFoundException e) {
      throw new GenericException("Unable to create bundle directory", e);
    }
    return bundleState;
  }

  public static void executeShaSumCommand(LocalInstance localInstance, String entity) throws PluginException {
    try {
      BundleState bundleStateFile = getBundleStateFile(localInstance);
      entity = entity.replace(' ', '_');
      String targetPath = Paths.get(bundleStateFile.getDestinationPath(), entity + ".shasum").toString();
      List<String> checksumCommand = new ArrayList<>();
      checksumCommand.add("/bin/sh");
      checksumCommand.add("-c");
      checksumCommand.add("cd " + bundleStateFile.getDestinationPath() + " && find ./" + entity
        + " -type f -print0 | xargs -0 shasum > " + targetPath);
      CommandUtility.execute(checksumCommand);
    } catch (CommandException | GenericException e) {
      throw new PluginException("Unable to execute command", e);
    }

  }
}
