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
import org.roda.core.common.IdUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.jobs.Job;
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
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin identifies risk of a file not being characterized.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FileNotCharacterizedRiskAssessmentPlugin extends AbstractPlugin<File> {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FileNotCharacterizedRiskAssessmentPlugin.class);

  /** Plugin version. */
  private static final String VERSION = "1.0";

  /** <i>File not characterized</i> risk ID. */
  private static final String FILE_NOT_CHARACTERIZED_RISK_ID = "urn:filenotcharacterized:r1";

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
  private static final PluginParameter PARAM_PRONOM = new PluginParameter(PRONOM, "PRONOM",
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check existence of a PRONOM Unique Identifier (PUID)?");

  /** Plugin parameter ID 'formatName'. */
  private static final String FORMAT_DESIGNATION_NAME = "formatName";

  /** Plugin parameter 'formatName'. */
  private static final PluginParameter PARAM_FORMAT_DESIGNATION_NAME = new PluginParameter(FORMAT_DESIGNATION_NAME,
    "Format designation name", PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check existence of a Format designation name?");

  /** Plugin parameter ID 'formatVersion'. */
  private static final String FORMAT_DESIGNATION_VERSION = "formatVersion";

  /** Plugin parameter 'formatVersion'. */
  private static final PluginParameter PARAM_FORMAT_DESIGNATION_VERSION = new PluginParameter(
    FORMAT_DESIGNATION_VERSION, "Format designation version", PluginParameter.PluginParameterType.BOOLEAN,
    PARAM_VALUE_TRUE, false, false, "Check existence of a Format designation version?");

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
    return "File not characterized risk assessment";
  }

  @Override
  public String getDescription() {
    return "Check file formatName information (Mimetype, PRONOM and Format designation). "
      + "If this information is missing, it creates a new risk called “File(s) not comprehensively characterized“ "
      + "and assigns the file to that risk in the Risk register.";
  }

  @Override
  public String getVersionImpl() {
    return VERSION;
  }

  @Override
  public Plugin<File> cloneMe() {
    return new FileNotCharacterizedRiskAssessmentPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return Arrays.asList(PARAM_MIMETYPE, PARAM_PRONOM, PARAM_FORMAT_DESIGNATION_NAME, PARAM_FORMAT_DESIGNATION_VERSION);
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
    return "File is comprehensively characterized.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "File is not comprehensively characterized.";
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
   * Check existence of a formatName designation name?
   *
   * @return <code>true</code> if plugin should check the existence of a Format
   *         designation name, <code>false</code> otherwise.
   */
  private boolean checkFormatDesignationName() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(FORMAT_DESIGNATION_NAME));
  }

  /**
   * Check existence of a formatName designation version?
   *
   * @return <code>true</code> if plugin should check the existence of a Format
   *         designation version, <code>false</code> otherwise.
   */
  private boolean checkFormatDesignationVersion() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(FORMAT_DESIGNATION_VERSION));
  }

  @Override
  public Report execute(final IndexService index, final ModelService model, final StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    final Result result = new Result();
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<File>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<File> plugin, File object) {
        result.addResult(executeOnFile(object, index, model, jobPluginInfo, report, cachedJob));
      }
    }, new RODAProcessingLogic<File>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<File> plugin) {
        report.addPluginDetails(result.toString());
      }
    }, index, model, storage, liteList);
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
   * @return a {@link Result} with the number of missing attributes.
   * @throws GenericException
   *           if some error occurred.
   */
  private Result executeOnFile(final File file, final IndexService index, final ModelService model,
    final JobPluginInfo jobPluginInfo, final Report jobReport, final Job job) {
    LOGGER.debug("Processing File {}", file.getId());

    final String fileUUID = IdUtils.getFileId(file);
    final Report fileReport = PluginHelper.initPluginReportItem(this, fileUUID, File.class);

    Result result = new Result();

    try {
      if (!file.isDirectory()) {
        result = assessRiskOnFileFormat(
          index.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN).getFileFormat());
      }

      if (file.isDirectory() || result.isOk()) {
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        fileReport.setPluginState(PluginState.SUCCESS);
        mitigateRisk(file, fileReport, index, model);
      } else {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        fileReport.setPluginState(PluginState.FAILURE);
        openRisk(file, fileReport, result, index, model);
      }

      addToReportDetails(fileReport, result.toString());

    } catch (final NotFoundException | GenericException e) {
      final String message = String.format("Error retrieving IndexedFile for File %s (%s)", file.getId(), fileUUID);
      LOGGER.debug(message, e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      fileReport.setPluginState(PluginState.FAILURE);
      addToReportDetails(fileReport, message);
    }

    createPreservationEvent(file, index, model, fileReport);

    jobReport.addReport(fileReport);
    PluginHelper.updatePartialJobReport(this, model, index, fileReport, true, job);
    return result;
  }

  /**
   * Mitigate risk.
   * 
   * @param file
   *          the {@link File}.
   * @param fileReport
   *          the {@link File} {@link Report}.
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   */
  private void mitigateRisk(final File file, final Report fileReport, final IndexService index,
    final ModelService model) {
    try {

      final RiskIncidence incidence = findUnmitigatedIncidence(file, index);
      incidence.setStatus(RiskIncidence.INCIDENCE_STATUS.MITIGATED);
      model.updateRiskIncidence(incidence, true);
      final String message = String.format("Incidence of risk \"%s\" mitigated for File \"%s\"",
        FILE_NOT_CHARACTERIZED_RISK_ID, file.getId());
      addToReportDetails(fileReport, message);
      LOGGER.info(message);

    } catch (final NotFoundException e) {

      LOGGER.trace(e.getMessage(), e);
      final String message = String.format(
        "File \"%s\" doesn't have an unmitigated incidence of risk \"%s\". Nothing to mitigate.", file.getId(),
        FILE_NOT_CHARACTERIZED_RISK_ID);
      LOGGER.info(message);

    } catch (final GenericException e) {

      final String message = String.format(
        "An internal error occurred searching incidence of risk \"%s\" for File \"%s\". (Technical details: %s).",
        FILE_NOT_CHARACTERIZED_RISK_ID, file, e.getMessage());
      LOGGER.warn(message, e);
      addToReportDetails(fileReport, message);
      fileReport.setPluginState(PluginState.PARTIAL_SUCCESS);

    }
  }

  /**
   * Open (unmitigated) risk incidence.
   * 
   * @param file
   *          the {@link File} with the risk.
   * @param fileReport
   *          the {@link File} {@link Report}.
   * @param result
   *          the {@link File} {@link Result}.
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   */
  private void openRisk(final File file, final Report fileReport, final Result result, final IndexService index,
    final ModelService model) {
    try {

      final RiskIncidence incidence = findUnmitigatedIncidence(file, index);
      model.updateRiskIncidence(incidence, true);
      final String message = String.format(
        "Unmitigated incidence of risk \"%s\" already exists for File \"%s\". Refreshing it.",
        FILE_NOT_CHARACTERIZED_RISK_ID, file.getId());
      LOGGER.info(message);

    } catch (final NotFoundException e) {
      LOGGER.trace(e.getMessage(), e);

      createRiskIncidence(file, fileReport, result, model);
      final String message = String.format("Unmitigated incidence of risk \"%s\" created for File \"%s\".",
        FILE_NOT_CHARACTERIZED_RISK_ID, file.getId());
      addToReportDetails(fileReport, message);
      LOGGER.info(message);

    } catch (final GenericException e) {

      final String message = String.format(
        "An internal error occurred searching incidence of risk \"%s\" for File \"%s\". (Technical details: %s).",
        FILE_NOT_CHARACTERIZED_RISK_ID, file.getId(), e.getMessage());
      LOGGER.warn(message, e);
      addToReportDetails(fileReport, message);
      fileReport.setPluginState(PluginState.PARTIAL_SUCCESS);

    }
  }

  /**
   * Find an unmitigated {@link RiskIncidence} for the specified {@link File}.
   * 
   * @param file
   *          the {@link File}.
   * @param index
   *          the {@link IndexService}.
   * @return the {@link RiskIncidence}.
   * @throws NotFoundException
   *           if an unmitigated risk incidence doesn't exist.
   * @throws GenericException
   *           if some error occurred.
   */
  private RiskIncidence findUnmitigatedIncidence(final File file, final IndexService index)
    throws NotFoundException, GenericException {
    final Filter filter = new Filter(
      Arrays.asList(new SimpleFilterParameter("status", RiskIncidence.INCIDENCE_STATUS.UNMITIGATED.toString()),
        new SimpleFilterParameter("riskId", FILE_NOT_CHARACTERIZED_RISK_ID),
        new SimpleFilterParameter("aipId", file.getAipId()),
        new SimpleFilterParameter("representationId", file.getRepresentationId()),
        new SimpleFilterParameter("fileId", file.getId())));
    // new SimpleFilterParameter("filePath", file.getPath())

    try {
      final List<RiskIncidence> results = index.find(RiskIncidence.class, filter,
        new Sorter(new SortParameter("detectedOn", true)), new Sublist(0, 1), new ArrayList<>()).getResults();
      if (results.isEmpty()) {
        throw new NotFoundException("Couldn't find RiskIncidence matching filter " + filter);
      } else {
        return results.get(0);
      }
    } catch (final RequestNotValidException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  /**
   * Assess the risk for the given {@link FileFormat}.
   * 
   * @param fileFormat
   *          the {@link FileFormat}.
   * @return the {@link Result} of the assessment.
   */
  private Result assessRiskOnFileFormat(final FileFormat fileFormat) {
    final boolean missingFormatName = checkFormatDesignationName()
      && (StringUtils.isBlank(fileFormat.getFormatDesignationName()));
    final boolean missingFormatVersion = checkFormatDesignationVersion()
      && (StringUtils.isBlank(fileFormat.getFormatDesignationVersion()));
    final boolean missingMimetype = checkMimetype() && StringUtils.isBlank(fileFormat.getMimeType());
    final boolean missingPronom = checkPronom() && StringUtils.isBlank(fileFormat.getPronom());
    return new Result(missingFormatName, missingFormatVersion, missingMimetype, missingPronom);
  }

  /**
   * Create a preservation event associated with the specified {@link File}.
   * 
   * @param file
   *          the {@link File}.
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   * @param report
   *          the {@link File} {@link Report}.
   */
  private void createPreservationEvent(final File file, final IndexService index, final ModelService model,
    final Report report) {
    try {
      PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
        model, index, null, null, report.getPluginState(), "", true);
    } catch (final RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      final String message = "Could not create plugin event for file " + file.getId();
      addToReportDetails(report, message);
      report.setPluginState(PluginState.FAILURE);
      LOGGER.error(message, e);
    }
  }

  /**
   * Create a {@link RiskIncidence}.
   * 
   * @param file
   *          the {@link File}.
   * @param report
   *          the {@link Report}.
   * @param result
   *          the execution {@link Result}.
   * @param model
   *          the {@link ModelService}.
   */
  private void createRiskIncidence(final File file, final Report report, final Result result,
    final ModelService model) {
    try {
      final Risk risk = PluginHelper.createRiskIfNotExists(model, 0, FILE_NOT_CHARACTERIZED_RISK_ID,
        getClass().getClassLoader());
      final RiskIncidence incidence = new RiskIncidence();
      incidence.setDetectedOn(new Date());
      incidence.setDetectedBy(this.getName());
      incidence.setRiskId(FILE_NOT_CHARACTERIZED_RISK_ID);
      incidence.setAipId(file.getAipId());
      incidence.setRepresentationId(file.getRepresentationId());
      incidence.setFilePath(file.getPath());
      incidence.setFileId(file.getId());
      incidence.setObjectClass(File.class.getSimpleName());
      incidence.setStatus(RiskIncidence.INCIDENCE_STATUS.UNMITIGATED);
      incidence.setSeverity(risk.getPreMitigationSeverityLevel());
      incidence.setDescription(result.toString());
      model.createRiskIncidence(incidence, false);
    } catch (final RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      final String message = String.format("Error creating risk %s incidence for File %s",
        FILE_NOT_CHARACTERIZED_RISK_ID, file.getId());
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

  /**
   * Result of executing the verification.
   */
  private class Result {
    /**
     * Missing formatName count.
     */
    private int formatName;
    /**
     * Missing formatVersion count.
     */
    private int formatVersion;
    /**
     * Missing mimetype count.
     */
    private int mimetype;
    /**
     * Missing pronom count.
     */
    private int pronom;
    /**
     * Number of results added to this {@link Result}.
     */
    private int count;

    /**
     * Constructor.
     */
    Result() {
      this(0, 0, 0, 0);
    }

    /**
     * Constructor.
     *
     * @param missingFormatName
     *          missing formatName name?
     * @param missingFormatVersion
     *          missing formatName version?
     * @param missingMimetype
     *          missing mimetype?
     * @param missingPronom
     *          missing pronom?
     */
    Result(final boolean missingFormatName, final boolean missingFormatVersion, final boolean missingMimetype,
      final boolean missingPronom) {
      this(missingFormatName ? 1 : 0, missingFormatVersion ? 1 : 0, missingMimetype ? 1 : 0, missingPronom ? 1 : 0);
    }

    /**
     * Constructor.
     * 
     * @param formatName
     *          missing formatName name count.
     * @param formatVersion
     *          missing formatName version count.
     * @param mimetype
     *          missing mimetype count.
     * @param pronom
     *          missing pronom count.
     */
    Result(final int formatName, final int formatVersion, final int mimetype, final int pronom) {
      this.formatName = formatName;
      this.formatVersion = formatVersion;
      this.mimetype = mimetype;
      this.pronom = pronom;
      this.count = 0;
    }

    /**
     * Is everything ok?
     * 
     * @return <code>true</code> if all checks were successful,
     *         <code>false</code> otherwise.
     */
    boolean isOk() {
      return this.formatName == 0 && this.formatVersion == 0 && this.mimetype == 0 && this.pronom == 0;
    }

    /**
     * Add a {@link Result} to this result.
     * 
     * @param result
     *          the {@link Result} to add.
     */
    void addResult(final Result result) {
      this.formatName += result.formatName;
      this.formatVersion += result.formatVersion;
      this.mimetype += result.mimetype;
      this.pronom += result.pronom;
      this.count++;
    }

    @Override
    public String toString() {
      final StringBuilder str = new StringBuilder();
      if (isOk()) {
        if (count > 0) {
          str.append("Files are comprehensively characterized.\n");
        } else {
          str.append("File is comprehensively characterized.\n");
        }
      } else {
        if (count > 0) {
          str.append("Files are not comprehensively characterized.\n");
        } else {
          str.append("File is not comprehensively characterized.\n");
        }
      }
      if (FileNotCharacterizedRiskAssessmentPlugin.this.checkFormatDesignationName()) {
        str.append(String.format("Has format designation name: %s.%n", numberToHuman(this.formatName, count)));
      }
      if (FileNotCharacterizedRiskAssessmentPlugin.this.checkFormatDesignationVersion()) {
        str.append(String.format("Has format designation version: %s.%n", numberToHuman(this.formatVersion, count)));
      }
      if (FileNotCharacterizedRiskAssessmentPlugin.this.checkMimetype()) {
        str.append(String.format("Has mimetype: %s.%n", numberToHuman(this.mimetype, count)));
      }
      if (FileNotCharacterizedRiskAssessmentPlugin.this.checkPronom()) {
        str.append(String.format("Has PRONOM UID: %s.%n", numberToHuman(this.pronom, count)));
      }
      return str.toString();
    }

    /**
     * Takes the <code>number</code> of missing items and the total result
     * <code>count</code> and returns a human-friendly representation (yes/no).
     * 
     * @param number
     *          the number of missing items.
     * @param count
     *          the total result count.
     * @return a {@link String} with the human representation of number.
     */
    private String numberToHuman(final int number, final int count) {
      final String humanString;
      if (count > 0) {
        humanString = Integer.toString(number);
      } else {
        humanString = (number > 0) ? "no" : "yes";
      }
      return humanString;
    }
  }

}
