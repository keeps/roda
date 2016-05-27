/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixityPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FixityPlugin.class);

  private static String riskId = "R36";

  @Override
  public void init() {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Fixity check";
  }

  @Override
  public String getDescription() {
    return "Computes the fixity check on AIP files";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    return parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = new SimpleJobPluginInfo(list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (AIP aip : list) {
        boolean isAipFailed = false;

        for (Representation r : aip.getRepresentations()) {
          LOGGER.debug("Checking fixity for files in representation " + r.getId() + " of AIP " + aip.getId());

          try {
            boolean recursive = true;
            CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), r.getId(),
              recursive);

            List<String> okFileIDS = new ArrayList<String>();
            List<String> koFileIDS = new ArrayList<String>();
            for (OptionalWithCause<File> oFile : allFiles) {
              if (oFile.isPresent()) {
                File file = oFile.get();
                if (!file.isDirectory()) {

                  StoragePath storagePath = ModelUtils.getFileStoragePath(file);
                  Binary currentFileBinary = storage.getBinary(storagePath);
                  Binary premisFile = model.retrievePreservationFile(file);
                  List<Fixity> fixities = PremisV3Utils.extractFixities(premisFile);

                  if (fixities != null) {
                    boolean fixityOK = true;
                    for (Fixity f : fixities) {
                      try {
                        Fixity currentFixity = PremisV3Utils.calculateFixity(currentFileBinary,
                          f.getMessageDigestAlgorithm(), "FixityCheck action");

                        if (!f.getMessageDigest().trim().equalsIgnoreCase(currentFixity.getMessageDigest().trim())) {
                          fixityOK = false;
                          break;
                        }
                      } catch (NoSuchAlgorithmException nsae) {
                        fixityOK = false;
                        break;
                      }
                    }
                    if (fixityOK) {
                      // TODO support file path
                      okFileIDS.add(file.getId());
                    } else {
                      koFileIDS.add(file.getId());
                      isAipFailed = true;

                      try {
                        model.retrieveRisk(riskId);
                      } catch (NotFoundException e) {
                        PluginHelper.createDefaultRisk(model, riskId, FixityPlugin.class);
                      }

                      model.addRiskIncidence(riskId, file.getAipId(), file.getRepresentationId(), file.getPath(),
                        file.getId(), "RiskIncidence");
                    }
                  }

                } else {
                  LOGGER.warn("Cannot process File", oFile.getCause());
                }
              }
            }

            if (koFileIDS.size() > 0) {
              LOGGER.debug("Fixity error for representation " + r.getId() + " of AIP " + aip.getId());
              StringBuilder sb = new StringBuilder();
              sb.append("<p>The following files have bad checksums:</p>");
              sb.append("<ul>");
              for (String s : koFileIDS) {
                sb.append("<li>" + s + "</li>");
              }
              sb.append("</ul>");

              // TODO FIXE PREMIS EVENT CREATION
              /*
               * PreservationMetadata pm = PluginHelper.createPluginEvent(this,
               * aip.getId(), r.getId(), null, null, model,
               * RodaConstants.PRESERVATION_EVENT_TYPE_FIXITY_CHECK,
               * "Checksums recorded in PREMIS were compared with the files in the repository"
               * , Arrays.asList(IdUtils.getLinkingIdentifierId(aip.getId(),
               * r.getId(), null, null)), null, "failure", "Reason",
               * sb.toString(), inotify);
               * notifyUserOfFixityCheckError(r.getId(), okFileIDS, koFileIDS,
               * pm);
               */
            } else {
              /*
               * LOGGER.debug("Fixity OK for representation " + r.getId() +
               * " of AIP " + aip.getId()); PreservationMetadata pm =
               * PluginHelper.createPluginEvent(this, aip.getId(), r.getId(),
               * null, null, model,
               * RodaConstants.PRESERVATION_EVENT_TYPE_FIXITY_CHECK,
               * "Checksums recorded in PREMIS were compared with the files in the repository"
               * , Arrays.asList(r.getId()), null, "success", okFileIDS.size() +
               * " files checked successfully", okFileIDS.toString(), inotify);
               * notifyUserOfFixityCheckSuccess(r.getId(), okFileIDS, koFileIDS,
               * pm);
               */
            }

            IOUtils.closeQuietly(allFiles);
            model.notifyAIPUpdated(aip.getId());
          } catch (IOException | RODAException | XmlException e) {
            LOGGER.error("Error processing Representation " + r.getId() + " - " + e.getMessage(), e);
          }

        }

        try {
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIPState.ACTIVE);

          if (!isAipFailed) {
            reportItem.setPluginState(PluginState.SUCCESS).setPluginDetails("Fixity checking ran successfully");
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
            PluginHelper.createPluginEvent(this, aip.getId(), model, index, PluginState.SUCCESS, "", true);
          } else {
            reportItem.setPluginState(PluginState.FAILURE).setPluginDetails("Fixity checking did not run successfully");
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            PluginHelper.createPluginEvent(this, aip.getId(), model, index, PluginState.FAILURE, "", true);
          }

          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
          | ValidationException | AlreadyExistsException e) {
          LOGGER.error("Could not create a Fixity Plugin event");
        }
      }

      jobPluginInfo.done();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    }
    return report;
  }

  private void notifyUserOfFixityCheckUndetermined(String representationID, PreservationMetadata event,
    String message) {
    // TODO Auto-generated method stub
  }

  private void notifyUserOfFixityCheckSuccess(String representationID, List<String> okFileIDS, List<String> koFileIDS,
    PreservationMetadata event) {
    // TODO Auto-generated method stub
  }

  private void notifyUserOfFixityCheckError(String representationID, List<String> okFileIDS, List<String> koFileIDS,
    PreservationMetadata event) {
    // TODO Auto-generated method stub
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new FixityPlugin();
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
    return PreservationEventType.FIXITY_CHECK;
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_FIXITY_CHECK);
  }
}
