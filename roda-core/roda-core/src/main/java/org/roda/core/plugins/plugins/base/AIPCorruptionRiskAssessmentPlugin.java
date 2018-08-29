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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
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
import org.roda.core.plugins.RODAObjectProcessingLogicNew;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
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
    risks = new ArrayList<>();
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
      + "that risk in the Risk register.\n It also creates an incidence linked to the representation if a PREMIS file exists but "
      + "the associated file does not. Within the repository, fixity checking is used to ensure that digital files have not been "
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
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogicNew<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        processAIP(index, model, storage, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job job, AIP aip) {
    boolean aipFailed = false;
    List<LinkingIdentifier> sources = new ArrayList<>();
    ValidationReport validationReport = new ValidationReport();

    for (Representation r : aip.getRepresentations()) {
      LOGGER.debug("Checking fixity for files in representation {} of AIP {}", r.getId(), aip.getId());

      try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), r.getId(), true)) {
        for (OptionalWithCause<File> oFile : allFiles) {
          if (oFile.isPresent()) {
            File file = oFile.get();

            if (!file.isDirectory()) {
              StoragePath storagePath = ModelUtils.getFileStoragePath(file);
              Binary currentFileBinary = storage.getBinary(storagePath);
              List<Fixity> fixities = null;

              try {
                Binary premisFile = model.retrievePreservationFile(file);
                fixities = PremisV3Utils.extractFixities(premisFile);
              } catch (NotFoundException e) {
                ValidationIssue issue = new ValidationIssue(
                  "File " + file.getId() + " of representation " + file.getRepresentationId() + " of AIP "
                    + file.getAipId() + " was found but the PREMIS file does not exist");
                validationReport.addIssue(issue);
              }

              sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), file.getRepresentationId(), file.getPath(),
                file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

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

                      String fileEntry = file.getRepresentationId()
                        + (file.getPath().isEmpty() ? "" : '/' + String.join("/", file.getPath())) + '/' + file.getId();
                      ValidationIssue issue = new ValidationIssue(
                        fileEntry + " (Checksums: [" + f.getMessageDigest().trim() + ", " + checksum.trim() + "])");
                      validationReport.addIssue(issue);

                      break;
                    }
                  }
                } catch (NoSuchAlgorithmException | IOException e) {
                  passedFixity = false;
                  ValidationIssue issue = new ValidationIssue("Could not check fixity: " + e.getMessage());
                  validationReport.addIssue(issue);
                  LOGGER.debug("Could not check fixity", e);
                }

                if (passedFixity) {
                  updateIncidence(model, index, file.getAipId(), file.getRepresentationId(), file.getPath(),
                    file.getId(), risks.get(0));
                } else {
                  aipFailed = true;
                  createIncidence(model, index, file.getAipId(), file.getRepresentationId(), file.getPath(),
                    file.getId(), risks.get(0));
                }
              } else {
                aipFailed = true;
                createIncidence(model, index, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
                  risks.get(0));
              }
            }
          }
        }

        CloseableIterable<OptionalWithCause<PreservationMetadata>> pmList = model.listPreservationMetadata(aip.getId(),
          r.getId());

        for (OptionalWithCause<PreservationMetadata> opm : pmList) {
          if (opm.isPresent()) {
            PreservationMetadata pm = opm.get();
            if (PreservationMetadataType.FILE.equals(pm.getType())) {
              try {
                model.retrieveFile(pm.getAipId(), pm.getRepresentationId(), pm.getFileDirectoryPath(), pm.getFileId());
              } catch (NotFoundException e) {
                ValidationIssue issue = new ValidationIssue(
                  "File " + pm.getFileId() + " of representation " + pm.getRepresentationId() + " of AIP "
                    + pm.getAipId() + " was not found but the PREMIS file exists");
                validationReport.addIssue(issue);
                aipFailed = true;
                createIncidence(model, index, aip.getId(), pm.getRepresentationId(), pm.getFileDirectoryPath(),
                  pm.getFileId(), risks.get(0));
              }
            }
          }
        }
      } catch (IOException | RODAException | XmlException e) {
        LOGGER.error("Error processing representation {}", r.getId(), e);
      }
    }

    try {
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);

      if (aipFailed) {
        reportItem.setPluginState(PluginState.FAILURE).setHtmlPluginDetails(true)
          .setPluginDetails(validationReport.toHtml(false, false, false, "Corrupted files and checksums"));
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, null, PluginState.FAILURE,
          validationReport.toHtml(false, false, false, "Corrupted files and their checksums"), true);
      } else {
        reportItem.setPluginState(PluginState.SUCCESS).setPluginDetails("Fixity checking ran successfully");
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, null, PluginState.SUCCESS, "", true);
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.error("Could not create a Fixity Plugin event");
    }
  }

  private void createIncidence(ModelService model, IndexService index, String aipId, String representationId,
    List<String> filePath, String fileId, String riskId)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    List<RiskIncidence> results = getUnmitigatedIncidences(index, aipId, representationId, filePath, fileId, riskId);

    if (results.isEmpty()) {
      Risk risk = PluginHelper.createRiskIfNotExists(model, riskId, getClass().getClassLoader());
      RiskIncidence incidence = new RiskIncidence();
      incidence.setDetectedOn(new Date());
      incidence.setDetectedBy(this.getName());
      incidence.setRiskId(riskId);
      incidence.setAipId(aipId);
      incidence.setRepresentationId(representationId);

      if (filePath != null) {
        incidence.setFilePath(filePath);
      }

      incidence.setFileId(fileId);
      incidence.setObjectClass(AIP.class.getSimpleName());
      incidence.setStatus(INCIDENCE_STATUS.UNMITIGATED);
      incidence.setSeverity(risk.getPreMitigationSeverityLevel());
      model.createRiskIncidence(incidence, false);
    }
  }

  private void updateIncidence(ModelService model, IndexService index, String aipId, String representationId,
    List<String> filePath, String fileId, String riskId) throws GenericException, RequestNotValidException {
    List<RiskIncidence> results = getUnmitigatedIncidences(index, aipId, representationId, filePath, fileId, riskId);

    for (RiskIncidence incidence : results) {
      incidence.setStatus(INCIDENCE_STATUS.MITIGATED);
      model.updateRiskIncidence(incidence, false);
    }
  }

  private List<RiskIncidence> getUnmitigatedIncidences(IndexService index, String aipId, String representationId,
    List<String> filePath, String fileId, String riskId) throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId),
      new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISK_ID, riskId),
      new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_STATUS, INCIDENCE_STATUS.UNMITIGATED.toString()));

    if (representationId != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representationId));
    }

    if (filePath != null && !filePath.isEmpty()) {
      filter.add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED,
        StringUtils.join(filePath, RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED_SEPARATOR)));
    } else {
      filter.add(new EmptyKeyFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED));
    }

    if (fileId != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_ID, fileId));
    }

    IndexResult<RiskIncidence> incidences = index.find(RiskIncidence.class, filter, Sorter.NONE, new Sublist(0, 1),
      new ArrayList<>());
    return incidences.getResults();
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
