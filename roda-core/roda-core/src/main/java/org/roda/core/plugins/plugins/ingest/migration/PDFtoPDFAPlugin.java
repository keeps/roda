/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ghost4j.GhostscriptException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.verapdf.core.VeraPDFException;

public class PDFtoPDFAPlugin extends AbstractConvertPlugin {

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "PDF to PDFA conversion";
  }

  @Override
  public String getDescription() {
    return "Generates PDFa format files from PDF files allowing them to pass on veraPDF validation";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new PDFtoPDFAPlugin();
  }

  @Override
  public PluginType getType() {
    return super.getType();
  }

  @Override
  public boolean areParameterValuesValid() {
    return super.areParameterValuesValid();
  }

  @Override
  public List<PluginParameter> getParameters() {
    return super.getParameters();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return super.getParameterValues();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    return super.execute(index, model, storage, list);
  }

  @Override
  public Path executePlugin(Binary binary) throws UnsupportedOperationException, IOException, CommandException {
    Path uriPath = Paths.get(binary.getContent().getURI());
    Path pluginResult = null;

    try {
      if (Files.exists(uriPath)) {
        pluginResult = PDFtoPDFAPluginUtils.runPDFtoPDFA(uriPath);
      } else {
        pluginResult = PDFtoPDFAPluginUtils.runPDFtoPDFA(binary.getContent().createInputStream());
      }
    } catch (VeraPDFException | GhostscriptException e) {
      logger.error("Error when running PDFtoPDFAPluginUtils ", e);
    }

    return pluginResult;
  }

  public void createEvent(List<String> alteredFiles, AIP aip, String representationID, String newRepresentionID,
    ModelService model, int state) throws PluginException {

    // building the detail extension for the plugin event
    String outcome = "success";
    StringBuilder stringBuilder = new StringBuilder();
    if (alteredFiles.size() == 0) {
      stringBuilder.append("No PDF/A file was generated on this representation.");
    } else {
      stringBuilder.append("The following files were converted to PDF/A format on a new representation (ID: "
        + newRepresentionID + "): ");
      for (String fileID : alteredFiles) {
        stringBuilder.append(fileID + ", ");
      }
      stringBuilder.setLength(stringBuilder.length() - 2);
    }

    // PDFtoPDFA plugin did not run correctly
    if (state == 0 || (state == 2 && hasPartialSuccessOnOutcome == false)) {
      outcome = "failure";
      stringBuilder.setLength(0);
    }
    // some pdf files were not converted
    if (state == 2 && hasPartialSuccessOnOutcome == true) {
      outcome = "partial success";
    }

    // FIXME revise PREMIS generation
    try {
      PluginHelper.createPluginEventAndAgent(aip.getId(), representationID, model,
        EventPreservationObject.PRESERVATION_EVENT_TYPE_MIGRATION, "Some PDF files were converted to PDF/A.",
        EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_EXECUTING_PROGRAM_TASK, "PDFtoPDFAConverter",
        Arrays.asList(representationID), outcome, stringBuilder.toString(), null, getClass().getName() + "@"
          + getVersion(), AgentPreservationObject.PRESERVATION_AGENT_TYPE_PDFTOPDFA_CONVERSION_PLUGIN);

    } catch (PremisMetadataException | IOException | RequestNotValidException | NotFoundException | GenericException
      | AlreadyExistsException | AuthorizationDeniedException e) {
      throw new PluginException(e.getMessage(), e);
    }
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
