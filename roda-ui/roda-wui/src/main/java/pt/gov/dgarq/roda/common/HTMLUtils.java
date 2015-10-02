package pt.gov.dgarq.roda.common;

import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.roda.common.RodaUtils;
import org.roda.index.utils.SolrUtils;
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
import org.roda.storage.fs.FileStorageService;

import config.i18n.server.Messages;
import lc.xmlns.premisV2.AgentComplexType;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.File;
import lc.xmlns.premisV2.Representation;
import pt.gov.dgarq.roda.core.common.Pair;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.PreservationMetadataBundle;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.RepresentationPreservationMetadataBundle;

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

  public static String descriptiveMetadataToHtml(Binary binary, final Locale locale)
    throws ModelServiceException, TransformerException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    return binaryToHtml(binary, true, null, messages
      .getTranslations(RodaConstants.I18N_BINARY_TO_HTML_PREFIX + binary.getStoragePath().getName(), Object.class));
  }

  public static String preservationObjectToHtml(Binary binary, final Locale locale)
    throws ModelServiceException, TransformerException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    Map<String, Object> stylesheetOpt = messages
      .getTranslations(RodaConstants.I18N_BINARY_TO_HTML_PREFIX + binary.getStoragePath().getName(), Object.class);
    return binaryToHtml(binary, false, "premis", stylesheetOpt);
  }

  public static String aipPremisToHTML2(AIP aip, ModelService model, StorageService storage, Locale locale)
    throws ModelServiceException, StorageServiceException {

    return null;
  }

  // FIXME close properly model related methods that may generate file
  // descriptor leaks
  public static PreservationMetadataBundle getPreservationMetadataBundle(String aipId, ModelService model,
    StorageService storage) throws ModelServiceException, StorageServiceException {
    AIP aip = model.retrieveAIP(aipId);
    List<RepresentationPreservationMetadataBundle> representations = new ArrayList<RepresentationPreservationMetadataBundle>();
    if (aip.getRepresentationIds() != null && aip.getRepresentationIds().size() > 0) {
      /*
       * agentsID = new TreeSet<String>(); for (String representationId :
       * aip.getRepresentationIds()) { ClosableIterable<PreservationMetadata>
       * preservationMetadata = model
       * .listPreservationMetadataBinaries(aip.getId(), representationId);
       * agentsID.addAll(extractAgents(preservationMetadata, storage)); }
       */
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
    Locale locale, Pair<Integer, Integer> pagingParametersAgents, Pair<Integer, Integer> pagingParametersEvents, Pair<Integer, Integer> pagingParametersFile) throws ModelServiceException, StorageServiceException, TransformerException {
    AIP aip = model.retrieveAIP(aipId);
    StringBuffer s = new StringBuffer();
    s.append("<span class='preservationMetadata'><div class='title'>PREMIS</div>");
    if (aip.getRepresentationIds() != null && aip.getRepresentationIds().size() > 0) {
      /*
       * agentsID = new TreeSet<String>(); for (String representationId :
       * aip.getRepresentationIds()) { ClosableIterable<PreservationMetadata>
       * preservationMetadata = model
       * .listPreservationMetadataBinaries(aip.getId(), representationId);
       * agentsID.addAll(extractAgents(preservationMetadata, storage)); }
       */
      for (String representationId : aip.getRepresentationIds()) {
        try {
          String html = getRepresentationPreservationMetadataHtml(
            ModelUtils.getPreservationPath(aipId, representationId), storage, locale,pagingParametersAgents,pagingParametersEvents,pagingParametersFile);
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
    final Locale locale, Pair<Integer,Integer> pagingParametersAgents, Pair<Integer,Integer> pagingParametersEvents, Pair<Integer,Integer> pagingParametersFiles) throws ModelServiceException, StorageServiceException {
    String html = "";
    try {
      
      String tempFolder = RandomStringUtils.randomAlphabetic(10);
      String tempFolder2 = RandomStringUtils.randomAlphabetic(10);
      Path p =  Paths.get(tempFolder);
      StorageService tempStorage = new FileStorageService(p);
      tempStorage.copy(storage, preservationPath, DefaultStoragePath.parse(tempFolder2));

      String xml = "<files>";
      ClosableIterable<Resource> resources = tempStorage.listResourcesUnderContainer(DefaultStoragePath.parse(tempFolder2));
      Iterator<Resource> it = resources.iterator();
      while (it.hasNext()) {
        Resource r = it.next();
        
        xml += "<file>"+tempFolder+"/" + r.getStoragePath().asString() + "</file>";
      }
      resources.close();
      xml += "</files>";
      resources.close();
      Path xmlFile = Paths.get(tempFolder,tempFolder2,"list.xml");
      java.io.File f = xmlFile.toFile();
      FileUtils.write(f, xml);

      Map<String,Object> parameters = new HashMap<String,Object>();
      parameters.put("fromEvent", pagingParametersEvents.getFirst().toString());
      parameters.put("maxEvents", pagingParametersEvents.getSecond().toString());
      parameters.put("startAgent", pagingParametersAgents.getFirst().toString());
      parameters.put("maxAgents", pagingParametersAgents.getSecond().toString());
      parameters.put("fromFile", pagingParametersFiles.getFirst().toString());
      parameters.put("maxFiles", pagingParametersFiles.getSecond().toString());
      
      Messages messages = RodaCoreFactory.getI18NMessages(locale); 
      for(Map.Entry<String, String> entry : messages.getTranslations("binaryToHtml.premis",String.class).entrySet()) {
        parameters.put(entry.getKey(), entry.getValue()); 
       }
      
      html = inputstreamToHtml(new FileInputStream(f), "join", parameters);
      removeDirectory(p.toFile());

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }

    /*
     * Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
     * stylesheetOpt.put("prefix",
     * RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
     * 
     * Pair<Integer, Integer> pagingParamsAgent =
     * ApiUtils.processPagingParams(startAgent, limitAgent); int counterAgent =
     * 0; Pair<Integer, Integer> pagingParamsEvent =
     * ApiUtils.processPagingParams(startEvent, limitEvent); int counterEvent =
     * 0; Pair<Integer, Integer> pagingParamsFile =
     * ApiUtils.processPagingParams(startFile, limitFile); int counterFile = 0;
     * 
     * List<Pair<Binary, EventComplexType>> events = new ArrayList<Pair<Binary,
     * EventComplexType>>(); List<Pair<Binary, File>> files = new
     * ArrayList<Pair<Binary, File>>(); List<Pair<Binary, AgentComplexType>>
     * agents = new ArrayList<Pair<Binary, AgentComplexType>>(); Binary
     * representation = null;
     * 
     * Iterator<PreservationMetadata> iterator =
     * preservationMetadata.iterator(); while (iterator.hasNext()) {
     * PreservationMetadata pm = iterator.next(); if
     * (pm.getType().equalsIgnoreCase("agent")) {
     * 
     * if (counterAgent >= pagingParamsAgent.getFirst() && (counterAgent <=
     * pagingParamsAgent.getSecond() || pagingParamsAgent.getSecond() == -1)) {
     * Binary b = storage.getBinary(pm.getStoragePath()); AgentComplexType agent
     * = ModelUtils.getPreservationAgentObject(b); agents.add(new Pair<Binary,
     * AgentComplexType>(b, agent)); } counterAgent++; } else if
     * (pm.getType().equalsIgnoreCase("event")) {
     * 
     * if (counterEvent >= pagingParamsEvent.getFirst() && (counterEvent <=
     * pagingParamsEvent.getSecond() || pagingParamsEvent.getSecond() == -1)) {
     * Binary b = storage.getBinary(pm.getStoragePath()); EventComplexType event
     * = ModelUtils.getPreservationEvent(b); events.add(new Pair<Binary,
     * EventComplexType>(b, event)); } counterEvent++; } else if
     * (pm.getType().equalsIgnoreCase("file")) {
     * 
     * if (counterFile >= pagingParamsFile.getFirst() && (counterFile <=
     * pagingParamsFile.getSecond() || pagingParamsFile.getSecond() == -1)) {
     * Binary b = storage.getBinary(pm.getStoragePath()); File file =
     * ModelUtils.getPreservationFileObject(b); files.add(new Pair<Binary,
     * File>(b, file)); } counterFile++; } else { representation =
     * storage.getBinary(pm.getStoragePath()); } }
     * 
     * // FIXME is this the right way for comparing dates? by comparing their //
     * strings? I don't think so! events.sort(new Comparator<Pair<Binary,
     * EventComplexType>>() {
     * 
     * @Override public int compare(Pair<Binary, EventComplexType> o1,
     * Pair<Binary, EventComplexType> o2) { int ret = 0; if
     * (o1.getSecond().xgetEventDateTime().getStringValue() != null) { if
     * (o2.getSecond().xgetEventDateTime().getStringValue() != null) { ret =
     * o1.getSecond().xgetEventDateTime().getStringValue()
     * .compareTo(o2.getSecond().xgetEventDateTime().getStringValue()); } else {
     * ret = 0; } } else { if
     * (o2.getSecond().xgetEventDateTime().getStringValue() != null) { ret = 1;
     * } else { ret = 0; } } return ret; } }); files =
     * sortFilesByRepresentationOrder(representation, files);
     * 
     * List<String> htmlEvents = new ArrayList<String>(); for (Pair<Binary,
     * EventComplexType> eventPair : events) { String html =
     * preservationObjectToHtml(eventPair.getFirst(), locale);
     * htmlEvents.add(html); } stylesheetOpt.put("events", htmlEvents);
     * 
     * List<String> htmlFiles = new ArrayList<String>(); for (Pair<Binary, File>
     * filePair : files) { String html =
     * preservationObjectToHtml(filePair.getFirst(), locale);
     * htmlFiles.add(html); } stylesheetOpt.put("files", htmlFiles);
     * 
     * List<String> htmlAgents = new ArrayList<String>(); for (Pair<Binary,
     * AgentComplexType> agentPair : agents) { String html =
     * preservationObjectToHtml(agentPair.getFirst(), locale);
     * htmlAgents.add(html); } stylesheetOpt.put("agents", htmlAgents);
     * 
     * XSLTMessages messages = RodaCoreFactory.getXSLTMessages(locale); for
     * (Map.Entry<String, String> entry :
     * messages.getTranslations("premis").entrySet()) {
     * stylesheetOpt.put(entry.getKey(), entry.getValue()); }
     * 
     * String html = binaryToHtml(representation, false, "premis",
     * stylesheetOpt);
     */
    return html;
  }

  public static void removeDirectory(java.io.File dir) {
    if (dir.isDirectory()) {
        java.io.File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for ( java.io.File aFile : files) {
                removeDirectory(aFile);
            }
        }
        dir.delete();
    } else {
        dir.delete();
    }
}
  public static TreeSet<String> extractAgents(ClosableIterable<PreservationMetadata> preservationMetadata,
    StorageService storage) throws ModelServiceException, StorageServiceException {
    Iterator<PreservationMetadata> iterator = preservationMetadata.iterator();
    TreeSet<String> agentsIDs = new TreeSet<String>();
    while (iterator.hasNext()) {
      PreservationMetadata pm = iterator.next();
      Binary b = storage.getBinary(pm.getStoragePath());

      EventComplexType event = ModelUtils.getPreservationEvent(b);
      if (event != null) {
        agentsIDs.addAll(ModelUtils.extractAgentIdsFromPreservationBinary(b, EventComplexType.class));
      } else {
        File file = ModelUtils.getPreservationFileObject(b);
        if (file != null) {
          agentsIDs.addAll(ModelUtils.extractAgentIdsFromPreservationBinary(b, File.class));
        } else {
          AgentComplexType agent = ModelUtils.getPreservationAgentObject(b);
          if (agent != null) {
            agentsIDs.addAll(ModelUtils.extractAgentIdsFromPreservationBinary(b, Representation.class));
          }
        }
      }
    }
    return agentsIDs;
  }

  private static String binaryToHtml(Binary binary, boolean useFilename, String alternativeFilenameToUse,
    Map<String, Object> stylesheetOpt) throws TransformerException, ModelServiceException {
    String filename;
    if (useFilename) {
      filename = binary.getStoragePath().getName();
    } else {
      filename = alternativeFilenameToUse;
    }

    InputStream inputStream = null;
    Reader reader = null;
    CharArrayWriter transformerResult;
    try {
      inputStream = binary.getContent().createInputStream();
      reader = new InputStreamReader(inputStream);

      InputStream transformerStream = getStylesheetInputStream("htmlXSLT", filename);
      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      transformerResult = new CharArrayWriter();
      RodaUtils.applyStylesheet(xsltReader, reader, stylesheetOpt, transformerResult);
    } catch (IOException e) {
      throw new ModelServiceException("Error transforming binary to HTML", ModelServiceException.INTERNAL_SERVER_ERROR,
        e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          LOGGER.warn("Could not close input stream, possible leak", e);
        }
      }
    }

    return transformerResult.toString();

  }

  private static String inputstreamToHtml(InputStream is, String filename, Map<String, Object> stylesheetOpt)
    throws ModelServiceException {
    try {
      Reader reader = new InputStreamReader(is);

      InputStream transformerStream = getStylesheetInputStream("htmlXSLT", filename);
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

  private static InputStream getStylesheetInputStream(String xsltFolder, String filename) {
    // FIXME this should be loaded from config folder (to be dynamic)
    ClassLoader classLoader = SolrUtils.class.getClassLoader();
    InputStream transformerStream = classLoader.getResourceAsStream(xsltFolder + "/" + filename + ".xslt");
    if (transformerStream == null) {
      LOGGER.warn("Didn't found proper stylesheet for dealing with file \"" + filename + "\". Using " + xsltFolder
        + "/plain.xslt");
      transformerStream = classLoader.getResourceAsStream(xsltFolder + "/plain.xslt");
    }

    return transformerStream;
  }

  // FIXME this does nothing!!!
  private static List<Pair<Binary, File>> sortFilesByRepresentationOrder(Binary representationBinary,
    List<Pair<Binary, File>> files) {
    return files;
  }
}
