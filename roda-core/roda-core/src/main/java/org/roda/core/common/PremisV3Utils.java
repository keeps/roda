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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPluginUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.util.FileUtility;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import gov.loc.premis.v3.AgentComplexType;
import gov.loc.premis.v3.AgentDocument;
import gov.loc.premis.v3.AgentIdentifierComplexType;
import gov.loc.premis.v3.ContentLocationComplexType;
import gov.loc.premis.v3.CreatingApplicationComplexType;
import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.EventDetailInformationComplexType;
import gov.loc.premis.v3.EventDocument;
import gov.loc.premis.v3.EventIdentifierComplexType;
import gov.loc.premis.v3.EventOutcomeDetailComplexType;
import gov.loc.premis.v3.EventOutcomeInformationComplexType;
import gov.loc.premis.v3.FixityComplexType;
import gov.loc.premis.v3.FormatComplexType;
import gov.loc.premis.v3.FormatDesignationComplexType;
import gov.loc.premis.v3.FormatRegistryComplexType;
import gov.loc.premis.v3.LinkingAgentIdentifierComplexType;
import gov.loc.premis.v3.LinkingObjectIdentifierComplexType;
import gov.loc.premis.v3.ObjectCharacteristicsComplexType;
import gov.loc.premis.v3.ObjectComplexType;
import gov.loc.premis.v3.ObjectDocument;
import gov.loc.premis.v3.ObjectIdentifierComplexType;
import gov.loc.premis.v3.RelatedObjectIdentifierComplexType;
import gov.loc.premis.v3.RelationshipComplexType;
import gov.loc.premis.v3.Representation;
import gov.loc.premis.v3.StorageComplexType;
import gov.loc.premis.v3.StringPlusAuthority;

