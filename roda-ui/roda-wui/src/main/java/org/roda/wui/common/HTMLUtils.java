/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.storage.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTML related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public final class HTMLUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(HTMLUtils.class);

  /** Private empty constructor */
  private HTMLUtils() {

  }

  public static String descriptiveMetadataToHtml(Binary binary, String descriptiveMetadataType,
    String descriptiveMetadataVersion, final Locale locale) throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    Map<String, Object> translations;
    if (descriptiveMetadataType != null) {
      String lowerCaseDescriptiveMetadataType = descriptiveMetadataType.toLowerCase();
      if (descriptiveMetadataVersion != null) {
        lowerCaseDescriptiveMetadataType += RodaConstants.METADATA_VERSION_SEPARATOR + descriptiveMetadataVersion;
      }
      translations = messages.getTranslations(
        RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + lowerCaseDescriptiveMetadataType, Object.class, true);
    } else {
      translations = new HashMap<>();
    }

    return binaryToHtml(binary, descriptiveMetadataType, descriptiveMetadataVersion, translations);
  }

  public static String preservationObjectToHtml(Binary binary, final Locale locale) throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    Map<String, Object> stylesheetOpt = messages.getTranslations(
      RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + binary.getStoragePath().getName(), Object.class, true);
    return binaryToHtml(binary, "premis", null, stylesheetOpt);
  }

  private static String binaryToHtml(Binary binary, String metadataType, String metadataVersion,
    Map<String, Object> stylesheetOpt) throws GenericException {
    try {
      Reader reader = new InputStreamReader(binary.getContent().createInputStream());
      return fileToHtml(reader, metadataType, metadataVersion, stylesheetOpt);
    } catch (TransformerException | IOException e) {
      LOGGER.error("Error transforming binary into HTML (type=" + metadataType + ")", e);
      throw new GenericException("Error transforming binary into HTML (type=" + metadataType + ")", e);
    }
  }

  private static String fileToHtml(Reader reader, String metadataType, String metadataVersion,
    Map<String, Object> stylesheetOpt) throws GenericException, IOException, TransformerException {

    InputStream transformerStream = getStylesheetInputStream(RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
      metadataType, metadataVersion);
    // TODO support the use of scripts for non-xml transformers
    Reader xsltReader = new InputStreamReader(transformerStream);
    CharArrayWriter transformerResult = new CharArrayWriter();
    RodaUtils.applyStylesheet(xsltReader, reader, stylesheetOpt, transformerResult);
    reader.close();

    return transformerResult.toString();
  }

  private static InputStream getStylesheetInputStream(String xsltFolder, String metadataType, String metadataVersion) {
    InputStream transformerStream = null;
    if (metadataType != null) {
      String lowerCaseMetadataType = metadataType.toLowerCase();
      if (metadataVersion != null) {
        String lowerCaseMetadataTypeWithVersion = lowerCaseMetadataType + RodaConstants.METADATA_VERSION_SEPARATOR + metadataVersion;
        transformerStream = RodaCoreFactory
          .getConfigurationFileAsStream("crosswalks/ingest/" + lowerCaseMetadataTypeWithVersion + ".xslt");
      }
      if (transformerStream == null) {
        transformerStream = RodaCoreFactory
          .getConfigurationFileAsStream("crosswalks/ingest/" + lowerCaseMetadataType + ".xslt");
      }
    }

    if (transformerStream == null) {
      transformerStream = RodaCoreFactory.getConfigurationFileAsStream(xsltFolder + "/plain.xslt");
    }
    return transformerStream;
  }
}
