/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.characterization.model.TechnicalMetadata;
import org.roda.core.common.characterization.model.TechnicalMetadataElement;
import org.roda.core.common.characterization.model.TechnicalMetadataField;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AlreadyHasInstanceIdentifier;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InstanceIdNotUpdated;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.utils.XMLUtils;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.base.characterization.PremisSkeletonPluginUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.FileUtility;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import gov.loc.premis.v3.AgentComplexType;
import gov.loc.premis.v3.AgentIdentifierComplexType;
import gov.loc.premis.v3.ContentLocationComplexType;
import gov.loc.premis.v3.CreatingApplicationComplexType;
import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.EventDetailInformationComplexType;
import gov.loc.premis.v3.EventIdentifierComplexType;
import gov.loc.premis.v3.EventOutcomeDetailComplexType;
import gov.loc.premis.v3.EventOutcomeInformationComplexType;
import gov.loc.premis.v3.ExtensionComplexType;
import gov.loc.premis.v3.FixityComplexType;
import gov.loc.premis.v3.FormatComplexType;
import gov.loc.premis.v3.FormatDesignationComplexType;
import gov.loc.premis.v3.FormatRegistryComplexType;
import gov.loc.premis.v3.LinkingAgentIdentifierComplexType;
import gov.loc.premis.v3.LinkingObjectIdentifierComplexType;
import gov.loc.premis.v3.ObjectCharacteristicsComplexType;
import gov.loc.premis.v3.ObjectFactory;
import gov.loc.premis.v3.ObjectIdentifierComplexType;
import gov.loc.premis.v3.OriginalNameComplexType;
import gov.loc.premis.v3.PreservationLevelComplexType;
import gov.loc.premis.v3.RelatedObjectIdentifierComplexType;
import gov.loc.premis.v3.RelationshipComplexType;
import gov.loc.premis.v3.Representation;
import gov.loc.premis.v3.StorageComplexType;
import gov.loc.premis.v3.StringPlusAuthority;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.util.ValidationEventCollector;

