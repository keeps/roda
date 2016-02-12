/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
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
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import lc.xmlns.premisV2.AgentComplexType;
import lc.xmlns.premisV2.AgentDocument;
import lc.xmlns.premisV2.AgentIdentifierComplexType;
import lc.xmlns.premisV2.ContentLocationComplexType;
import lc.xmlns.premisV2.CreatingApplicationComplexType;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.EventDocument;
import lc.xmlns.premisV2.EventIdentifierComplexType;
import lc.xmlns.premisV2.EventOutcomeDetailComplexType;
import lc.xmlns.premisV2.EventOutcomeInformationComplexType;
import lc.xmlns.premisV2.FixityComplexType;
import lc.xmlns.premisV2.FormatComplexType;
import lc.xmlns.premisV2.FormatDesignationComplexType;
import lc.xmlns.premisV2.FormatRegistryComplexType;
import lc.xmlns.premisV2.LinkingAgentIdentifierComplexType;
import lc.xmlns.premisV2.LinkingObjectIdentifierComplexType;
import lc.xmlns.premisV2.ObjectCharacteristicsComplexType;
import lc.xmlns.premisV2.ObjectComplexType;
import lc.xmlns.premisV2.ObjectDocument;
import lc.xmlns.premisV2.ObjectIdentifierComplexType;
import lc.xmlns.premisV2.RelatedObjectIdentificationComplexType;
import lc.xmlns.premisV2.RelationshipComplexType;
import lc.xmlns.premisV2.Representation;
import lc.xmlns.premisV2.StorageComplexType;

public class PremisUtils {
  private static final String SEPARATOR = "/";
  private static final Set<String> MANDATORY_CHECKSUM_ALGORITHMS = new HashSet<>(Arrays.asList("SHA-256"));
  private final static Logger LOGGER = LoggerFactory.getLogger(PremisUtils.class);
  private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

  public static Fixity calculateFixity(Binary binary, String digestAlgorithm, String originator)
    throws IOException, NoSuchAlgorithmException {
    InputStream dsInputStream = binary.getContent().createInputStream();
    Fixity fixity = new Fixity(digestAlgorithm, FileUtility.calculateChecksumInHex(dsInputStream, digestAlgorithm),
      originator);
    dsInputStream.close();
    return fixity;
  }

  public static boolean isPremisV2(Binary binary, Path configBasePath) throws IOException, SAXException {
    boolean premisV2 = true;
    InputStream inputStream = binary.getContent().createInputStream();
    InputStream schemaStream = RodaCoreFactory.getConfigurationFileAsStream("schemas/premis-v2-0.xsd");
    Source xmlFile = new StreamSource(inputStream);
    SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
    Validator validator = schema.newValidator();
    RodaErrorHandler errorHandler = new RodaErrorHandler();
    validator.setErrorHandler(errorHandler);
    try {
      validator.validate(xmlFile);
      List<SAXParseException> errors = errorHandler.getErrors();
      if (errors.size() > 0) {
        premisV2 = false;
      }
    } catch (SAXException e) {
      premisV2 = false;
    }
    return premisV2;
  }

  public static Binary updatePremisToV3IfNeeded(Binary binary, Path configBasePath) throws IOException, SAXException,
    TransformerException, RequestNotValidException, NotFoundException, GenericException {
    if (isPremisV2(binary, configBasePath)) {
      LOGGER.debug("Binary " + binary.getStoragePath().asString() + " is Premis V2... Needs updated...");
      return updatePremisV2toV3(binary, configBasePath);
    } else {
      return binary;
    }

  }

