package org.roda.core.plugins.plugins.internal.synchronization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.risks.RiskIncidence;
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
import org.roda.core.util.IdUtils;
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
    bundleState.setId(IdUtils.createUUID());
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

  /**
   * Iterates over the files in bundle (AIP, DIP, Risks) and creates the Files.
   * 
   * @param bundleState
   *          {@link BundleState}.
   * @throws GenericException
   *           if some error occurs.
   * @throws IOException
   *           if some i/o error occurs.
   */
  public static void createLocalInstanceLists(BundleState bundleState) throws GenericException, IOException {
    final Map<Path, Class<? extends IsIndexed>> localInstanceListsMap = createLocalInstanceListPaths(bundleState);
    for (Map.Entry entry : localInstanceListsMap.entrySet()) {
      createLocalInstanceList((Path) entry.getKey(), (Class<? extends IsIndexed>) entry.getValue());
    }
  }

  /**
   * Creates {@link Map} with the List of entities path in bundle and the indexed
   * {@link Class<? extends IsIndexed>}.
   * 
   * @param bundleState
   *          {@link BundleState}.
   * @return {@link Map}.
   */
  private static Map<Path, Class<? extends IsIndexed>> createLocalInstanceListPaths(final BundleState bundleState) {
    final HashMap<Path, Class<? extends IsIndexed>> localInstanceListsMap = new HashMap<>();
    final Path aipListPath = Paths.get(bundleState.getDestinationPath())
      .resolve(bundleState.getEntitiesBundle().getAipFileName() + ".json");
    localInstanceListsMap.put(aipListPath, IndexedAIP.class);

    final Path dipListPath = Paths.get(bundleState.getDestinationPath())
      .resolve(bundleState.getEntitiesBundle().getDipFileName() + ".json");
    localInstanceListsMap.put(dipListPath, IndexedDIP.class);

    final Path riskListPath = Paths.get(bundleState.getDestinationPath())
      .resolve(bundleState.getEntitiesBundle().getRiskFileName() + ".json");
    localInstanceListsMap.put(riskListPath, RiskIncidence.class);

    return localInstanceListsMap;
  }

  /**
   * Write the List in the file to bundle.
   * 
   * @param destinationPath
   *          {@link Path}.
   * @param indexedClass
   *          {@link Class<? extends IsIndexed>}.
   * @throws IOException
   *           if some i/o error occurs.
   */
  private static void createLocalInstanceList(final Path destinationPath, final Class<? extends IsIndexed> indexedClass)
    throws IOException {
    final IndexService index = RodaCoreFactory.getIndexService();
    final Filter filter = new Filter();
    if (indexedClass == IndexedPreservationEvent.class) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    }
    SyncUtils.init(destinationPath);
    try (IterableIndexResult<? extends IsIndexed> result = index.findAll(indexedClass, filter, true,
      Collections.singletonList(RodaConstants.INDEX_UUID))) {
      result.forEach(indexed -> {
        try {
          SyncUtils.writeString(indexed.getId());
        } catch (final IOException e) {
          LOGGER.error("Error writing the ID {} {}", indexed.getId(), e);
        }
      });
    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting iterator when creating aip list", e);
    }

    SyncUtils.close(true);

  }
}
