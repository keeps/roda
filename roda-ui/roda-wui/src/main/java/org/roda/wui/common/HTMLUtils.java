/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.Pair;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
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

  public static String descriptiveMetadataToHtml(Binary binary, String descriptiveMetadataType, final Locale locale)
    throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    Map<String, Object> translations;
    if (descriptiveMetadataType != null) {
      String lowerCaseDescriptiveMetadataType = descriptiveMetadataType.toLowerCase();
      translations = messages.getTranslations(
        RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + lowerCaseDescriptiveMetadataType, Object.class, true);
    } else {
      translations = new HashMap<>();
    }

    return binaryToHtml(binary, descriptiveMetadataType, translations);
  }

  public static String preservationObjectToHtml(Binary binary, final Locale locale) throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    Map<String, Object> stylesheetOpt = messages.getTranslations(
      RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + binary.getStoragePath().getName(), Object.class, true);
    return binaryToHtml(binary, "premis", stylesheetOpt);
  }

  private static String binaryToHtml(Binary binary, String metadataType, Map<String, Object> stylesheetOpt)
    throws GenericException {
    try {
      Reader reader = new InputStreamReader(binary.getContent().createInputStream());
      return fileToHtml(reader, metadataType, stylesheetOpt);
    } catch (TransformerException | IOException e) {
      LOGGER.error("Error transforming binary into HTML (type=" + metadataType + ")", e);
      throw new GenericException("Error transforming binary into HTML (type=" + metadataType + ")", e);
    }
  }

  private static String fileToHtml(File file, String metadataType, Map<String, Object> stylesheetOpt)
    throws GenericException {
    try {
      Reader reader = new InputStreamReader(new FileInputStream(file));
      return fileToHtml(reader, metadataType, stylesheetOpt);
    } catch (TransformerException | IOException e) {
      LOGGER.error("Error transforming file into HTML (type=" + metadataType + ")", e);

      throw new GenericException("Error transforming file into HTML (type=" + metadataType + ")", e);
    }
  }

  private static String fileToHtml(Reader reader, String metadataType, Map<String, Object> stylesheetOpt)
    throws GenericException, IOException, TransformerException {

    InputStream transformerStream = getStylesheetInputStream(RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
      metadataType);
    // TODO support the use of scripts for non-xml transformers
    Reader xsltReader = new InputStreamReader(transformerStream);
    CharArrayWriter transformerResult = new CharArrayWriter();
    RodaUtils.applyStylesheet(xsltReader, reader, stylesheetOpt, transformerResult);
    reader.close();

    return transformerResult.toString();
  }

  private static InputStream getStylesheetInputStream(String xsltFolder, String metadataType) {
    InputStream transformerStream = null;
    if (metadataType != null) {
      String lowerCaseMetadataType = metadataType.toLowerCase();
      transformerStream = RodaCoreFactory
        .getConfigurationFileAsStream(xsltFolder + "/" + lowerCaseMetadataType + ".xslt");
    }

    if (transformerStream == null) {
      transformerStream = RodaCoreFactory.getConfigurationFileAsStream(xsltFolder + "/plain.xslt");
    }
    return transformerStream;
  }
}
