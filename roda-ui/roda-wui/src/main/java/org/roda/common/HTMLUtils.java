package org.roda.common;

import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.roda.core.common.Pair;
import org.roda.core.common.RodaConstants;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.PreservationMetadata;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Resource;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;
import org.roda.wui.dissemination.browse.client.PreservationMetadataBundle;
import org.roda.wui.dissemination.browse.client.RepresentationPreservationMetadataBundle;

import config.i18n.server.Messages;

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

  public static String descriptiveMetadataToHtml(Binary binary, final Locale locale, Path configBasePath)
    throws ModelServiceException, TransformerException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    return binaryToHtml(binary, true, null,
      messages.getTranslations(
        RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + binary.getStoragePath().getName(), Object.class,
        true),
      configBasePath);
  }

  public static String preservationObjectToHtml(Binary binary, final Locale locale, Path configBasePath)
    throws ModelServiceException, TransformerException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    Map<String, Object> stylesheetOpt = messages.getTranslations(
      RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + binary.getStoragePath().getName(), Object.class, true);
    return binaryToHtml(binary, false, "premis", stylesheetOpt, configBasePath);
  }

  public static PreservationMetadataBundle getPreservationMetadataBundle(String aipId, ModelService model,
    StorageService storage) throws ModelServiceException, StorageServiceException {
    AIP aip = model.retrieveAIP(aipId);
    List<RepresentationPreservationMetadataBundle> representations = new ArrayList<RepresentationPreservationMetadataBundle>();
    if (aip.getRepresentationIds() != null && aip.getRepresentationIds().size() > 0) {
      for (String representationId : aip.getRepresentationIds()) {
        ClosableIterable<PreservationMetadata> preservationMetadata = model
          .listPreservationMetadataBinaries(aip.getId(), representationId);
        try {
          RepresentationPreservationMetadataBundle representationPreservationMetadataBundle = getRepresentationPreservationMetadataBundle(
            representationId, preservationMetadata, storage);
          representations.add(representationPreservationMetadataBundle);

        } finally {
          try {
            preservationMetadata.close();
          } catch (IOException e) {
            LOGGER.error("Error while while freeing up resources", e);
          }
        }
      }
    }
    return new PreservationMetadataBundle(representations);
  }

  public static String getPreservationMetadataHTML(String aipId, ModelService model, StorageService storage,
    Locale locale, Pair<Integer, Integer> pagingParametersAgents, Pair<Integer, Integer> pagingParametersEvents,
    Pair<Integer, Integer> pagingParametersFile, Path configBasePath)
      throws ModelServiceException, StorageServiceException, TransformerException {
    AIP aip = model.retrieveAIP(aipId);
    StringBuilder s = new StringBuilder();
    s.append("<span class='preservationMetadata'><div class='title'>PREMIS</div>");
    if (aip.getRepresentationIds() != null && aip.getRepresentationIds().size() > 0) {
      for (String representationId : aip.getRepresentationIds()) {
        try {
          String html = getRepresentationPreservationMetadataHtml(
            ModelUtils.getPreservationPath(aipId, representationId), storage, locale, pagingParametersAgents,
            pagingParametersEvents, pagingParametersFile, configBasePath);
          s.append(html);
        } finally {

        }
      }

    }
    s.append("</span>");

    return s.toString();
  }

  private static RepresentationPreservationMetadataBundle getRepresentationPreservationMetadataBundle(
    String representationID, ClosableIterable<PreservationMetadata> preservationMetadata, StorageService storage)
      throws ModelServiceException, StorageServiceException {
    RepresentationPreservationMetadataBundle representationBundle = new RepresentationPreservationMetadataBundle();
    List<String> agentIds = new ArrayList<String>();
    List<String> eventIds = new ArrayList<String>();
    List<String> fileIds = new ArrayList<String>();
    Iterator<PreservationMetadata> iterator = preservationMetadata.iterator();
    while (iterator.hasNext()) {
      PreservationMetadata pm = iterator.next();
      String type = pm.getType();
      if (type.equalsIgnoreCase("event")) {
        eventIds.add(pm.getId());
      } else if (type.equalsIgnoreCase("agent")) {
        agentIds.add(pm.getId());
      } else if (type.equalsIgnoreCase("file")) {
        fileIds.add(pm.getId());
      }
    }
    representationBundle.setAgentIds(agentIds);
    representationBundle.setEventIds(eventIds);
    representationBundle.setFileIds(fileIds);
    representationBundle.setRepresentationID(representationID);
    return representationBundle;
  }

  public static String getRepresentationPreservationMetadataHtml(StoragePath preservationPath, StorageService storage,
    final Locale locale, Pair<Integer, Integer> pagingParametersAgents, Pair<Integer, Integer> pagingParametersEvents,
    Pair<Integer, Integer> pagingParametersFiles, Path configBasePath)
      throws ModelServiceException, StorageServiceException {
    String html = "";
    try {

      String tempFolder = RandomStringUtils.randomAlphabetic(10);
      String tempFolder2 = RandomStringUtils.randomAlphabetic(10);
      Path p = Paths.get(tempFolder);
      StorageService tempStorage = new FileStorageService(p);
      tempStorage.copy(storage, preservationPath, DefaultStoragePath.parse(tempFolder2));

      StringBuilder xml = new StringBuilder("<files>");
      ClosableIterable<Resource> resources = tempStorage
        .listResourcesUnderContainer(DefaultStoragePath.parse(tempFolder2));
      Iterator<Resource> it = resources.iterator();
      while (it.hasNext()) {
        Resource r = it.next();

        xml.append("<file>").append(tempFolder).append("/").append(r.getStoragePath().asString()).append("</file>");
      }
      resources.close();
      xml.append("</files>");
      Path xmlFile = Paths.get(tempFolder, tempFolder2, "list.xml");
      java.io.File f = xmlFile.toFile();
      FileUtils.write(f, xml.toString());

      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("fromEvent", pagingParametersEvents.getFirst());
      parameters.put("maxEvents", pagingParametersEvents.getSecond());
      parameters.put("fromAgent", pagingParametersAgents.getFirst());
      parameters.put("maxAgents", pagingParametersAgents.getSecond());
      parameters.put("fromFile", pagingParametersFiles.getFirst());
      parameters.put("maxFiles", pagingParametersFiles.getSecond());

      Messages i18nMessages = RodaCoreFactory.getI18NMessages(locale);
      Map<String, String> translations = i18nMessages
        .getTranslations(RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + "premis", String.class, true);
      parameters.putAll(translations);

      html = binaryToHtml(new FileInputStream(f), "join", parameters, configBasePath);
      FSUtils.deletePath(p);

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
    return html;
  }

  private static String binaryToHtml(Binary binary, boolean useFilename, String alternativeFilenameToUse,
    Map<String, Object> stylesheetOpt, Path configBasePath) throws TransformerException, ModelServiceException {
    String filename;
    if (useFilename) {
      filename = binary.getStoragePath().getName();
    } else {
      filename = alternativeFilenameToUse;
    }

    try {
      return binaryToHtml(binary.getContent().createInputStream(), filename, stylesheetOpt, configBasePath);
    } catch (IOException e) {
      throw new ModelServiceException("Error transforming binary file into HTML (filename=" + filename + ")",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

  }

  private static String binaryToHtml(InputStream is, String filename, Map<String, Object> stylesheetOpt,
    Path configBasePath) throws ModelServiceException {
    try {
      Reader reader = new InputStreamReader(is);

      InputStream transformerStream = getStylesheetInputStream(RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
        filename, configBasePath);
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

  private static InputStream getStylesheetInputStream(String xsltFolder, String filename, Path configBasePath) {
    InputStream transformerStream = RodaUtils.getResourceInputStream(configBasePath,
      xsltFolder + "/" + filename + ".xslt", "Transforming");
    if (transformerStream == null) {
      transformerStream = RodaUtils.getResourceInputStream(configBasePath, xsltFolder + "/plain.xslt", "Transforming");
    }
    return transformerStream;
  }
}
