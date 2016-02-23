/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ValidationUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);
  private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

  /** Private empty constructor */
  private ValidationUtils() {

  }

  public static ValidationReport isAIPMetadataValid(boolean forceDescriptiveMetadataType,
    boolean validateDescriptiveMetadata, String fallbackMetadataType, boolean validatePremis, ModelService model,
    String aipId) throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException,
      ValidationException {
    ValidationReport report = new ValidationReport();
    report.setValid(false);
    List<DescriptiveMetadata> descriptiveMetadata = model.retrieveAIP(aipId).getDescriptiveMetadata();
    for (DescriptiveMetadata dm : descriptiveMetadata) {
      StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(dm);
      Binary binary = model.getStorage().getBinary(storagePath);
      if (forceDescriptiveMetadataType) {
        if (validateDescriptiveMetadata) {
          ValidationReport dmReport = validateDescriptiveBinary(binary.getContent(), fallbackMetadataType, true);
          report.setValid(report.isValid() && dmReport.isValid());
          report.getIssues().addAll(dmReport.getIssues());
        }
        // XXX review why should a validation method update data
        String message = "Forcing metadata type to " + fallbackMetadataType;
        model.updateDescriptiveMetadata(aipId, dm.getId(), binary.getContent(), fallbackMetadataType, message);
        report.setValid(true);
        LOGGER.debug(storagePath + " valid for metadata type " + fallbackMetadataType);

      } else if (validateDescriptiveMetadata) {
        String metadataType = dm.getType() != null ? dm.getType() : fallbackMetadataType;
        ValidationReport dmReport = validateDescriptiveBinary(binary.getContent(), metadataType, true);
        report.setValid(report.isValid() && dmReport.isValid());
        report.getIssues().addAll(dmReport.getIssues());
      }

    }

    // TODO handle premis...
    return report;

  }

  /**
   * Validates all descriptive metadata files contained in the AIP
   * 
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws GenericException
   * 
   * @throws ValidationException
   */
  public static ValidationReport isAIPDescriptiveMetadataValid(ModelService model, String aipId, boolean failIfNoSchema)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    boolean valid = true;
    List<ValidationIssue> issues = new ArrayList<>();
    List<DescriptiveMetadata> descriptiveMetadata = model.retrieveAIP(aipId).getDescriptiveMetadata();
    for (DescriptiveMetadata dm : descriptiveMetadata) {
      ValidationReport report = isDescriptiveMetadataValid(model, dm, failIfNoSchema);
      valid &= report.isValid();
      issues.addAll(report.getIssues());
    }

    ValidationReport ret = new ValidationReport();
    ret.setValid(valid);
    ret.setIssues(issues);
    return ret;
  }

  /**
   * Validates all preservation metadata files contained in the AIP
   * 
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   * 
   * @throws ValidationException
   */
  public static boolean isAIPPreservationMetadataValid(ModelService model, String aipId, boolean failIfNoSchema)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    // TODO !!!!
    return true;
  }

  /**
   * Validates descriptive medatada (e.g. against its schema, but other
   * strategies may be used)
   * 
   * @param failIfNoSchema
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws ValidationException
   */
  public static ValidationReport isDescriptiveMetadataValid(ModelService model, DescriptiveMetadata metadata,
    boolean failIfNoSchema)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ValidationReport ret;
    if (metadata != null) {
      StoragePath storagePath = ModelUtils.getDescriptiveMetadataPath(metadata.getAipId(), metadata.getId());
      Binary binary = model.getStorage().getBinary(storagePath);
      ret = validateDescriptiveBinary(binary.getContent(), metadata.getType(), failIfNoSchema);
    } else {
      ret = new ValidationReport();
      ret.setValid(false);
      ret.setMessage("Metadata is NULL");
    }

    return ret;
  }

  private static ValidationIssue convertSAXParseException(SAXParseException e) {
    ValidationIssue issue = new ValidationIssue();
    issue.setMessage(e.getMessage());
    issue.setLineNumber(e.getLineNumber());
    issue.setColumnNumber(e.getColumnNumber());

    return issue;
  }

  /**
   * Validates preservation medatada (e.g. against its schema, but other
   * strategies may be used)
   * 
   * @param failIfNoSchema
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws ValidationException
   */
  public static ValidationReport isPreservationMetadataValid(ModelService model, PreservationMetadata metadata,
    boolean failIfNoSchema)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(metadata);
    Binary binary = model.getStorage().getBinary(storagePath);
    return validatePreservationBinary(binary, failIfNoSchema);
  }

  /**
   * Validates descriptive medatada (e.g. against its schema, but other
   * strategies may be used)
   * 
   * @param descriptiveMetadataType
   * 
   * @param failIfNoSchema
   * @throws ValidationException
   */
  public static ValidationReport validateDescriptiveBinary(ContentPayload descriptiveMetadataPayload,
    String descriptiveMetadataType, boolean failIfNoSchema) {
    ValidationReport ret = new ValidationReport();
    InputStream inputStream = null;
    InputStream schemaStream = null;
    try {
      inputStream = descriptiveMetadataPayload.createInputStream();

      if (descriptiveMetadataType != null) {
        schemaStream = RodaCoreFactory
          .getConfigurationFileAsStream("schemas/" + descriptiveMetadataType.toLowerCase() + ".xsd");
      }

      if (schemaStream != null) {
        Source xmlFile = new StreamSource(inputStream);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
        Validator validator = schema.newValidator();
        RodaErrorHandler errorHandler = new RodaErrorHandler();
        validator.setErrorHandler(errorHandler);
        try {
          validator.validate(xmlFile);
          ret.setValid(errorHandler.getErrors().isEmpty());
          for (SAXParseException saxParseException : errorHandler.getErrors()) {
            ret.addIssue(convertSAXParseException(saxParseException));
          }
        } catch (SAXException e) {
          LOGGER.error("Error validating descriptive binary " + descriptiveMetadataType, e);
          ret.setValid(false);
          for (SAXParseException saxParseException : errorHandler.getErrors()) {
            ret.addIssue(convertSAXParseException(saxParseException));
          }
        }
      } else {
        if (failIfNoSchema) {
          ret.setValid(false);
          ret.setMessage("No schema to validate " + descriptiveMetadataType);
        }
      }
    } catch (SAXException | IOException e) {
      LOGGER.error("Error validating descriptive metadata", e);
      ret.setValid(false);
      ret.setMessage(e.getMessage());
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(schemaStream);
    }

    return ret;

  }

  /**
   * Validates preservation medatada (e.g. against its schema, but other
   * strategies may be used)
   * 
   * @param failIfNoSchema
   * 
   * @param descriptiveMetadataId
   * 
   * @param failIfNoSchema
   * @throws ValidationException
   */
  public static ValidationReport validatePreservationBinary(Binary binary, boolean failIfNoSchema) {
    ValidationReport report = new ValidationReport();
    try {
      InputStream inputStream = binary.getContent().createInputStream();
      InputStream schemaStream = RodaCoreFactory.getConfigurationFileAsStream("schemas/premis-v2-0.xsd");
      if (schemaStream != null) {
        Source xmlFile = new StreamSource(inputStream);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
        Validator validator = schema.newValidator();
        RodaErrorHandler errorHandler = new RodaErrorHandler();
        validator.setErrorHandler(errorHandler);
        try {
          validator.validate(xmlFile);
          report.setValid(errorHandler.getErrors().isEmpty());
          for (SAXParseException saxParseException : errorHandler.getErrors()) {
            report.addIssue(convertSAXParseException(saxParseException));
          }
        } catch (SAXException e) {
          LOGGER.error("Error validating preservation binary " + binary.getStoragePath().asString(), e);
          report.setValid(false);
          for (SAXParseException saxParseException : errorHandler.getErrors()) {
            report.addIssue(convertSAXParseException(saxParseException));
          }

        }
      } else if (failIfNoSchema) {
        report.setValid(false);
        report.setMessage("No schema to validate PREMIS");
      }

      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(schemaStream);
    } catch (SAXException | IOException e) {
      report.setValid(false);
      report.setMessage(e.getMessage());
    }
    return report;

  }

  private static class RodaErrorHandler extends DefaultHandler {
    List<SAXParseException> errors;

    public RodaErrorHandler() {
      errors = new ArrayList<SAXParseException>();
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
      errors.add(e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
      errors.add(e);
    }

    @Override
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
