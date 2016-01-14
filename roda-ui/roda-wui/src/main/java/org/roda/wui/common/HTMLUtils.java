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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.Pair;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.PreservationMetadata;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.wui.client.browse.PreservationMetadataBundle;
import org.roda.wui.client.browse.RepresentationPreservationMetadataBundle;
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
    throws ModelServiceException, TransformerException {
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

  public static String preservationObjectToHtml(Binary binary, final Locale locale)
    throws ModelServiceException, TransformerException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    Map<String, Object> stylesheetOpt = messages.getTranslations(
      RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + binary.getStoragePath().getName(), Object.class, true);
    return binaryToHtml(binary, "premis", stylesheetOpt);
  }

  public static PreservationMetadataBundle getPreservationMetadataBundle(String aipId, ModelService model,
    StorageService storage)
      throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
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
    Pair<Integer, Integer> pagingParametersFile) throws TransformerException, RequestNotValidException,
      NotFoundException, GenericException, AuthorizationDeniedException {
    AIP aip = model.retrieveAIP(aipId);
    StringBuilder s = new StringBuilder();
    s.append("<span class='preservationMetadata'><div class='title'>PREMIS</div>");
    if (aip.getRepresentationIds() != null && aip.getRepresentationIds().size() > 0) {
      for (String representationId : aip.getRepresentationIds()) {
        try {
          String html = getRepresentationPreservationMetadataHtml(
            ModelUtils.getPreservationPath(aipId, representationId), storage, locale, pagingParametersAgents,
            pagingParametersEvents, pagingParametersFile);
          s.append(html);
        } finally {

        }
      }

    }
    s.append("</span>");

    return s.toString();
  }

  private static RepresentationPreservationMetadataBundle getRepresentationPreservationMetadataBundle(
    String representationID, ClosableIterable<PreservationMetadata> preservationMetadata, StorageService storage) {
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
    Pair<Integer, Integer> pagingParametersFiles) {
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

      html = fileToHtml(f, "join", parameters);
      FSUtils.deletePath(p);

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
    return html;
  }

  private static String binaryToHtml(Binary binary, String metadataType, Map<String, Object> stylesheetOpt)
    throws TransformerException, ModelServiceException {
    try {
      Reader reader = new InputStreamReader(binary.getContent().createInputStream());

      InputStream transformerStream = getStylesheetInputStream(RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
        metadataType);
      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      RodaUtils.applyStylesheet(xsltReader, reader, stylesheetOpt, transformerResult);
      reader.close();
      return transformerResult.toString();
    } catch (TransformerException | IOException e) {
      LOGGER.error("Error transforming binary file into HTML (type=" + metadataType + ")", e);
      throw new ModelServiceException("Error transforming binary file into HTML (type=" + metadataType + ")",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  private static String fileToHtml(File file, String metadataType, Map<String, Object> stylesheetOpt)
    throws TransformerException, ModelServiceException {
    try {
      Reader reader = new InputStreamReader(new FileInputStream(file));

      InputStream transformerStream = getStylesheetInputStream(RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
        metadataType);
      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      RodaUtils.applyStylesheet(xsltReader, reader, stylesheetOpt, transformerResult);
      reader.close();
      return transformerResult.toString();
    } catch (TransformerException | IOException e) {
      LOGGER.error("Error transforming binary file into HTML (type=" + metadataType + ")", e);
      throw new ModelServiceException("Error transforming binary file into HTML (type=" + metadataType + ")",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
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
