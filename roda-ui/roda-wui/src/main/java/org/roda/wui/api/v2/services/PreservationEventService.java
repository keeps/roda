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
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationEventsLinkingObjects;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.util.IdUtils;
import org.roda.wui.common.HTMLUtils;
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
    User user) throws ValidationException, GenericException {
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
        String uuid = LinkingObjectUtils.getAipIdFromLinkingId(idValue);
        List<String> aipFields = Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.INDEX_UUID);
        IndexedAIP aip = indexService.retrieve(user, IndexedAIP.class, uuid, aipFields);
        aips.put(idValue, aip);
      } else if (RodaConstants.RODA_TYPE.REPRESENTATION.equals(linkingType)) {
        String uuid = LinkingObjectUtils.getRepresentationIdFromLinkingId(idValue);
        List<String> representationFields = Arrays.asList(RodaConstants.REPRESENTATION_ID,
          RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ORIGINAL);
        IndexedRepresentation rep = indexService.retrieve(user, IndexedRepresentation.class, uuid,
          representationFields);
        representations.put(idValue, rep);
      } else if (RodaConstants.RODA_TYPE.FILE.equals(linkingType)) {
        List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
        fileFields.addAll(RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
        fileFields.addAll(Arrays.asList(RodaConstants.FILE_ORIGINALNAME, RodaConstants.FILE_SIZE,
          RodaConstants.FILE_FILEFORMAT, RodaConstants.FILE_FORMAT_VERSION, RodaConstants.FILE_FORMAT_DESIGNATION));
        IndexedFile file = indexService.retrieve(user, IndexedFile.class,
          LinkingObjectUtils.getFileIdFromLinkingId(idValue), fileFields);
        files.put(idValue, file);
      } else if (RodaConstants.RODA_TYPE.TRANSFERRED_RESOURCE.equals(linkingType)) {
        String id = LinkingObjectUtils.getTransferredResourceIdFromLinkingId(idValue);
        if (id != null) {
          List<String> resourceFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.TRANSFERRED_RESOURCE_NAME,
            RodaConstants.TRANSFERRED_RESOURCE_FULLPATH);
          TransferredResource tr = indexService.retrieve(user, TransferredResource.class, IdUtils.createUUID(id),
            resourceFields);
          transferredResources.put(idValue, tr);
        }
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

  public List<IndexedPreservationAgent> getAgentsFromPreservationEventBinary(Binary preservationEventBinary, User user)
    throws ValidationException, GenericException {
    List<IndexedPreservationAgent> agents = new ArrayList<>();
    List<String> agentFields = Arrays.asList(RodaConstants.PRESERVATION_AGENT_ID, RodaConstants.PRESERVATION_AGENT_NAME,
      RodaConstants.PRESERVATION_AGENT_TYPE, RodaConstants.PRESERVATION_AGENT_ROLES,
      RodaConstants.PRESERVATION_AGENT_VERSION, RodaConstants.PRESERVATION_AGENT_NOTE,
      RodaConstants.PRESERVATION_AGENT_EXTENSION);

    EventComplexType eventComplexType = PremisV3Utils.binaryToEvent(preservationEventBinary.getContent(), false);
    for (LinkingAgentIdentifierComplexType linkingAgentIdentifierComplexType : eventComplexType
      .getLinkingAgentIdentifier()) {

      IndexedPreservationAgent agent = indexService.retrieve(user, IndexedPreservationAgent.class,
        linkingAgentIdentifierComplexType.getLinkingAgentIdentifierValue(), agentFields);
      agents.add(agent);

    }

    return agents;
  }

  public Binary getPreservationEventBinary(IndexedPreservationEvent preservationEvent, User user)
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
        IndexedRepresentation representation = indexService.retrieve(user, IndexedRepresentation.class,
          preservationEvent.getRepresentationUUID(), new ArrayList<>());
        representationId = representation.getId();
      }

      if (preservationEvent.getFileUUID() != null) {
        IndexedFile file = indexService.retrieve(user, IndexedFile.class, preservationEvent.getFileUUID(),
          new ArrayList<>());
        fileId = file.getId();
        directoryFilePath = file.getPath();
      }
      binary = model.retrievePreservationEvent(preservationEvent.getAipID(), representationId, directoryFilePath,
        fileId, preservationEvent.getId());
    }

    return binary;
  }

  public StreamResponse retrievePreservationEventFile(IndexedPreservationEvent preservationEvent, User user)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    Binary binary = getPreservationEventBinary(preservationEvent, user);
    final ConsumesOutputStream stream = new BinaryConsumesOutputStream(binary,
      RodaConstants.MEDIA_TYPE_APPLICATION_XML);
    return new StreamResponse(stream);
  }

  public StreamResponse retrievePreservationEventDetails(IndexedPreservationEvent preservationEvent, User user,
    String language)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Binary binary = getPreservationEventBinary(preservationEvent, user);
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
