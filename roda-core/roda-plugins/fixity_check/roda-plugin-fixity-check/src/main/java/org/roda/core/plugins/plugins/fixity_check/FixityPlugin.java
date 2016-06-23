/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.fixity_check;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
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
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.Fixity;
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
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixityPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FixityPlugin.class);

  private static String riskId = "R36";

  @Override
  public void init() {
    // do nothing
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      try {
        for (AIP aip : list) {
          boolean aipFailed = false;

          for (Representation r : aip.getRepresentations()) {
            LOGGER.debug("Checking fixity for files in representation " + r.getId() + " of AIP " + aip.getId());

            try {
              List<String> passedFiles = new ArrayList<String>();
              List<String> failedFiles = new ArrayList<String>();
              boolean recursive = true;
              CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), r.getId(),
                recursive);

              for (OptionalWithCause<File> oFile : allFiles) {
                if (oFile.isPresent()) {
                  File file = oFile.get();

                  if (!file.isDirectory()) {
                    StoragePath storagePath = ModelUtils.getFileStoragePath(file);
                    Binary currentFileBinary = storage.getBinary(storagePath);
                    Binary premisFile = model.retrievePreservationFile(file);
                    List<Fixity> fixities = PremisV3Utils.extractFixities(premisFile);

                    if (fixities != null) {
                      boolean passedFixity = true;

                      // get all necessary hash algorithms
                      Set<String> algorithms = new HashSet<>();
                      for (Fixity f : fixities) {
                        algorithms.add(f.getMessageDigestAlgorithm());
                      }

                      // calculate hashes
                      try {
                        Map<String, String> checksums = FileUtility
                          .checksums(currentFileBinary.getContent().createInputStream(), algorithms);

                        for (Fixity f : fixities) {
                          String checksum = checksums.get(f.getMessageDigestAlgorithm());

                          if (!f.getMessageDigest().trim().equalsIgnoreCase(checksum.trim())) {
                            passedFixity = false;
                            break;
                          }
                        }
                      } catch (NoSuchAlgorithmException e) {
                        passedFixity = false;
                        // TODO add exception to plugin report
                        LOGGER.debug("Could not check fixity", e);
                      }

                      if (passedFixity) {
                        // TODO support file path
                        passedFiles.add(file.getId());
                      } else {
                        failedFiles.add(file.getId());
                        aipFailed = true;

                        PluginHelper.createRiskIfNotExists(model, riskId, getClass());
                        model.addRiskIncidence(riskId, file.getAipId(), file.getRepresentationId(), file.getPath(),
                          file.getId());
                      }
                    }

                  } else {
                    LOGGER.warn("Cannot process File ", oFile.getCause());
                  }
                }
              }

              IOUtils.closeQuietly(allFiles);
              model.notifyAIPUpdated(aip.getId());
            } catch (IOException | RODAException | XmlException e) {
              LOGGER.error("Error processing Representation " + r.getId() + " - " + e.getMessage(), e);
            }
          }

          try {
            Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);

            if (aipFailed) {
              reportItem.setPluginState(PluginState.FAILURE)
                .setPluginDetails("Fixity checking did not run successfully");
              jobPluginInfo.incrementObjectsProcessedWithFailure();
              PluginHelper.createPluginEvent(this, aip.getId(), model, index, PluginState.FAILURE, "", true);
            } else {
              reportItem.setPluginState(PluginState.SUCCESS).setPluginDetails("Fixity checking ran successfully");
              jobPluginInfo.incrementObjectsProcessedWithSuccess();
              PluginHelper.createPluginEvent(this, aip.getId(), model, index, PluginState.SUCCESS, "", true);
            }

            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
            | ValidationException | AlreadyExistsException e) {
            LOGGER.error("Could not create a Fixity Plugin event");
          }

        }
      } catch (ClassCastException e) {
        LOGGER.error("Trying to execute an AIP-only plugin with other objects");
        jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (

    JobException e) {
      LOGGER.error("Could not update Job information");
    }

    return report;
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
