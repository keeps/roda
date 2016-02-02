/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFtoPDFAPlugin implements Plugin<AIP> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private long maxKbytes = 20000; // default 20000 kb
  private boolean hasPartialSuccessOnOutcome = true; // default with partial
                                                     // success included

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
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // indicates the maximum kbytes the files that will be processed must have
    if (parameters.containsKey("maxKbytes")) {
      maxKbytes = Long.parseLong(parameters.get("maxKbytes"));
    }

    // indicates outcome types: success, partial success (if true), failure
    if (parameters.containsKey("hasPartialSuccessOnOutcome")) {
      hasPartialSuccessOnOutcome = Boolean.parseBoolean(parameters.get("hasPartialSuccessOnOutcome"));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {
      logger.debug("Processing AIP " + aip.getId());
      List<String> newRepresentations = new ArrayList<String>();
      String newRepresentationID = UUID.randomUUID().toString();

      for (Representation representation : aip.getRepresentations()) {
        List<String> alteredFiles = new ArrayList<String>();
        int state = 1;

        try {
          logger.debug("Processing representation " + representation.getId() + " of AIP " + aip.getId());

          Iterable<File> allFiles = model.listAllFiles(aip.getId(), representation.getId());
          for (File file : allFiles) {
            logger.debug("Processing file: " + file);

            if (!file.isDirectory()) {
              /*
               * && file.getOriginalName().endsWith(".pdf") && (file.getSize()
               * <= maxKbytes * 1024)
               */
              // TODO filter by file type and size

              StoragePath fileStoragePath = ModelUtils.getRepresentationFileStoragePath(file);
              Binary binary = storage.getBinary(fileStoragePath);

              // FIXME file that doesn't get deleted afterwards
              logger.debug("Running PDFtoPDFAPlugin on " + file.getId());
              Path pluginResult = PDFtoPDFAPluginUtils.runPDFtoPDFA(binary.getContent().createInputStream(),
                file.getId());

              if (pluginResult != null) {
                Binary resource = (Binary) FSUtils.convertPathToResource(pluginResult.getParent(), pluginResult);
                StoragePath storagePath = ModelUtils.getRepresentationPath(aip.getId(), representation.getId());

                // create a new representation if it does not exist
                if (!newRepresentations.contains(newRepresentationID)) {
                  logger.debug("Creating a new representation " + newRepresentationID + " on AIP " + aip.getId());
                  boolean original = false;
                  model.createRepresentation(aip.getId(), newRepresentationID, original, model.getStorage(),
                    storagePath);
                  StoragePath storagePreservationPath = ModelUtils.getPreservationRepresentationPath(aip.getId(),
                    newRepresentationID);
                  model.getStorage().createDirectory(storagePreservationPath);
                }

                // update file on new representation
                model.updateFile(aip.getId(), newRepresentationID, file.getPath(), file.getId(), resource, true, true);
                alteredFiles.add(file.getId());
                newRepresentations.add(newRepresentationID);

              } else {
                logger.debug("PDFA conversion failed on file: " + file);
                state = 2;
              }
            }
          }

        } catch (Throwable e) {
          logger.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          state = 0;
        }

        logger.debug("Creating PDFtoPDFA plugin event for the representation " + representation.getId());
        createEvent(alteredFiles, aip, representation.getId(), newRepresentationID, model, state);
      }
    }

    return null;
  }

  private void createEvent(List<String> alteredFiles, AIP aip, String representationID, String newRepresentionID,
    ModelService model, int state) throws PluginException {

    // building the detail extension for the plugin event
    String outcome = "success";
    StringBuilder stringBuilder = new StringBuilder();
    if (alteredFiles.size() == 0) {
      stringBuilder.append("No PDF/A file was generated on this representation.");
    } else {
      stringBuilder.append(
        "The following files were converted to PDF/A format on a new representation (ID: " + newRepresentionID + "): ");
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
        Arrays.asList(representationID), outcome, stringBuilder.toString(), null,
        getClass().getName() + "@" + getVersion(),
        AgentPreservationObject.PRESERVATION_AGENT_TYPE_PDFTOPDFA_CONVERSION_PLUGIN);

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

  @Override
  public Plugin<AIP> cloneMe() {
    return new PDFtoPDFAPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