public final class PremisV3Utils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PremisV3Utils.class);
  private static final String FIXITY_ORIGINATOR = "RODA";
  private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

  private static final ObjectFactory FACTORY = new ObjectFactory();

  /** Private empty constructor */
  private PremisV3Utils() {
    // do nothing
  }

  public static List<Fixity> calculateFixities(Binary binary, Collection<String> algorithms, String originator)
      throws IOException, NoSuchAlgorithmException {
    List<Fixity> ret = new ArrayList<>();
    try (InputStream stream = binary.getContent().createInputStream()) {
      Map<String, String> checksums = FileUtility.checksums(stream, algorithms);

      for (Entry<String, String> entry : checksums.entrySet()) {
        String algorithm = entry.getKey();
        String checksum = entry.getValue();
        ret.add(new Fixity(algorithm, checksum, originator));
      }
    }

    return ret;
  }

  public static boolean isPremisV2(Binary binary) throws IOException, SAXException {
    boolean premisV2 = true;
    try (InputStream inputStream = binary.getContent().createInputStream();
         InputStream schemaStream = RodaCoreFactory.getConfigurationFileAsStream("schemas/premis-v2-0.xsd")) {
      Source xmlFile = new StreamSource(inputStream);
      SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
      Validator validator = schema.newValidator();
      RodaErrorHandler errorHandler = new RodaErrorHandler();
      validator.setErrorHandler(errorHandler);
      try {
        validator.validate(xmlFile);
        List<SAXParseException> errors = errorHandler.getErrors();
        if (!errors.isEmpty()) {
          premisV2 = false;
        }
      } catch (SAXException e) {
        premisV2 = false;
      }
    }

    return premisV2;
  }

  public static void updateFileFormat(gov.loc.premis.v3.File file, String formatDesignationName,
                                      String formatDesignationVersion, String pronom, String mimeType) {

    if (StringUtils.isNotBlank(formatDesignationName)) {
      FormatDesignationComplexType fdct = getFormatDesignation(file);
      fdct.setFormatName(getStringPlusAuthority(formatDesignationName));
    }

    if (StringUtils.isNotBlank(formatDesignationVersion)) {
      FormatDesignationComplexType fdct = getFormatDesignation(file);
      fdct.setFormatVersion(formatDesignationVersion);
    }

    if (StringUtils.isNotBlank(pronom)) {
      FormatRegistryComplexType frct = getFormatRegistry(file, RodaConstants.PRESERVATION_REGISTRY_PRONOM);
      frct.setFormatRegistryKey(getStringPlusAuthority(pronom));
    }
    if (StringUtils.isNotBlank(mimeType)) {
      FormatRegistryComplexType frct = getFormatRegistry(file, RodaConstants.PRESERVATION_REGISTRY_MIME);
      frct.setFormatRegistryKey(getStringPlusAuthority(mimeType));
    }

  }

  public static void updateTechnicalMetadata(gov.loc.premis.v3.File file, TechnicalMetadata technicalMetadata)
      throws JAXBException {

    String type = technicalMetadata.getType();
    ExtensionComplexType ect = null;
    if (StringUtils.isNotBlank(type)) {
      ect = getTechnicalMetadata(file, type);
    }

    // Create element based on the class technicalMetadata
    JAXBElement<TechnicalMetadata> dataElement = new JAXBElement<>(new QName("http://www.loc.gov/premis/v3", type),
        TechnicalMetadata.class, null, technicalMetadata);

    // Add to objectCharacteristicsExtension
    ect.getAny().add(dataElement);
  }

  public static void updateCreatingApplication(gov.loc.premis.v3.File file, String creatingApplicationName,
                                               String creatingApplicationVersion, String dateCreatedByApplication) {
    if (StringUtils.isNotBlank(creatingApplicationName)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.getCreatingApplicationName().add(getStringPlusAuthority(creatingApplicationName));
    }

    if (StringUtils.isNotBlank(creatingApplicationVersion)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.getCreatingApplicationVersion().add(creatingApplicationVersion);
    }

    if (StringUtils.isNotBlank(dateCreatedByApplication)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.getDateCreatedByApplication().add(dateCreatedByApplication);
    }
  }

  private static CreatingApplicationComplexType getCreatingApplication(gov.loc.premis.v3.File f) {
    ObjectCharacteristicsComplexType occt;
    CreatingApplicationComplexType cact;
    if (f.getObjectCharacteristics() == null || f.getObjectCharacteristics().isEmpty()) {
      f.getObjectCharacteristics().add(FACTORY.createObjectCharacteristicsComplexType());
    }
    occt = f.getObjectCharacteristics().get(0);

    if (occt.getCreatingApplication() == null || occt.getCreatingApplication().isEmpty()) {
      occt.getCreatingApplication().add(FACTORY.createCreatingApplicationComplexType());
    }
    cact = occt.getCreatingApplication().get(0);

    return cact;
  }

  private static ExtensionComplexType getTechnicalMetadata(gov.loc.premis.v3.File f, String type) {

    ObjectCharacteristicsComplexType occt;

    if (f.getObjectCharacteristics() == null || f.getObjectCharacteristics().isEmpty()) {
      occt = FACTORY.createObjectCharacteristicsComplexType();
      ExtensionComplexType extensionComplexType = FACTORY.createExtensionComplexType();
      occt.getObjectCharacteristicsExtension().add(extensionComplexType);
      f.getObjectCharacteristics().add(occt);
      return extensionComplexType;
    }

    for (ObjectCharacteristicsComplexType complexType : f.getObjectCharacteristics()) {
      for (ExtensionComplexType ect : complexType.getObjectCharacteristicsExtension()) {
        if (ect.getAny().get(0).toString().contains(type)) {
          ect.getAny().remove(0);
          return ect;
        }
      }
    }
    occt = FACTORY.createObjectCharacteristicsComplexType();
    ExtensionComplexType extensionComplexType = FACTORY.createExtensionComplexType();
    occt.getObjectCharacteristicsExtension().add(extensionComplexType);
    f.getObjectCharacteristics().add(occt);
    return extensionComplexType;
  }

  public static FormatRegistryComplexType getFormatRegistry(gov.loc.premis.v3.File file, String registryName) {
    ObjectCharacteristicsComplexType objectCharacteristics;
    FormatRegistryComplexType formatRegistry = null;
    if (file.getObjectIdentifier() != null && !file.getObjectIdentifier().isEmpty()) {
      objectCharacteristics = file.getObjectCharacteristics().get(0);
    } else {
      objectCharacteristics = FACTORY.createObjectCharacteristicsComplexType();
      file.getObjectCharacteristics().add(objectCharacteristics);
    }

    if (objectCharacteristics.getFormat() != null && !objectCharacteristics.getFormat().isEmpty()) {
      for (FormatComplexType format : objectCharacteristics.getFormat()) {
        if (format.getFormatRegistry() != null && !format.getFormatRegistry().isEmpty()) {
          if (format.getFormatRegistry().get(0).getFormatRegistryName().getValue().equalsIgnoreCase(registryName)) {
            formatRegistry = format.getFormatRegistry().get(0);
            break;
          }
        }
      }

      if (formatRegistry == null) {
        FormatComplexType formatComplexType = FACTORY.createFormatComplexType();
        formatRegistry = FACTORY.createFormatRegistryComplexType();
        formatRegistry.setFormatRegistryName(getStringPlusAuthority(registryName));
        formatComplexType.getFormatRegistry().add(formatRegistry);
        objectCharacteristics.getFormat().add(formatComplexType);
      }
    } else {
      FormatComplexType formatComplexType = FACTORY.createFormatComplexType();
      formatRegistry = FACTORY.createFormatRegistryComplexType();
      formatRegistry.setFormatRegistryName(getStringPlusAuthority(registryName));
      formatComplexType.getFormatRegistry().add(formatRegistry);
      objectCharacteristics.getFormat().add(formatComplexType);
    }
    return formatRegistry;
  }

  private static FormatDesignationComplexType getFormatDesignation(gov.loc.premis.v3.File file) {
    ObjectCharacteristicsComplexType objectCharacteristics;
    FormatComplexType format;
    FormatDesignationComplexType formatDesignation;
    if (file.getObjectCharacteristics() != null && !file.getObjectCharacteristics().isEmpty()) {
      objectCharacteristics = file.getObjectCharacteristics().get(0);
    } else {
      objectCharacteristics = FACTORY.createObjectCharacteristicsComplexType();
      file.getObjectCharacteristics().add(objectCharacteristics);
    }

    if (objectCharacteristics.getFormat() != null && !objectCharacteristics.getFormat().isEmpty()) {
      format = objectCharacteristics.getFormat().get(0);
    } else {
      format = FACTORY.createFormatComplexType();
    }
    if (format.getFormatDesignation() != null && !format.getFormatDesignation().isEmpty()) {
      formatDesignation = format.getFormatDesignation().get(0);
    } else {
      formatDesignation = FACTORY.createFormatDesignationComplexType();
    }
    return formatDesignation;
  }

  public static ContentPayload createPremisEventBinary(String eventID, Date date, String type, String details,
                                                       List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, String outcome, String detailNote,
                                                       String detailExtension, List<LinkingIdentifier> agentIds) {
    EventComplexType ect = FACTORY.createEventComplexType();
    ect.setEventDateTime(DateTime.parse(date.toInstant().toString()).toString());
    ect.setEventType(getStringPlusAuthority(type));

    EventIdentifierComplexType eict = FACTORY.createEventIdentifierComplexType();
    eict.setEventIdentifierValue(eventID);
    eict.setEventIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    ect.setEventIdentifier(eict);

    EventDetailInformationComplexType edict = FACTORY.createEventDetailInformationComplexType();
    edict.setEventDetail(details);
    ect.getEventDetailInformation().add(edict);

    if (sources != null) {
      for (LinkingIdentifier identifier : sources) {
        LinkingObjectIdentifierComplexType loict = FACTORY.createLinkingObjectIdentifierComplexType();
        loict.setLinkingObjectIdentifierValue(identifier.getValue());
        loict.setLinkingObjectIdentifierType(getStringPlusAuthority(identifier.getType()));
        if (identifier.getRoles() != null) {
          loict.getLinkingObjectRole().addAll(getStringPlusAuthorityArray(identifier.getRoles()));
        }
        ect.getLinkingObjectIdentifier().add(loict);
      }
    }

    if (outcomes != null) {
      for (LinkingIdentifier identifier : outcomes) {
        LinkingObjectIdentifierComplexType loict = FACTORY.createLinkingObjectIdentifierComplexType();
        loict.setLinkingObjectIdentifierValue(identifier.getValue());
        loict.setLinkingObjectIdentifierType(getStringPlusAuthority(identifier.getType()));
        if (identifier.getRoles() != null) {
          loict.getLinkingObjectRole().addAll(getStringPlusAuthorityArray(identifier.getRoles()));
        }
        ect.getLinkingObjectIdentifier().add(loict);
      }
    }

    if (agentIds != null) {
      for (LinkingIdentifier identifier : agentIds) {
        LinkingAgentIdentifierComplexType laict = FACTORY.createLinkingAgentIdentifierComplexType();
        laict.setLinkingAgentIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
        laict.setLinkingAgentIdentifierValue(identifier.getValue());
        if (identifier.getRoles() != null) {
          laict.getLinkingAgentRole().addAll(getStringPlusAuthorityArray(identifier.getRoles()));
        }
        ect.getLinkingAgentIdentifier().add(laict);
      }
    }

    EventOutcomeInformationComplexType eoict = FACTORY.createEventOutcomeInformationComplexType();
    eoict.getEventOutcome().add(getStringPlusAuthority(outcome));
    StringBuilder outcomeDetailNote = new StringBuilder(detailNote);
    if (StringUtils.isNotBlank(detailExtension)) {
      outcomeDetailNote.append("\n").append(detailExtension);
    }

    EventOutcomeDetailComplexType eodct = FACTORY.createEventOutcomeDetailComplexType();
    eodct.getEventOutcomeDetailNote().add(outcomeDetailNote.toString());
    eoict.getEventOutcomeDetail().add(eodct);
    ect.getEventOutcomeInformation().add(eoict);

    return MetadataUtils.saveToContentPayload(FACTORY.createEvent(ect), EventComplexType.class);
  }

  public static ContentPayload retrievePremisEventBinary(String eventID, Date date, String type, String details,
                                                         List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, String outcome, String detailNote,
                                                         String detailExtension, List<LinkingIdentifier> agentIds) throws GenericException, ValidationException {

    EventComplexType eventComplexType = FACTORY.createEventComplexType();
    EventIdentifierComplexType eventIdentifier = FACTORY.createEventIdentifierComplexType();
    eventIdentifier.setEventIdentifierValue(eventID);
    eventIdentifier.setEventIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    eventComplexType.setEventDateTime(DateTime.parse(date.toInstant().toString()).toString());
    eventComplexType.setEventType(getStringPlusAuthority(type));

    EventDetailInformationComplexType eventDetailInformation = FACTORY.createEventDetailInformationComplexType();
    eventDetailInformation.setEventDetail(details);
    if (sources != null) {
      for (LinkingIdentifier identifier : sources) {
        LinkingObjectIdentifierComplexType linkingObjectIdentifier = FACTORY.createLinkingObjectIdentifierComplexType();
        linkingObjectIdentifier.setLinkingObjectIdentifierValue(identifier.getValue());
        linkingObjectIdentifier.setLinkingObjectIdentifierType(getStringPlusAuthority(identifier.getType()));
        if (identifier.getRoles() != null) {
          linkingObjectIdentifier.getLinkingObjectRole().addAll(getStringPlusAuthorityArray(identifier.getRoles()));
        }
        eventComplexType.getLinkingObjectIdentifier().add(linkingObjectIdentifier);
      }

    }

    if (outcomes != null) {
      for (LinkingIdentifier identifier : outcomes) {
        LinkingObjectIdentifierComplexType linkingObjectIdentifier = FACTORY.createLinkingObjectIdentifierComplexType();
        linkingObjectIdentifier.setLinkingObjectIdentifierValue(identifier.getValue());
        linkingObjectIdentifier.setLinkingObjectIdentifierType(getStringPlusAuthority(identifier.getType()));
        if (identifier.getRoles() != null) {
          linkingObjectIdentifier.getLinkingObjectRole().addAll(getStringPlusAuthorityArray(identifier.getRoles()));
        }
        eventComplexType.getLinkingObjectIdentifier().add(linkingObjectIdentifier);
      }
    }

    if (agentIds != null) {
      for (LinkingIdentifier agentId : agentIds) {
        LinkingAgentIdentifierComplexType linkingAgentIdentifier = FACTORY.createLinkingAgentIdentifierComplexType();
        linkingAgentIdentifier
            .setLinkingAgentIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
        linkingAgentIdentifier.setLinkingAgentIdentifierValue(agentId.getValue());
        if (agentId.getRoles() != null) {
          linkingAgentIdentifier.getLinkingAgentRole().addAll(getStringPlusAuthorityArray(agentId.getRoles()));
        }
        eventComplexType.getLinkingAgentIdentifier().add(linkingAgentIdentifier);
      }
    }

    eventComplexType.getEventDetailInformation().add(eventDetailInformation);

    EventOutcomeInformationComplexType eventOutcomeInformation = FACTORY.createEventOutcomeInformationComplexType();
    eventOutcomeInformation.getEventOutcome().add(getStringPlusAuthority(outcome));
    StringBuilder outcomeDetailNote = new StringBuilder(detailNote);
    if (StringUtils.isNotBlank(detailExtension)) {
      outcomeDetailNote.append("\n").append(detailExtension);
    }
    EventOutcomeDetailComplexType eventOutcomeDetail = FACTORY.createEventOutcomeDetailComplexType();
    eventOutcomeDetail.getEventOutcomeDetailNote().add(outcomeDetailNote.toString());
    eventOutcomeInformation.getEventOutcomeDetail().add(eventOutcomeDetail);
    eventComplexType.getEventOutcomeInformation().add(eventOutcomeInformation);

    return MetadataUtils.saveToContentPayload(FACTORY.createEvent(eventComplexType), eventComplexType.getClass());
  }

  public static ContentPayload createPremisAgentBinary(String id, String name, PreservationAgentType type,
                                                       String extension, String note, String version) {
    AgentComplexType agent = FACTORY.createAgentComplexType();
    AgentIdentifierComplexType agentIdentifier = FACTORY.createAgentIdentifierComplexType();
    agentIdentifier.setAgentIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    agentIdentifier.setAgentIdentifierValue(id);
    agent.getAgentIdentifier().add(agentIdentifier);
    agent.setAgentType(getStringPlusAuthority(type.toString()));

    if (StringUtils.isNotBlank(name)) {
      agent.getAgentName().add(getStringPlusAuthority(name));
    }

    if (StringUtils.isNotBlank(note)) {
      agent.getAgentNote().add(note);
    }

    if (StringUtils.isNotBlank(extension)) {
      ExtensionComplexType extensionComplexType = FACTORY.createExtensionComplexType();
      extensionComplexType.getAny().add(extension);
      agent.getAgentExtension().add(extensionComplexType);
    }

    return MetadataUtils.saveToContentPayload(FACTORY.createAgent(agent), AgentComplexType.class);
  }

  public static Representation createBaseRepresentation(String aipId, String representationId) {
    Representation representation = FACTORY.createRepresentation();

    ObjectIdentifierComplexType objectIdentifier = FACTORY.createObjectIdentifierComplexType();
    objectIdentifier.setObjectIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    objectIdentifier.setObjectIdentifierValue(
        IdUtils.getRepresentationPreservationId(aipId, representationId, RODAInstanceUtils.getLocalInstanceIdentifier()));
    representation.getObjectIdentifier().add(objectIdentifier);
    PreservationLevelComplexType preservationLevelComplexType = FACTORY.createPreservationLevelComplexType();
    preservationLevelComplexType.setPreservationLevelValue(getStringPlusAuthority(""));
    representation.getPreservationLevel().add(preservationLevelComplexType);

    return representation;
  }

  public static ContentPayload createBaseFile(File originalFile, ModelService model,
                                              Collection<String> fixityAlgorithms)
      throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {

    gov.loc.premis.v3.File file = FACTORY.createFile();
    PreservationLevelComplexType preservationLevel = FACTORY.createPreservationLevelComplexType();
    preservationLevel.setPreservationLevelValue(getStringPlusAuthority(RodaConstants.PRESERVATION_LEVEL_FULL));
    file.getPreservationLevel().add(preservationLevel);

    // URN-local identifier
    ObjectIdentifierComplexType objectIdentifier = FACTORY.createObjectIdentifierComplexType();
    objectIdentifier.setObjectIdentifierValue(URNUtils.createRodaPreservationURN(PreservationMetadataType.FILE,
        originalFile.getId(), RODAInstanceUtils.getLocalInstanceIdentifier()));
    objectIdentifier.setObjectIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN_LOCAL));
    file.getObjectIdentifier().add(objectIdentifier);

    // URN identifier (UUID)
    ObjectIdentifierComplexType objectIdentifier2 = FACTORY.createObjectIdentifierComplexType();
    objectIdentifier2.setObjectIdentifierValue(URNUtils.createRodaPreservationURN(PreservationMetadataType.FILE,
        IdUtils.getFileId(originalFile), RODAInstanceUtils.getLocalInstanceIdentifier()));
    objectIdentifier2.setObjectIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    file.getObjectIdentifier().add(objectIdentifier2);

    ObjectCharacteristicsComplexType objectCharacteristics = FACTORY.createObjectCharacteristicsComplexType();
    FormatComplexType formatComplexType = FACTORY.createFormatComplexType();
    FormatDesignationComplexType formatDesignationComplexType = FACTORY.createFormatDesignationComplexType();
    formatDesignationComplexType.setFormatName(getStringPlusAuthority(""));
    formatDesignationComplexType.setFormatVersion("");
    formatComplexType.getFormatDesignation().add(formatDesignationComplexType);
    objectCharacteristics.getFormat().add(formatComplexType);
    file.getObjectCharacteristics().add(objectCharacteristics);

    Binary binary = model.getStorage().getBinary(ModelUtils.getFileStoragePath(originalFile));
    if (binary != null && binary.getContentDigest() != null && !binary.getContentDigest().isEmpty()) {
      // use binary content digest information
      for (Entry<String, String> entry : binary.getContentDigest().entrySet()) {
        FixityComplexType fixity = FACTORY.createFixityComplexType();
        fixity.setMessageDigest(entry.getKey());
        fixity.setMessageDigestAlgorithm(getStringPlusAuthority(entry.getValue()));
        fixity.setMessageDigestOriginator(getStringPlusAuthority(FIXITY_ORIGINATOR));
        objectCharacteristics.getFixity().add(fixity);
      }
    } else {
      // if binary does not contain digest, create a new one
      try {
        List<Fixity> fixities = calculateFixities(binary, fixityAlgorithms, FIXITY_ORIGINATOR);
        for (Fixity fixity : fixities) {
          FixityComplexType premisFixity = FACTORY.createFixityComplexType();
          premisFixity.setMessageDigest(fixity.getMessageDigest());
          premisFixity.setMessageDigestAlgorithm(getStringPlusAuthority(fixity.getMessageDigestAlgorithm()));
          premisFixity.setMessageDigestOriginator(getStringPlusAuthority(fixity.getMessageDigestOriginator()));
          objectCharacteristics.getFixity().add(premisFixity);
        }
      } catch (IOException | NoSuchAlgorithmException e) {
        LOGGER.warn("Could not calculate fixity for file " + originalFile);
      }
    }

    objectCharacteristics.setSize(binary.getSizeInBytes());
    file.getObjectCharacteristics().add(objectCharacteristics);

    OriginalNameComplexType originalName = FACTORY.createOriginalNameComplexType();
    originalName.setValue(originalFile.getId());
    file.setOriginalName(originalName);

    StorageComplexType storage = FACTORY.createStorageComplexType();
    String contentLocation;
    try {
      contentLocation = String
          .valueOf(model.getStorage().getBinary(ModelUtils.getFileStoragePath(originalFile)).getContent().getURI());
    } catch (IOException e) {
      LOGGER.debug(String.format("Can't create URI, %s: %s", e.getCause(), e.getMessage()));
      contentLocation = ModelUtils.getFileStoragePath(originalFile).asString("/", null, null, false);
    }

    ContentLocationComplexType contentLocationComplexType = FACTORY.createContentLocationComplexType();
    contentLocationComplexType.setContentLocationType(getStringPlusAuthority(RodaConstants.URI_TYPE));
    contentLocationComplexType.setContentLocationValue(contentLocation);
    storage.getContentLocation().add(contentLocationComplexType);
    file.getStorage().add(storage);

    return MetadataUtils.saveToContentPayload(FACTORY.createObject(file), gov.loc.premis.v3.File.class);
  }

  public static List<Fixity> extractFixities(Binary premisFile) throws GenericException, IOException {
    List<Fixity> fixities = new ArrayList<>();
    try (InputStream inputStream = premisFile.getContent().createInputStream()) {
      gov.loc.premis.v3.File file = binaryToFile(inputStream);
      List<ObjectCharacteristicsComplexType> objectCharacteristics = file.getObjectCharacteristics();
      if (objectCharacteristics != null && !objectCharacteristics.isEmpty()) {
        List<FixityComplexType> fixityComplexTypes = objectCharacteristics.get(0).getFixity();
        if (fixityComplexTypes != null && !fixityComplexTypes.isEmpty()) {
          for (FixityComplexType fixity : fixityComplexTypes) {
            Fixity fixityRODA = new Fixity();
            fixityRODA.setMessageDigest(fixity.getMessageDigest());
            fixityRODA.setMessageDigestAlgorithm(fixity.getMessageDigestAlgorithm().getValue());
            fixityRODA.setMessageDigestOriginator(fixity.getMessageDigestOriginator().getValue());
            fixities.add(fixityRODA);
          }
        }
      }
    }

    return fixities;
  }

  public static String extractFixity(Binary premisFile, String fixityType) throws IOException, GenericException {
    try (InputStream inputStream = premisFile.getContent().createInputStream()) {
      gov.loc.premis.v3.File file = binaryToFile(inputStream);
      List<ObjectCharacteristicsComplexType> objectCharacteristics = file.getObjectCharacteristics();
      if (objectCharacteristics != null && !objectCharacteristics.isEmpty()) {
        List<FixityComplexType> fixityComplexTypes = objectCharacteristics.get(0).getFixity();
        if (fixityComplexTypes != null && !fixityComplexTypes.isEmpty()) {
          for (FixityComplexType fixity : fixityComplexTypes) {
            if (fixity.getMessageDigestAlgorithm().getValue().equalsIgnoreCase(fixityType)) {
              return fixity.getMessageDigest();
            }
          }
        }
      }
    }

    return null;
  }

  public static Representation binaryToRepresentation(InputStream binaryInputStream) throws GenericException {
    JAXBContext jaxbContext;
    try {
      jaxbContext = JAXBContext.newInstance(gov.loc.premis.v3.Representation.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Object unmarshal = jaxbUnmarshaller.unmarshal(binaryInputStream);
      return ((gov.loc.premis.v3.Representation) ((JAXBElement<?>) unmarshal).getValue());
    } catch (JAXBException e) {
      throw new GenericException("Failed to load representation: " + e.getMessage(), e);
    }
  }

  public static AgentComplexType binaryToAgent(InputStream binaryInputStream) throws IOException, GenericException {
    JAXBContext jaxbContext;
    try {
      jaxbContext = JAXBContext.newInstance(gov.loc.premis.v3.AgentComplexType.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Object unmarshal = jaxbUnmarshaller.unmarshal(binaryInputStream);
      return ((gov.loc.premis.v3.AgentComplexType) ((JAXBElement<?>) unmarshal).getValue());
    } catch (JAXBException e) {
      throw new GenericException("Failed to load Agent: " + e.getMessage(), e);
    }
  }

  public static SolrInputDocument getSolrDocument(Binary premisBinary) throws GenericException {
    SolrInputDocument doc = new SolrInputDocument();

    try (InputStream inputStream = premisBinary.getContent().createInputStream()) {
      gov.loc.premis.v3.File premisFile = binaryToFile(inputStream);
      if (premisFile.getOriginalName() != null) {
        doc.setField(RodaConstants.FILE_ORIGINALNAME, premisFile.getOriginalName().getValue());
        // TODO extension
      }

      if (premisFile.getObjectCharacteristics() != null && !premisFile.getObjectCharacteristics().isEmpty()) {
        ObjectCharacteristicsComplexType objectCharacteristics = premisFile.getObjectCharacteristics().get(0);
        doc.setField(RodaConstants.FILE_SIZE, objectCharacteristics.getSize());
        if (objectCharacteristics.getFixity() != null && !objectCharacteristics.getFixity().isEmpty()) {
          List<String> hashes = new ArrayList<>();
          for (FixityComplexType fct : objectCharacteristics.getFixity()) {
            StringBuilder fixityPrint = new StringBuilder();
            fixityPrint.append(fct.getMessageDigest());
            fixityPrint.append(" (");
            fixityPrint.append(fct.getMessageDigestAlgorithm().getValue());
            if (StringUtils.isNotBlank(fct.getMessageDigestOriginator().getValue())) {
              fixityPrint.append(", "); //
              fixityPrint.append(fct.getMessageDigestOriginator().getValue());
            }
            fixityPrint.append(")");
            hashes.add(fixityPrint.toString());
          }
          doc.addField(RodaConstants.FILE_HASH, hashes);
        }
        if (objectCharacteristics.getFormat() != null && !objectCharacteristics.getFormat().isEmpty()) {
          FormatComplexType fct = objectCharacteristics.getFormat().get(0);
          if (fct.getFormatDesignation() != null && !fct.getFormatDesignation().isEmpty()) {
            String format = fct.getFormatDesignation().get(0).getFormatName().getValue();
            String formatVersion = fct.getFormatDesignation().get(0).getFormatVersion();
            String formatDesignation = "";

            if (StringUtils.isNotBlank(format)) {
              doc.addField(RodaConstants.FILE_FILEFORMAT, format);
              formatDesignation += format;
            }
            if (StringUtils.isNotBlank(formatVersion)) {
              doc.addField(RodaConstants.FILE_FORMAT_VERSION, formatVersion);
              formatDesignation += " " + formatVersion;
            }
            if (StringUtils.isNotBlank(formatDesignation)) {
              doc.addField(RodaConstants.FILE_FORMAT_DESIGNATION, formatDesignation);
            }
          }

          FormatRegistryComplexType pronomRegistry = getFormatRegistry(premisFile,
              RodaConstants.PRESERVATION_REGISTRY_PRONOM);
          if (pronomRegistry.getFormatRegistryKey() != null) {
            doc.addField(RodaConstants.FILE_PRONOM, pronomRegistry.getFormatRegistryKey().getValue());
          }
          FormatRegistryComplexType mimeRegistry = getFormatRegistry(premisFile,
              RodaConstants.PRESERVATION_REGISTRY_MIME);
          if (mimeRegistry.getFormatRegistryKey() != null) {
            doc.addField(RodaConstants.FILE_FORMAT_MIMETYPE, mimeRegistry.getFormatRegistryKey().getValue());
          }
          // TODO extension
        }
        if (objectCharacteristics.getCreatingApplication() != null
            && !objectCharacteristics.getCreatingApplication().isEmpty()) {
          CreatingApplicationComplexType cact = objectCharacteristics.getCreatingApplication().get(0);
          if (cact.getCreatingApplicationName() != null && !cact.getCreatingApplicationName().isEmpty()) {
            doc.addField(RodaConstants.FILE_CREATING_APPLICATION_NAME,
                cact.getCreatingApplicationName().get(0).getValue());
          }
          doc.addField(RodaConstants.FILE_CREATING_APPLICATION_VERSION, cact.getCreatingApplicationVersion());
          doc.addField(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION, cact.getDateCreatedByApplication());
        }

        SolrInputDocument docTechnicalMetadata = SolrUtils.getTechnicalMetadataFields(premisBinary, "premis", "3");

        for (String key : docTechnicalMetadata.getFieldNames()) {
          doc.addField(key, docTechnicalMetadata.getFieldValue(key));
        }
      }

    } catch (IOException e) {
      LOGGER.error("Error updating Solr document", e);
    }

    return doc;
  }

  public static PreservationMetadata createPremisAgentBinary(Plugin<?> plugin, ModelService model, boolean notify)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
    String id = IdUtils.getPluginAgentId(plugin.getClass().getName(), plugin.getVersion(),
        RODAInstanceUtils.getLocalInstanceIdentifier());
    ContentPayload agentPayload = PremisV3Utils.createPremisAgentBinary(id, plugin.getName(), plugin.getAgentType(), "",
        plugin.getDescription(), plugin.getVersion());
    return model.createPreservationMetadata(PreservationMetadataType.AGENT, id, agentPayload, notify);
  }

  public static void linkFileToRepresentation(String fileId, List<String> filePath, String relationshipType,
                                              String relationshipSubType, Representation representation) {
    RelationshipComplexType relationship = FACTORY.createRelationshipComplexType();
    relationship.setRelationshipType(getStringPlusAuthority(relationshipType));
    relationship.setRelationshipSubType(getStringPlusAuthority(relationshipSubType));
    RelatedObjectIdentifierComplexType roict = FACTORY.createRelatedObjectIdentifierComplexType();
    roict.setRelatedObjectIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    roict.setRelatedObjectIdentifierValue(IdUtils.getPreservationFileId(filePath, fileId));
    relationship.getRelatedObjectIdentifier().add(roict);

    representation.getRelationship().add(relationship);
  }

  public static List<LinkingIdentifier> extractAgentsFromEvent(Binary binary)
      throws ValidationException, GenericException {
    List<LinkingIdentifier> identifiers = new ArrayList<>();
    EventComplexType event = PremisV3Utils.binaryToEvent(binary.getContent(), true);
    if (event.getLinkingObjectIdentifier() != null && !event.getLinkingObjectIdentifier().isEmpty()) {
      for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifier()) {
        LinkingIdentifier li = new LinkingIdentifier();
        li.setType(laict.getLinkingAgentIdentifierType().getValue());
        li.setValue(laict.getLinkingAgentIdentifierValue());
        li.setRoles(toStringList(laict.getLinkingAgentRole()));
        identifiers.add(li);
      }
    }
    if (event.getLinkingAgentIdentifier() != null && !event.getLinkingAgentIdentifier().isEmpty()) {
      for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifier()) {
        LinkingIdentifier li = new LinkingIdentifier();
        li.setType(laict.getLinkingAgentIdentifierType().getValue());
        li.setValue(laict.getLinkingAgentIdentifierValue());
        li.setRoles(toStringList(laict.getLinkingAgentRole()));
        identifiers.add(li);
      }
    }
    return identifiers;
  }

  public static List<LinkingIdentifier> extractObjectFromEvent(Binary binary)
      throws ValidationException, GenericException {
    List<LinkingIdentifier> identifiers = new ArrayList<>();
    EventComplexType event = PremisV3Utils.binaryToEvent(binary.getContent(), true);
    if (event.getLinkingObjectIdentifier() != null && !event.getLinkingObjectIdentifier().isEmpty()) {
      for (LinkingObjectIdentifierComplexType loict : event.getLinkingObjectIdentifier()) {
        LinkingIdentifier li = new LinkingIdentifier();
        li.setType(loict.getLinkingObjectIdentifierType().getValue());
        li.setValue(loict.getLinkingObjectIdentifierValue());
        li.setRoles(toStringList(loict.getLinkingObjectRole()));
        identifiers.add(li);
      }
    }
    return identifiers;
  }

  public static StringPlusAuthority getStringPlusAuthority(String value) {
    return getStringPlusAuthority(value, "");
  }

  private static StringPlusAuthority getStringPlusAuthority(String value, String authority) {
    StringPlusAuthority spa = FACTORY.createStringPlusAuthority();
    spa.setValue(value);
    if (StringUtils.isNotBlank(authority)) {
      spa.setAuthority(authority);
    }
    return spa;
  }

  public static List<StringPlusAuthority> getStringPlusAuthorityArray(List<String> values) {
    List<StringPlusAuthority> l = new ArrayList<>();
    if (values != null && !values.isEmpty()) {
      for (String value : values) {
        l.add(getStringPlusAuthority(value));
      }
    }
    return l;
  }

  public static List<String> toStringList(List<StringPlusAuthority> source) {
    List<String> dst = new ArrayList<>();
    if (source != null && !source.isEmpty()) {
      for (StringPlusAuthority spa : source) {
        dst.add(spa.getValue());
      }
    }
    return dst;
  }

  public static void updatePremisEventInstanceId(PreservationMetadata pm, ModelService model, IndexService index,
                                                 String instanceId, String username) throws AuthorizationDeniedException, RequestNotValidException, GenericException,
      ValidationException, AlreadyExistsException, AlreadyHasInstanceIdentifier, InstanceIdNotUpdated {

    if (URNUtils.verifyInstanceIdentifier(pm.getId(), instanceId)) {
      throw new AlreadyHasInstanceIdentifier(
          "The preservation event (" + pm.getId() + ") already has instance identifier");
    }

    String updatedId = IdUtils.updatePreservationMetadataInstanceId(pm.getId(), instanceId);

    if (pm.getType().equals(PreservationMetadata.PreservationMetadataType.EVENT)) {
      IndexedPreservationEvent event = null;
      try {
        Binary binary = model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(),
            pm.getFileDirectoryPath(), pm.getFileId(), pm.getId());

        model.createPreservationMetadata(pm.getType(), updatedId, pm.getAipId(), pm.getRepresentationId(),
            pm.getFileDirectoryPath(), pm.getFileId(), binary.getContent(), username, true);

        model.deletePreservationMetadata(pm.getType(), pm.getAipId(), pm.getRepresentationId(), pm.getId(),
            pm.getFileDirectoryPath(), true);
      } catch (NotFoundException e) {
        throw new InstanceIdNotUpdated(e);
      }
    } else if (pm.getType().equals(PreservationMetadataType.REPRESENTATION)
        || pm.getType().equals(PreservationMetadataType.FILE)) {
      try {
        StoragePath path = ModelUtils.getPreservationMetadataStoragePath(pm);
        ContentPayload payload = model.getStorage().getBinary(path).getContent();

        model.createPreservationMetadata(pm.getType(), updatedId, pm.getAipId(), pm.getRepresentationId(),
            pm.getFileDirectoryPath(), pm.getFileId(), payload, username, false);

        model.deletePreservationMetadata(pm, false);
      } catch (NotFoundException e) {
        throw new InstanceIdNotUpdated(e);
      }
    }
  }

  public static void updatePremisUserAgentId(PreservationMetadata pm, ModelService model, IndexService index,
                                             String instanceId) throws GenericException, AuthorizationDeniedException, RequestNotValidException,
      AlreadyExistsException, ValidationException, NotFoundException {

    String updatedId = IdUtils.updatePreservationMetadataInstanceId(pm.getId(), instanceId);

    IndexedPreservationAgent agent;
    try {
      agent = index.retrieve(IndexedPreservationAgent.class, pm.getId(), new ArrayList<>());

      ContentPayload agentPayload = PremisV3Utils.createPremisAgentBinary(updatedId, agent.getName(),
          PreservationAgentType.valueOf(agent.getType().toUpperCase()), agent.getExtension(), agent.getNote(),
          agent.getVersion());

      model.createPreservationMetadata(PreservationMetadataType.AGENT, updatedId, agentPayload, true);

      model.deletePreservationMetadata(PreservationMetadataType.AGENT, pm.getAipId(), pm.getRepresentationId(),
          pm.getId(), pm.getFileDirectoryPath(), true);

    } catch (NotFoundException e) {
      RODAMember member = index.retrieve(RODAMember.class,
          IdUtils.getUserId(URNUtils.getAgentUsernameFromURN(pm.getId())),
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.MEMBERS_FULLNAME, RodaConstants.MEMBERS_EMAIL));
      if (member instanceof User) {
        User user = (User) member;
        String note = user.getEmail();
        ContentPayload agentPayload = PremisV3Utils.createPremisAgentBinary(updatedId, member.getFullName(),
            PreservationAgentType.PERSON, null, note, null);

        model.createPreservationMetadata(PreservationMetadataType.AGENT, updatedId, agentPayload, true);

        model.deletePreservationMetadata(PreservationMetadataType.AGENT, pm.getAipId(), pm.getRepresentationId(),
            pm.getId(), pm.getFileDirectoryPath(), true);
      }
    }

  }

  public static PreservationMetadata createOrUpdatePremisUserAgentBinary(String username, ModelService model,
                                                                         IndexService index, boolean notify) throws GenericException, ValidationException, NotFoundException,
      RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    return createOrUpdatePremisUserAgentBinary(username, model, index, notify, null);
  }

  public static PreservationMetadata createOrUpdatePremisUserAgentBinary(String username, ModelService model,
                                                                         IndexService index, boolean notify, Job job) throws GenericException, ValidationException, NotFoundException,
      RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    PreservationMetadata pm = null;

    if (StringUtils.isNotBlank(username)) {
      String id = IdUtils.getUserAgentId(username, RODAInstanceUtils.getLocalInstanceIdentifier());
      String fullName = "";
      String extension = "";
      String note = "";
      String version = "";

      if (job != null) {
        for (JobUserDetails jobUserDetails : job.getJobUsersDetails()) {
          if (jobUserDetails.getUsername().equals(username)) {
            fullName = jobUserDetails.getFullname();
            note = jobUserDetails.getEmail();
          }
        }
      } else {
        try {
          RODAMember member = index.retrieve(RODAMember.class, IdUtils.getUserId(username),
              Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.MEMBERS_FULLNAME, RodaConstants.MEMBERS_EMAIL));

          fullName = member.getFullName();
          if (member instanceof User) {
            User user = (User) member;
            note = user.getEmail();
          }
        } catch (NotFoundException e) {
          LOGGER.warn("Could not find user and add its details to the PREMIS agent", e);
        }
      }

      ContentPayload agentPayload = PremisV3Utils.createPremisAgentBinary(id, fullName, PreservationAgentType.PERSON,
          extension, note, version);

      try {
        if (model.retrievePreservationAgent(id) != null) {
          pm = model.updatePreservationMetadata(PreservationMetadataType.AGENT, id, agentPayload, notify);
        } else {
          pm = model.createPreservationMetadata(PreservationMetadataType.AGENT, id, agentPayload, notify);
        }
      } catch (NotFoundException e) {
        pm = model.createPreservationMetadata(PreservationMetadataType.AGENT, id, agentPayload, notify);
      }
    }

    return pm;
  }

  public static void updateFormatPreservationMetadata(ModelService model, String aipId, String representationId,
                                                      List<String> fileDirectoryPath, String fileId, String format, String version, String pronom, String mime,
                                                      String username, boolean notify) {
    Binary premisBin;

    try {
      try {
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      } catch (NotFoundException e) {
        LOGGER.debug("PREMIS object skeleton does not exist yet. Creating PREMIS object!");
        List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();

        if (fileId == null) {
          PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aipId, representationId, algorithms,
              username);
        } else {
          // File file;
          // if (shallow) {
          // file = model.retrieveFileInsideManifest(aipId, representationId,
          // fileDirectoryPath, fileId);
          // } else {
          // file = model.retrieveFile(aipId, representationId, fileDirectoryPath,
          // fileId);
          // }
          File file = model.retrieveFile(aipId, representationId, fileDirectoryPath, fileId);
          PremisSkeletonPluginUtils.createPremisSkeletonOnFile(model, file, algorithms, username);
        }

        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
        LOGGER.debug("PREMIS object skeleton created");
      }

      gov.loc.premis.v3.File premisFile = binaryToFile(premisBin.getContent(), false);
      PremisV3Utils.updateFileFormat(premisFile, format, version, pronom, mime);

      PreservationMetadataType type = PreservationMetadataType.FILE;
      String id = IdUtils.getPreservationFileId(fileId, RODAInstanceUtils.getLocalInstanceIdentifier());

      ContentPayload premisFilePayload = fileToBinary(premisFile);
      model.updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, premisFilePayload,
          username, notify);
    } catch (RODAException | IOException e) {
      LOGGER.error("PREMIS will not be updated due to an error", e);
    }
  }

  public static Representation binaryToRepresentation(ContentPayload payload, boolean validate)
      throws ValidationException, GenericException {
    try (InputStream inputStream = payload.createInputStream()) {
      return binaryToRepresentation(inputStream, validate);
    } catch (IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }
  }

  public static gov.loc.premis.v3.File binaryToFile(ContentPayload payload, boolean validate)
      throws ValidationException, GenericException {
    try (InputStream inputStream = payload.createInputStream()) {
      return binaryToFile(inputStream, validate);
    } catch (IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }
  }

  public static EventComplexType binaryToEvent(ContentPayload payload, boolean validate)
      throws ValidationException, GenericException {
    try (InputStream inputStream = payload.createInputStream()) {
      return binaryToEvent(inputStream, validate);
    } catch (IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }
  }

  public static AgentComplexType binaryToAgent(ContentPayload payload, boolean validate)
      throws ValidationException, GenericException {
    try (InputStream inputStream = payload.createInputStream()) {
      return binaryToAgent(inputStream, validate);
    } catch (IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }
  }

  public static ContentPayload fileToBinary(gov.loc.premis.v3.File file, Class<?>... additionalClass)
      throws GenericException, ValidationException {
    if (additionalClass == null || additionalClass.length == 0) {
      return MetadataUtils.saveToContentPayload(FACTORY.createObject(file), gov.loc.premis.v3.File.class);
    }

    List<Class<?>> tClasses = new ArrayList<>(Arrays.asList(additionalClass));
    tClasses.add(gov.loc.premis.v3.File.class);
    return MetadataUtils.saveToContentPayload(FACTORY.createObject(file), tClasses.toArray(new Class<?>[0]));
  }

  public static ContentPayload fileToBinary(gov.loc.premis.v3.File file) throws GenericException, ValidationException {
    return MetadataUtils.saveToContentPayload(FACTORY.createObject(file), gov.loc.premis.v3.File.class);
  }

  public static ContentPayload representationToBinary(Representation representation) {
    return MetadataUtils.saveToContentPayload(FACTORY.createObject(representation), Representation.class);
  }

  public static void updateCreatingApplicationPreservationMetadata(ModelService model, String aipId,
                                                                   String representationId, List<String> fileDirectoryPath, String fileId, String creatingApplicationName,
                                                                   String creatingApplicationVersion, String dateCreatedByApplication, String username, boolean notify) {
    Binary premisBin;

    try {
      try {
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      } catch (NotFoundException e) {
        LOGGER.debug("PREMIS object skeleton does not exist yet. Creating PREMIS object!");
        List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
        PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aipId, representationId, algorithms,
            username);
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
        LOGGER.debug("PREMIS object skeleton created");
      }

      gov.loc.premis.v3.File premisFile = binaryToFile(premisBin.getContent(), false);
      PremisV3Utils.updateCreatingApplication(premisFile, creatingApplicationName, creatingApplicationVersion,
          dateCreatedByApplication);

      PreservationMetadataType type = PreservationMetadataType.FILE;
      String id = IdUtils.getPreservationFileId(fileId, RODAInstanceUtils.getLocalInstanceIdentifier());

      ContentPayload premisFilePayload = fileToBinary(premisFile);
      model.updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, premisFilePayload,
          username, notify);
    } catch (RODAException | IOException e) {
      LOGGER.error("PREMIS will not be updated due to an error", e);
    }
  }

  public static void updateCreatingApplicationTechnicalMetadata(ModelService model, String aipId,
                                                                String representationId, List<String> fileDirectoryPath, String fileId, String username, boolean notify,
                                                                TechnicalMetadata technicalMetadata) {
    Binary premisBin;

    try {
      try {
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      } catch (NotFoundException e) {
        LOGGER.debug("PREMIS object skeleton does not exist yet. Creating PREMIS object!");
        List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
        PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aipId, representationId, algorithms,
            username);
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
        LOGGER.debug("PREMIS object skeleton created");
      }

      gov.loc.premis.v3.File premisFile = binaryToFile(premisBin.getContent(), false);

      PremisV3Utils.updateTechnicalMetadata(premisFile, technicalMetadata);
      PreservationMetadataType pmtype = PreservationMetadataType.FILE;
      String id = IdUtils.getPreservationFileId(fileId, RODAInstanceUtils.getLocalInstanceIdentifier());
      ContentPayload premisFilePayload = fileToBinary(premisFile, TechnicalMetadata.class, TechnicalMetadataElement.class, TechnicalMetadataField.class);
      model.updatePreservationMetadata(id, pmtype, aipId, representationId, fileDirectoryPath, fileId,
          premisFilePayload, username, notify);
    } catch (RODAException | IOException | JAXBException e) {
      LOGGER.error("PREMIS will not be updated due to an error", e);
    }
  }

  public static List<String> getApplicationTechnicalMetadataParameters(ModelService model, String aipId,
                                                                       String representationId, List<String> fileDirectoryPath, String fileId) {
    Binary premisBin = null;
    List<String> parameters = null;
    try {
      try {
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      } catch (NotFoundException e) {
        LOGGER.debug("PREMIS object skeleton does not exist");
      }

      parameters = getTechnicalMetadataParameters(premisBin);

    } catch (RODAException | IOException e) {
      LOGGER.error("Error fetching technical parameters", e);
    }
    return parameters;
  }

  private static List<String> getTechnicalMetadataParameters(Binary premisFile) throws IOException {

    return XMLUtility.getListString(premisFile.getContent().createInputStream(),
        "//featureExtractor/@featureExtractorType | //@featureExtractorVersion");
  }

  private static Representation binaryToRepresentation(InputStream binaryInputStream, boolean validate)
      throws GenericException, ValidationException {
    JAXBContext jaxbContext;
    ValidationEventCollector validationCollector = new ValidationEventCollector();

    try {
      jaxbContext = JAXBContext.newInstance(gov.loc.premis.v3.Representation.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      if (validate) {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource source = new StreamSource(
            PremisV3Utils.class.getClassLoader().getResourceAsStream("premis/v3/premis.xsd"));
        Schema schema = sf.newSchema(source);
        jaxbUnmarshaller.setSchema(schema);
        jaxbUnmarshaller.setEventHandler(validationCollector);
      }

      SAXSource xmlSource = XMLUtils.getSafeSAXSource(new InputSource(new InputStreamReader(binaryInputStream)));
      Object unmarshal = jaxbUnmarshaller.unmarshal(xmlSource);
      return ((Representation) ((JAXBElement<?>) unmarshal).getValue());
    } catch (SAXException | JAXBException | ParserConfigurationException e) {
      if (validate && validationCollector.hasEvents()) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationCollector));
      } else {
        throw new GenericException("Failed to load representation: " + e.getMessage(), e);
      }
    }
  }

  private static AgentComplexType binaryToAgent(InputStream binaryInputStream, boolean validate)
      throws IOException, GenericException, ValidationException {
    JAXBContext jaxbContext;
    ValidationEventCollector validationCollector = new ValidationEventCollector();

    try {
      jaxbContext = JAXBContext.newInstance(gov.loc.premis.v3.AgentComplexType.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      if (validate) {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource source = new StreamSource(
            PremisV3Utils.class.getClassLoader().getResourceAsStream("premis/v3/premis.xsd"));
        Schema schema = sf.newSchema(source);
        jaxbUnmarshaller.setSchema(schema);
        jaxbUnmarshaller.setEventHandler(validationCollector);
      }

      SAXSource xmlSource = XMLUtils.getSafeSAXSource(new InputSource(new InputStreamReader(binaryInputStream)));
      Object unmarshal = jaxbUnmarshaller.unmarshal(xmlSource);
      return ((AgentComplexType) ((JAXBElement<?>) unmarshal).getValue());
    } catch (JAXBException | SAXException | ParserConfigurationException e) {
      if (validate && validationCollector.hasEvents()) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationCollector));
      } else {
        throw new GenericException("Failed to load Agent: " + e.getMessage(), e);
      }
    }
  }

  private static gov.loc.premis.v3.File binaryToFile(InputStream binaryInputStream, boolean validate)
      throws GenericException, ValidationException {
    JAXBContext jaxbContext;
    ValidationEventCollector validationCollector = new ValidationEventCollector();

    try {
      jaxbContext = JAXBContext.newInstance(gov.loc.premis.v3.File.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      if (validate) {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource source = new StreamSource(
            PremisV3Utils.class.getClassLoader().getResourceAsStream("premis/v3/premis.xsd"));
        Schema schema = sf.newSchema(source);
        jaxbUnmarshaller.setSchema(schema);
        jaxbUnmarshaller.setEventHandler(validationCollector);
      }

      SAXSource xmlSource = XMLUtils.getSafeSAXSource(new InputSource(new InputStreamReader(binaryInputStream)));
      return jaxbUnmarshaller.unmarshal(xmlSource, gov.loc.premis.v3.File.class).getValue();

    } catch (SAXException | JAXBException | ParserConfigurationException e) {
      if (validate && validationCollector.hasEvents()) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationCollector));
      } else {
        throw new GenericException("Failed to load representation: " + e.getMessage(), e);
      }
    }
  }

  private static EventComplexType binaryToEvent(InputStream binaryInputStream, boolean validate)
      throws GenericException, ValidationException {
    JAXBContext jaxbContext;
    ValidationEventCollector validationCollector = new ValidationEventCollector();

    try {
      jaxbContext = JAXBContext.newInstance(gov.loc.premis.v3.EventComplexType.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      if (validate) {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource source = new StreamSource(
            PremisV3Utils.class.getClassLoader().getResourceAsStream("premis/v3/premis.xsd"));
        Schema schema = sf.newSchema(source);
        jaxbUnmarshaller.setSchema(schema);
        jaxbUnmarshaller.setEventHandler(validationCollector);
      }

      SAXSource xmlSource = XMLUtils.getSafeSAXSource(new InputSource(new InputStreamReader(binaryInputStream)));
      return jaxbUnmarshaller.unmarshal(xmlSource, EventComplexType.class).getValue();
    } catch (JAXBException | SAXException | ParserConfigurationException e) {
      if (validate && validationCollector.hasEvents()) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationCollector));
      } else {
        throw new GenericException("Failed to load Event: " + e.getMessage(), e);
      }
    }
  }

  public static gov.loc.premis.v3.File binaryToFile(InputStream binaryInputStream) throws GenericException {
    try {
      return binaryToFile(binaryInputStream, false);
    } catch (ValidationException e) {
      // do nothing
      throw new GenericException();
    }
  }

  public static EventComplexType binaryToEvent(InputStream binaryInputStream) throws GenericException {
    try {
      return binaryToEvent(binaryInputStream, false);
    } catch (ValidationException e) {
      // do nothing
      throw new GenericException();
    }
  }

  private static class RodaErrorHandler extends DefaultHandler {
    List<SAXParseException> errors;

    public RodaErrorHandler() {
      errors = new ArrayList<>();
    }

    @Override
    public void warning(SAXParseException e) {
      errors.add(e);
    }

    @Override
    public void error(SAXParseException e) {
      errors.add(e);
    }

    @Override
    public void fatalError(SAXParseException e) {
      errors.add(e);
    }

    public List<SAXParseException> getErrors() {
      return errors;
    }
  }
}
