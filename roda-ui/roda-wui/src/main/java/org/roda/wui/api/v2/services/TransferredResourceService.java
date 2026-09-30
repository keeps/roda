/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.common.notifications.WebhookUrlValidator;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.base.ingest.v2.ConfigurableIngestPlugin;
import org.roda.core.plugins.base.ingest.v2.MinimalIngestPlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.MovePlugin;
import org.roda.core.plugins.base.notifications.HttpGenericNotification;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
public class TransferredResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceService.class);

  // ingest plugins that honour the HTTP notification endpoint parameter
  private static final Set<String> WEBHOOK_INGEST_PLUGINS = Set.of(ConfigurableIngestPlugin.class.getName(),
    MinimalIngestPlugin.class.getName());
  private static final String GENERIC_HTTP_NOTIFICATION_PARAMETERS_PREFIX = "parameter.notification."
    + HttpGenericNotification.class.getSimpleName() + ".";

  @Autowired
  private JobService jobService;

  public List<TransferredResource> retrieveSelectedTransferredResource(IndexService index, SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException {
    switch (selected) {
      case SelectedItemsList<TransferredResource> selectedList -> {
        Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
        IndexResult<TransferredResource> results = index.find(TransferredResource.class,
          filter, Sorter.NONE, new Sublist(0, selectedList.getIds().size()), new ArrayList<>());
        return results.getResults();
      }
      case SelectedItemsFilter<TransferredResource> selectedFilter -> {
        Long counter = index.count(TransferredResource.class, selectedFilter.getFilter());
        IndexResult<TransferredResource> results = index.find(TransferredResource.class,
          selectedFilter.getFilter(), Sorter.NONE, new Sublist(0, counter.intValue()), new ArrayList<>());
        return results.getResults();
      }
      case null, default -> {
        return new ArrayList<>();
      }
    }
  }

  public Job deleteTransferredResourcesByJob(SelectedItems<TransferredResource> selected, User user)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return CommonServicesUtils.createAndExecuteInternalJob("Delete transferred resources", selected,
      DeleteRODAObjectPlugin.class, user, Collections.emptyMap(),
      "Could not execute delete transferred resources action");
  }

  public String renameTransferredResource(IndexService index, String transferredResourceId, String newName, Boolean replaceExisting)
    throws GenericException, RequestNotValidException, AlreadyExistsException, IsStillUpdatingException,
    NotFoundException, AuthorizationDeniedException {
    List<String> resourceFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.TRANSFERRED_RESOURCE_FULLPATH,
      RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID);

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, transferredResourceId));
    IndexResult<TransferredResource> resources = index.find(TransferredResource.class,
      filter, Sorter.NONE, new Sublist(0, 1), resourceFields);

    if (!resources.getResults().isEmpty()) {
      TransferredResource resource = resources.getResults().getFirst();
      return RodaCoreFactory.getTransferredResourcesScanner().renameTransferredResource(resource, newName,
        replaceExisting, true);
    } else {
      return transferredResourceId;
    }
  }

  public void updateTransferredResources(Optional<String> folderRelativePath, boolean waitToFinish)
    throws IsStillUpdatingException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getTransferredResourcesScanner().updateTransferredResources(folderRelativePath, waitToFinish);
  }

  public TransferredResource reindexTransferredResource(IndexService indexService, String path)
    throws IsStillUpdatingException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransferredResourcesScanner scanner = RodaCoreFactory.getTransferredResourcesScanner();
    Optional<String> normalizedPath = scanner.updateTransferredResources(Optional.ofNullable(path), true);
    if (normalizedPath.isPresent()) {
      return indexService.retrieve(TransferredResource.class,
        IdUtils.getTransferredResourceUUID(normalizedPath.get()), Collections.emptyList());
    } else {
      return null;
    }
  }

  public TransferredResource createTransferredResourcesFolder(String parentUUID, String folderName, boolean forceCommit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    TransferredResource transferredResource = RodaCoreFactory.getTransferredResourcesScanner().createFolder(parentUUID,
      folderName);
    if (forceCommit) {
      RodaCoreFactory.getTransferredResourcesScanner().commit();
    }

    transferredResource.setFullPath("");

    return transferredResource;
  }

  public TransferredResource createTransferredResourceFile(String parentUUID, String fileName, InputStream inputStream,
    boolean forceCommit)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    LOGGER.debug("createTransferredResourceFile(path={}, name={})", parentUUID, fileName);
    TransferredResource transferredResource = RodaCoreFactory.getTransferredResourcesScanner().createFile(parentUUID,
      fileName, inputStream);

    if (forceCommit) {
      RodaCoreFactory.getTransferredResourcesScanner().commit();
    }

    return transferredResource;
  }

  /**
   * Validates the whole request before storing anything, then stores the file
   * and creates an ingest job for it. If the job cannot be created, the stored
   * file is removed.
   */
  public Job createTransferredResourceAndIngest(User user, String parentUUID, String fileName, InputStream inputStream,
    String plugin, String parametersJson, String webhook) throws RODAException {
    String pluginId = StringUtils.isBlank(plugin) ? ConfigurableIngestPlugin.class.getName() : plugin;
    Map<String, String> pluginParameters = parsePluginParameters(parametersJson);

    if (StringUtils.isNotBlank(webhook)) {
      if (!WEBHOOK_INGEST_PLUGINS.contains(pluginId)) {
        throw new RequestNotValidException("A webhook can only be used with the plugins " + WEBHOOK_INGEST_PLUGINS);
      }
      WebhookUrlValidator.validate(webhook);
      pluginParameters.put(RodaConstants.NOTIFICATION_HTTP_ENDPOINT, webhook);
      pluginParameters.put(RodaConstants.NOTIFICATION_HTTP_ENDPOINT_RESTRICTED, Boolean.TRUE.toString());
    }

    Job job = new Job();
    job.setPlugin(pluginId);
    job.setPluginParameters(pluginParameters);
    job.setPriority(JobPriority.MEDIUM);
    job.setParallelism(JobParallelism.NORMAL);
    job.setSourceObjects(SelectedItemsList.create(TransferredResource.class, new ArrayList<>()));
    jobService.validateAndSetJobInformation(user, job);
    if (job.getPluginType() != PluginType.INGEST) {
      throw new RequestNotValidException("Plugin '" + pluginId + "' is not an ingest plugin");
    }

    TransferredResource transferredResource = createTransferredResourceFile(parentUUID, fileName, inputStream, true);
    job.setSourceObjects(SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));
    try {
      return jobService.createJob(job, true);
    } catch (RODAException | RuntimeException e) {
      try {
        RodaCoreFactory.getTransferredResourcesScanner()
          .deleteTransferredResource(Collections.singletonList(transferredResource.getUUID()));
      } catch (RODAException deleteException) {
        LOGGER.error("Could not remove transferred resource '{}' after failing to create its ingest job",
          transferredResource.getUUID(), deleteException);
      }
      throw e;
    }
  }

  private static Map<String, String> parsePluginParameters(String parametersJson) throws RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    if (StringUtils.isNotBlank(parametersJson)) {
      try {
        pluginParameters.putAll(
          JsonMapper.builder().build().readValue(parametersJson, new TypeReference<Map<String, String>>() {}));
      } catch (JacksonException e) {
        throw new RequestNotValidException("Parameters must be a JSON object with string values");
      }
    }

    // the webhook must only be set through the validated webhook parameter
    for (String key : pluginParameters.keySet()) {
      if (RodaConstants.NOTIFICATION_HTTP_ENDPOINT.equals(key) || key.startsWith(GENERIC_HTTP_NOTIFICATION_PARAMETERS_PREFIX)) {
        throw new RequestNotValidException("Parameter '" + key + "' is not allowed, use the webhook parameter instead");
      }
    }
    return pluginParameters;
  }

  public Job moveTransferredResource(User user, SelectedItems<TransferredResource> selected,
    TransferredResource transferredResource)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    String resourceRelativePath = "";
    if (transferredResource != null) {
      resourceRelativePath = transferredResource.getRelativePath();
    }

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, resourceRelativePath);
    return CommonServicesUtils.createAndExecuteInternalJob("Move transferred resources", selected, MovePlugin.class,
      user, pluginParameters, "Could not execute move transferred resources action");
  }

  public StreamResponse createStreamResponse(TransferredResource transferredResource) {
    ConsumesOutputStream stream = new ConsumesOutputStream() {

      @Override
      public String getMediaType() {
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
      }

      @Override
      public String getFileName() {
        return transferredResource.getName();
      }

      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {
        try (InputStream in = RodaCoreFactory.getTransferredResourcesScanner()
          .retrieveFile(transferredResource.getRelativePath())) {
          IOUtils.copy(in, out);
        } catch (RequestNotValidException | NotFoundException | GenericException e) {
          // do nothing
        }
      }

      @Override
      public Date getLastModified() {
        return null;
      }

      @Override
      public long getSize() {
        return transferredResource.getSize();
      }
    };

    return new StreamResponse(stream);
  }
}
