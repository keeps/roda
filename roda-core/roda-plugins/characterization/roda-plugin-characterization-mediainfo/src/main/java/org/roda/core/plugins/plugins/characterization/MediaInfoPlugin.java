/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MediaInfoPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaInfoPlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "AIP feature extraction (MediaInfo)";
  }

  @Override
  public String getDescription() {
    return "MediaInfo extracts technical and tag data for video and audio files.\nMediaInfo supports popular video formats (e.g. AVI, WMV, "
      + "QuickTime, Real, DivX, XviD) as well as lesser known or emerging formats such as MKV including WebM.\nMediaInfo reveals information "
      + "such as: Title, author, director, album, track number, date, duration, codec, aspect ratio, framerate, bitrate, Audio codec, sample "
      + "rate, channels, language, bitrate, subtitle language, etc.\nThe task updates PREMIS objects metadata in the Archival Information "
      + "Package (AIP) to store the results of the characterization process. A PREMIS event is also recorded after the task is run.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        processAIP(index, model, storage, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, Job job, AIP aip) {
    LOGGER.debug("Processing AIP {}", aip.getId());
    boolean inotify = false;
    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);
    PluginState reportState = PluginState.SUCCESS;
    ValidationReport validationReport = new ValidationReport();
    List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

    for (Representation representation : aip.getRepresentations()) {
      LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), aip.getId());
      DirectResourceAccess directAccess = null;
      try {
        StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(aip.getId(),
          representation.getId());
        directAccess = storage.getDirectAccess(representationDataPath);

        String mediaInfoOutput = MediaInfoPluginUtils.runMediaInfoOnPath(directAccess.getPath());

        Map<String, Path> mediaInfoParsed = parseMediaInfoOutput(mediaInfoOutput);
        for (Map.Entry<String, Path> entry : mediaInfoParsed.entrySet()) {
          // XXX directories are not supported
          List<String> directoryPath = new ArrayList<>();
          String fileId = entry.getKey();
          ContentPayload payload = new FSPathContentPayload(entry.getValue());
          LOGGER.debug("Creating other metadata (AIP: {}, REPRESENTATION: {}, FILE: {})", aip.getId(),
            representation.getId(), entry.getValue().toFile().getName());
          model.createOrUpdateOtherMetadata(aip.getId(), representation.getId(), directoryPath, fileId, ".xml",
            RodaConstants.OTHER_METADATA_TYPE_MEDIAINFO, payload, inotify);

          sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), representation.getId(), directoryPath, fileId,
            RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
        }
      } catch (RODAException | IOException | XPathExpressionException | ParserConfigurationException | SAXException
        | TransformerFactoryConfigurationError | TransformerException e) {
        LOGGER.error("Error processing AIP {}: {}", aip.getId(), e.getMessage());
        reportState = PluginState.FAILURE;
        validationReport.addIssue(new ValidationIssue(e.getMessage()));
      } finally {
        IOUtils.closeQuietly(directAccess);
      }
    }

    try {
      model.notifyAIPUpdated(aip.getId());
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error notifying of AIP update", e);
    }

    if (reportState.equals(PluginState.SUCCESS)) {
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      reportItem.setPluginState(PluginState.SUCCESS);
    } else {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setHtmlPluginDetails(true).setPluginState(PluginState.FAILURE);
      reportItem.setPluginDetails(validationReport.toHtml(false, false, false, "Error list"));
    }

    try {
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, new ArrayList<LinkingIdentifier>(),
        reportItem.getPluginState(), "", true);
    } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException | AlreadyExistsException e) {
      LOGGER.error("Error creating event: {}", e.getMessage(), e);
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
  }

  private Map<String, Path> parseMediaInfoOutput(String mediaInfoOutput) throws ParserConfigurationException,
    SAXException, IOException, TransformerFactoryConfigurationError, TransformerException, XPathExpressionException {
    Map<String, Path> parsed = new HashMap<String, Path>();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(mediaInfoOutput));

    Document doc = db.parse(is);
    NodeList nodes = doc.getElementsByTagName("File");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      Path nodeResult = Files.createTempFile("mediaInfo", ".xml");
      FileWriter fw = new FileWriter(nodeResult.toFile());
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(node), new StreamResult(fw));
      String fileName = extractFileName(nodeResult);
      String[] tokens = fileName.split("/");
      fileName = tokens[tokens.length - 1];
      parsed.put(fileName, nodeResult);
    }
    return parsed;
  }

  private String extractFileName(Path nodeResult) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(Files.newBufferedReader(nodeResult));
    Document doc = db.parse(is);
    NodeList nodes = doc.getElementsByTagName("Complete_name");
    return nodes.item(0).getTextContent();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new MediaInfoPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.METADATA_EXTRACTION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