public final class PremisV3Utils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PremisV3Utils.class);
  private static final String FIXITY_ORIGINATOR = "RODA";
  private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

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

  public static void updateCreatingApplication(gov.loc.premis.v3.File file, String creatingApplicationName,
    String creatingApplicationVersion, String dateCreatedByApplication) {
    if (StringUtils.isNotBlank(creatingApplicationName)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.setCreatingApplicationName(getStringPlusAuthority(creatingApplicationName));
    }

    if (StringUtils.isNotBlank(creatingApplicationVersion)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.setCreatingApplicationVersion(creatingApplicationVersion);
    }

    if (StringUtils.isNotBlank(dateCreatedByApplication)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.setDateCreatedByApplication(dateCreatedByApplication);
    }
  }

  private static CreatingApplicationComplexType getCreatingApplication(gov.loc.premis.v3.File f) {
    ObjectCharacteristicsComplexType occt;
    CreatingApplicationComplexType cact;
    if (f.getObjectCharacteristicsArray() != null && f.getObjectCharacteristicsArray().length > 0) {
      occt = f.getObjectCharacteristicsArray(0);
    } else {
      occt = f.addNewObjectCharacteristics();
    }
    if (occt.getCreatingApplicationArray() != null && occt.getCreatingApplicationArray().length > 0) {
      cact = occt.getCreatingApplicationArray(0);
    } else {
      cact = occt.addNewCreatingApplication();
    }
    return cact;
  }

  public static FormatRegistryComplexType getFormatRegistry(gov.loc.premis.v3.File f, String registryName) {
    ObjectCharacteristicsComplexType occt;
    FormatRegistryComplexType frct = null;
    if (f.getObjectCharacteristicsArray() != null && f.getObjectCharacteristicsArray().length > 0) {
      occt = f.getObjectCharacteristicsArray(0);
    } else {
      occt = f.addNewObjectCharacteristics();
    }
    if (occt.getFormatArray() != null && occt.getFormatArray().length > 0) {
      for (FormatComplexType fct : occt.getFormatArray()) {
        if (fct.getFormatRegistry() != null
          && fct.getFormatRegistry().getFormatRegistryName().getStringValue().equalsIgnoreCase(registryName)) {
          frct = fct.getFormatRegistry();
          break;
        }
      }
      if (frct == null) {
        FormatComplexType fct = occt.addNewFormat();
        frct = fct.addNewFormatRegistry();
        frct.setFormatRegistryName(getStringPlusAuthority(registryName));
      }
    } else {
      FormatComplexType fct = occt.addNewFormat();
      frct = fct.addNewFormatRegistry();
      frct.setFormatRegistryName(getStringPlusAuthority(registryName));
    }
    return frct;
  }

  private static FormatDesignationComplexType getFormatDesignation(gov.loc.premis.v3.File f) {
    ObjectCharacteristicsComplexType occt;
    FormatComplexType fct;
    FormatDesignationComplexType fdct;
    if (f.getObjectCharacteristicsArray() != null && f.getObjectCharacteristicsArray().length > 0) {
      occt = f.getObjectCharacteristicsArray(0);
    } else {
      occt = f.addNewObjectCharacteristics();
    }
    if (occt.getFormatArray() != null && occt.getFormatArray().length > 0) {
      fct = occt.getFormatArray(0);
    } else {
      fct = occt.addNewFormat();
    }
    if (fct.getFormatDesignation() != null) {
      fdct = fct.getFormatDesignation();
    } else {
      fdct = fct.addNewFormatDesignation();
    }
    return fdct;
  }

  public static ContentPayload createPremisEventBinary(String eventID, Date date, String type, String details,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, String outcome, String detailNote,
    String detailExtension, List<String> agentIds) throws GenericException, ValidationException {
    EventDocument event = EventDocument.Factory.newInstance();
    EventComplexType ect = event.addNewEvent();
    EventIdentifierComplexType eict = ect.addNewEventIdentifier();
    eict.setEventIdentifierValue(eventID);
    eict.setEventIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    ect.setEventDateTime(DateParser.getIsoDate(date));
    ect.setEventType(getStringPlusAuthority(type));
    EventDetailInformationComplexType edict = ect.addNewEventDetailInformation();
    edict.setEventDetail(details);
    if (sources != null) {
      for (LinkingIdentifier identifier : sources) {
        LinkingObjectIdentifierComplexType loict = ect.addNewLinkingObjectIdentifier();
        loict.setLinkingObjectIdentifierValue(identifier.getValue());
        loict.setLinkingObjectIdentifierType(getStringPlusAuthority(identifier.getType()));
        if (identifier.getRoles() != null) {
          loict.setLinkingObjectRoleArray(getStringPlusAuthorityArray(identifier.getRoles()));
        }
      }
    }

    if (outcomes != null) {
      for (LinkingIdentifier identifier : outcomes) {
        LinkingObjectIdentifierComplexType loict = ect.addNewLinkingObjectIdentifier();
        loict.setLinkingObjectIdentifierValue(identifier.getValue());
        loict.setLinkingObjectIdentifierType(getStringPlusAuthority(identifier.getType()));
        if (identifier.getRoles() != null) {
          loict.setLinkingObjectRoleArray(getStringPlusAuthorityArray(identifier.getRoles()));
        }
      }
    }

    if (agentIds != null) {
      for (String agentId : agentIds) {
        LinkingAgentIdentifierComplexType agentIdentifier = ect.addNewLinkingAgentIdentifier();
        agentIdentifier.setLinkingAgentIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
        agentIdentifier.setLinkingAgentIdentifierValue(agentId);
      }
    }

    EventOutcomeInformationComplexType outcomeInformation = ect.addNewEventOutcomeInformation();
    outcomeInformation.setEventOutcome(getStringPlusAuthority(outcome));
    StringBuilder outcomeDetailNote = new StringBuilder(detailNote);
    if (StringUtils.isNotBlank(detailExtension)) {
      outcomeDetailNote.append("\n").append(detailExtension);
    }
    EventOutcomeDetailComplexType eodct = outcomeInformation.addNewEventOutcomeDetail();
    eodct.setEventOutcomeDetailNote(outcomeDetailNote.toString());

    return MetadataUtils.saveToContentPayload(event, true);
  }

  public static ContentPayload createPremisAgentBinary(String id, String name, PreservationAgentType type,
    String extension, String note, String version) throws GenericException, ValidationException {
    AgentDocument agent = AgentDocument.Factory.newInstance();

    AgentComplexType act = agent.addNewAgent();
    AgentIdentifierComplexType agentIdentifier = act.addNewAgentIdentifier();
    agentIdentifier.setAgentIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    agentIdentifier.setAgentIdentifierValue(id);

    act.setAgentType(getStringPlusAuthority(type.toString()));

    if (StringUtils.isNotBlank(name)) {
      act.addNewAgentName().setStringValue(name);
    }

    if (StringUtils.isNotBlank(note)) {
      act.addAgentNote(note);
    }

    if (StringUtils.isNotBlank(version)) {
      act.setAgentVersion(version);
    }
    if (StringUtils.isNotBlank(extension)) {
      try {
        act.addNewAgentExtension().set(XmlObject.Factory.parse(extension));
      } catch (XmlException e) {
        throw new ValidationException(e.getMessage());
      }
    }

    return MetadataUtils.saveToContentPayload(agent, true);
  }

  public static Representation createBaseRepresentation(String aipId, String representationId)
    throws GenericException, ValidationException {

    Representation representation = Representation.Factory.newInstance();
    ObjectIdentifierComplexType oict = representation.addNewObjectIdentifier();
    oict.setObjectIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    String identifier = IdUtils.getPreservationId(PreservationMetadataType.REPRESENTATION, aipId, representationId,
      null, null);
    oict.setObjectIdentifierValue(identifier);
    representation.addNewPreservationLevel().setPreservationLevelValue(getStringPlusAuthority(""));
    return representation;
  }

  public static ContentPayload createBaseFile(File originalFile, ModelService model,
    Collection<String> fixityAlgorithms) throws GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException, ValidationException, XmlException {
    ObjectDocument document = ObjectDocument.Factory.newInstance();
    gov.loc.premis.v3.File file = gov.loc.premis.v3.File.Factory.newInstance();
    file.addNewPreservationLevel()
      .setPreservationLevelValue(getStringPlusAuthority(RodaConstants.PRESERVATION_LEVEL_FULL));
    ObjectIdentifierComplexType oict = file.addNewObjectIdentifier();
    oict.setObjectIdentifierValue(
      URNUtils.createRodaPreservationURN(PreservationMetadataType.FILE, originalFile.getId()));
    oict.setObjectIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    ObjectCharacteristicsComplexType occt = file.addNewObjectCharacteristics();
    // TODO
    // occt.setCompositionLevel(CompositionLevelComplexType.Factory.parse("0"));
    FormatComplexType fct = occt.addNewFormat();
    FormatDesignationComplexType fdct = fct.addNewFormatDesignation();
    fdct.setFormatName(getStringPlusAuthority(""));
    fdct.setFormatVersion("");
    Binary binary = model.getStorage().getBinary(ModelUtils.getFileStoragePath(originalFile));

    if (binary.getContentDigest() != null && !binary.getContentDigest().isEmpty()) {
      // use binary content digest information
      for (Entry<String, String> entry : binary.getContentDigest().entrySet()) {
        FixityComplexType premisFixity = occt.addNewFixity();
        premisFixity.setMessageDigest(entry.getKey());
        premisFixity.setMessageDigestAlgorithm(getStringPlusAuthority(entry.getValue()));
        premisFixity.setMessageDigestOriginator(getStringPlusAuthority(FIXITY_ORIGINATOR));
      }
    } else {
      // if binary does not contain digest, create a new one
      try {
        List<Fixity> fixities = calculateFixities(binary, fixityAlgorithms, FIXITY_ORIGINATOR);

        for (Fixity fixity : fixities) {
          FixityComplexType premisFixity = occt.addNewFixity();
          premisFixity.setMessageDigest(fixity.getMessageDigest());
          premisFixity.setMessageDigestAlgorithm(getStringPlusAuthority(fixity.getMessageDigestAlgorithm()));
          premisFixity.setMessageDigestOriginator(getStringPlusAuthority(fixity.getMessageDigestOriginator()));
        }
      } catch (IOException | NoSuchAlgorithmException e) {
        LOGGER.warn("Could not calculate fixity for file " + originalFile);
      }
    }

    occt.setSize(binary.getSizeInBytes());
    // occt.addNewObjectCharacteristicsExtension().set("");
    file.addNewOriginalName().setStringValue(originalFile.getId());
    StorageComplexType sct = file.addNewStorage();
    ContentLocationComplexType clct = sct.addNewContentLocation();
    clct.setContentLocationType(getStringPlusAuthority(""));
    clct.setContentLocationValue("");

    document.setObject(file);

    return MetadataUtils.saveToContentPayload(document, true);
  }

  public static List<Fixity> extractFixities(Binary premisFile) throws GenericException, XmlException, IOException {
    List<Fixity> fixities = new ArrayList<>();
    try (InputStream inputStream = premisFile.getContent().createInputStream()) {
      gov.loc.premis.v3.File f = binaryToFile(inputStream);
      if (f.getObjectCharacteristicsArray() != null && f.getObjectCharacteristicsArray().length > 0) {
        ObjectCharacteristicsComplexType occt = f.getObjectCharacteristicsArray(0);
        if (occt.getFixityArray() != null && occt.getFixityArray().length > 0) {
          for (FixityComplexType fct : occt.getFixityArray()) {
            Fixity fix = new Fixity();
            fix.setMessageDigest(fct.getMessageDigest());
            fix.setMessageDigestAlgorithm(fct.getMessageDigestAlgorithm().getStringValue());
            fix.setMessageDigestOriginator(fct.getMessageDigestOriginator().getStringValue());
            fixities.add(fix);
          }
        }
      }
    }

    return fixities;
  }

  public static String extractFixity(Binary premisFile, String fixityType)
    throws IOException, GenericException, XmlException {
    String fixityValue = null;
    try (InputStream inputStream = premisFile.getContent().createInputStream()) {
      gov.loc.premis.v3.File f = binaryToFile(inputStream);
      if (f.getObjectCharacteristicsArray() != null && f.getObjectCharacteristicsArray().length > 0) {
        ObjectCharacteristicsComplexType occt = f.getObjectCharacteristicsArray(0);
        if (occt.getFixityArray() != null && occt.getFixityArray().length > 0) {
          for (FixityComplexType fct : occt.getFixityArray()) {
            if (fct.getMessageDigestAlgorithm().getStringValue().equalsIgnoreCase(fixityType)) {
              fixityValue = fct.getMessageDigest();
              break;
            }
          }
        }
      }
    }

    return fixityValue;
  }

  public static gov.loc.premis.v3.Representation binaryToRepresentation(InputStream binaryInputStream)
    throws XmlException, IOException, GenericException {
    ObjectDocument objectDocument = ObjectDocument.Factory.parse(binaryInputStream);

    ObjectComplexType object = objectDocument.getObject();
    if (object instanceof Representation) {
      return (Representation) object;
    } else {
      throw new GenericException("Trying to load a representation but was a " + object.getClass().getSimpleName());
    }
  }

  public static gov.loc.premis.v3.File binaryToFile(InputStream binaryInputStream)
    throws XmlException, IOException, GenericException {
    ObjectDocument objectDocument = ObjectDocument.Factory.parse(binaryInputStream);

    ObjectComplexType object = objectDocument.getObject();
    if (object instanceof gov.loc.premis.v3.File) {
      return (gov.loc.premis.v3.File) object;
    } else {
      throw new GenericException("Trying to load a file but was a " + object.getClass().getSimpleName());
    }
  }

  public static EventComplexType binaryToEvent(InputStream binaryInputStream) throws XmlException, IOException {
    return EventDocument.Factory.parse(binaryInputStream).getEvent();
  }

  public static AgentComplexType binaryToAgent(InputStream binaryInputStream) throws XmlException, IOException {
    return AgentDocument.Factory.parse(binaryInputStream).getAgent();
  }

  public static gov.loc.premis.v3.Representation binaryToRepresentation(ContentPayload payload, boolean validate)
    throws ValidationException, GenericException {
    Representation representation;

    try (InputStream inputStream = payload.createInputStream()) {
      representation = binaryToRepresentation(inputStream);

      List<XmlValidationError> validationErrors = new ArrayList<>();
      XmlOptions validationOptions = new XmlOptions();
      validationOptions.setErrorListener(validationErrors);

      if (validate && !representation.validate(validationOptions)) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationErrors));
      }
    } catch (XmlException | IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }

    return representation;
  }

  public static gov.loc.premis.v3.File binaryToFile(ContentPayload payload, boolean validate)
    throws ValidationException, GenericException {
    gov.loc.premis.v3.File file;
    List<XmlValidationError> validationErrors = new ArrayList<>();

    try (InputStream inputStream = payload.createInputStream()) {
      file = binaryToFile(inputStream);

      XmlOptions validationOptions = new XmlOptions();
      validationOptions.setErrorListener(validationErrors);

      if (validate && !file.validate(validationOptions)) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationErrors));
      }
    } catch (XmlException e) {
      ValidationException exception = new ValidationException(e);
      exception.setReport(MetadataUtils.xmlValidationErrorsToValidationReport(validationErrors));
      throw exception;
    } catch (IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }

    return file;
  }

  public static ContentPayload fileToBinary(gov.loc.premis.v3.File file) throws GenericException, ValidationException {
    ObjectDocument d = ObjectDocument.Factory.newInstance();
    d.setObject(file);
    return MetadataUtils.saveToContentPayload(d, true);
  }

  public static ContentPayload representationToBinary(Representation representation)
    throws GenericException, ValidationException {
    ObjectDocument d = ObjectDocument.Factory.newInstance();
    d.setObject(representation);
    return MetadataUtils.saveToContentPayload(d, true);
  }

  public static EventComplexType binaryToEvent(ContentPayload payload, boolean validate)
    throws ValidationException, GenericException {
    EventComplexType event;

    try (InputStream inputStream = payload.createInputStream()) {
      event = binaryToEvent(inputStream);

      List<XmlValidationError> validationErrors = new ArrayList<>();
      XmlOptions validationOptions = new XmlOptions();
      validationOptions.setErrorListener(validationErrors);

      if (validate && !event.validate(validationOptions)) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationErrors));
      }
    } catch (XmlException | IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }

    return event;
  }

  public static AgentComplexType binaryToAgent(ContentPayload payload, boolean validate)
    throws ValidationException, GenericException {
    AgentComplexType agent;

    try (InputStream inputStream = payload.createInputStream()) {
      agent = binaryToAgent(inputStream);

      List<XmlValidationError> validationErrors = new ArrayList<>();
      XmlOptions validationOptions = new XmlOptions();
      validationOptions.setErrorListener(validationErrors);

      if (validate && !agent.validate(validationOptions)) {
        throw new ValidationException(MetadataUtils.xmlValidationErrorsToValidationReport(validationErrors));
      }
    } catch (XmlException | IOException e) {
      throw new GenericException("Error loading representation premis file", e);
    }

    return agent;
  }

  public static SolrInputDocument getSolrDocument(Binary premisBinary) throws GenericException {
    SolrInputDocument doc = new SolrInputDocument();

    try (InputStream inputStream = premisBinary.getContent().createInputStream()) {
      gov.loc.premis.v3.File premisFile = binaryToFile(inputStream);
      if (premisFile.getOriginalName() != null) {
        doc.setField(RodaConstants.FILE_ORIGINALNAME, premisFile.getOriginalName().getStringValue());
        // TODO extension
      }

      if (premisFile.getObjectCharacteristicsArray() != null && premisFile.getObjectCharacteristicsArray().length > 0) {
        ObjectCharacteristicsComplexType occt = premisFile.getObjectCharacteristicsArray(0);
        doc.setField(RodaConstants.FILE_SIZE, occt.getSize());
        if (occt.getFixityArray() != null && occt.getFixityArray().length > 0) {
          List<String> hashes = new ArrayList<>();
          for (FixityComplexType fct : occt.getFixityArray()) {
            StringBuilder fixityPrint = new StringBuilder();
            fixityPrint.append(fct.getMessageDigest());
            fixityPrint.append(" (");
            fixityPrint.append(fct.getMessageDigestAlgorithm().getStringValue());
            if (StringUtils.isNotBlank(fct.getMessageDigestOriginator().getStringValue())) {
              fixityPrint.append(", "); //
              fixityPrint.append(fct.getMessageDigestOriginator().getStringValue());
            }
            fixityPrint.append(")");
            hashes.add(fixityPrint.toString());
          }
          doc.addField(RodaConstants.FILE_HASH, hashes);
        }
        if (occt.getFormatArray() != null && occt.getFormatArray().length > 0) {
          FormatComplexType fct = occt.getFormatArray(0);
          if (fct.getFormatDesignation() != null) {
            String format = fct.getFormatDesignation().getFormatName().getStringValue();
            String formatVersion = fct.getFormatDesignation().getFormatVersion();
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
          if (pronomRegistry != null && pronomRegistry.getFormatRegistryKey() != null) {
            doc.addField(RodaConstants.FILE_PRONOM, pronomRegistry.getFormatRegistryKey().getStringValue());
          }
          FormatRegistryComplexType mimeRegistry = getFormatRegistry(premisFile,
            RodaConstants.PRESERVATION_REGISTRY_MIME);
          if (mimeRegistry != null && mimeRegistry.getFormatRegistryKey() != null) {
            doc.addField(RodaConstants.FILE_FORMAT_MIMETYPE, mimeRegistry.getFormatRegistryKey().getStringValue());
          }
          // TODO extension
        }
        if (occt.getCreatingApplicationArray() != null && occt.getCreatingApplicationArray().length > 0) {
          CreatingApplicationComplexType cact = occt.getCreatingApplicationArray(0);
          if (cact.getCreatingApplicationName() != null) {
            doc.addField(RodaConstants.FILE_CREATING_APPLICATION_NAME,
              cact.getCreatingApplicationName().getStringValue());
          }
          doc.addField(RodaConstants.FILE_CREATING_APPLICATION_VERSION, cact.getCreatingApplicationVersion());
          doc.addField(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION, cact.getDateCreatedByApplication());
        }
      }

    } catch (XmlException | IOException e) {
      LOGGER.error("Error updating Solr document", e);
    }

    return doc;
  }

  public static PreservationMetadata createPremisAgentBinary(Plugin<?> plugin, ModelService model, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException {
    String id = IdUtils.getPluginAgentId(plugin.getClass().getName(), plugin.getVersion());
    String extension = "";
    ContentPayload agentPayload = PremisV3Utils.createPremisAgentBinary(id, plugin.getName(), plugin.getAgentType(),
      extension, plugin.getDescription(), plugin.getVersion());
    return model.createPreservationMetadata(PreservationMetadataType.AGENT, id, agentPayload, notify);
  }

  public static void linkFileToRepresentation(String fileId, String relationshipType, String relationshipSubType,
    Representation r) {
    RelationshipComplexType relationship = r.addNewRelationship();
    relationship.setRelationshipType(getStringPlusAuthority(relationshipType));
    relationship.setRelationshipSubType(getStringPlusAuthority(relationshipSubType));
    RelatedObjectIdentifierComplexType roict = relationship.addNewRelatedObjectIdentifier();
    roict.setRelatedObjectIdentifierType(getStringPlusAuthority(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN));
    roict.setRelatedObjectIdentifierValue(fileId);
  }

  public static List<LinkingIdentifier> extractAgentsFromEvent(Binary b) throws ValidationException, GenericException {
    List<LinkingIdentifier> identifiers = new ArrayList<>();
    EventComplexType event = PremisV3Utils.binaryToEvent(b.getContent(), true);
    if (event.getLinkingAgentIdentifierArray() != null && event.getLinkingAgentIdentifierArray().length > 0) {
      for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifierArray()) {
        LinkingIdentifier li = new LinkingIdentifier();
        li.setType(laict.getLinkingAgentIdentifierType().getStringValue());
        li.setValue(laict.getLinkingAgentIdentifierValue());
        li.setRoles(toStringList(laict.getLinkingAgentRoleArray()));
        identifiers.add(li);
      }
    }
    return identifiers;
  }

  public static List<LinkingIdentifier> extractObjectFromEvent(Binary binary)
    throws ValidationException, GenericException {
    List<LinkingIdentifier> identifiers = new ArrayList<>();
    EventComplexType event = PremisV3Utils.binaryToEvent(binary.getContent(), true);
    if (event.getLinkingObjectIdentifierArray() != null && event.getLinkingObjectIdentifierArray().length > 0) {
      for (LinkingObjectIdentifierComplexType loict : event.getLinkingObjectIdentifierArray()) {
        LinkingIdentifier li = new LinkingIdentifier();
        li.setType(loict.getLinkingObjectIdentifierType().getStringValue());
        li.setValue(loict.getLinkingObjectIdentifierValue());
        li.setRoles(toStringList(loict.getLinkingObjectRoleArray()));
        identifiers.add(li);
      }
    }
    return identifiers;
  }

  public static StringPlusAuthority getStringPlusAuthority(String value) {
    return getStringPlusAuthority(value, "");
  }

  private static StringPlusAuthority getStringPlusAuthority(String value, String authority) {
    StringPlusAuthority spa = StringPlusAuthority.Factory.newInstance();
    spa.setStringValue(value);
    if (StringUtils.isNotBlank(authority)) {
      spa.setAuthority(authority);
    }
    return spa;
  }

  public static StringPlusAuthority[] getStringPlusAuthorityArray(List<String> values) {
    List<StringPlusAuthority> l = new ArrayList<>();
    if (values != null && !values.isEmpty()) {
      for (String value : values) {
        l.add(getStringPlusAuthority(value));
      }
    }
    return l.toArray(new StringPlusAuthority[l.size()]);
  }

  public static List<String> toStringList(StringPlusAuthority[] source) {
    List<String> dst = new ArrayList<>();
    if (source != null && source.length > 0) {
      for (StringPlusAuthority spa : source) {
        dst.add(spa.getStringValue());
      }
    }
    return dst;
  }

  public static PreservationMetadata createOrUpdatePremisUserAgentBinary(String username, ModelService model,
    IndexService index, boolean notify) throws GenericException, ValidationException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    PreservationMetadata pm = null;

    if (StringUtils.isNotBlank(username)) {
      String id = IdUtils.getUserAgentId(username);
      String fullName = "";
      String extension = "";
      String note = "";
      String version = "";

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
    boolean notify) {
    Binary premisBin;

    try {
      try {
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      } catch (NotFoundException e) {
        LOGGER.debug("PREMIS object skeleton does not exist yet. Creating PREMIS object!");
        List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();

        if (fileId == null) {
          PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aipId, representationId, algorithms);
        } else {
          File file = model.retrieveFile(aipId, representationId, fileDirectoryPath, fileId);
          PremisSkeletonPluginUtils.createPremisSkeletonOnFile(model, file, algorithms);
        }

        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
        LOGGER.debug("PREMIS object skeleton created");
      }

      gov.loc.premis.v3.File premisFile = binaryToFile(premisBin.getContent(), false);
      PremisV3Utils.updateFileFormat(premisFile, format, version, pronom, mime);

      PreservationMetadataType type = PreservationMetadataType.FILE;
      String id = IdUtils.getPreservationFileId(fileId);

      ContentPayload premisFilePayload = fileToBinary(premisFile);
      model.updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, premisFilePayload,
        notify);
    } catch (RODAException | XmlException | IOException e) {
      LOGGER.error("PREMIS will not be updated due to an error", e);
    }
  }

  public static void updateCreatingApplicationPreservationMetadata(ModelService model, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, String creatingApplicationName,
    String creatingApplicationVersion, String dateCreatedByApplication, boolean notify) {
    Binary premisBin;

    try {
      try {
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      } catch (NotFoundException e) {
        LOGGER.debug("PREMIS object skeleton does not exist yet. Creating PREMIS object!");
        List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
        PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aipId, representationId, algorithms);
        premisBin = model.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
        LOGGER.debug("PREMIS object skeleton created");
      }

      gov.loc.premis.v3.File premisFile = binaryToFile(premisBin.getContent(), false);
      PremisV3Utils.updateCreatingApplication(premisFile, creatingApplicationName, creatingApplicationVersion,
        dateCreatedByApplication);

      PreservationMetadataType type = PreservationMetadataType.FILE;
      String id = IdUtils.getPreservationFileId(fileId);

      ContentPayload premisFilePayload = fileToBinary(premisFile);
      model.updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, premisFilePayload,
        notify);
    } catch (RODAException | XmlException | IOException e) {
      LOGGER.error("PREMIS will not be updated due to an error", e);
    }
  }
}
