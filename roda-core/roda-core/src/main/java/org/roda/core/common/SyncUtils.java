package org.roda.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.tools.ZipEntryInfo;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.bundle.RemoteActions;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.ZipUtility;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SyncUtils {
  // Central instance methods

  public static StreamResponse createRemoteActionsBundle(String instanceIdentifier)
    throws GenericException, NotFoundException {
    String fileName = instanceIdentifier + ".zip";
    RemoteActions remoteActions = new RemoteActions();

    IndexService index = RodaCoreFactory.getIndexService();
    StorageService storage = RodaCoreFactory.getStorageService();

    List<ZipEntryInfo> zipEntries = new ArrayList<>();
    try {
      Path tempFile = Files.createTempFile(instanceIdentifier, ".json");
      JsonUtils.writeObjectToFile(remoteActions, tempFile);

      Filter filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.JOB_INSTANCE_ID, instanceIdentifier));
      filter.add(new SimpleFilterParameter(RodaConstants.JOB_STATE, "CREATED"));
      Long count = index.count(Job.class, filter);
      if (count == 0) {
        throw new NotFoundException("Cannot retrieve jobs for this instance ID");
      }
      for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        List<Job> jobs = index
          .find(Job.class, filter, null, new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), new ArrayList<>())
          .getResults();

        for (Job job : jobs) {
          StoragePath jobStoragePath = ModelUtils.getJobStoragePath(job.getId());
          Binary binary = storage.getBinary(jobStoragePath);
          ZipEntryInfo info = new ZipEntryInfo(jobStoragePath.getName(), binary.getContent());
          zipEntries.add(info);

          remoteActions.addToJobList(job.getId());
        }
        JsonUtils.writeObjectToFile(remoteActions, tempFile);
        ZipEntryInfo info = new ZipEntryInfo(instanceIdentifier + ".json", tempFile);
        zipEntries.add(info);
      }
      return DownloadUtils.createZipStreamResponse(zipEntries, fileName);
    } catch (RequestNotValidException | AuthorizationDeniedException | IOException e) {
      throw new GenericException("Unable to create remote actions file: " + e.getMessage());
    }
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

  public static void requestRemoteActions(LocalInstance localInstance) throws GenericException {
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
        Path path = downloadRemoteActions(response, localInstance.getId());
        if (path != null) {
          Path extractFile = Files.createTempDirectory(path.getFileName().toString());
          ZipUtility.extractFilesFromZIP(path.toFile(), extractFile.toFile(), true);
          createJobs(extractFile, localInstance.getId());
        }
      }
    } catch (AuthenticationDeniedException | IOException e) {
      throw new GenericException("unable to communicate with the central instance");
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      throw new GenericException("Unable to perform remote actions");
    } catch (JobAlreadyStartedException e) {
      // Do nothing
    }
  }

  public static Path downloadRemoteActions(HttpResponse response, String instanceId) {
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

  public static void createJobs(Path path, String instanceId) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, JobAlreadyStartedException, NotFoundException {
    Path remoteActionsFile = path.resolve(instanceId + ".json");
    RemoteActions remoteActions = JsonUtils.readObjectFromFile(remoteActionsFile, RemoteActions.class);
    for (String jobId : remoteActions.getJobList()) {
      Path jobFile = path.resolve(jobId + ".json");
      Job job = JsonUtils.readObjectFromFile(jobFile, Job.class);
      PluginHelper.createAndExecuteJob(job);
    }
  }
}
