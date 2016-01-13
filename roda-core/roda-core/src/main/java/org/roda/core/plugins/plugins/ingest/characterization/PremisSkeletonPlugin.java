/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisUtils;
import org.roda.core.data.Attribute;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.ReportItem;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.core.data.v2.PluginType;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.RepresentationPreservationObject;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;
import org.roda.core.model.AIP;
import org.roda.core.model.File;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PremisSkeletonPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPlugin.class);

  private Map<String, String> parameters;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "PREMIS basic information";
  }

  @Override
  public String getDescription() {
    return "Create basic PREMIS information";
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
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    try {
      Path temp = Files.createTempDirectory("temp");
      for (AIP aip : list) {
        LOGGER.debug("Processing AIP " + aip.getId());
        ReportItem reportItem = PluginHelper.createPluginReportItem(this, "Creating base PREMIS", aip.getId(), null);

        try {
          for (String representationID : aip.getRepresentationIds()) {
            createPremisForRepresentation(model, storage, temp, aip, representationID);
          }

          state = PluginState.SUCCESS;
          reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
            new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

        } catch (RODAException | PremisMetadataException e) {
          LOGGER.error("Error processing AIP " + aip.getId(), e);

          state = PluginState.FAILURE;
          reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
            new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
            new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
        }

        report.addItem(reportItem);

        PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
          aip.getId());
      }
    } catch (IOException ioe) {
      LOGGER.error("Error executing FastCharacterizationAction: " + ioe.getMessage(), ioe);
    }
    return report;
  }

  private void createPremisForRepresentation(ModelService model, StorageService storage, Path temp, AIP aip,
    String representationID) throws IOException, PremisMetadataException, RequestNotValidException, GenericException,
      NotFoundException, AuthorizationDeniedException {
    LOGGER.debug("Processing representation " + representationID + " from AIP " + aip.getId());

    RepresentationPreservationObject pObject = new RepresentationPreservationObject();
    pObject.setId(representationID);
    pObject.setPreservationLevel("");

    List<RepresentationFilePreservationObject> pObjectPartFiles = new ArrayList<RepresentationFilePreservationObject>();
    Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
    for (String fileID : representation.getFileIds()) {
      pObjectPartFiles = createPremisForRepresentationFile(model, storage, temp, aip, representationID, pObject,
        pObjectPartFiles, fileID);
    }

    createPremisObjectForRepresentation(model, aip, representationID, pObject, pObjectPartFiles);
  }

  private void createPremisObjectForRepresentation(ModelService model, AIP aip, String representationID,
    RepresentationPreservationObject pObject, List<RepresentationFilePreservationObject> pObjectPartFiles)
      throws IOException, PremisMetadataException, RequestNotValidException, GenericException, NotFoundException,
      AuthorizationDeniedException {
    pObject.setPartFiles(pObjectPartFiles.toArray(new RepresentationFilePreservationObject[pObjectPartFiles.size()]));

    Path premisRepresentation = Files.createTempFile("representation", ".premis.xml");
    PremisRepresentationObjectHelper helper = new PremisRepresentationObjectHelper(pObject);
    helper.saveToFile(premisRepresentation.toFile());
    model.createPreservationMetadata(aip.getId(), representationID, "representation.premis.xml",
      (Binary) FSUtils.convertPathToResource(premisRepresentation.getParent(), premisRepresentation));

    FSUtils.deletePath(premisRepresentation);
  }

  private List<RepresentationFilePreservationObject> createPremisForRepresentationFile(ModelService model,
    StorageService storage, Path temp, AIP aip, String representationID, RepresentationPreservationObject pObject,
    List<RepresentationFilePreservationObject> pObjectPartFiles, String fileID)
      throws IOException, PremisMetadataException, RequestNotValidException, GenericException, NotFoundException,
      AuthorizationDeniedException {
    LOGGER.debug("Processing file " + fileID + " from " + representationID + " of AIP " + aip.getId());

    File file = model.retrieveFile(aip.getId(), representationID, fileID);
    Binary binary = storage.getBinary(file.getStoragePath());
    Path pathFile = Paths.get(temp.toString(), file.getStoragePath().getName());
    Files.copy(binary.getContent().createInputStream(), pathFile, StandardCopyOption.REPLACE_EXISTING);

    RepresentationFilePreservationObject premisObject = PremisUtils.createPremisFromFile(file, binary,
      "PremisSkeletonAction");
    Path premis = Files.createTempFile(file.getId(), ".premis.xml");
    PremisFileObjectHelper helper = new PremisFileObjectHelper(premisObject);
    helper.saveToFile(premis.toFile());
    model.createPreservationMetadata(aip.getId(), representationID, file.getId() + ".premis.xml",
      (Binary) FSUtils.convertPathToResource(premis.getParent(), premis));
    if (pObject.getRootFile() == null) {
      pObject.setRootFile(premisObject);
    } else {
      pObjectPartFiles.add(premisObject);
    }
    FSUtils.deletePath(premis);

    return pObjectPartFiles;
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
    return new PremisSkeletonPlugin();
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