  private static Binary updatePremisV2toV3(Binary binary, Path configBasePath)
    throws IOException, TransformerException, RequestNotValidException, NotFoundException, GenericException {
    InputStream transformerStream = null;
    InputStream bais = null;

    try {
      Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
      Reader reader = new InputStreamReader(binary.getContent().createInputStream());
      transformerStream = RodaCoreFactory.getConfigurationFileAsStream("crosswalks/migration/v2Tov3.xslt");
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      RodaUtils.applyStylesheet(xsltReader, reader, stylesheetOpt, transformerResult);
      Path p = Files.createTempFile("preservation", ".tmp");
      bais = new ByteArrayInputStream(transformerResult.toString().getBytes("UTF-8"));
      Files.copy(bais, p, StandardCopyOption.REPLACE_EXISTING);

      return (Binary) FSUtils.convertPathToResource(p.getParent(), p);
    } catch (IOException e) {
      throw e;
    } catch (TransformerException e) {
      throw e;
    } finally {
      if (transformerStream != null) {
        try {
          transformerStream.close();
        } catch (IOException e) {

        }
      }
      if (bais != null) {
        try {
          bais.close();
        } catch (IOException e) {

        }
      }
    }
  }

  private static class RodaErrorHandler extends DefaultHandler {
    List<SAXParseException> errors;

    public RodaErrorHandler() {
      errors = new ArrayList<SAXParseException>();
    }

    public void warning(SAXParseException e) throws SAXException {
      errors.add(e);
    }

    public void error(SAXParseException e) throws SAXException {
      errors.add(e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
      errors.add(e);
    }

    public List<SAXParseException> getErrors() {
      return errors;
    }

    public void setErrors(List<SAXParseException> errors) {
      this.errors = errors;
    }

  }

  public static void updateFileFormat(lc.xmlns.premisV2.File file, String formatDesignationName,
    String formatDesignationVersion, String pronom, String mimeType) {

    if (!StringUtils.isBlank(formatDesignationName)) {
      FormatDesignationComplexType fdct = getFormatDesignation(file);
      fdct.setFormatName(formatDesignationName);
    }

    if (!StringUtils.isBlank(formatDesignationVersion)) {
      FormatDesignationComplexType fdct = getFormatDesignation(file);
      fdct.setFormatVersion(formatDesignationVersion);
    }

    if (!StringUtils.isBlank(pronom)) {
      FormatRegistryComplexType frct = getFormatRegistry(file, RodaConstants.PRESERVATION_REGISTRY_PRONOM);
      frct.setFormatRegistryKey(pronom);
    }
    if (!StringUtils.isBlank(mimeType)) {
      FormatRegistryComplexType frct = getFormatRegistry(file, RodaConstants.PRESERVATION_REGISTRY_MIME);
      frct.setFormatRegistryKey(mimeType);
    }

  }

  public static void updateCreatingApplication(lc.xmlns.premisV2.File file, String creatingApplicationName,
    String creatingApplicationVersion, String dateCreatedByApplication) {
    if (!StringUtils.isBlank(creatingApplicationName)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.setCreatingApplicationName(creatingApplicationName);
    }

    if (!StringUtils.isBlank(creatingApplicationVersion)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.setCreatingApplicationVersion(creatingApplicationVersion);
    }

    if (!StringUtils.isBlank(dateCreatedByApplication)) {
      CreatingApplicationComplexType cact = getCreatingApplication(file);
      cact.setDateCreatedByApplication(dateCreatedByApplication);
    }
  }

  private static CreatingApplicationComplexType getCreatingApplication(lc.xmlns.premisV2.File f) {
    ObjectCharacteristicsComplexType occt;
    CreatingApplicationComplexType cact;
    if (f.getObjectCharacteristicsList() != null && f.getObjectCharacteristicsList().size() > 0) {
      occt = f.getObjectCharacteristicsList().get(0);
    } else {
      occt = f.addNewObjectCharacteristics();
    }
    if (occt.getCreatingApplicationList() != null && occt.getCreatingApplicationList().size() > 0) {
      cact = occt.getCreatingApplicationArray(0);
    } else {
      cact = occt.addNewCreatingApplication();
    }
    return cact;
  }

