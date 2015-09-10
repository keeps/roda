package org.roda.common;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.roda.index.utils.SolrUtils;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;

import pt.gov.dgarq.roda.core.common.RodaConstants;

/**
 * HTML related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public final class HTMLUtils {
  private static final Logger LOGGER = Logger.getLogger(HTMLUtils.class);

  /** Private empty constructor */
  private HTMLUtils() {

  }

  public static String descriptiveMetadataToHtml(Binary binary, final Locale locale) throws ModelServiceException {
    Map<String, String> stylesheetOpt = new HashMap<String, String>();
    stylesheetOpt.put("title", binary.getStoragePath().getName());
    return binaryToHtml(binary, locale, true, null, stylesheetOpt);
  }

  // TODO: improve html.premis.xml.xslt
  public static String preservationObjectToHtml(Binary binary, final Locale locale) throws ModelServiceException {
    Map<String, String> stylesheetOpt = new HashMap<String, String>();
    stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
    return binaryToHtml(binary, locale, false, "premis", stylesheetOpt);
  }

  private static String binaryToHtml(Binary binary, final Locale locale, boolean useFilename,
    String alternativeFilenameToUse, Map<String, String> stylesheetOpt) throws ModelServiceException {
    String filename;
    if (useFilename) {
      filename = binary.getStoragePath().getName();
    } else {
      filename = alternativeFilenameToUse;
    }
    try {
      Locale properLocale = locale;
      if (locale == null) {
        properLocale = new Locale("pt", "PT");
      }
      InputStream inputStream = binary.getContent().createInputStream();
      Reader reader = new InputStreamReader(inputStream);

      InputStream transformerStream = getStylesheetInputStream("htmlXSLT", properLocale, filename);
      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      RodaUtils.applyStylesheet(xsltReader, reader, stylesheetOpt, transformerResult);
      reader.close();
      return transformerResult.toString();
    } catch (TransformerException | IOException e) {
      LOGGER.error("Error transforming binary file into HTML (filename=" + filename + ")", e);
      throw new ModelServiceException("Error transforming binary file into HTML (filename=" + filename + ")",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  private static InputStream getStylesheetInputStream(String xsltFolder, Locale locale, String filename) {
    // FIXME this should be loaded from config folder (to be dynamic)
    ClassLoader classLoader = SolrUtils.class.getClassLoader();
    InputStream transformerStream = classLoader.getResourceAsStream(
      xsltFolder + "/" + locale.getLanguage() + "/" + locale.getCountry() + "/" + filename + ".xslt");
    if (transformerStream == null) {
      transformerStream = classLoader
        .getResourceAsStream(xsltFolder + "/" + locale.getLanguage() + "/" + filename + ".xslt");
      if (transformerStream == null) {
        LOGGER.warn("Didn't found proper stylesheet for dealing with file \"" + filename + "\" in the locale \""
          + locale + "\". Using " + xsltFolder + "/" + locale.getLanguage() + "/plain.xslt");
        transformerStream = classLoader.getResourceAsStream(xsltFolder + "/" + locale.getLanguage() + "/plain.xslt");
      }
    }
    return transformerStream;
  }

}
