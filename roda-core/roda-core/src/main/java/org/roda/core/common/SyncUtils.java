/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.ZipUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SyncUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncUtils.class);

  /**
   * Central Bundle methods
   */
  public static boolean createCentralSyncBundle(Path workingDir, String instanceIdentifier)
    throws AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, GenericException {
    boolean jobsPackage = createCentralJobsPackage(workingDir, instanceIdentifier);
    boolean riskPackage = createCentralRiskPackage(workingDir);
    boolean riPackage = createCentralRepresentationInformationPackage(workingDir);

    return jobsPackage || riskPackage || riPackage;
  }

  private static boolean createCentralJobsPackage(Path workingDir, String instanceIdentifier)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException {
    final Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.INDEX_INSTANCE_ID, instanceIdentifier));
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_STATE, "CREATED"));
    final IterableIndexResult<Job> jobs = RodaCoreFactory.getIndexService().findAll(Job.class, filter, true,
      new ArrayList<>());

    final Path destinationPath = workingDir.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_JOB);
    return createCentralPackage(ModelUtils.getJobContainerPath(), jobs, RodaConstants.JOB_FILE_EXTENSION,
      destinationPath);
  }

  private static boolean createCentralRiskPackage(Path workingDir)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException {
    final Filter filter = new Filter();
    final IterableIndexResult<IndexedRisk> risks = RodaCoreFactory.getIndexService().findAll(IndexedRisk.class, filter,
      Collections.singletonList(RodaConstants.INDEX_UUID));

    final Path destinationPath = workingDir.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_RISK);

    return createCentralPackage(ModelUtils.getRiskContainerPath(), risks, RodaConstants.RISK_FILE_EXTENSION,
      destinationPath);
  }

  private static boolean createCentralRepresentationInformationPackage(Path workingDir)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException {
    final Filter filter = new Filter();
    final IterableIndexResult<RepresentationInformation> representationInformation = RodaCoreFactory.getIndexService()
      .findAll(RepresentationInformation.class, filter, Collections.singletonList(RodaConstants.INDEX_UUID));

    final Path destinationPath = workingDir.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_REPRESENTATION_INFORMATION);

    return createCentralPackage(ModelUtils.getRepresentationInformationContainerPath(), representationInformation,
      RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION, destinationPath);
  }

  private static boolean createCentralPackage(StoragePath containerPath,
    IterableIndexResult<? extends IsIndexed> indexedObject, String fileExtension, Path destinationPath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    if (indexedObject.getTotalCount() > 0) {
      final StorageService storage = RodaCoreFactory.getStorageService();

      for (IsIndexed object : indexedObject) {
        final String filename = object.getId() + fileExtension;
        storage.copy(storage, containerPath, destinationPath.resolve(filename), filename);
      }
      return true;
    }
    return false;
  }

  /**
   * Common Bundle methods
   */
  public static Path getBundleWorkingDirectory(String instanceId) throws IOException {
    Path tempDirectory = Files.createTempDirectory(RodaCoreFactory.getWorkingDirectory(),
      RodaConstants.CORE_SYNCHRONIZATION_FOLDER + instanceId);
    LOGGER.debug("Creating " + tempDirectory);
    return tempDirectory;
  }

  public static String getInstanceBundleName(String instanceId) {
    final String date = new SimpleDateFormat("yyyyMMdd'T'HHmmss'.zip'").format(new Date());
    return instanceId + "_" + date;
  }

  public static Path getSyncOutcomeBundlePath(String bundleName) {
    return RodaCoreFactory.getSynchronizationDirectoryPath().resolve(RodaConstants.CORE_SYNCHRONIZATION_OUTCOME_FOLDER)
      .resolve(bundleName);
  }

  public static Path getSyncIncomingBundlePath(String bundleName) {
    return RodaCoreFactory.getSynchronizationDirectoryPath().resolve(RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER)
      .resolve(bundleName);
  }

  public static boolean removeSyncBundleLocal(String bundleName, String deletionDirectory) throws IOException {
    Path syncBundlePath;
    if (RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER.equals(deletionDirectory)) {
      syncBundlePath = SyncUtils.getSyncIncomingBundlePath(bundleName);
    } else {
      syncBundlePath = SyncUtils.getSyncOutcomeBundlePath(bundleName);
    }

    return Files.deleteIfExists(syncBundlePath);

  }

  public static Path receiveBundle(String fileName, InputStream inputStream) throws IOException {
    Path filePath = RodaCoreFactory.getSynchronizationDirectoryPath()
      .resolve(RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER).resolve(fileName);
    FileUtils.copyInputStreamToFile(inputStream, filePath.toFile());
    return filePath;
  }

  public static Path compress(Path workingDir, String filename)
    throws NotFoundException, GenericException, IOException {
    Path outcomePath = RodaCoreFactory.getSynchronizationDirectoryPath()
      .resolve(RodaConstants.CORE_SYNCHRONIZATION_OUTCOME_FOLDER).resolve(filename);
    LOGGER.debug("Compress files to " + outcomePath);

    if (FSUtils.exists(outcomePath)) {
      FSUtils.deletePath(outcomePath);
    }
    ZipUtility.createZIPFile(Files.createFile(outcomePath).toFile(), workingDir.toFile());
    return outcomePath;
  }

  public static void extract(Path workingDir, Path incomingPath) throws IOException {
    LOGGER.debug("Extracting files to " + workingDir);
    ZipUtility.extractFilesFromZIP(incomingPath.toFile(), workingDir.toFile(), true);
  }

  public static StreamResponse createBundleStreamResponse(Path zipPath) {
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

      if (response.getStatusLine().getStatusCode() != RodaConstants.HTTP_RESPONSE_CODE_SUCCESS
        && response.getStatusLine().getStatusCode() != RodaConstants.HTTP_RESPONSE_CODE_NOT_FOUND) {
        throw new GenericException(
          "Unable to retrieve instance status error code: " + response.getStatusLine().getStatusCode());
      }

      return JsonUtils.getObjectFromJson(response.getEntity().getContent(), DistributedInstance.class);
    } catch (AuthenticationDeniedException | GenericException | IOException e) {
      throw new GenericException("Unable to retrieve instance status: " + e.getMessage());
    }
  }

  public static Path requestRemoteActions(LocalInstance localInstance) throws GenericException {
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
    return null;
  }

  private static Path downloadRemoteActions(HttpResponse response, String instanceId) {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      try (InputStream in = entity.getContent()) {
        String bundleName = instanceId + ".zip";
        Header contentDisposition = response.getFirstHeader("Content-Disposition");
        if (contentDisposition != null) {
          String responseValue = contentDisposition.getValue();
          bundleName = responseValue.split("filename=")[1].substring(1,
            responseValue.split("filename=")[1].length() - 1);
        }
        Path actionsFile = SyncUtils.getSyncIncomingBundlePath(bundleName);
        if (Files.exists(actionsFile)) {
          Files.delete(actionsFile);
        }

        Files.copy(in, actionsFile, StandardCopyOption.REPLACE_EXISTING);
        return actionsFile;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static void removeSyncBundlesFromCentral(String bundleName, String bundleDirectory,
    LocalInstance localInstance) {
    try {
      AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);
      String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE + "remove"
        + RodaConstants.API_SEP + "bundle" + RodaConstants.API_QUERY_START + RodaConstants.SYNCHRONIZATION_BUNDLE_NAME
        + RodaConstants.API_QUERY_ASSIGN_SYMBOL + bundleName + RodaConstants.API_QUERY_SEP
        + RodaConstants.SYNCHRONIZATION_BUNDLE_DIRECTORY + RodaConstants.API_QUERY_ASSIGN_SYMBOL + bundleDirectory;

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGet = new HttpGet(localInstance.getCentralInstanceURL() + resource);
      httpGet.addHeader("Authorization", "Bearer " + accessToken.getToken());
      httpGet.addHeader("content-type", "application/json");
      httpGet.addHeader("Accept", "application/json");

      HttpResponse response = httpClient.execute(httpGet);

      if (response.getStatusLine().getStatusCode() == RodaConstants.HTTP_RESPONSE_CODE_SUCCESS) {
        EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
      }
    } catch (AuthenticationDeniedException | GenericException | IOException e) {
      // do nothing
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

  /**
   * Create {@link JsonParser} to read the file.
   *
   * @param path
   *          {@link Path}.
   * @return {@link JsonParser}.
   * @throws IOException
   *           if some i/o error occurs.
   */
  public static JsonParser createJsonParser(Path path) throws IOException {
    final JsonFactory jfactory = new JsonFactory();
    return jfactory.createParser(path.toFile());
  }

  public static void updateDistributedInstance(LocalInstance localInstance, DistributedInstance distributedInstance)
    throws GenericException {
    try {
      AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);
      String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE + "update";

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpPut httpPut = new HttpPut(localInstance.getCentralInstanceURL() + resource);
      httpPut.setEntity(new StringEntity(JsonUtils.getJsonFromObject(distributedInstance)));
      httpPut.addHeader("Authorization", "Bearer " + accessToken.getToken());
      httpPut.addHeader("content-type", "application/json");
      httpPut.addHeader("Accept", "application/json");

      HttpResponse response = httpClient.execute(httpPut);

      if (response.getStatusLine().getStatusCode() != RodaConstants.HTTP_RESPONSE_CODE_SUCCESS) {
        throw new GenericException(
          "Unable to update the distributed instance error code: " + response.getStatusLine().getStatusCode());
      }
    } catch (AuthenticationDeniedException | GenericException | IOException e) {
      throw new GenericException("Unable to retrieve instance status: " + e.getMessage());
    }
  }

  public static Long getUpdatesFromDistributedInstance(LocalInstance localInstance) throws GenericException {
    try {
      AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);
      String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE
        + RodaConstants.API_PATH_PARAM_DISTRIBUTED_INSTANCE_GET_UPDATES + RodaConstants.API_SEP + localInstance.getId();

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGet = new HttpGet(localInstance.getCentralInstanceURL() + resource);
      httpGet.addHeader("Authorization", "Bearer " + accessToken.getToken());
      httpGet.addHeader("content-type", "application/json");
      httpGet.addHeader("Accept", "application/json");

      HttpResponse response = httpClient.execute(httpGet);
      String message = JsonUtils.parseJson(EntityUtils.toString(response.getEntity())).get("message").textValue();

      if (SynchronizingStatus.INACTIVE.equals(localInstance.getStatus())) {
        throw new GenericException("Instance is Inactive");
      }
      if (response.getStatusLine().getStatusCode() != RodaConstants.HTTP_RESPONSE_CODE_SUCCESS) {
        throw new GenericException(
          "Unable to update the distributed instance error code: " + response.getStatusLine().getStatusCode());
      } else {
        return Long.parseLong(message);
      }
    } catch (AuthenticationDeniedException | GenericException | IOException e) {
      throw new GenericException("Unable to retrieve instance status: " + e.getMessage());
    }
  }

}
