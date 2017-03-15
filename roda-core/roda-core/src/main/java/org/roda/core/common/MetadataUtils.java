/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.InputStreamContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an utility class for metadata helpers.
 * 
 * @author Luis Faria <lfaria@keep.pt>
 */
public final class MetadataUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataUtils.class);

  /** Private empty constructor */
  private MetadataUtils() {

  }

  /**
   * Saves the current XML object to a byte array.
   * 
   * @param xmlObject
   * 
   * @return a <code>byte[]</code> with the contents of the XML file.
   * @throws ValidationException
   * @throws GenericException
   * 
   * @throws MetadataException
   *           if the XML object is not valid or if something goes wrong with
   *           the serialisation.
   */
  public static byte[] saveToByteArray(XmlObject xmlObject) throws GenericException, ValidationException {
    return saveToByteArray(xmlObject, true);
  }

  /**
   * Saves the current XML object to a byte array.
   * 
   * @param xmlObject
   * @param writeXMLDeclaration
   * 
   * @return a <code>byte[]</code> with the contents of the XML file.
   * @throws ValidationException
   * @throws GenericException
   * 
   */
  public static byte[] saveToByteArray(XmlObject xmlObject, boolean writeXMLDeclaration)
    throws GenericException, ValidationException {

    // Save the xml object to a byte array
    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

    saveToOutputStream(xmlObject, byteArrayStream, writeXMLDeclaration);

    return byteArrayStream.toByteArray();
  }

  public static String saveToString(XmlObject xmlObject, boolean writeXMLDeclaration)
    throws GenericException, ValidationException {

    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
    saveToOutputStream(xmlObject, byteArrayStream, writeXMLDeclaration);

    return byteArrayStream.toString();
  }

  public static ContentPayload saveToContentPayload(final XmlObject xmlObject, final boolean writeXMLDeclaration) {
    return new InputStreamContentPayload(new ProvidesInputStream() {

      @Override
      public InputStream createInputStream() throws IOException {
        try {
          return MetadataUtils.createInputStream(xmlObject, writeXMLDeclaration);
        } catch (GenericException | ValidationException e) {
          throw new IOException(e);
        }
      }
    });
  }

  /**
   * Saves the current XML object to a {@link File}.
   * 
   * @param xmlObject
   *          the XML object to save.
   * @param file
   *          the {@link File}.
   * 
   * @throws MetadataException
   *           if the XML object is not valid or if something goes wrong with
   *           the serialisation.
   * 
   * @throws FileNotFoundException
   *           if the specified {@link File} couldn't be opened.
   * @throws IOException
   *           if {@link FileOutputStream} associated with the {@link File}
   *           couldn't be closed.
   * @throws ValidationException
   * @throws GenericException
   */
  public static void saveToFile(XmlObject xmlObject, Path path)
    throws IOException, GenericException, ValidationException {
    OutputStream outputStream = Files.newOutputStream(path);
    saveToOutputStream(xmlObject, outputStream, true);
    outputStream.close();
  }

  /**
   * Saves the current XML object to an {@link OutputStream}.
   * 
   * @param xmlObject
   *          the XML object to save.
   * @param outputStream
   *          the {@link OutputStream}.
   * @param writeXMLDeclaration
   * 
   * @throws GenericException
   * @throws ValidationException
   */
  public static void saveToOutputStream(XmlObject xmlObject, OutputStream outputStream, boolean writeXMLDeclaration)
    throws GenericException, ValidationException {

    LOGGER.trace("Serializing XML Object {}", xmlObject);

    // Create an XmlOptions instance and set the error listener.
    XmlOptions validateOptions = new XmlOptions();
    List<XmlValidationError> errorList = new ArrayList<>();
    validateOptions.setErrorListener(errorList);

    // Validate the XML.
    boolean isValid = xmlObject.validate(validateOptions);
    if (isValid) {

      try {

        XmlOptions xmlSaveOptions = new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint()
          .setSaveAggressiveNamespaces();

        if (!writeXMLDeclaration) {
          xmlSaveOptions = xmlSaveOptions.setSaveNoXmlDecl();
        }

        xmlObject.save(outputStream, xmlSaveOptions);

      } catch (IOException e) {
        LOGGER.debug("Error serializing XML object - " + e.getMessage(), e);
        throw new GenericException("Error serializing XML object", e);
      }

    } else {
      throw new ValidationException(xmlValidationErrorsToValidationReport(errorList));
    }
  }

  public static InputStream createInputStream(XmlObject xmlObject, boolean writeXMLDeclaration)
    throws GenericException, ValidationException {

    // Create an XmlOptions instance and set the error listener.
    XmlOptions validateOptions = new XmlOptions();
    List<XmlValidationError> errorList = new ArrayList<>();
    validateOptions.setErrorListener(errorList);

    // Validate the XML.
    boolean isValid = xmlObject.validate(validateOptions);
    if (isValid) {

      XmlOptions xmlSaveOptions = new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint()
        .setSaveAggressiveNamespaces();

      if (!writeXMLDeclaration) {
        xmlSaveOptions = xmlSaveOptions.setSaveNoXmlDecl();
      }

      return xmlObject.newInputStream(xmlSaveOptions);

    } else {
      throw new ValidationException(xmlValidationErrorsToValidationReport(errorList));
    }
  }

  public static ValidationReport xmlValidationErrorsToValidationReport(List<XmlValidationError> validationErrors) {
    ValidationReport report = new ValidationReport();
    report.setValid(false);
    List<ValidationIssue> issues = new ArrayList<>();
    for (XmlValidationError error : validationErrors) {
      ValidationIssue issue = new ValidationIssue();
      issue.setMessage(error.getMessage());
      issue.setColumnNumber(error.getColumn());
      issue.setLineNumber(error.getLine());
      issues.add(issue);
    }
    report.setIssues(issues);
    return report;
  }

}
