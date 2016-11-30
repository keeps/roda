/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.risks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.validation.ValidationException;
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
 * Format Missing Representation Information risk assessment plugin.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FormatMissingRepresentationInformationPlugin extends AbstractPlugin<File> {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FormatMissingRepresentationInformationPlugin.class);

  /** Plugin version. */
  private static final String VERSION = "1.0";

  /** Risk ID. */
  private static final String RISK_ID = "urn:FormatMissingRepresentationInformation:r1";

  /** Plugin parameter value 'true'. */
  private static final String PARAM_VALUE_TRUE = "true";

  /** Plugin parameter ID 'mimetype'. */
  private static final String MIMETYPE = "mimetype";

  /** Plugin parameter 'mimetype'. */
  private static final PluginParameter PARAM_MIMETYPE = new PluginParameter(MIMETYPE, "Mimetype",
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false, "Check Mimetype?");

  /** Plugin parameter ID 'pronom'. */
  private static final String PRONOM = "pronom";

  /** Plugin parameter 'pronom'. */
  private static final PluginParameter PARAM_PRONOM = new PluginParameter(PRONOM, "PRONOM",
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check PRONOM Unique Identifier (PUID)?");

  /** Plugin parameter ID 'format'. */
  private static final String FORMAT_DESIGNATION = "format";

  /** Plugin parameter 'format'. */
  private static final PluginParameter PARAM_FORMAT_DESIGNATION = new PluginParameter(FORMAT_DESIGNATION,
    "Format designation", PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check Format designation name and version?");

  /** Plugin parameter ID 'matchOne'. */
  private static final String MATCH_ONE = "matchOne";

  /** Plugin parameter 'matchOne'. */
  private static final PluginParameter PARAM_MATCH_ONE = new PluginParameter(MATCH_ONE,
    "Match (at least) one format type", PluginParameter.PluginParameterType.BOOLEAN, "false", false, false,
    "Don't create risk incidence(s) if at least one of the selected format types is found.");

  /**
   * String format for {@link Format} name and version.
   */
  private static final String FORMAT_NAME_PATTERN = "%1$s, version %2$s";

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
    return "Format missing representation information risk assessment";
  }

  @Override
  public String getDescription() {
    return "Check file format (Mimetype, PRONOM and Format designation) in the Format Registry. "
      + "If File Format is not present in the Format Registry, it creates a new risk called "
      + "“Comprehensive representation information is missing for some files in the repository“ "
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
        executeOnFile(file, index, model, jobPluginInfo, report);
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
    return new FormatMissingRepresentationInformationPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return Arrays.asList(PARAM_MIMETYPE, PARAM_PRONOM, PARAM_FORMAT_DESIGNATION, PARAM_MATCH_ONE);
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.RISK_MANAGEMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return getName();
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File format has Representation information.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "File format doesn't have Representation information.";
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
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_RISK_MANAGEMENT);
  }

  @Override
  public List<Class<File>> getObjectClasses() {
    return Collections.singletonList(File.class);
  }

  /**
   * Check the mimetype?
   * 
   * @return <code>true</code> if plugin should search for the mimetype in the
   *         Format Registry, <code>false</code> otherwise.
   */
  private boolean checkMimetype() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(MIMETYPE));
  }

  /**
   * Check the PRONOM Unique Identifier (PUID)?
   *
   * @return <code>true</code> if plugin should search for the PRONOM Unique
   *         Identifier (PUID) in the Format Registry, <code>false</code>
   *         otherwise.
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
   * Match at least one format type?
   *
   * @return <code>true</code> if plugin should not create a risk incidence if
   *         at least one format type is found, <code>false</code> otherwise.
   */
  private boolean matchAtLeastOneFormatType() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(MATCH_ONE));
  }

  /**
   * Execute verifications on a single {@link File}.
   *
   * @param file
   *          the {@link File}.
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   * @param jobPluginInfo
   *          the {@link JobPluginInfo}
   * @param jobReport
   *          the {@link Report}.
   */
  private void executeOnFile(final File file, final IndexService index, final ModelService model,
    final JobPluginInfo jobPluginInfo, final Report jobReport) {
    LOGGER.debug("Processing File {}", file.getId());

    final String fileUUID = IdUtils.getFileId(file);
    final Report fileReport = PluginHelper.initPluginReportItem(this, fileUUID, File.class, AIPState.ACTIVE);
    PluginHelper.updatePartialJobReport(this, model, index, fileReport, false);

    try {
      final FileFormat fileFormat = index.retrieve(IndexedFile.class, fileUUID).getFileFormat();

      if (isMissingAttributes(fileFormat) || isInRisk(fileFormat, index)) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        fileReport.setPluginState(PluginState.FAILURE);
        addToReportDetails(fileReport, getPreservationEventFailureMessage());
        createIncidence(model, file, RISK_ID, fileReport);
      } else {
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        fileReport.setPluginState(PluginState.SUCCESS);
        addToReportDetails(fileReport, getPreservationEventSuccessMessage());
      }

    } catch (final NotFoundException | GenericException e) {
      final String message = String.format("Error retrieving IndexedFile for File %s (%s)", file.getId(), fileUUID);
      LOGGER.debug(message, e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      fileReport.setPluginState(PluginState.FAILURE);
      addToReportDetails(fileReport, message);
    }

    try {
      PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
        model, index, null, null, fileReport.getPluginState(), "", true);
    } catch (final RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      final String message = "Could not create plugin event for file " + file.getId();
      addToReportDetails(fileReport, message);
      fileReport.setPluginState(PluginState.FAILURE);
      LOGGER.error(message, e);
    }

    jobReport.addReport(fileReport);
    PluginHelper.updatePartialJobReport(this, model, index, fileReport, true);
  }

  /**
   * Check if a {@link FileFormat} is in risk. A {@link FileFormat} is in risk
   * if the Format registry doesn't have a {@link Format} with it's attributes
   * (format designation, mimetype, pronom).
   *
   * @param fileFormat
   *          the {@link FileFormat} to check.
   * @param index
   *          the {@link IndexService}.
   * @return <code>true</code> if the {@link FileFormat} is in risk,
   *         <code>false</code> otherwise.
   */
  private boolean isInRisk(final FileFormat fileFormat, final IndexService index) {

    try {

      final List<FilterParameter> filterParams = getFilterParameters(fileFormat);

      if (matchAtLeastOneFormatType()) {
        int matchCount = 0;
        for (int i = 0; i < filterParams.size() && matchCount == 0; i++) {
          matchCount += index.count(Format.class, new Filter(filterParams.get(i)));
        }
        return matchCount == 0;
      } else {
        return index.count(Format.class, new Filter(filterParams)) == 0;
      }

    } catch (final RequestNotValidException | GenericException e) {
      LOGGER.error("Error searching for format in registry.", e);
      return false;
    }

  }

  /**
   * Check if all needed attributes exist in {@link FileFormat}.
   *
   * @param fileFormat
   *          the {@link FileFormat}.
   * @return <code>true</code> if all attributes exist, <code>false</code>
   *         otherwise.
   */
  private boolean isMissingAttributes(final FileFormat fileFormat) {
    final boolean missingFormat = checkFormatDesignation()
      && (StringUtils.isBlank(fileFormat.getFormatDesignationName())
        || StringUtils.isBlank(fileFormat.getFormatDesignationVersion()));
    final boolean missingMimetype = checkMimetype() && StringUtils.isBlank(fileFormat.getMimeType());
    final boolean missingPronom = checkPronom() && StringUtils.isBlank(fileFormat.getPronom());
    return missingFormat || missingMimetype || missingPronom;
  }

  /**
   * Get the {@link FilterParameter}s for the specified {@link FileFormat}
   * format type attributes.
   * 
   * @param fileFormat
   *          the {@link FileFormat}.
   * @return a {@link List<FilterParameter>}.
   */
  private List<FilterParameter> getFilterParameters(final FileFormat fileFormat) {
    final List<FilterParameter> filterParams = new ArrayList<>();

    if (checkMimetype()) {
      filterParams.add(new SimpleFilterParameter(RodaConstants.FORMAT_MIMETYPES, fileFormat.getMimeType()));
    }

    if (checkPronom()) {
      filterParams.add(new SimpleFilterParameter(RodaConstants.FORMAT_PRONOMS, fileFormat.getPronom()));
    }

    if (checkFormatDesignation()) {
      final String formatNamePattern = RodaCoreFactory.getRodaConfiguration().getString(
        "core.plugins.external.FormatMissingRepresentationInformation.format_name_pattern", FORMAT_NAME_PATTERN);
      final String name = String.format(formatNamePattern, fileFormat.getFormatDesignationName(),
        fileFormat.getFormatDesignationVersion());
      filterParams.add(new SimpleFilterParameter(RodaConstants.FORMAT_NAME, name));
    }

    return filterParams;
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
      final Risk risk = PluginHelper.createRiskIfNotExists(model, 0, riskId, getClass().getClassLoader());
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
      incidence.setDescription("Comprehensive representation information is missing.");
      model.createRiskIncidence(incidence, false);
    } catch (final RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      final String message = String.format("Error creating risk %s incidence for File %s", riskId, file.getId());
      addToReportDetails(report, message);
      report.setPluginState(PluginState.FAILURE);
      LOGGER.error(message, e);
    }
  }

  /**
   * Add a message to the {@link Report} details.
   *
   * @param report
   *          the {@link Report}.
   * @param message
   *          the message to add.
   */
  private void addToReportDetails(final Report report, final String message) {
    final String pluginDetails;
    if (StringUtils.isBlank(report.getPluginDetails())) {
      pluginDetails = message;
    } else {
      pluginDetails = String.format("%n%n%s", message);
    }
    report.addPluginDetails(pluginDetails);
  }

}
