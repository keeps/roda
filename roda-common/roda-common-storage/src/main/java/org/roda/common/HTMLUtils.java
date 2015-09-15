package org.roda.common;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.PreservationMetadata;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;

import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.File;
import lc.xmlns.premisV2.Representation;
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
    Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
    stylesheetOpt.put("title", binary.getStoragePath().getName());
    return binaryToHtml(binary, locale, true, null, stylesheetOpt);
  }

  public static String preservationObjectToHtml(Binary binary, final Locale locale) throws ModelServiceException {
    Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
    stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
    return binaryToHtml(binary, locale, false, "premis", stylesheetOpt);
  }

  public static String aipPremisToHTML2(AIP aip, ModelService model, StorageService storage, Locale locale)
    throws ModelServiceException, StorageServiceException {

    return null;
  }

  // FIXME close properly model related methods that may generate file
  // descriptor leaks
  public static String aipPremisToHTML(AIP aip, ModelService model, StorageService storage, Locale locale)
    throws ModelServiceException, StorageServiceException {
    TreeSet<String> agentsID = null;
    StringBuffer s = new StringBuffer();
    s.append("<span class='representations'>");
    if (aip.getRepresentationIds() != null && aip.getRepresentationIds().size() > 0) {
      for (String representationId : aip.getRepresentationIds()) {
        ClosableIterable<PreservationMetadata> preservationMetadata = model
          .listPreservationMetadataBinaries(aip.getId(), representationId);
        s.append(representationPremisToHtml(preservationMetadata, storage, locale));
      }
      agentsID = new TreeSet<String>();
      for (String representationId : aip.getRepresentationIds()) {
        ClosableIterable<PreservationMetadata> preservationMetadata = model
          .listPreservationMetadataBinaries(aip.getId(), representationId);
        agentsID.addAll(extractAgents(preservationMetadata, storage));
      }
    }
    s.append("</span>");
    return s.toString();
  }

  private static String representationPremisToHtml(ClosableIterable<PreservationMetadata> preservationMetadata,
    StorageService storage, final Locale locale) throws ModelServiceException, StorageServiceException {
    Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
    stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);

    List<Binary> events = new ArrayList<Binary>();
    List<Binary> files = new ArrayList<Binary>();
    List<Binary> agents = new ArrayList<Binary>();
    Binary representation = null;

    Iterator<PreservationMetadata> iterator = preservationMetadata.iterator();
    while (iterator.hasNext()) {
      PreservationMetadata pm = iterator.next();
      Binary b = storage.getBinary(pm.getStoragePath());
      if (ModelUtils.isPreservationEvent(b)) {
        events.add(b);
      } else if (ModelUtils.isPreservationFileObject(b)) {
        files.add(b);
      } else if (ModelUtils.isPreservationAgentObject(b)) {
        agents.add(b);
      } else {
        representation = b;
      }
    }

    events = ModelUtils.sortEventsByDate(events);
    files = ModelUtils.sortFilesByRepresentationOrder(representation, files);

    List<String> htmlEvents = new ArrayList<String>();
    for (Binary event : events) {
      String html = preservationObjectToHtml(event, locale);
      htmlEvents.add(html);
    }
    stylesheetOpt.put("events", htmlEvents);

    List<String> htmlFiles = new ArrayList<String>();
    for (Binary file : files) {
      String html = preservationObjectToHtml(file, locale);
      htmlFiles.add(html);
    }
    stylesheetOpt.put("files", htmlFiles);

    List<String> htmlAgents = new ArrayList<String>();
    for (Binary agent : agents) {
      String html = preservationObjectToHtml(agent, locale);
      htmlAgents.add(html);
    }
    stylesheetOpt.put("agents", htmlAgents);

    return binaryToHtml(representation, locale, false, "premis", stylesheetOpt);
  }

  public static TreeSet<String> extractAgents(ClosableIterable<PreservationMetadata> preservationMetadata,
    StorageService storage) throws ModelServiceException, StorageServiceException {
    Iterator<PreservationMetadata> iterator = preservationMetadata.iterator();
    TreeSet<String> agentsIDs = new TreeSet<String>();
    while (iterator.hasNext()) {
      PreservationMetadata pm = iterator.next();
      Binary b = storage.getBinary(pm.getStoragePath());
      if (ModelUtils.isPreservationEvent(b)) {
        agentsIDs.addAll(ModelUtils.extractAgentIdsFromPreservationBinary(b, EventComplexType.class));
      } else if (ModelUtils.isPreservationFileObject(b)) {
        agentsIDs.addAll(ModelUtils.extractAgentIdsFromPreservationBinary(b, File.class));
      } else {
        agentsIDs.addAll(ModelUtils.extractAgentIdsFromPreservationBinary(b, Representation.class));
      }
    }
    return agentsIDs;
  }

  private static String binaryToHtml(Binary binary, final Locale locale, boolean useFilename,
    String alternativeFilenameToUse, Map<String, Object> stylesheetOpt) throws ModelServiceException {
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
