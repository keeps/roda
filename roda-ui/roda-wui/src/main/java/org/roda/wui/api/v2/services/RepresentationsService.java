package org.roda.wui.api.v2.services;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.maintenance.ChangeTypePlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.Directory;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Service
public class RepresentationsService {

  @Autowired
  private JobService jobService;

  public Representation retrieveAIPRepresentation(IndexedRepresentation representation)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String aipId = representation.getAipId();
    String representationId = representation.getId();
    ModelService model = RodaCoreFactory.getModelService();
    return model.retrieveRepresentation(aipId, representationId);
  }

  public StreamResponse retrieveAIPRepresentationBinary(IndexedRepresentation representation)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String aipId = representation.getAipId();
    String representationId = representation.getId();
    StoragePath storagePath = ModelUtils.getRepresentationStoragePath(representation.getAipId(),
      representation.getId());
    // ModelUtils.getOtherMetadataStoragePath();
    Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
    return ApiUtils.download(directory, representationId);
  }

  public Representation createRepresentation(User user, String aipId, String representationId, String type, String details)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    String eventDescription = "The process of creating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();

    try {
      Representation representation = model.createRepresentation(aipId, representationId, true, type, true,
        user.getName());

      List<LinkingIdentifier> targets = new ArrayList<>();
      targets.add(PluginHelper.getLinkingIdentifier(aipId, representation.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

      String outcomeText = "The representation '" + representationId + "' has been manually created";
      model.createEvent(aipId, null, null, null, RodaConstants.PreservationEventType.CREATION, eventDescription, null, targets,
        PluginState.SUCCESS, outcomeText, details, user.getName(), true);

      RodaCoreFactory.getIndexService().commit(IndexedRepresentation.class);
      return representation;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
             | AlreadyExistsException e) {
      String outcomeText = "The representation '" + representationId + "' has not been manually created";
      model.createUpdateAIPEvent(aipId, null, null, null, RodaConstants.PreservationEventType.CREATION, eventDescription,
        PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public Job changeRepresentationType(User user, SelectedItems<IndexedRepresentation> selected, String newType,
                                      String details) throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_TYPE, newType);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return jobService.createAndExecuteInternalJob("Change representation type", selected, ChangeTypePlugin.class, user,
      pluginParameters, "Could not change representation type");
  }

  public Job deleteRepresentation(User user, SelectedItems<IndexedRepresentation> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return jobService.createAndExecuteInternalJob("Delete representations", selected, DeleteRODAObjectPlugin.class, user,
      pluginParameters, "Could not execute representations delete action");
  }

  public void changeRepresentationStates(User user, IndexedRepresentation representation, List<String> newStates,
                                                String details) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    String eventDescription = "The process of updating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    List<LinkingIdentifier> sources = new ArrayList<>();
    sources.add(PluginHelper.getLinkingIdentifier(representation.getAipId(), representation.getId(),
      RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

    try {
      model.changeRepresentationStates(representation.getAipId(), representation.getId(), newStates, user.getName());
      index.commit(IndexedRepresentation.class);
      StringBuilder outcomeText = new StringBuilder().append("The states of the representation '")
        .append(representation.getId()).append("' were updated.");

      model.createEvent(representation.getAipId(), representation.getId(), null, null, RodaConstants.PreservationEventType.UPDATE,
        eventDescription, sources, null, PluginState.SUCCESS, outcomeText.toString(), details, user.getName(), true);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      StringBuilder outcomeText = new StringBuilder().append("The states of the representation '")
        .append(representation.getId()).append("' were not updated.");

      model.createEvent(representation.getAipId(), representation.getId(), null, null, RodaConstants.PreservationEventType.UPDATE,
        eventDescription, sources, null, PluginState.FAILURE, outcomeText.toString(), details, user.getName(), true);
      throw e;
    }
  }

  public Job createFormatIdentificationJob(User user, SelectedItems<?> selected)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return jobService.createAndExecuteJob("Format identification using Siegfried", selected, SiegfriedPlugin.class,
      PluginType.MISC, user, Collections.emptyMap(), "Could not execute format identification using Siegfrid action");
  }

  public StreamResponse retrieveAIPRepresentationOthermetadata(IndexedRepresentation representation)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String aipId = representation.getAipId();
    String representationId = representation.getId();
    StoragePath storagePath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, "", "", "");
    Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
    return ApiUtils.download(directory, representationId);
  }

}
