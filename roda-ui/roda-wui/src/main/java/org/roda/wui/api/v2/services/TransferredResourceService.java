package org.roda.wui.api.v2.services;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
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
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.MovePlugin;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

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

@Service
public class TransferredResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceService.class);

  public List<TransferredResource> retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException {
    if (selected instanceof SelectedItemsList<TransferredResource> selectedList) {
      Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
      IndexResult<TransferredResource> results = RodaCoreFactory.getIndexService().find(TransferredResource.class,
        filter, Sorter.NONE, new Sublist(0, selectedList.getIds().size()), new ArrayList<>());
      return results.getResults();
    } else if (selected instanceof SelectedItemsFilter<TransferredResource> selectedFilter) {
      Long counter = RodaCoreFactory.getIndexService().count(TransferredResource.class, selectedFilter.getFilter());
      IndexResult<TransferredResource> results = RodaCoreFactory.getIndexService().find(TransferredResource.class,
        selectedFilter.getFilter(), Sorter.NONE, new Sublist(0, counter.intValue()), new ArrayList<>());
      return results.getResults();
    } else {
      return new ArrayList<>();
    }
  }

  public Job deleteTransferredResourcesByJob(SelectedItems<TransferredResource> selected, User user)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return CommonServicesUtils.createAndExecuteInternalJob("Delete transferred resources", selected,
      DeleteRODAObjectPlugin.class, user, Collections.emptyMap(),
      "Could not execute delete transferred resources action");
  }

  public String renameTransferredResource(String transferredResourceId, String newName, Boolean replaceExisting)
    throws GenericException, RequestNotValidException, AlreadyExistsException, IsStillUpdatingException,
    NotFoundException, AuthorizationDeniedException {
    List<String> resourceFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.TRANSFERRED_RESOURCE_FULLPATH,
      RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID);

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, transferredResourceId));
    IndexResult<TransferredResource> resources = RodaCoreFactory.getIndexService().find(TransferredResource.class,
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

  public TransferredResource reindexTransferredResource(String path)
    throws IsStillUpdatingException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransferredResourcesScanner scanner = RodaCoreFactory.getTransferredResourcesScanner();
    Optional<String> normalizedPath = scanner.updateTransferredResources(Optional.ofNullable(path), true);
    if (normalizedPath.isPresent()) {
      return RodaCoreFactory.getIndexService().retrieve(TransferredResource.class,
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
