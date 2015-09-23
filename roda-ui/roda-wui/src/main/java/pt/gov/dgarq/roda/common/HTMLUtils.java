package pt.gov.dgarq.roda.common;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

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
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;

import config.i18n.server.XSLTMessages;
import lc.xmlns.premisV2.AgentComplexType;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.File;
import lc.xmlns.premisV2.Representation;
import pt.gov.dgarq.roda.core.common.Pair;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.PreservationMetadataBundle;

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
    XSLTMessages messages = new XSLTMessages(locale);
    for (Map.Entry<String, String> entry : messages.getTranslations(binary.getStoragePath().getName()).entrySet()) {
      stylesheetOpt.put(entry.getKey(), entry.getValue());
    }
    return binaryToHtml(binary, locale, true, null, stylesheetOpt);
  }

  public static String preservationObjectToHtml(Binary binary, final Locale locale) throws ModelServiceException {
    Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
    stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
    XSLTMessages messages = new XSLTMessages(locale);
    for (Map.Entry<String, String> entry : messages.getTranslations("premis").entrySet()) {
      stylesheetOpt.put(entry.getKey(), entry.getValue());
    }
    return binaryToHtml(binary, locale, false, "premis", stylesheetOpt);
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
    long totalSizeInBytes = 0L;
    int numberOfFiles = 0;
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
          PreservationMetadataBundle representationPreservationMetadataBundle = getRepresentationPreservationMetadataBundle(
            preservationMetadata, storage);
          totalSizeInBytes += representationPreservationMetadataBundle.getSizeInBytes();
          numberOfFiles += representationPreservationMetadataBundle.getNumberOfFiles();
        } finally {
          try {
            preservationMetadata.close();
          } catch (IOException e) {
            LOGGER.error("Error while while freeing up resources", e);
          }
        }
      }
    }
    return new PreservationMetadataBundle(totalSizeInBytes, numberOfFiles);
  }

  public static String getPreservationMetadataHTML(String aipId, ModelService model, StorageService storage,
    Locale locale) throws ModelServiceException, StorageServiceException {
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
        ClosableIterable<PreservationMetadata> preservationMetadata = model
          .listPreservationMetadataBinaries(aip.getId(), representationId);
        try {
          String html = getRepresentationPreservationMetadataHtml(preservationMetadata, storage, locale);
          s.append(html);
        } finally {
          try {
            preservationMetadata.close();
          } catch (IOException e) {
            LOGGER.error("Error while while freeing up resources", e);
          }
        }
      }

    }
    s.append("</span>");

    return s.toString();
  }

  private static PreservationMetadataBundle getRepresentationPreservationMetadataBundle(
    ClosableIterable<PreservationMetadata> preservationMetadata, StorageService storage)
      throws ModelServiceException, StorageServiceException {
    long totalSizeInBytes = 0L;
    int numberOfFiles = 0;

    Iterator<PreservationMetadata> iterator = preservationMetadata.iterator();
    while (iterator.hasNext()) {
      PreservationMetadata pm = iterator.next();
      Binary b = storage.getBinary(pm.getStoragePath());
      totalSizeInBytes += b.getSizeInBytes();
      numberOfFiles++;

    }
    return new PreservationMetadataBundle(totalSizeInBytes, numberOfFiles);
  }

  private static String getRepresentationPreservationMetadataHtml(
    ClosableIterable<PreservationMetadata> preservationMetadata, StorageService storage, final Locale locale)
      throws ModelServiceException, StorageServiceException {

    Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
    stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);

    List<Pair<Binary, EventComplexType>> events = new ArrayList<Pair<Binary, EventComplexType>>();
    List<Pair<Binary, File>> files = new ArrayList<Pair<Binary, File>>();
    List<Pair<Binary, AgentComplexType>> agents = new ArrayList<Pair<Binary, AgentComplexType>>();
    Binary representation = null;

    Iterator<PreservationMetadata> iterator = preservationMetadata.iterator();
    while (iterator.hasNext()) {
      PreservationMetadata pm = iterator.next();
      Binary b = storage.getBinary(pm.getStoragePath());

      EventComplexType event = ModelUtils.getPreservationEvent(b);
      if (event != null) {
        events.add(new Pair<Binary, EventComplexType>(b, event));
      } else {
        File file = ModelUtils.getPreservationFileObject(b);
        if (file != null) {
          files.add(new Pair<Binary, File>(b, file));
        } else {
          AgentComplexType agent = ModelUtils.getPreservationAgentObject(b);
          if (agent != null) {
            agents.add(new Pair<Binary, AgentComplexType>(b, agent));
          } else {
            representation = b;
          }
        }
      }
    }

    events.sort(new Comparator<Pair<Binary, EventComplexType>>() {

      @Override
      public int compare(Pair<Binary, EventComplexType> o1, Pair<Binary, EventComplexType> o2) {
        int ret = 0;
        if (o1.getSecond().xgetEventDateTime().getStringValue() != null) {
          if (o2.getSecond().xgetEventDateTime().getStringValue() != null) {
            ret = o1.getSecond().xgetEventDateTime().getStringValue()
              .compareTo(o2.getSecond().xgetEventDateTime().getStringValue());
          } else {
            ret = 0;
          }
        } else {
          if (o2.getSecond().xgetEventDateTime().getStringValue() != null) {
            ret = 1;
          } else {
            ret = 0;
          }
        }
        return ret;
      }
    });
    files = sortFilesByRepresentationOrder(representation, files);

    List<String> htmlEvents = new ArrayList<String>();
    for (Pair<Binary, EventComplexType> eventPair : events) {
      String html = preservationObjectToHtml(eventPair.getFirst(), locale);
      htmlEvents.add(html);
    }
    stylesheetOpt.put("events", htmlEvents);

    List<String> htmlFiles = new ArrayList<String>();
    for (Pair<Binary, File> filePair : files) {
      String html = preservationObjectToHtml(filePair.getFirst(), locale);
      htmlFiles.add(html);
    }
    stylesheetOpt.put("files", htmlFiles);

    List<String> htmlAgents = new ArrayList<String>();
    for (Pair<Binary, AgentComplexType> agentPair : agents) {
      String html = preservationObjectToHtml(agentPair.getFirst(), locale);
      htmlAgents.add(html);
    }
    stylesheetOpt.put("agents", htmlAgents);

    XSLTMessages messages = new XSLTMessages(locale);
    for (Map.Entry<String, String> entry : messages.getTranslations("premis").entrySet()) {
      stylesheetOpt.put(entry.getKey(), entry.getValue());
    }

    String html = binaryToHtml(representation, locale, false, "premis", stylesheetOpt);
    return html;
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

  private static List<Pair<Binary, File>> sortFilesByRepresentationOrder(Binary representationBinary,
    List<Pair<Binary, File>> files) {
    return files;
  }
}
