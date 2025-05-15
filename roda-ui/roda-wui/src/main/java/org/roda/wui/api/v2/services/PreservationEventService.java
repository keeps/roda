package org.roda.wui.api.v2.services;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.DefaultConsumesOutputStream;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationEventsLinkingObjects;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.util.IdUtils;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.ServerTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.LinkingAgentIdentifierComplexType;
import gov.loc.premis.v3.LinkingObjectIdentifierComplexType;
import gov.loc.premis.v3.StringPlusAuthority;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class PreservationEventService {

  private static final String HTML_EXT = ".html";
  private IndexService indexService;

  @Autowired
  public void setIndexService(IndexService service) {
    this.indexService = service;
  }

  public PreservationEventsLinkingObjects getLinkingObjectsFromPreservationEventBinary(Binary preservationEventBinary,
    RequestContext context) throws ValidationException, GenericException {
    PreservationEventsLinkingObjects linkingObjects = new PreservationEventsLinkingObjects();
    List<LinkingIdentifier> sourceObjectIds = new ArrayList<>();
    Map<String, IndexedAIP> aips = new HashMap<>();
    Map<String, IndexedRepresentation> representations = new HashMap<>();
    Map<String, IndexedFile> files = new HashMap<>();
    Map<String, TransferredResource> transferredResources = new HashMap<>();
    List<String> uris = new ArrayList<>();

    EventComplexType eventComplexType = PremisV3Utils.binaryToEvent(preservationEventBinary.getContent(), false);

    for (LinkingObjectIdentifierComplexType linkingObjectIdentifierComplexType : eventComplexType
      .getLinkingObjectIdentifier()) {
      LinkingIdentifier identifier = getLinkingIdentifier(linkingObjectIdentifierComplexType);
      sourceObjectIds.add(identifier);

      String idValue = identifier.getValue();
      RodaConstants.RODA_TYPE linkingType = LinkingObjectUtils.getLinkingIdentifierType(idValue);

      if (RodaConstants.RODA_TYPE.AIP.equals(linkingType)) {
        aips.putAll(linkingAIPObjectsIdentifier(idValue, context));
      } else if (RodaConstants.RODA_TYPE.REPRESENTATION.equals(linkingType)) {
        representations.putAll(linkingRepresentationObjectsIdentifier(idValue, context));
      } else if (RodaConstants.RODA_TYPE.FILE.equals(linkingType)) {
        files.putAll(linkingFileObjectIdentifier(idValue, context));
      } else if (RodaConstants.RODA_TYPE.TRANSFERRED_RESOURCE.equals(linkingType)) {
        transferredResources.putAll(linkingTransferredResourceObjectIdentifier(idValue, context));
      } else if (RodaConstants.URI_TYPE.equals(identifier.getType())) {
        uris.add(idValue);
      } else {
        throw new GenericException("No support for linking object type: " + idValue);
      }
    }

    linkingObjects.setSourceObjectIds(sourceObjectIds);
    linkingObjects.setOutcomeObjectIds(sourceObjectIds);
    linkingObjects.setAips(aips);
    linkingObjects.setFiles(files);
    linkingObjects.setRepresentations(representations);
    linkingObjects.setTransferredResources(transferredResources);
    linkingObjects.setUris(uris);
    return linkingObjects;
  }

  private Map<String, IndexedAIP> linkingAIPObjectsIdentifier(String idValue, RequestContext context) {
    Map<String, IndexedAIP> map = new HashMap<>();
    String uuid = LinkingObjectUtils.getAipIdFromLinkingId(idValue);
    if (uuid != null) {
      List<String> aipFields = Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.INDEX_UUID);
      FindRequest findRequest = FindRequest
        .getBuilder(new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, uuid)), false)
        .withFieldsToReturn(aipFields).withSublist(new Sublist(0, 1)).build();

      IndexResult<IndexedAIP> indexedAIPIndexResult = indexService.find(IndexedAIP.class, findRequest, context);
      if (indexedAIPIndexResult.getTotalCount() == 1) {
        map.put(idValue, indexedAIPIndexResult.getResults().getFirst());
      }
    }

    return map;
  }

  private Map<String, IndexedRepresentation> linkingRepresentationObjectsIdentifier(String idValue,
    RequestContext context) {
    Map<String, IndexedRepresentation> map = new HashMap<>();

    String uuid = LinkingObjectUtils.getRepresentationIdFromLinkingId(idValue);

    if (uuid != null) {
      List<String> representationFields = Arrays.asList(RodaConstants.REPRESENTATION_ID,
        RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ORIGINAL);

      FindRequest findRequest = FindRequest
        .getBuilder(new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, uuid)), false)
        .withFieldsToReturn(representationFields).withSublist(new Sublist(0, 1)).build();

      IndexResult<IndexedRepresentation> indexedRepresentationIndexResult = indexService
        .find(IndexedRepresentation.class, findRequest, context);
      if (indexedRepresentationIndexResult.getTotalCount() == 1) {
        map.put(idValue, indexedRepresentationIndexResult.getResults().getFirst());
      }
    }

    return map;
  }

  private Map<String, IndexedFile> linkingFileObjectIdentifier(String idValue, RequestContext context) {
    Map<String, IndexedFile> map = new HashMap<>();
    String uuid = LinkingObjectUtils.getFileIdFromLinkingId(idValue);

    if (uuid == null) {
      return map;
    }

    List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
    fileFields.addAll(RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
    fileFields.addAll(Arrays.asList(RodaConstants.FILE_ORIGINALNAME, RodaConstants.FILE_SIZE,
      RodaConstants.FILE_FILEFORMAT, RodaConstants.FILE_FORMAT_VERSION, RodaConstants.FILE_FORMAT_DESIGNATION));

    FindRequest findRequest = FindRequest
      .getBuilder(new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, uuid)), false)
      .withFieldsToReturn(fileFields).withSublist(new Sublist(0, 1)).build();

    IndexResult<IndexedFile> indexedFileIndexResult = indexService.find(IndexedFile.class, findRequest, context);

    if (indexedFileIndexResult.getTotalCount() == 1) {
      map.put(idValue, indexedFileIndexResult.getResults().getFirst());
    }

    return map;
  }

  private Map<String, TransferredResource> linkingTransferredResourceObjectIdentifier(String idValue,
    RequestContext context) {
    Map<String, TransferredResource> map = new HashMap<>();

    String id = LinkingObjectUtils.getTransferredResourceIdFromLinkingId(idValue);
    if (id != null) {
      List<String> resourceFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.TRANSFERRED_RESOURCE_NAME,
        RodaConstants.TRANSFERRED_RESOURCE_FULLPATH);

      FindRequest findRequest = FindRequest
        .getBuilder(new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, IdUtils.createUUID(id))), false)
        .withFieldsToReturn(resourceFields).withSublist(new Sublist(0, 1)).build();

      IndexResult<TransferredResource> transferredResourceIndexResult = indexService.find(TransferredResource.class,
        findRequest, context);

      if (transferredResourceIndexResult.getTotalCount() == 1) {
        map.put(idValue, transferredResourceIndexResult.getResults().getFirst());
      }
    }

    return map;
  }

  private LinkingIdentifier getLinkingIdentifier(
    LinkingObjectIdentifierComplexType linkingObjectIdentifierComplexType) {
    LinkingIdentifier identifier = new LinkingIdentifier();
    identifier.setValue(linkingObjectIdentifierComplexType.getLinkingObjectIdentifierValue());
    identifier.setType(linkingObjectIdentifierComplexType.getLinkingObjectIdentifierType().getValue());
    List<String> roles = new ArrayList<>();
    for (StringPlusAuthority stringPlusAuthority : linkingObjectIdentifierComplexType.getLinkingObjectRole()) {
      roles.add(stringPlusAuthority.getValue());
    }
    identifier.setRoles(roles);
    return identifier;
  }

  public List<IndexedPreservationAgent> getAgentsFromPreservationEventBinary(Binary preservationEventBinary,
    RequestContext context) throws ValidationException, GenericException {
    List<IndexedPreservationAgent> agents = new ArrayList<>();
    List<String> agentFields = Arrays.asList(RodaConstants.PRESERVATION_AGENT_ID, RodaConstants.PRESERVATION_AGENT_NAME,
      RodaConstants.PRESERVATION_AGENT_TYPE, RodaConstants.PRESERVATION_AGENT_ROLES,
      RodaConstants.PRESERVATION_AGENT_VERSION, RodaConstants.PRESERVATION_AGENT_NOTE,
      RodaConstants.PRESERVATION_AGENT_EXTENSION);

    EventComplexType eventComplexType = PremisV3Utils.binaryToEvent(preservationEventBinary.getContent(), false);
    for (LinkingAgentIdentifierComplexType linkingAgentIdentifierComplexType : eventComplexType
      .getLinkingAgentIdentifier()) {

      IndexedPreservationAgent agent = indexService.retrieve(context, IndexedPreservationAgent.class,
        linkingAgentIdentifierComplexType.getLinkingAgentIdentifierValue(), agentFields);
      agents.add(agent);

    }

    return agents;
  }

  public Binary getPreservationEventBinary(IndexedPreservationEvent preservationEvent, RequestContext context)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    ModelService model = RodaCoreFactory.getModelService();
    Binary binary;

    if (preservationEvent.getObjectClass().equals(IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY)) {
      binary = model.retrieveRepositoryPreservationEvent(preservationEvent.getId());
    } else {
      String representationId = null;
      String fileId = null;
      List<String> directoryFilePath = null;

      if (preservationEvent.getRepresentationUUID() != null) {
        IndexedRepresentation representation = indexService.retrieve(context, IndexedRepresentation.class,
          preservationEvent.getRepresentationUUID(), new ArrayList<>());
        representationId = representation.getId();
      }

      if (preservationEvent.getFileUUID() != null) {
        IndexedFile file = indexService.retrieve(context, IndexedFile.class, preservationEvent.getFileUUID(),
          new ArrayList<>());
        fileId = file.getId();
        directoryFilePath = file.getPath();
      }
      binary = model.retrievePreservationEvent(preservationEvent.getAipID(), representationId, directoryFilePath,
        fileId, preservationEvent.getId());
    }

    return binary;
  }

  public StreamResponse retrievePreservationEventFile(IndexedPreservationEvent preservationEvent,
    RequestContext context)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    Binary binary = getPreservationEventBinary(preservationEvent, context);
    DirectResourceAccess resourceAccess = RodaCoreFactory.getModelService().getDirectAccess(preservationEvent);

    final ConsumesOutputStream stream = new BinaryConsumesOutputStream(binary, resourceAccess.getPath(),
      RodaConstants.MEDIA_TYPE_APPLICATION_XML);
    return new StreamResponse(stream);
  }

  public StreamResponse retrievePreservationEventDetails(IndexedPreservationEvent preservationEvent,
    RequestContext context, String language)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Binary binary = getPreservationEventBinary(preservationEvent, context);
    final String filename = binary.getStoragePath().getName() + HTML_EXT;
    final String htmlEvent = HTMLUtils.preservationMetadataEventToHtml(binary, true, ServerTools.parseLocale(language));

    final ConsumesOutputStream stream = new DefaultConsumesOutputStream(filename, RodaConstants.MEDIA_TYPE_TEXT_HTML,
      out -> {
        PrintStream printStream = new PrintStream(out);
        printStream.print(htmlEvent);
        printStream.close();
      });

    return new StreamResponse(stream);
  }
}
