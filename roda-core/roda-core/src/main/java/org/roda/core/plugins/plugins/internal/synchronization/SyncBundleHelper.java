package org.roda.core.plugins.plugins.internal.synchronization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.roda.core.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SyncBundleHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncBundleHelper.class);
  private final static String STORAGE_DIR = RodaConstants.CORE_STORAGE_FOLDER;
  private final static String STATE_FILE = "state.json";
  private final static String PACKAGES_DIR = "packages";

  /**
   * Bundle Synchronization related
   */
  public static BundleState createBundleStateFile() throws RODAException, IOException {
    BundleState bundleState = new BundleState();
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    DistributedInstance distributedInstance = SyncUtils.requestInstanceStatus(localInstance);
    Path bundlePath = Paths.get(localInstance.getBundlePath());

    if (Files.exists(bundlePath)) {
      FSUtils.deletePath(bundlePath);
    }

    Files.createDirectories(bundlePath);
    bundleState.setDestinationPath(localInstance.getBundlePath());
    bundleState.setFromDate(distributedInstance.getLastSyncDate());
    bundleState.setToDate(new Date());
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);

    return bundleState;
  }

  public static BundleState getBundleStateFile(LocalInstance localInstance) throws GenericException {
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    return JsonUtils.readObjectFromFile(bundleStateFilePath, BundleState.class);
  }

  public static void updateBundleStateFile(BundleState bundleState) throws GenericException {
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    Path bundleStateFilePath = Paths.get(localInstance.getBundlePath()).resolve(STATE_FILE);
    JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);
  }

  public static BundleState buildBundleStateFile() throws GenericException {
    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    BundleState bundleState = getBundleStateFile(localInstance);
    Path packagesPath = Paths.get(localInstance.getBundlePath()).resolve(PACKAGES_DIR);
    List<PackageState> packageStateList = new ArrayList<>();
    for (File file : FileUtility.listFilesRecursively(packagesPath.toFile())) {
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

  public static void createAipLocalInstanceList(final String destinationPath, final String aipListFileName)
    throws GenericException {
    final Path aipListPath = Paths.get(destinationPath).resolve(aipListFileName + ".json");
    final List<String> aipIdList = new ArrayList<>();
    final IndexService index = RodaCoreFactory.getIndexService();
    try (IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class,
      new Filter(), true, Collections.singletonList(RodaConstants.INDEX_UUID))) {
      for (IndexedAIP aip : result) {
        aipIdList.add(aip.getId());
      }
    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting AIP iterator when creating aip list", e);
    }
    JsonUtils.writeObjectToFile(aipIdList, aipListPath);

  }
}
