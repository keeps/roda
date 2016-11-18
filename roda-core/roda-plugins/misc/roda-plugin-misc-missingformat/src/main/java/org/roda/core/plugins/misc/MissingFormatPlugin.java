/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.misc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.IdUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin identifies risk of missing format information in files.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class MissingFormatPlugin extends AbstractPlugin<File> {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MissingFormatPlugin.class);

  /** Plugin version. */
  private static final String VERSION = "1.0";

  /** <i>Unknown mimetype</i> risk ID. */
  private static final String MISSING_MIMETYPE_RISK_ID = "urn:missingmimetype:r1";

  /** <i>Unknown pronom PUID</i> risk ID. */
  private static final String MISSING_PRONOM_RISK_ID = "urn:missingpronom:r1";

  /** <i>Unknown format</i> risk ID. */
  private static final String MISSING_FORMAT_RISK_ID = "urn:missingformat:r1";

  /** Plugin parameter value 'true'. */
  private static final String PARAM_VALUE_TRUE = "true";

  /** Plugin parameter ID 'mimetype'. */
  private static final String MIMETYPE = "mimetype";

  /** Plugin parameter 'mimetype'. */
  private static final PluginParameter PARAM_MIMETYPE = new PluginParameter(MIMETYPE, "Mimetype",
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false, "Check existence of a Mimetype?");

  /** Plugin parameter ID 'pronom'. */
  private static final String PRONOM = "pronom";

  /** Plugin parameter 'pronom'. */
  private static final PluginParameter PARAM_PRONOM = new PluginParameter(PRONOM, "Pronom ID",
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check existence of a PRONOM Unique Identifier (PUID)?");

  /** Plugin parameter ID 'format'. */
  private static final String FORMAT_DESIGNATION = "format";

  /** Plugin parameter 'format'. */
  private static final PluginParameter PARAM_FORMAT_DESIGNATION = new PluginParameter(FORMAT_DESIGNATION,
    "Format designation", PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check existence of a Format designation?");

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
    return "File with missing format information risk assessment";
  }

  @Override
  public String getDescription() {
    return "Check file format information (Mimetype, Pronom ID and Format designation). "
      + "If this information is missing a, it creates a new risk called "
      + "“<Mimetype/Pronom ID/Format designation> information missing“ "
      + "and assigns the file to that risk in the Risk register.";
  }

  @Override
  public String getVersionImpl() {
    return VERSION;
  }

  @Override
  public Report execute(final IndexService index, final ModelService model, final StorageService storage,
    final List<File> list) throws PluginException {

    try {
      final Report report = PluginHelper.initPluginReport(this);
      final SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());

      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (File file : list) {
        executeOnIndexedFile(file, index, model, jobPluginInfo, report);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      return report;

    } catch (final JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }
  }

  @Override
  public Plugin<File> cloneMe() {
    return new MissingFormatPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return Arrays.asList(PARAM_MIMETYPE, PARAM_PRONOM, PARAM_FORMAT_DESIGNATION);
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.FORMAT_IDENTIFICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked for the presence of format identification information.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File format information is present.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "File format information is missing.";
  }

  @Override
  public Report beforeAllExecute(final IndexService index, final ModelService model, final StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(final IndexService index, final ModelService model, final StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_MISC);
  }

  @Override
  public List<Class<File>> getObjectClasses() {
    return Collections.singletonList(File.class);
  }

  /**
   * Check existence of a mimetype?
   * 
   * @return <code>true</code> if plugin should check the existence of a
   *         mimetype, <code>false</code> otherwise.
   */
  private boolean checkMimetype() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(MIMETYPE));
  }

  /**
   * Check existence of a PRONOM Unique Identifier (PUID)?
   *
   * @return <code>true</code> if plugin should check the existence of a PRONOM
   *         Unique Identifier (PUID), <code>false</code> otherwise.
   */
  private boolean checkPronom() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(PRONOM));
  }

  /**
   * Check existence of a Format?
   *
   * @return <code>true</code> if plugin should check the existence of a Format
   *         designation, <code>false</code> otherwise.
   */
  private boolean checkFormatDesignation() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(FORMAT_DESIGNATION));
  }

  /**
   * Execute verifications on a single {@link IndexedFile}.
   *
   * @param file
   *          the {@link File}.
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   * @param jobPluginInfo
   *          the {@link JobPluginInfo}
   * @param report
   *          the {@link Report}.
   */
  private void executeOnIndexedFile(final File file, final IndexService index, final ModelService model,
    final JobPluginInfo jobPluginInfo, final Report report) {
    LOGGER.debug("Processing File {}", file.getId());

    final String fileUUID = IdUtils.getFileId(file);
    final Report reportItem = PluginHelper.initPluginReportItem(this, fileUUID, File.class, AIPState.ACTIVE);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);

    try {
      final IndexedFile indexedFile = index.retrieve(IndexedFile.class, fileUUID);
      boolean fileOk = true;

      if (checkMimetype() && StringUtils.isBlank(indexedFile.getFileFormat().getMimeType())) {
        createIncidence(model, file, MISSING_MIMETYPE_RISK_ID, reportItem);
        fileOk = false;
      }
      if (checkPronom() && StringUtils.isBlank(indexedFile.getFileFormat().getPronom())) {
        createIncidence(model, file, MISSING_PRONOM_RISK_ID, reportItem);
        fileOk = false;
      }
      if (checkFormatDesignation() && StringUtils.isBlank(indexedFile.getFileFormat().getFormatDesignationName())) {
        createIncidence(model, file, MISSING_FORMAT_RISK_ID, reportItem);
        fileOk = false;
      }

      if (fileOk) {
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
      } else {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE);
      }

    } catch (final NotFoundException | GenericException e) {
      LOGGER.debug(String.format("Error retrieving IndexedFile for File %s (%s)", file.getId(), fileUUID), e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setPluginState(PluginState.FAILURE);
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
  }

  /**
   * Create a {@link RiskIncidence}.
   * 
   * @param model
   *          the {@link ModelService}.
   * @param file
   *          the {@link File}.
   * @param riskId
   *          the risk ID.
   * @param report
   *          the {@link Report}.
   */
  private void createIncidence(final ModelService model, final File file, final String riskId, final Report report) {
    try {
      final Risk risk = PluginHelper.createRiskIfNotExists(model, 0, riskId, getClass());
      final RiskIncidence incidence = new RiskIncidence();
      incidence.setDetectedOn(new Date());
      incidence.setDetectedBy(this.getName());
      incidence.setRiskId(riskId);
      incidence.setAipId(file.getAipId());
      incidence.setRepresentationId(file.getRepresentationId());
      incidence.setFilePath(file.getPath());
      incidence.setFileId(file.getId());
      incidence.setObjectClass(File.class.getSimpleName());
      incidence.setStatus(RiskIncidence.INCIDENCE_STATUS.UNMITIGATED);
      incidence.setSeverity(risk.getPreMitigationSeverityLevel());
      model.createRiskIncidence(incidence, false);
    } catch (final RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      final String message = String.format("Error creating risk %s incidence for File %s", riskId, file.getId());
      report.setPluginState(PluginState.FAILURE).setPluginDetails(message);
      LOGGER.error(message, e);

    }
  }

}
