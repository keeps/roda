package org.roda.wui.api.v2.services;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.DefaultConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.maintenance.ChangeRepresentationStatusPlugin;
import org.roda.core.plugins.base.maintenance.ChangeTypePlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Directory;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Service
public class RepresentationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationService.class);
  private static final String HTML_EXT = ".html";

  private static DescriptiveMetadataInfo retrieveDescriptiveMetadataBundle(String aipId, String representationId,
    DescriptiveMetadata descriptiveMetadata, final Locale locale) {
    ModelService model = RodaCoreFactory.getModelService();
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    DescriptiveMetadataInfo info = new DescriptiveMetadataInfo();
    info.setId(descriptiveMetadata.getId());

    if (descriptiveMetadata.getType() != null) {
      try {
        String labelWithoutVersion = messages.getTranslation(
          RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase(),
          descriptiveMetadata.getId());

        if (descriptiveMetadata.getVersion() != null) {
          String labelWithVersion = messages.getTranslation(
            RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase()
              + RodaConstants.METADATA_VERSION_SEPARATOR + descriptiveMetadata.getVersion().toLowerCase(),
            labelWithoutVersion);
          info.setLabel(labelWithVersion);
        } else {
          info.setLabel(labelWithoutVersion);
        }
      } catch (MissingResourceException e) {
        info.setLabel(descriptiveMetadata.getId());
      }
    }

    try {
      StoragePath storagePath;
      storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId, descriptiveMetadata.getId());
      info.setHasHistory(!CloseableIterables.isEmpty(model.listBinaryVersions(storagePath)));
    } catch (RODAException | RuntimeException e) {
      info.setHasHistory(false);
    }

    return info;
  }

  public Representation retrieveAIPRepresentation(IndexedRepresentation representation)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String aipId = representation.getAipId();
    String representationId = representation.getId();
    ModelService model = RodaCoreFactory.getModelService();
    return model.retrieveRepresentation(aipId, representationId);
  }

  public StreamResponse retrieveAIPRepresentationBinary(IndexedRepresentation representation)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String representationId = representation.getId();
    StoragePath storagePath = ModelUtils.getRepresentationStoragePath(representation.getAipId(),
      representation.getId());

    Directory directory = RodaCoreFactory.getModelService().getDirectory(storagePath);
    return ApiUtils.download(directory, representationId);
  }

  public Representation createRepresentation(User user, String aipId, String representationId, String type,
    String details) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
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
      model.createEvent(aipId, null, null, null, RodaConstants.PreservationEventType.CREATION, eventDescription, null,
        targets, PluginState.SUCCESS, outcomeText, details, user.getName(), true);

      RodaCoreFactory.getIndexService().commit(IndexedRepresentation.class);
      return representation;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      String outcomeText = "The representation '" + representationId + "' has not been manually created";
      model.createUpdateAIPEvent(aipId, null, null, null, RodaConstants.PreservationEventType.CREATION,
        eventDescription, PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public Job changeRepresentationType(User user, SelectedItems<IndexedRepresentation> selected, String newType,
    String details) throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_TYPE, newType);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return CommonServicesUtils.createAndExecuteInternalJob("Change representation type", selected,
      ChangeTypePlugin.class, user, pluginParameters, "Could not change representation type");
  }

  public Job deleteRepresentation(User user, SelectedItems<IndexedRepresentation> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return CommonServicesUtils.createAndExecuteInternalJob("Delete representations", selected,
      DeleteRODAObjectPlugin.class, user, pluginParameters, "Could not execute representations delete action");
  }

  public Job changeRepresentationStatus(User user, SelectedItems<IndexedRepresentation> selected,
    List<String> newStatus, String details)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_STATUS, String.join(",", newStatus));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return CommonServicesUtils.createAndExecuteInternalJob("Change representation status", selected,
      ChangeRepresentationStatusPlugin.class, user, pluginParameters, "Could not change representation status");
  }

  public Job createFormatIdentificationJob(User user, SelectedItems<?> selected)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return CommonServicesUtils.createAndExecuteJob("Format identification using Siegfried", selected,
      SiegfriedPlugin.class, PluginType.MISC, user, Collections.emptyMap(),
      "Could not execute format identification using Siegfrid action");
  }

  private static List<DescriptiveMetadata> orderDescriptiveMetadata(List<DescriptiveMetadata> metadata) {
    List<DescriptiveMetadata> orderedMetadata = new ArrayList<>();
    boolean order = RodaCoreFactory.getProperty("ui.browser.metadata.order", false);
    List<DescriptiveMetadata> metadataCopy = new ArrayList<>(metadata);

    if (order) {
      List<String> metadataDefaultTypes = RodaCoreFactory
        .getRodaConfigurationAsList("ui.browser.metadata.descriptive.types");

      for (String metadataDefaultType : metadataDefaultTypes) {
        for (DescriptiveMetadata dm : metadata) {
          String id = StringUtils.isNotBlank(dm.getVersion()) ? dm.getType() + "_" + dm.getVersion() : dm.getType();
          if (metadataDefaultType.equals(id)) {
            orderedMetadata.add(dm);
            metadataCopy.remove(dm);
          }
        }
      }
    }

    orderedMetadata.addAll(metadataCopy);
    return orderedMetadata;
  }

  public StreamResponse retrieveRepresentationDescriptiveMetadata(String aipId, String representationId,
    String metadataId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ModelService modelService = RodaCoreFactory.getModelService();
    Binary descriptiveMetadataBinary = modelService.retrieveDescriptiveMetadataBinary(aipId, representationId,
      metadataId);
    String filename = descriptiveMetadataBinary.getStoragePath().getName() + HTML_EXT;
    DescriptiveMetadata descriptiveMetadata = modelService.retrieveDescriptiveMetadata(aipId, representationId,
      metadataId);
    String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(descriptiveMetadataBinary,
      descriptiveMetadata.getType(), descriptiveMetadata.getVersion(), ServerTools.parseLocale(localeString));

    ConsumesOutputStream stream = new DefaultConsumesOutputStream(filename, RodaConstants.MEDIA_TYPE_TEXT_HTML, out -> {
      PrintStream printStream = new PrintStream(out);
      printStream.print(htmlDescriptive);
      printStream.close();
    });

    return new StreamResponse(stream);
  }

  public StreamResponse retrieveAIPRepresentationOtherMetadata(IndexedRepresentation representation)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String aipId = representation.getAipId();
    String representationId = representation.getId();
    StoragePath storagePath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, "", "", "");
    Directory directory = RodaCoreFactory.getModelService().getDirectory(storagePath);
    return ApiUtils.download(directory, representationId);
  }

  public DescriptiveMetadataInfos getDescriptiveMetadata(IndexedRepresentation indexedRepresentation,
    String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Locale locale = ServerTools.parseLocale(localeString);
    DescriptiveMetadataInfos descriptiveMetadataInfos = new DescriptiveMetadataInfos();

    Representation representation = RodaCoreFactory.getModelService()
      .retrieveRepresentation(indexedRepresentation.getAipId(), indexedRepresentation.getId());

    // Can be null when the AIP is a ghost
    if (representation.getDescriptiveMetadata() != null) {
      List<DescriptiveMetadata> orderedMetadata = orderDescriptiveMetadata(representation.getDescriptiveMetadata());

      for (DescriptiveMetadata descriptiveMetadata : orderedMetadata) {
        descriptiveMetadataInfos.addObject(retrieveDescriptiveMetadataBundle(representation.getAipId(),
          representation.getId(), descriptiveMetadata, locale));
      }
    }

    return descriptiveMetadataInfos;
  }

  public Optional<String> retrieveDistributedInstanceName(String instanceId, boolean isLocalInstance) {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

      if (RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
        if (isLocalInstance) {
          return Optional.of(RodaCoreFactory.getProperty(RodaConstants.CENTRAL_INSTANCE_NAME_PROPERTY,
            RodaConstants.DEFAULT_CENTRAL_INSTANCE_NAME));
        } else {
          DistributedInstance distributedInstance = model.retrieveDistributedInstance(instanceId);
          return Optional.of(distributedInstance.getName());
        }
      }
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      LOGGER.warn("Could not retrieve the distributed instance", e);
      return Optional.empty();
    }

    return Optional.empty();
  }

  public List<String> getConfigurationRepresentationRules(User user) {
    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION_INFORMATION)) {
      return RodaCoreFactory.getRodaConfigurationAsList("ui.ri.rule.Representation").stream()
        .map(r -> RodaCoreFactory.getRodaConfigurationAsString(r, RodaConstants.SEARCH_FIELD_FIELDS)).toList();
    } else {
      return Collections.emptyList();
    }
  }
}
