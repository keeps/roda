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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.FileFormat;
import org.roda.core.data.v2.Fixity;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.File;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class PremisUtils {
  private final static Logger logger = LoggerFactory.getLogger(PremisUtils.class);
  private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

  public static Fixity calculateFixity(Binary binary, String digestAlgorithm, String originator)
    throws IOException, NoSuchAlgorithmException {
    InputStream dsInputStream = binary.getContent().createInputStream();
    Fixity fixity = new Fixity(digestAlgorithm, FileUtility.calculateChecksumInHex(dsInputStream, digestAlgorithm),
      originator);
    dsInputStream.close();
    return fixity;
  }

  public static RepresentationFilePreservationObject createPremisFromFile(File file, Binary binaryFile,
    String originator) throws IOException, PremisMetadataException {
    RepresentationFilePreservationObject pObjectFile = new RepresentationFilePreservationObject();
    pObjectFile.setId(file.getId());
    pObjectFile.setPreservationLevel(RepresentationFilePreservationObject.PRESERVATION_LEVEL_FULL);
    pObjectFile.setCompositionLevel(0);
    try {
      Fixity[] fixities = new Fixity[2];
      fixities[0] = calculateFixity(binaryFile, "MD5", originator);
      fixities[1] = calculateFixity(binaryFile, "SHA-1", originator);
      pObjectFile.setFixities(fixities);
    } catch (NoSuchAlgorithmException e) {
      logger.error("Error calculating datastream checksum - " + e.getMessage(), e);
      throw new PremisMetadataException("Error calculating datastream checksum - " + e.getMessage(), e);
    } catch (IOException e) {
      logger.error("Error calculating datastream checksum - " + e.getMessage(), e);
      throw new PremisMetadataException("Error calculating datastream checksum - " + e.getMessage(), e);
    }

    pObjectFile.setSize(binaryFile.getSizeInBytes());
    pObjectFile.setObjectCharacteristicsExtension("");
    pObjectFile.setOriginalName(binaryFile.getStoragePath().getName());
    pObjectFile.setContentLocationType("");
    pObjectFile.setContentLocationValue("");
    pObjectFile.setFormatDesignationName("");
    return pObjectFile;
  }

  public static RepresentationFilePreservationObject addFormatToPremis(RepresentationFilePreservationObject pObjectFile,
    FileFormat format) throws IOException, PremisMetadataException {
    pObjectFile.setFormatDesignationName(format.getMimetype());
    pObjectFile.setFormatRegistryName("pronom");
    pObjectFile.setFormatRegistryKey(format.getPuid());
    return pObjectFile;
  }

  public static RepresentationFilePreservationObject getPremisFile(StorageService storage, String aipID,
    String representationID, String fileID) throws StorageServiceException, IOException, PremisMetadataException {
    Binary binary = storage.getBinary(ModelUtils.getPreservationFilePath(aipID, representationID, fileID));
    Path p = Files.createTempFile("temp", ".premis.xml");
    Files.copy(binary.getContent().createInputStream(), p, StandardCopyOption.REPLACE_EXISTING);
    return PremisFileObjectHelper.newInstance(Files.newInputStream(p)).getRepresentationFilePreservationObject();
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

  public static Binary updatePremisToV3IfNeeded(Binary binary, Path configBasePath)
    throws IOException, SAXException, StorageServiceException, TransformerException {
    if (isPremisV2(binary, configBasePath)) {
      logger.debug("Binary " + binary.getStoragePath().asString() + " is Premis V2... Needs updated...");
      return updatePremisV2toV3(binary, configBasePath);
    } else {
      return binary;
    }

  }

  private static Binary updatePremisV2toV3(Binary binary, Path configBasePath)
    throws IOException, TransformerException, StorageServiceException {
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
    } catch (StorageServiceException e) {
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

  public static RepresentationFilePreservationObject updatePremisFile(RepresentationFilePreservationObject premisFile,
    String toolName, Binary toolOutput) {
    // TODO Update RepresentationFilePreservationObject with tool output

    return premisFile;
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
}
