package org.roda.core.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.CentralEntitiesJsonUtils;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.bundle.AttachmentState;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.CentralEntities;
import org.roda.core.data.v2.synchronization.bundle.EntitiesBundle;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Container;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.FileUtility;
import org.roda.core.util.IdUtils;
import org.roda.core.util.ZipUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SyncUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncUtils.class);
  private final static String STATE_FILE = "state.json";
  private final static String PACKAGES_DIR = "packages";
  private final static String ATTACHMENTS_DIR = "job-attachments";
  private final static String INCOMING_PREFIX = "incoming_sync_";
  private final static String OUTCOME_PREFIX = "outcome_sync_";

  /**
   * Common Bundle methods
   */
  private static Path getSyncBundleWorkingDirectory(String prefix, String instanceIdentifier) {
    return RodaCoreFactory.getWorkingDirectory().resolve(prefix + instanceIdentifier);
  }

  private static Path createSyncBundleWorkingDirectory(String prefix, String instanceIdentifier) throws IOException {
    Path syncBundleWorkingDirectory = getSyncBundleWorkingDirectory(prefix, instanceIdentifier);
    deleteSyncBundleWorkingDirectory(prefix, instanceIdentifier);
    return Files.createDirectory(syncBundleWorkingDirectory);
  }

  private static void deleteSyncBundleWorkingDirectory(String prefix, String instanceIdentifier) {
    FSUtils.deletePathQuietly(getSyncBundleWorkingDirectory(prefix, instanceIdentifier));
  }

  public static BundleState getBundleState(String prefix, String instanceIdentifier) throws GenericException {
    Path syncBundleWorkingDirectory = getSyncBundleWorkingDirectory(prefix, instanceIdentifier);
    Path bundleStateFilePath = syncBundleWorkingDirectory.resolve(STATE_FILE);
    return JsonUtils.readObjectFromFile(bundleStateFilePath, BundleState.class);
  }

  // packages
  private static Path getEntityPackageStatePath(String prefix, String instanceIdentifier, String entity) {
    Path packagesPath = getSyncBundleWorkingDirectory(prefix, instanceIdentifier).resolve(PACKAGES_DIR);
    return packagesPath.resolve(entity + ".json");
  }

  public static PackageState getEntityPackageState(String prefix, String instanceIdentifier, String entity)
    throws GenericException, NotFoundException {
    Path entityPackageStatePath = getEntityPackageStatePath(prefix, instanceIdentifier, entity);
    if (Files.exists(entityPackageStatePath)) {
      return JsonUtils.readObjectFromFile(entityPackageStatePath, PackageState.class);
    } else {
      throw new NotFoundException("Not found package for entity " + entity);
    }
  }

  /**
   * Incoming Bundle methods
   */
  public static BundleState getIncomingBundleState(String instanceIdentifier) throws GenericException {
    return getBundleState(INCOMING_PREFIX, instanceIdentifier);
  }

  public static Path receiveBundle(String fileName, InputStream inputStream) throws IOException {
    Path filePath = RodaCoreFactory.getSynchronizationDirectoryPath()
      .resolve(RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER).resolve(fileName);
    FileUtils.copyInputStreamToFile(inputStream, filePath.toFile());
    return filePath;
  }

  public static Path extractBundle(String instanceIdentifier, Path path) throws IOException {
    Path incomingBundleWorkingDir = createSyncBundleWorkingDirectory(INCOMING_PREFIX, instanceIdentifier);
    ZipUtility.extractFilesFromZIP(path.toFile(), incomingBundleWorkingDir.toFile(), true);
    return incomingBundleWorkingDir;
  }

  private static Path getAttachmentsDir(String instanceIdentifier) {
    return getSyncBundleWorkingDirectory(INCOMING_PREFIX, instanceIdentifier).resolve(ATTACHMENTS_DIR);
  }

  public static void copyAttachments(String instanceIdentifier) throws GenericException {
    BundleState bundleState = getIncomingBundleState(instanceIdentifier);
    for (AttachmentState attachmentState : bundleState.getAttachmentStateList()) {
      for (String attachmentId : attachmentState.getAttachmentIdList()) {
        try {
          String fileName = Paths.get(attachmentId).toAbsolutePath().getParent().toUri()
            .relativize(Paths.get(attachmentId).toAbsolutePath().toUri()).toString();
          Path sourcePath = getAttachmentsDir(instanceIdentifier).resolve(attachmentState.getJobId()).resolve(fileName);
          Path targetPath = RodaCoreFactory.getJobAttachmentsDirectoryPath().resolve(attachmentState.getJobId())
            .resolve(attachmentId);
          FSUtils.copy(sourcePath, targetPath, true);
        } catch (AlreadyExistsException e) {
          // do nothing
        }
      }
    }
  }

  /**
   * Outcome Bundle methods
   */
  public static BundleState getOutcomeBundleState(String instanceIdentifier) throws GenericException {
    return getBundleState(OUTCOME_PREFIX, instanceIdentifier);
  }

  public static BundleState createBundleState(String instanceIdentifier) throws GenericException, IOException {
    Path syncBundleWorkingDirectory = createSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier);
    Path bundleStateFilePath = syncBundleWorkingDirectory.resolve(STATE_FILE);
    BundleState bundleState = new BundleState();
    bundleState.setDestinationPath(syncBundleWorkingDirectory.toString());
    bundleState.setToDate(new Date());

    JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);
    return bundleState;
  }

  public static void updateBundleState(BundleState bundleState, String instanceIdentifier) throws GenericException {
    Path syncBundleWorkingDirectory = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier);
    Path bundleStateFilePath = syncBundleWorkingDirectory.resolve(STATE_FILE);
    JsonUtils.writeObjectToFile(bundleState, bundleStateFilePath);
  }

  public static BundleState buildBundleStateFile(String instanceIdentifier) throws GenericException {
    BundleState bundleState = getOutcomeBundleState(instanceIdentifier);
    Path packagesPath = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier).resolve(PACKAGES_DIR);
    List<PackageState> packageStateList = new ArrayList<>();
    for (File file : FileUtility.listFilesRecursively(packagesPath.toFile())) {
      PackageState entityPackageState = JsonUtils.readObjectFromFile(Paths.get(file.getPath()), PackageState.class);
      if (entityPackageState.getClassName() != null) {
        packageStateList.add(entityPackageState);
      }
    }
    bundleState.setPackageStateList(packageStateList);

    try {
      PackageState job = getOutcomeEntityPackageState(instanceIdentifier, "job");
      for (String jobId : job.getIdList()) {
        Path attachmentsPath = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier)
          .resolve(ATTACHMENTS_DIR).resolve(jobId);
        if (Files.exists(attachmentsPath)) {
          List<String> idList = new ArrayList<>();
          for (File file : FileUtility.listFilesRecursively(attachmentsPath.toFile())) {
            idList.add(file.getName());
          }
          AttachmentState attachmentState = new AttachmentState();
          attachmentState.setJobId(jobId);
          attachmentState.setAttachmentIdList(idList);
          bundleState.getAttachmentStateList().add(attachmentState);
        }
      }
    } catch (NotFoundException e) {
      // do nothing
    }

    updateBundleState(bundleState, instanceIdentifier);
    return bundleState;
  }

  public static Path compressBundle(String instanceIdentifier) throws PluginException {
    try {
      BundleState bundleState = getOutcomeBundleState(instanceIdentifier);
      String fileName = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'.zip'").format(bundleState.getToDate());
      Path filePath = RodaCoreFactory.getSynchronizationDirectoryPath()
        .resolve(RodaConstants.CORE_SYNCHRONIZATION_OUTCOME_FOLDER).resolve(fileName);
      if (FSUtils.exists(filePath)) {
        FSUtils.deletePath(filePath);
      }

      Path file = Files.createFile(filePath);
      bundleState.setZipFile(filePath.toString());
      bundleState.setSyncState(BundleState.Status.PREPARED);
      updateBundleState(bundleState, instanceIdentifier);
      ZipUtility.createZIPFile(file.toFile(),
        getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier).toFile());
      return filePath;
    } catch (GenericException | IOException | NotFoundException e) {
      LOGGER.error("Unable to read bundle state file", e);
      throw new PluginException("Unable to read bundle state file", e);
    }
  }

  // Packages
  public static Path getEntityStoragePath(String instanceIdentifier, String entity) {
    return getSyncBundleWorkingDirectory(INCOMING_PREFIX, instanceIdentifier).resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(entity);
  }

  public static PackageState createEntityPackageState(String instanceIdentifier, String entity)
    throws IOException, GenericException {
    PackageState entityPackageState = new PackageState();
    Path syncBundleWorkingDirectory = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier);
    Path packagesPath = syncBundleWorkingDirectory.resolve(PACKAGES_DIR);
    Path entityPackageStatePath = getEntityPackageStatePath(OUTCOME_PREFIX, instanceIdentifier, entity);
    Files.createDirectories(packagesPath);

    if (Files.exists(entityPackageStatePath)) {
      Files.delete(entityPackageStatePath);
    }

    entityPackageState.setStatus(PackageState.Status.CREATED);
    JsonUtils.writeObjectToFile(entityPackageState, entityPackageStatePath);
    return entityPackageState;
  }

  public static PackageState getIncomingEntityPackageState(String instanceIdentifier, String entity)
    throws NotFoundException, GenericException {
    return getEntityPackageState(INCOMING_PREFIX, instanceIdentifier, entity);
  }

  public static PackageState getOutcomeEntityPackageState(String instanceIdentifier, String entity)
    throws NotFoundException, GenericException {
    return getEntityPackageState(OUTCOME_PREFIX, instanceIdentifier, entity);
  }

  public static void updateEntityPackageState(String instanceIdentifier, String entity, PackageState entityPackageState)
    throws IOException, GenericException {
    Path entityPackageStatePath = getEntityPackageStatePath(OUTCOME_PREFIX, instanceIdentifier, entity);
    if (!Files.exists(entityPackageStatePath)) {
      Path packagesPath = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier).resolve(PACKAGES_DIR);
      Files.createDirectories(packagesPath);
    }
    JsonUtils.writeObjectToFile(entityPackageState, entityPackageStatePath);
  }

  /**
   * Central instance methods
   */
  public static StreamResponse createCentralSyncBundle(String instanceIdentifier) throws GenericException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    try {
      createBundleState(instanceIdentifier);

      final boolean createdJobsBundle = createCentralJobsBundle(instanceIdentifier);
      final boolean createdRepresentationInformationBundle = createCentralRepresentationInformationPackageState(
        instanceIdentifier);
      final boolean createdRiskBundle = createCentralRiskPackageState(instanceIdentifier);

      if (createdJobsBundle || createdRepresentationInformationBundle || createdRiskBundle) {
        SyncUtils.buildBundleStateFile(instanceIdentifier);
        return createBundleStreamResponse(compressBundle(instanceIdentifier));
      }

    } catch (IOException | PluginException e) {
      throw new GenericException("Unable to create remote actions file: " + e.getMessage());
    }
    throw new NotFoundException("Cannot retrieve jobs for this instance ID");
  }

  private static IterableIndexResult<Job> getRemoteActions(String instanceIdentifier)
    throws GenericException, NotFoundException {
    IndexService index = RodaCoreFactory.getIndexService();
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_INSTANCE_ID, instanceIdentifier));
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_STATE, "CREATED"));
    try {
      Long count = index.count(Job.class, filter);
      return index.findAll(Job.class, filter, true, new ArrayList<>());

    } catch (RequestNotValidException e) {
      throw new GenericException("Unable to create remote actions file: " + e.getMessage());
    }
  }

  private static boolean createCentralJobsBundle(final String instanceIdentifier) throws NotFoundException,
    GenericException, RequestNotValidException, IOException, AuthorizationDeniedException, AlreadyExistsException {
    boolean createdBundle = false;
    final StorageService storage = RodaCoreFactory.getStorageService();

    final Path destinationPath = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier)
      .resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_JOB);

    final StoragePath jobContainerPath = ModelUtils.getJobContainerPath();

    final IterableIndexResult<Job> jobs = getRemoteActions(instanceIdentifier);

    if (jobs.getTotalCount() > 0) {
      final PackageState packageState = createEntityPackageState(instanceIdentifier, "job");
      packageState.setClassName(Job.class);
      packageState.setCount((int) jobs.getTotalCount());
      packageState.setStatus(PackageState.Status.CREATED);
      final ArrayList<String> idList = new ArrayList<>();
      for (Job job : jobs) {
        idList.add(job.getId());
        final String jobFile = job.getId() + RodaConstants.JOB_FILE_EXTENSION;
        storage.copy(storage, jobContainerPath, destinationPath.resolve(jobFile), jobFile);
      }
      packageState.setIdList(idList);
      SyncUtils.updateEntityPackageState(instanceIdentifier, "job", packageState);
      createdBundle = true;
    }
    return createdBundle;
  }

  private static boolean createCentralRepresentationInformationPackageState(final String instanceIdentifier)
    throws RequestNotValidException, GenericException, IOException, AuthorizationDeniedException,
    AlreadyExistsException {
    boolean createdBundle = false;
    final List<String> representationInformationList = new ArrayList<>();
    final Filter filter = new Filter();

    final IterableIndexResult<RepresentationInformation> representationInformations = RodaCoreFactory.getIndexService()
      .findAll(RepresentationInformation.class, filter, Collections.singletonList(RodaConstants.INDEX_UUID));

    if (representationInformations.getTotalCount() > 0) {
      final StorageService storage = RodaCoreFactory.getStorageService();

      final Path destinationPath = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier)
        .resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_REPRESENTATION_INFORMATION);

      final StoragePath representationInformationContainerPath = ModelUtils.getRepresentationInformationContainerPath();
      final PackageState packageState = createEntityPackageState(instanceIdentifier,
        RodaConstants.CORE_REPRESENTATION_INFORMATION_FOLDER);
      packageState.setClassName(RepresentationInformation.class);
      packageState.setCount((int) representationInformations.getTotalCount());
      packageState.setStatus(PackageState.Status.CREATED);
      for (RepresentationInformation representationInformation : representationInformations) {
        representationInformationList.add(representationInformation.getId());
        final String representationInformationFile = representationInformation.getId()
          + RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION;
        storage.copy(storage, representationInformationContainerPath,
          destinationPath.resolve(representationInformationFile), representationInformationFile);
      }
      packageState.setIdList(representationInformationList);
      SyncUtils.updateEntityPackageState(instanceIdentifier, RodaConstants.CORE_REPRESENTATION_INFORMATION_FOLDER,
        packageState);
      createdBundle = true;
    }
    return createdBundle;

  }

  private static boolean createCentralRiskPackageState(final String instanceIdentifier) throws RequestNotValidException,
    GenericException, IOException, AuthorizationDeniedException, AlreadyExistsException {
    boolean createdBundle = false;
    final List<String> riskList = new ArrayList<>();
    final Filter filter = new Filter();

    final IterableIndexResult<IndexedRisk> risks = RodaCoreFactory.getIndexService().findAll(IndexedRisk.class, filter,
      Collections.singletonList(RodaConstants.INDEX_UUID));

    if (risks.getTotalCount() > 0) {
      final StorageService storage = RodaCoreFactory.getStorageService();

      final Path destinationPath = getSyncBundleWorkingDirectory(OUTCOME_PREFIX, instanceIdentifier)
        .resolve(RodaConstants.CORE_STORAGE_FOLDER).resolve(RodaConstants.STORAGE_CONTAINER_RISK);

      final StoragePath riskContainerPath = ModelUtils.getRiskContainerPath();
      final PackageState packageState = createEntityPackageState(instanceIdentifier, RodaConstants.CORE_RISK_FOLDER);
      packageState.setClassName(Risk.class);
      packageState.setCount((int) risks.getTotalCount());
      packageState.setStatus(PackageState.Status.CREATED);
      for (IndexedRisk risk : risks) {
        riskList.add(risk.getId());
        final String riskFile = risk.getId() + RodaConstants.RISK_FILE_EXTENSION;
        storage.copy(storage, riskContainerPath, destinationPath.resolve(riskFile), riskFile);
      }
      packageState.setIdList(riskList);
      SyncUtils.updateEntityPackageState(instanceIdentifier, RodaConstants.CORE_RISK_FOLDER, packageState);
      createdBundle = true;
    }
    return createdBundle;
  }

  private static StreamResponse createBundleStreamResponse(Path zipPath) {
    ConsumesOutputStream stream = new ConsumesOutputStream() {
      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {
        Files.copy(zipPath, out);
      }

      @Override
      public long getSize() {
        long size;
        try {
          size = Files.size(zipPath);
        } catch (IOException e) {
          size = -1;
        }

        return size;
      }

      @Override
      public Date getLastModified() {
        Date ret;
        try {
          ret = new Date(Files.getLastModifiedTime(zipPath).toMillis());
        } catch (IOException e) {
          ret = null;
        }
        return ret;
      }

      @Override
      public String getFileName() {
        return zipPath.getFileName().toString();
      }

      @Override
      public String getMediaType() {
        return RodaConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM;
      }
    };
    return new StreamResponse(stream);
  }

  // Local instance methods
  public static DistributedInstance requestInstanceStatus(LocalInstance localInstance) throws GenericException {
    try {
      AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);
      String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE + "status"
        + RodaConstants.API_SEP + localInstance.getId();

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGet = new HttpGet(localInstance.getCentralInstanceURL() + resource);
      httpGet.addHeader("Authorization", "Bearer " + accessToken.getToken());
      httpGet.addHeader("content-type", "application/json");
      httpGet.addHeader("Accept", "application/json");

      HttpResponse response = httpClient.execute(httpGet);

      if (response.getStatusLine().getStatusCode() != RodaConstants.HTTP_RESPONSE_CODE_SUCCESS) {
        throw new GenericException(
          "Unable to retrieve instance status error code: " + response.getStatusLine().getStatusCode());
      }
      return JsonUtils.getObjectFromJson(response.getEntity().getContent(), DistributedInstance.class);
    } catch (AuthenticationDeniedException | GenericException | IOException e) {
      throw new GenericException("Unable to retrieve instance status: " + e.getMessage());
    }
  }

  public static Path requestRemoteActions(LocalInstance localInstance) throws GenericException {
    Path remoteActionsPath = null;
    try {
      AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);
      String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE + "remote_actions"
        + RodaConstants.API_SEP + localInstance.getId();

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGet = new HttpGet(localInstance.getCentralInstanceURL() + resource);
      httpGet.addHeader("Authorization", "Bearer " + accessToken.getToken());
      httpGet.addHeader("content-type", "application/json");
      httpGet.addHeader("Accept", "application/json");

      HttpResponse response = httpClient.execute(httpGet);

      if (response.getStatusLine().getStatusCode() == RodaConstants.HTTP_RESPONSE_CODE_SUCCESS) {
        return downloadRemoteActions(response, localInstance.getId());
      }
    } catch (AuthenticationDeniedException | IOException e) {
      throw new GenericException("unable to communicate with the central instance");
    }
    return remoteActionsPath;
  }

  private static Path downloadRemoteActions(HttpResponse response, String instanceId) {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      try (InputStream in = entity.getContent()) {

        Path remoteActionsDir = RodaCoreFactory.getSynchronizationDirectoryPath()
          .resolve(RodaConstants.SYNCHRONIZATION_INCOMING_REMOTE_ACTIONS_PATH);
        if (!Files.exists(remoteActionsDir)) {
          Files.createDirectories(remoteActionsDir);
        }
        Path actionsFile = remoteActionsDir.resolve(instanceId + ".zip");
        Files.copy(in, actionsFile, StandardCopyOption.REPLACE_EXISTING);
        return actionsFile;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Creates {@link Map} with the List of entities path in bundle and the indexed
   * {@link Class<? extends IsIndexed>}.
   * 
   * @param bundleWorkingDir
   *          {@link Path} to blunde dir
   * @param entitiesBundle
   *          {@link EntitiesBundle}
   * @return {@link Map}
   */
  public static Map<Path, Class<? extends IsIndexed>> createEntitiesPaths(final Path bundleWorkingDir,
    final EntitiesBundle entitiesBundle) {
    final HashMap<Path, Class<? extends IsIndexed>> entitiesPathMap = new HashMap<>();
    final Path aipListPath = bundleWorkingDir.resolve(entitiesBundle.getAipFileName() + ".json");
    entitiesPathMap.put(aipListPath, IndexedAIP.class);

    final Path dipListPath = bundleWorkingDir.resolve(entitiesBundle.getDipFileName() + ".json");
    entitiesPathMap.put(dipListPath, IndexedDIP.class);

    final Path riskListPath = bundleWorkingDir.resolve(entitiesBundle.getRiskFileName() + ".json");
    entitiesPathMap.put(riskListPath, RiskIncidence.class);

    return entitiesPathMap;
  }

  /**
   * Write the file with the entities removed and the missing entities from the
   * last synchronization.
   * 
   * @param centralEntities
   *          {@link CentralEntities}.
   * @param instanceIdentifier
   *          The instance identifier.
   * @throws IOException
   *           if some i/o error occurs.
   */
  public static void writeEntitiesFile(final CentralEntities centralEntities, String instanceIdentifier)
    throws IOException {
    final StringBuilder fileNameBuilder = new StringBuilder();
    fileNameBuilder.append(RodaConstants.SYNCHRONIZATION_REPORT_FILE).append("_").append(instanceIdentifier)
      .append(".json");

    final Path temporaryPath = RodaCoreFactory.getWorkingDirectory().resolve(fileNameBuilder.toString());
    final Path lastSyncReportPath = RodaCoreFactory.getSynchronizationDirectoryPath()
      .resolve(fileNameBuilder.toString());

    try {
      Files.deleteIfExists(temporaryPath);
      Files.createFile(temporaryPath);
      CentralEntitiesJsonUtils.writeJsonToFile(centralEntities, temporaryPath);
      Files.move(temporaryPath, lastSyncReportPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (final IOException e) {
      Files.deleteIfExists(temporaryPath);
    }
  }

  /**
   * Get the stream response from the given path.
   * 
   * @param path
   *          {@link Path}
   * @return {@link StreamResponse}.
   */
  public static StreamResponse createLastSyncFileStreamResponse(Path path) {
    return createBundleStreamResponse(path);
  }

  // Import methods

  /**
   * Imports from synchronization bundle to RODA storage.
   * 
   * @param storage
   *          {@link StorageService}
   * @param tempDirectory
   *          {@link Path} to temporary directory.
   * @param bundleState
   *          {@link BundleState}.
   * @param jobPluginInfo
   *          {@link JobPluginInfo}.
   * @param moveJob
   *          flag if is to move jobs and jobs reports or not.
   * @return the value of moved files.
   * @throws GenericException
   *           if some error occurs.
   * @throws NotFoundException
   *           if some error occurs.
   * @throws AuthorizationDeniedException
   *           if some error occurs.
   * @throws RequestNotValidException
   *           if some error occurs.
   * @throws AlreadyExistsException
   *           if some error occurs.
   * @throws JobAlreadyStartedException
   *           if some error occurs.
   */
  public static int importStorage(final StorageService storage, final Path tempDirectory, final BundleState bundleState,
    final JobPluginInfo jobPluginInfo, final boolean moveJob) throws GenericException, NotFoundException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, JobAlreadyStartedException {
    int count = 0;
    final FileStorageService temporaryStorage = new FileStorageService(
      tempDirectory.resolve(RodaConstants.CORE_STORAGE_FOLDER), false, null, false);
    final CloseableIterable<Container> containers = temporaryStorage.listContainers();
    final Iterator<Container> containerIterator = containers.iterator();
    while (containerIterator.hasNext()) {
      final Container container = containerIterator.next();
      final StoragePath containerStoragePath = container.getStoragePath();

      final CloseableIterable<Resource> resources = temporaryStorage.listResourcesUnderContainer(containerStoragePath,
        false);
      final Iterator<Resource> resourceIterator = resources.iterator();
      while (resourceIterator.hasNext()) {
        final Resource resource = resourceIterator.next();
        final StoragePath storagePath = resource.getStoragePath();

        // if the resource already exists, remove it before moving the updated resource
        if (!resource.getStoragePath().getContainerName().equals(RodaConstants.STORAGE_CONTAINER_JOB)) {
          if (storage.exists(storagePath)) {
            storage.deleteResource(storagePath);
          }
          storage.move(temporaryStorage, storagePath, storagePath);
        }

        // Job and job reports (This step is for Local Instance job)
        if (moveJob && resource.getStoragePath().getContainerName().equals(RodaConstants.STORAGE_CONTAINER_JOB)) {
          moveJobsAndJobsReports(storage, resource, storagePath, temporaryStorage);
        }

        count++;
      }
    }
    reindexBundle(bundleState, jobPluginInfo);
    return count;
  }

  /**
   * Reindex all objects moved from bundle to storage.
   * 
   * @param bundleState
   *          {@link BundleState}.
   * @param jobPluginInfo
   *          {@link JobPluginInfo}.
   * @throws NotFoundException
   *           if some error occurs.
   * @throws AuthorizationDeniedException
   *           if some error occurs.
   * @throws JobAlreadyStartedException
   *           if some error occurs.
   * @throws GenericException
   *           if some error occurs.
   * @throws RequestNotValidException
   *           if some error occurs.
   */
  private static void reindexBundle(final BundleState bundleState, final JobPluginInfo jobPluginInfo)
    throws NotFoundException, AuthorizationDeniedException, JobAlreadyStartedException, GenericException,
    RequestNotValidException {
    final List<PackageState> packageStateList = bundleState.getPackageStateList();
    jobPluginInfo.setSourceObjectsCount(packageStateList.size());
    for (PackageState packageState : packageStateList) {
      if (packageState.getCount() > 0) {
        final Job job = new Job();
        job.setId(IdUtils.createUUID());
        job.setName("Reindex RODA entity (" + packageState.getClassName() + ")");
        job.setPluginType(PluginType.INTERNAL);
        job.setUsername(RodaConstants.ADMIN);

        job.setPlugin(PluginHelper.getReindexPluginName(packageState.getClassName()));
        job.setSourceObjects(SelectedItemsList.create(packageState.getClassName(), packageState.getIdList()));

        PluginHelper.createAndExecuteJob(job);
      }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    }
  }

  /**
   * Move Jobs and Jobs reports from local bundle to RODA central storage.
   * 
   * @param storage
   *          {@link StorageService}.
   * @param resource
   *          {@link Resource}
   * @param storagePath
   *          {@link Path} to storage.
   * @param temporaryStorage
   *          {@link Path} to temporary storage
   * @throws AlreadyExistsException
   *           if some error occurs.
   * @throws AuthorizationDeniedException
   *           if some error occurs.
   * @throws NotFoundException
   *           if some error occurs.
   * @throws GenericException
   *           if some error occurs.
   * @throws RequestNotValidException
   *           if some error occurs.
   */
  private static void moveJobsAndJobsReports(final StorageService storage, final Resource resource,
    final StoragePath storagePath, final FileStorageService temporaryStorage) throws AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    if (storage.exists(storagePath)) {
      storage.deleteResource(storagePath);
    }
    storage.move(temporaryStorage, storagePath, storagePath);

    if (!resource.isDirectory()) {
      try (InputStream inputStream = storage.getBinary(resource.getStoragePath()).getContent().createInputStream()) {
        final Job jobToImport = JsonUtils.getObjectFromJson(inputStream, Job.class);
        final StoragePath jobReportsContainerPath = ModelUtils.getJobReportsStoragePath(jobToImport.getId());
        if (storage.exists(jobReportsContainerPath)) {
          storage.deleteResource(jobReportsContainerPath);
        }
        storage.createDirectory(jobReportsContainerPath);
      } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
        | IOException e) {
        LOGGER.error("Error getting Job json from binary", e);
      }
    }
  }

}
