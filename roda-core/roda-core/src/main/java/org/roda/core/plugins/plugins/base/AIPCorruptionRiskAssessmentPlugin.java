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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;
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
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIPCorruptionRiskAssessmentPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPCorruptionRiskAssessmentPlugin.class);

  private static List<String> risks;
  static {
    risks = new ArrayList<String>();
    risks.add("urn:fixityplugin:r1");
  }

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
    return "AIP corruption risk assessment";
  }

  @Override
  public String getDescription() {
    return "Computes the fixity/checksum information of files inside an Archival Information Package (AIP) and verifies if this "
      + "information differs from the information stored in the preservation metadata (i.e. PREMIS objects). If so, it creates a "
      + "new risk called “File(s) corrupted due to hardware malfunction or human intervention“ and assigns the corrupted file to "
      + "that risk in the Risk register.\nWithin the repository, fixity checking is used to ensure that digital files have not been "
      + "affected by data rot or other digital preservation dangers. By itself, fixity checking does not ensure the preservation "
      + "of a digital file. Instead, it allows a repository to identify which corrupted files to replace with a clean copy from "
      + "the producer or from a backup.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, liteList.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      Job job = PluginHelper.getJob(this, model);
      List<AIP> list = PluginHelper.transformLitesIntoObjects(model, index, this, report, jobPluginInfo, liteList, job);

      try {
        for (AIP aip : list) {
          boolean aipFailed = false;
          List<String> passedFiles = new ArrayList<String>();
          Map<String, Pair<String, String>> failedFiles = new HashMap<>();
          List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

          for (Representation r : aip.getRepresentations()) {
            LOGGER.debug("Checking fixity for files in representation {} of AIP {}", r.getId(), aip.getId());

            try {
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
                    sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), file.getRepresentationId(),
                      file.getPath(), file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

                    String fileEntry = file.getRepresentationId()
                      + (file.getPath().isEmpty() ? "" : '/' + String.join("/", file.getPath())) + '/' + file.getId();

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
                            failedFiles.put(fileEntry, new Pair<>(f.getMessageDigest().trim(), checksum.trim()));
                            break;
                          }
                        }
                      } catch (NoSuchAlgorithmException e) {
                        passedFixity = false;
                        // TODO add exception to plugin report
                        LOGGER.debug("Could not check fixity", e);
                      }

                      if (passedFixity) {
                        passedFiles.add(fileEntry);
                      } else {
                        aipFailed = true;
                        createIncidence(model, file, risks.get(0));
                      }
                    }
                  }
                }
              }

              IOUtils.closeQuietly(allFiles);
              model.notifyAIPUpdated(aip.getId());
            } catch (IOException | RODAException | XmlException e) {
              LOGGER.error("Error processing Representation {}", r.getId(), e);
            }
          }

          try {
            Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);

            if (aipFailed) {
              ValidationReport validationReport = new ValidationReport();

              for (Entry<String, Pair<String, String>> entry : failedFiles.entrySet()) {
                ValidationIssue issue = new ValidationIssue(entry.getKey() + " (Checksums: ["
                  + entry.getValue().getFirst() + ", " + entry.getValue().getSecond() + "])");
                validationReport.addIssue(issue);
              }

              reportItem.setPluginState(PluginState.FAILURE).setHtmlPluginDetails(true)
                .setPluginDetails(validationReport.toHtml(false, false, false, "Corrupted files and their checksums"));
              jobPluginInfo.incrementObjectsProcessedWithFailure();
              PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, null, PluginState.FAILURE,
                validationReport.toHtml(false, false, false, "Corrupted files and their checksums"), true);
            } else {
              reportItem.setPluginState(PluginState.SUCCESS).setPluginDetails("Fixity checking ran successfully");
              jobPluginInfo.incrementObjectsProcessedWithSuccess();
              PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, null, PluginState.SUCCESS, "",
                true);
            }

            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
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
    } catch (JobException | AuthorizationDeniedException | NotFoundException | GenericException
      | RequestNotValidException e) {
      LOGGER.error("Could not update Job information");
    }

    return report;
  }

  private void createIncidence(ModelService model, File file, String riskId) throws RequestNotValidException,
    GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    Risk risk = PluginHelper.createRiskIfNotExists(model, 0, risks.get(0), getClass().getClassLoader());
    RiskIncidence incidence = new RiskIncidence();
    incidence.setDetectedOn(new Date());
    incidence.setDetectedBy(this.getName());
    incidence.setRiskId(risks.get(0));
    incidence.setAipId(file.getAipId());
    incidence.setRepresentationId(file.getRepresentationId());
    incidence.setFilePath(file.getPath());
    incidence.setFileId(file.getId());
    incidence.setObjectClass(AIP.class.getSimpleName());
    incidence.setStatus(INCIDENCE_STATUS.UNMITIGATED);
    incidence.setSeverity(risk.getPreMitigationSeverityLevel());
    model.createRiskIncidence(incidence, false);
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AIPCorruptionRiskAssessmentPlugin();
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
    return "Computed the fixity information of files inside the AIP and compared to fixity information recorded in preservation metadata";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Fixity of files inside the AIP has been assessed and there was no evidence of corruption";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Test of the fixity information of files inside AIPs failed";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_RISK_MANAGEMENT);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
