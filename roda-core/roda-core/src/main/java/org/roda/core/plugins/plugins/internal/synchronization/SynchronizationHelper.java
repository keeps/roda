package org.roda.core.plugins.plugins.internal.synchronization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.roda.core.util.FileUtility;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SynchronizationHelper {
  private final static String STORAGE_DIR = RodaConstants.CORE_STORAGE_FOLDER;
  private final static String STATE_FILE = "state.json";
  private final static String PACKAGES_DIR = "packages";

  /**
   * Bundle Synchronization related
   */
  public static BundleState createBundleStateFile() throws RODAException, IOException {
    BundleState bundleState = new BundleState();
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    Path bundlePath = Paths.get(localInstance.getBundlePath());

    if (Files.exists(bundlePath)) {
      FSUtils.deletePath(bundlePath);
    }

    Files.createDirectories(bundlePath);
    bundleState.setDestinationPath(localInstance.getBundlePath());
    bundleState.setFromDate(localInstance.getLastSynchronizationDate());
    bundleState.setToDate(new Date());
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);

    return bundleState;
  }

  public static BundleState getBundleStateFile(LocalInstance localInstance) throws GenericException {
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    return JsonUtils.readObjectFromFile(bundleStateFilePath, BundleState.class);
  }

  public static void updateBundleStateFile(BundleState bundleState)
    throws GenericException {
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);
  }

  public static BundleState buildBundleStateFile() throws GenericException {
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    BundleState bundleState = getBundleStateFile(localInstance);
    Path packagesPath = Paths.get(localInstance.getBundlePath()).resolve(PACKAGES_DIR);
    List<PackageState> packageStateList = new ArrayList<>();
    for(File file : FileUtility.listFilesRecursively(packagesPath.toFile())){
      PackageState entityPackageState = JsonUtils.readObjectFromFile(Paths.get(file.getPath()), PackageState.class);
      if (entityPackageState.getClassName() != null) {
        packageStateList.add(entityPackageState);
      }
    }
    bundleState.setPackageStateList(packageStateList);
    updateBundleStateFile(bundleState);
    return bundleState;
  }

  /**
   * Entity packages file related
   */
  public static PackageState createEntityPackageState(String entity) throws GenericException, IOException {
    PackageState entityPackageState = new PackageState();
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    Path packagesPath = Paths.get(localInstance.getBundlePath()).resolve(PACKAGES_DIR);
    Path entityPackageStatePath = getEntityPackageStatePath(entity);

    if (Files.exists(entityPackageStatePath)) {
      Files.delete(entityPackageStatePath);
    }
    Files.createDirectories(packagesPath);
    entityPackageState.setStatus(PackageState.Status.CREATED);
    JsonUtils.writeObjectToFile(entityPackageState, entityPackageStatePath);
    return entityPackageState;
  }

  public static Path getEntityPackageStatePath(String entity) throws GenericException {
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    return Paths.get(localInstance.getBundlePath()).resolve(PACKAGES_DIR).resolve(entity + ".json");
  }

  public static PackageState getEntityPackageState(String entity) throws GenericException, NotFoundException {
    Path entityPackageStatePath = getEntityPackageStatePath(entity);
    if (Files.exists(entityPackageStatePath)) {
      return JsonUtils.readObjectFromFile(entityPackageStatePath, PackageState.class);
    } else {
      throw new NotFoundException("Not found package for entity " + entity);
    }
  }

  public static void updateEntityPackageState(String entity, PackageState entityPackageState)
    throws GenericException, IOException {
    Path entityPackageStatePath = getEntityPackageStatePath(entity);
    if (!Files.exists(entityPackageStatePath)) {
      LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
      Path packagesPath = Paths.get(localInstance.getBundlePath()).resolve(PACKAGES_DIR);
      Files.createDirectories(packagesPath);
    }
    JsonUtils.writeObjectToFile(entityPackageState, entityPackageStatePath);
  }

  public static String calculateEntityTopHash(String entity) throws GenericException {
    try {
      String tophash = null;
      LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
      Path entityBundleStoragePath = Paths.get(localInstance.getBundlePath()).resolve(STORAGE_DIR).resolve(entity);
      if (Files.exists(entityBundleStoragePath)) {
        List<String> command = new ArrayList<>();
        String topHashCommand = "sort -z | xargs -0 sha1sum | cut -d' ' -f1 | sha1sum | cut -d' ' -f1";
        command.add("/bin/sh");
        command.add("-c");
        command.add("find " + entityBundleStoragePath.toString() + " -type f -print0 | " + topHashCommand);
        tophash = CommandUtility.execute(command).replace("\n", "");
      }
      return tophash;
    } catch (CommandException e) {
      throw new GenericException("Unable to execute command", e);
    }
  }
}
