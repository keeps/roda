/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.ingest.characterization.jpylyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.roda.action.ingest.characterization.FFProbe.utils.FFProbeUtils;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.v2.Representation;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JpylyzerAction implements Plugin<AIP> {
  private static final Logger LOGGER = Logger.getLogger(JpylyzerAction.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "FFProbe characterization action";
  }

  @Override
  public String getDescription() {
    return "Generates the FFProbe output for each file in the AIP";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // no params
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());
      for (String representationID : aip.getRepresentationIds()) {
        LOGGER.debug("Processing representation " + representationID + " from AIP " + aip.getId());
        try {
          Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
          for (String fileID : representation.getFileIds()) {
            try {
              LOGGER.debug("Processing file " + fileID + " from " + representationID + " of AIP " + aip.getId());
              File file = model.retrieveFile(aip.getId(), representationID, fileID);
              Binary binary = storage.getBinary(file.getStoragePath());

              Path ffProbeResults = FFProbeUtils.runFFProbe(file, binary, getParameterValues());
              Binary resource = (Binary) FSUtils.convertPathToResource(ffProbeResults.getParent(), ffProbeResults);
              model.createOtherMetadata(aip.getId(), representationID, file.getStoragePath().getName() + ".xml",
                "jpylyzer", resource);
              ffProbeResults.toFile().delete();
            } catch (StorageServiceException sse) {
              LOGGER.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage());
            } catch (IOException ioe) {
              LOGGER.error("Error processing AIP " + aip.getId() + ": " + ioe.getMessage());
            } catch (PluginException ce) {
              LOGGER.error("Error processing AIP " + aip.getId() + ": " + ce.getMessage());
            }
          }
        } catch (ModelServiceException mse) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage());
        }
      }

    }
    return null;
  }

  private Map<String, Path> parseMediaInfoOutput(String mediaInfoOutput) throws ParserConfigurationException,
    SAXException, IOException, TransformerFactoryConfigurationError, TransformerException, XPathExpressionException {
    Map<String, Path> parsed = new HashMap<String, Path>();

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    XPathExpression expr = xpath.compile("//Complete_name");

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
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

}