  public static FormatRegistryComplexType getFormatRegistry(lc.xmlns.premisV2.File f, String registryName) {
    ObjectCharacteristicsComplexType occt;
    FormatRegistryComplexType frct = null;
    if (f.getObjectCharacteristicsList() != null && f.getObjectCharacteristicsList().size() > 0) {
      occt = f.getObjectCharacteristicsList().get(0);
    } else {
      occt = f.addNewObjectCharacteristics();
    }
    if (occt.getFormatList() != null && occt.getFormatList().size() > 0) {
      for (FormatComplexType fct : occt.getFormatList()) {
        if (fct.getFormatRegistry() != null) {
          if (fct.getFormatRegistry().getFormatRegistryName().equalsIgnoreCase(registryName)) {
            frct = fct.getFormatRegistry();
            break;
          }
        }
      }
      if (frct == null) {
        FormatComplexType fct = occt.addNewFormat();
        frct = fct.addNewFormatRegistry();
        frct.setFormatRegistryName(registryName);
      }
    } else {
      FormatComplexType fct = occt.addNewFormat();
      frct = fct.addNewFormatRegistry();
      frct.setFormatRegistryName(registryName);
    }
    return frct;
  }

  private static FormatDesignationComplexType getFormatDesignation(lc.xmlns.premisV2.File f) {
    ObjectCharacteristicsComplexType occt;
    FormatComplexType fct;
    FormatDesignationComplexType fdct;
    if (f.getObjectCharacteristicsList() != null && f.getObjectCharacteristicsList().size() > 0) {
      occt = f.getObjectCharacteristicsList().get(0);
    } else {
      occt = f.addNewObjectCharacteristics();
    }
    if (occt.getFormatList() != null && occt.getFormatList().size() > 0) {
      fct = occt.getFormatList().get(0);
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
    List<String> sources, List<String> targets, String outcome, String detailNote, String detailExtension,
    List<IndexedPreservationAgent> agents) throws GenericException, ValidationException {
    EventDocument event = EventDocument.Factory.newInstance();
    EventComplexType ect = event.addNewEvent();
    EventIdentifierComplexType eict = ect.addNewEventIdentifier();
    eict.setEventIdentifierValue(eventID);
    eict.setEventIdentifierType("local");
    ect.setEventDateTime(DateParser.getIsoDate(date));
    ect.setEventType(type);
    ect.setEventDetail(details);
    if (sources != null) {
      for (String source : sources) {
        LinkingObjectIdentifierComplexType loict = ect.addNewLinkingObjectIdentifier();
        loict.setLinkingObjectIdentifierValue(source);
        loict.setLinkingObjectIdentifierType("source");
      }
    }

    if (targets != null) {
      for (String target : targets) {
        LinkingObjectIdentifierComplexType loict = ect.addNewLinkingObjectIdentifier();
        loict.setLinkingObjectIdentifierValue(target);
        loict.setLinkingObjectIdentifierType("target");
      }
    }

    if (agents != null) {
      for (IndexedPreservationAgent agent : agents) {
        LinkingAgentIdentifierComplexType agentIdentifier = ect.addNewLinkingAgentIdentifier();
        agentIdentifier.setLinkingAgentIdentifierType("local");
        agentIdentifier.setLinkingAgentIdentifierValue(agent.getId());
        agentIdentifier.setType("simple");
      }
    }
    EventOutcomeInformationComplexType outcomeInformation = ect.addNewEventOutcomeInformation();
    outcomeInformation.setEventOutcome(outcome);
    EventOutcomeDetailComplexType eodct = outcomeInformation.addNewEventOutcomeDetail();
    eodct.setEventOutcomeDetailNote(detailNote);

    // TODO handle...
    /*
     * if(detailExtension!=null){ ExtensionComplexType extension =
     * eodct.addNewEventOutcomeDetailExtension();
     * extension.set(XmlObject.Factory.newValue("<p>"+detailExtension+"</p>"));
     * }
     */
    return new StringContentPayload(MetadataUtils.saveToString(event, true));

  }

  public static ContentPayload createPremisAgentBinary(String id, String name, String type, String extension,
    String note) throws GenericException, ValidationException {
    AgentDocument agent = AgentDocument.Factory.newInstance();

    AgentComplexType act = agent.addNewAgent();
    AgentIdentifierComplexType agentIdentifier = act.addNewAgentIdentifier();
    agentIdentifier.setAgentIdentifierType("local");
    agentIdentifier.setAgentIdentifierValue(id);

    act.setAgentType(type);

    if (StringUtils.isNotBlank(name)) {
      act.addAgentName(name);
    }

    if (StringUtils.isNotBlank(note)) {
      act.addAgentNote(note);
    }
    if (StringUtils.isNotBlank(extension)) {
      try {
        act.addNewAgentExtension().set(XmlObject.Factory.parse(extension));
      } catch (XmlException e) {
        // e.getError()
        // TODO convert XmlException to a Valiation Exception in MetadataUtils
        throw new ValidationException(e.getMessage());
      }
    }

    return new StringContentPayload(MetadataUtils.saveToString(agent, true));

  }

  public static Representation createBaseRepresentation(String aipID, String representationId)
    throws GenericException, ValidationException {

    Representation representation = Representation.Factory.newInstance();
    ObjectIdentifierComplexType oict = representation.addNewObjectIdentifier();
    oict.setObjectIdentifierType("local");
    String identifier = createPremisRepresentationIdentifier(aipID, representationId);
    oict.setObjectIdentifierValue(identifier);
    representation.addNewPreservationLevel().setPreservationLevelValue("");

    return representation;
  }

  public static ContentPayload createBaseFile(File originalFile, ModelService model) throws GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException, ValidationException {
    ObjectDocument document = ObjectDocument.Factory.newInstance();
    lc.xmlns.premisV2.File file = lc.xmlns.premisV2.File.Factory.newInstance();
    file.addNewPreservationLevel().setPreservationLevelValue(RodaConstants.PRESERVATION_LEVEL_FULL);
    ObjectIdentifierComplexType oict = file.addNewObjectIdentifier();
    String identifier = createPremisFileIdentifier(originalFile);
    oict.setObjectIdentifierValue(identifier);
    oict.setObjectIdentifierType("local");
    ObjectCharacteristicsComplexType occt = file.addNewObjectCharacteristics();
    occt.setCompositionLevel(BigInteger.valueOf(0));
    FormatComplexType fct = occt.addNewFormat();
    FormatDesignationComplexType fdct = fct.addNewFormatDesignation();
    fdct.setFormatName("");
    fdct.setFormatVersion("");
    Binary binary = model.getStorage().getBinary(ModelUtils.getFileStoragePath(originalFile));

    if (binary.getContentDigest() != null && !binary.getContentDigest().isEmpty()) {
      // TODO use binary content digest information
    } else {
      // if binary does not contain digest, create a new one
      try {
        for (String algorithm : MANDATORY_CHECKSUM_ALGORITHMS) {
          // TODO set better originator
          Fixity fixity = calculateFixity(binary, algorithm, "RODA");
          FixityComplexType premis_fixity = occt.addNewFixity();
          premis_fixity.setMessageDigest(fixity.getMessageDigest());
          premis_fixity.setMessageDigestAlgorithm(fixity.getMessageDigestAlgorithm());
          premis_fixity.setMessageDigestOriginator(fixity.getMessageDigestOriginator());
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
    clct.setContentLocationType("");
    clct.setContentLocationValue("");

    document.setObject(file);

    return new StringContentPayload(MetadataUtils.saveToString(document, true));
  }

  public static List<Fixity> extractFixities(Binary premisFile) throws GenericException, XmlException, IOException {
    List<Fixity> fixities = new ArrayList<Fixity>();
    lc.xmlns.premisV2.File f = binaryToFile(premisFile.getContent().createInputStream());
    if (f.getObjectCharacteristicsList() != null && f.getObjectCharacteristicsList().size() > 0) {
      ObjectCharacteristicsComplexType occt = f.getObjectCharacteristicsList().get(0);
      if (occt.getFixityList() != null && occt.getFixityList().size() > 0) {
        for (FixityComplexType fct : occt.getFixityList()) {
          Fixity fix = new Fixity();
          fix.setMessageDigest(fct.getMessageDigest());
          fix.setMessageDigestAlgorithm(fct.getMessageDigestAlgorithm());
          fix.setMessageDigestOriginator(fct.getMessageDigestOriginator());
          fixities.add(fix);
        }
      }
    }
    return fixities;
  }

  public static lc.xmlns.premisV2.Representation binaryToRepresentation(InputStream binaryInputStream)
    throws XmlException, IOException, GenericException {
    ObjectDocument objectDocument = ObjectDocument.Factory.parse(binaryInputStream);

    ObjectComplexType object = objectDocument.getObject();
    if (object instanceof Representation) {
      return (Representation) object;
    } else {
      throw new GenericException("Trying to load a representation but was a " + object.getClass().getSimpleName());
    }
  }

  public static lc.xmlns.premisV2.File binaryToFile(InputStream binaryInputStream)
    throws XmlException, IOException, GenericException {
    ObjectDocument objectDocument = ObjectDocument.Factory.parse(binaryInputStream);

    ObjectComplexType object = objectDocument.getObject();
    if (object instanceof lc.xmlns.premisV2.File) {
      return (lc.xmlns.premisV2.File) object;
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

  public static lc.xmlns.premisV2.Representation binaryToRepresentation(ContentPayload payload, boolean validate)
    throws ValidationException, GenericException {
    Representation representation;
    try {
      representation = binaryToRepresentation(payload.createInputStream());

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

  public static lc.xmlns.premisV2.File binaryToFile(ContentPayload payload, boolean validate)
    throws ValidationException, GenericException {
    lc.xmlns.premisV2.File file;
    List<XmlValidationError> validationErrors = new ArrayList<>();
    try {
      file = binaryToFile(payload.createInputStream());

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

  public static ContentPayload fileToBinary(lc.xmlns.premisV2.File file) throws GenericException, ValidationException {
    ObjectDocument d = ObjectDocument.Factory.newInstance();
    d.setObject(file);
    return new StringContentPayload(MetadataUtils.saveToString(d, true));
  }

  public static ContentPayload representationToBinary(Representation representation)
    throws GenericException, ValidationException {
    ObjectDocument d = ObjectDocument.Factory.newInstance();
    d.setObject(representation);
    return new StringContentPayload(MetadataUtils.saveToString(d, true));
  }

  public static EventComplexType binaryToEvent(ContentPayload payload, boolean validate)
    throws ValidationException, GenericException {
    EventComplexType event;
    try {
      event = binaryToEvent(payload.createInputStream());

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
    try {
      agent = binaryToAgent(payload.createInputStream());

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

  public static SolrInputDocument updateSolrDocument(SolrInputDocument doc, Binary premisBinary)
    throws GenericException {
    try {
      lc.xmlns.premisV2.File premisFile = binaryToFile(premisBinary.getContent().createInputStream());
      if (premisFile.getOriginalName() != null) {
        doc.setField(RodaConstants.FILE_ORIGINALNAME, premisFile.getOriginalName().getStringValue());

        // TODO extension
      }
      if (premisFile.getObjectCharacteristicsList() != null && premisFile.getObjectCharacteristicsList().size() > 0) {
        ObjectCharacteristicsComplexType occt = premisFile.getObjectCharacteristicsList().get(0);
        doc.setField(RodaConstants.FILE_SIZE, occt.getSize());
        if (occt.getFixityList() != null && occt.getFixityList().size() > 0) {
          List<String> hashes = new ArrayList<>();
          for (FixityComplexType fct : occt.getFixityList()) {
            StringBuilder fixityPrint = new StringBuilder();
            fixityPrint.append(fct.getMessageDigest());
            fixityPrint.append(" (");
            fixityPrint.append(fct.getMessageDigestAlgorithm());
            if (StringUtils.isNotBlank(fct.getMessageDigestOriginator())) {
              fixityPrint.append(", "); //
              fixityPrint.append(fct.getMessageDigestOriginator());
            }
            fixityPrint.append(")");
            hashes.add(fixityPrint.toString());
          }
          doc.addField(RodaConstants.FILE_HASH, hashes);
        }
        if (occt.getFormatList() != null && occt.getFormatList().size() > 0) {
          FormatComplexType fct = occt.getFormatList().get(0);
          if (fct.getFormatDesignation() != null) {
            doc.addField(RodaConstants.FILE_FILEFORMAT, fct.getFormatDesignation().getFormatName());
            doc.addField(RodaConstants.FILE_FORMAT_VERSION, fct.getFormatDesignation().getFormatVersion());
          }

          FormatRegistryComplexType pronomRegistry = getFormatRegistry(premisFile,
            RodaConstants.PRESERVATION_REGISTRY_PRONOM);
          if (pronomRegistry != null) {
            doc.addField(RodaConstants.FILE_PRONOM, pronomRegistry.getFormatRegistryKey());
          }
          FormatRegistryComplexType mimeRegistry = getFormatRegistry(premisFile,
            RodaConstants.PRESERVATION_REGISTRY_MIME);
          if (mimeRegistry != null) {
            doc.addField(RodaConstants.FILE_FORMAT_MIMETYPE, mimeRegistry.getFormatRegistryKey());
          }
          // TODO extension
        }
        if (occt.getCreatingApplicationList() != null && occt.getCreatingApplicationList().size() > 0) {
          CreatingApplicationComplexType cact = occt.getCreatingApplicationList().get(0);
          doc.addField(RodaConstants.FILE_CREATING_APPLICATION_NAME, cact.getCreatingApplicationName());
          doc.addField(RodaConstants.FILE_CREATING_APPLICATION_VERSION, cact.getCreatingApplicationVersion());
          doc.addField(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION, cact.getDateCreatedByApplication());
        }
      }

    } catch (XmlException | IOException e) {

    }
    return doc;
  }

  public static IndexedPreservationAgent createPremisAgentBinary(Plugin<?> plugin,
    String preservationAgentTypeCharacterizationPlugin, ModelService model, boolean notify)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
    String id = plugin.getClass().getName() + "@" + plugin.getVersion();
    ContentPayload agentPayload;

    // TODO set agent extension
    agentPayload = PremisUtils.createPremisAgentBinary(id, plugin.getName(),
      RodaConstants.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN, "", plugin.getDescription());
    model.createPreservationMetadata(PreservationMetadataType.AGENT, id, agentPayload, notify);
    IndexedPreservationAgent agent = getPreservationAgent(plugin, preservationAgentTypeCharacterizationPlugin, model);
    return agent;
  }

  public static IndexedPreservationAgent getPreservationAgent(Plugin<?> plugin,
    String preservationAgentTypeCharacterizationPlugin, ModelService model) {
    String id = plugin.getClass().getName() + "@" + plugin.getVersion();
    IndexedPreservationAgent agent = new IndexedPreservationAgent();
    agent.setId(id);
    agent.setName(plugin.getName());
    return agent;
  }

  public static void linkFileToRepresentation(File file, String relationshipType, String relationshipSubType,
    Representation r) throws GenericException, RequestNotValidException, NotFoundException,
      AuthorizationDeniedException, XmlException, IOException, ValidationException {

    RelationshipComplexType relationship = r.addNewRelationship();
    relationship.setRelationshipType(relationshipType);
    relationship.setRelationshipSubType(relationshipSubType);
    RelatedObjectIdentificationComplexType roict = relationship.addNewRelatedObjectIdentification();
    roict.setRelatedObjectIdentifierType(RodaConstants.PREMIS_IDENTIFIER_TYPE_LOCAL);
    roict.setRelatedObjectIdentifierValue(createPremisFileIdentifier(file));
  }

  public static String createPremisRepresentationIdentifier(String aipId, String representationId) {
    return aipId + SEPARATOR + representationId;
  }

  public static String createPremisFileIdentifier(File f) {
    String identifier = createPremisRepresentationIdentifier(f.getAipId(), f.getRepresentationId());
    if (f.getPath() != null && f.getPath().size() > 0) {
      identifier += SEPARATOR;
      identifier += StringUtils.join(f.getPath(), SEPARATOR);
    }
    identifier += SEPARATOR;
    identifier += f.getId();
    return identifier;
  }

}
