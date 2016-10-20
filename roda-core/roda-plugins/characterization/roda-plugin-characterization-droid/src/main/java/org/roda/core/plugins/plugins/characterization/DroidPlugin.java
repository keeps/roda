/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
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
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroidPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DroidPlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "AIP file format identification (DROID)";
  }

  @Override
  public String getDescription() {
    return "DROID (Digital Record Object Identification) is a software tool developed to perform automated batch identification of file formats."
      + " DROID is designed to meet the fundamental requirement of any digital repository to be able to identify the precise format of all stored digital objects, "
      + "and to link that identification to a central registry of technical information about that format and its dependencies.\nDROID uses the PRONOM signature files to"
      + " perform format identification. Like PRONOM, it was developed by the National Archives of the UK.\nThe task updates PREMIS objects metadata in the Archival Information "
      + "Package (AIP) to store the results of the characterization process. It also creates a new file under the [AIP_ID]/representation/metadata/other/droid/ with the results"
      + " of identification. A PREMIS event is recorded after the task is run.";
  }

  @Override
  public String getVersionImpl() {
    String signatureFile = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "droid", "signatureFile");
    if (StringUtils.isNotBlank(signatureFile)) {
      return signatureFile.substring(signatureFile.lastIndexOf("/")).replaceAll(".*DROID_SignatureFile_(V[0-9]+).*",
        "$1");
    }

    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      try {

        for (AIP aip : list) {
          LOGGER.debug("Processing AIP {}", aip.getId());
          boolean inotify = false;
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class,
            AIPState.INGEST_PROCESSING);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
          PluginState reportState = PluginState.SUCCESS;
          ValidationReport validationReport = new ValidationReport();
          List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

          for (Representation representation : aip.getRepresentations()) {
            LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());
            DirectResourceAccess directAccess = null;
            try {
              StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(aip.getId(),
                representation.getId());
              directAccess = storage.getDirectAccess(representationDataPath);

              sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), representation.getId(),
                RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

              CloseableIterable<OptionalWithCause<org.roda.core.data.v2.ip.File>> allFiles = model
                .listFilesUnder(aip.getId(), representation.getId(), true);

              if (!CloseableIterables.isEmpty(allFiles)) {
                String droidOutput = DroidPluginUtils.runDROIDOnPath(directAccess.getPath());

                for (String outputLine : droidOutput.split("\n")) {
                  int splitterPosition = outputLine.lastIndexOf(",");
                  // TODO get file directory path
                  List<String> fileDirectoryPath = new ArrayList<>();
                  String fileId = outputLine.substring(0, splitterPosition);
                  fileId = fileId.substring(fileId.lastIndexOf(File.separatorChar) + 1);
                  String pronom = outputLine.substring(splitterPosition + 1);
                  String xmlOutput = "<droid>" + pronom + "</droid>";
                  ContentPayload payload = new StringContentPayload(xmlOutput);

                  model.createOtherMetadata(aip.getId(), representation.getId(), fileDirectoryPath, fileId, ".xml",
                    RodaConstants.OTHER_METADATA_TYPE_DROID, payload, inotify);

                  PremisV3Utils.updateFormatPreservationMetadata(model, aip.getId(), representation.getId(),
                    fileDirectoryPath, fileId, null, null, pronom, null, false);
                  model.notifyRepresentationUpdated(representation);
                }
              }

            } catch (RODAException e) {
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
            PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, null, reportItem.getPluginState(),
              "", true);
          } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
            | AuthorizationDeniedException | AlreadyExistsException e) {
            LOGGER.error("Error creating event: " + e.getMessage(), e);
          }

          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
        }
      } catch (ClassCastException e) {
        LOGGER.error("Trying to execute an AIP-only plugin with other objects");
        jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new DroidPlugin();
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
    return PreservationEventType.FORMAT_IDENTIFICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Identified the object's file formats and versions using DROID.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File formats were identified and recorded in PREMIS objects.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to identify file formats in the package.";
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
