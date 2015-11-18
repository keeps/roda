/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.CommandException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MediaInfoPlugin implements Plugin<AIP> {
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
    return "MediaInfo characterization action";
  }

  @Override
  public String getDescription() {
    return "Generates the MediaInfo output for each file in the AIP";
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
          Path data = Files.createTempDirectory("data");
          StorageService tempStorage = new FileStorageService(data);
          StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representationID);
          tempStorage.copy(storage, representationPath, representationPath);
          String mediaInfoOutput = MediaInfoPluginUtils.runMediaInfoOnPath(data.resolve(representationPath.asString()));

          Map<String, Path> mediaInfoParsed = parseMediaInfoOutput(mediaInfoOutput);
          for (Map.Entry<String, Path> entry : mediaInfoParsed.entrySet()) {
            Binary resource = (Binary) FSUtils.convertPathToResource(entry.getValue().getParent(), entry.getValue());
            LOGGER.debug("Creating other metadata (AIP: " + aip.getId() + ", REPRESENTATION: " + representationID
              + ", FILE: " + entry.getValue().toFile().getName() + ")");
            model.createOtherMetadata(aip.getId(), representationID, entry.getKey() + ".xml", "MediaInfo", resource);
          }
          FSUtils.deletePath(data);
        } catch (StorageServiceException sse) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage());
        } catch (IOException ioe) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + ioe.getMessage());
        } catch (CommandException ce) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + ce.getMessage());
        } catch (XPathExpressionException xpee) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + xpee.getMessage());
        } catch (ParserConfigurationException pce) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + pce.getMessage());
        } catch (SAXException se) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + se.getMessage());
        } catch (TransformerFactoryConfigurationError tfce) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + tfce.getMessage());
        } catch (TransformerException te) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + te.getMessage());
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
