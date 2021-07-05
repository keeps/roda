package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
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
  private final static Path bundleStatePath = RodaCoreFactory.getSynchronizationDirectoryPath().resolve("state.json");

  public static SelectedItemsFilter getSelectItems(Class<?> bundleClass, Date initialDate, Date finalDate)
    throws NotFoundException {
    Filter filter = new Filter();
    if (bundleClass.equals(AIP.class)) {
      if (initialDate != null) {
        filter.add(new DateIntervalFilterParameter(RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON,
          initialDate, finalDate));
      }
      return new SelectedItemsFilter(filter, IndexedAIP.class.getName(), false);
    } else {
      throw new NotFoundException("No Bundle plugin available");
    }
  }

  public static BundleState getBundleStateFile() throws GenericException {
    BundleState bundleState;
    if (FSUtils.exists(bundleStatePath)) {
      bundleState = JsonUtils.readObjectFromFile(bundleStatePath, BundleState.class);
    } else {
      bundleState = new BundleState();
      JsonUtils.writeObjectToFile(bundleState, bundleStatePath);
    }
    return bundleState;
  }

  public static void updateBundleStateFile(BundleState bundleState) throws GenericException {
    JsonUtils.writeObjectToFile(bundleState, bundleStatePath);
  }

  public static PackageState getPackageState(String entity) throws GenericException {
    return getBundleStateFile().getPackageState(entity);
  }

  public static void updatePackageState(String entity, PackageState packageState) throws GenericException {
    BundleState bundleState = getBundleStateFile();
    bundleState.setPackageState(entity, packageState);
    updateBundleStateFile(bundleState);
  }

  public static void updatePackageStateStatus(String entity, PackageState.Status status) throws GenericException {
    PackageState packageState = getPackageState(entity);
    packageState.setStatus(status);
    updatePackageState(entity, packageState);
  }

  public static void executeShaSumCommand(String entity) throws PluginException {
    try {
      BundleState bundleStateFile = getBundleStateFile();
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
