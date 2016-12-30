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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
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
  private static final String VERSION = "1.1";

  /** Risk ID. */
  private static final String RISK_ID = "urn:FormatMissingRepresentationInformation:r1";

  /** Plugin parameter value 'true'. */
  private static final String PARAM_VALUE_TRUE = "true";

  /** Plugin parameter ID 'mimetype'. */
  private static final String MIMETYPE = "mimetype";

  /** Plugin parameter name 'mimetype'. */
  private static final String MIMETYPE_NAME = "Mimetype";

  /** Plugin parameter 'mimetype'. */
  private static final PluginParameter PARAM_MIMETYPE = new PluginParameter(MIMETYPE, MIMETYPE_NAME,
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false, "Check Mimetype?");

  /** Plugin parameter ID 'pronom'. */
  private static final String PRONOM = "pronom";

  /** Plugin parameter name 'pronom'. */
  private static final String PRONOM_NAME = "PRONOM";

  /** Plugin parameter 'pronom'. */
  private static final PluginParameter PARAM_PRONOM = new PluginParameter(PRONOM, PRONOM_NAME,
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check PRONOM Unique Identifier (PUID)?");

  /** Plugin parameter ID 'format'. */
  private static final String FORMAT_DESIGNATION = "format";

  /** Plugin parameter name 'format'. */
  private static final String FORMAT_DESIGNATION_NAME = "Format designation";

  /** Plugin parameter 'format'. */
  private static final PluginParameter PARAM_FORMAT_DESIGNATION = new PluginParameter(FORMAT_DESIGNATION,
    FORMAT_DESIGNATION_NAME, PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false,
    "Check Format designation name and version?");

  /** Plugin parameter ID 'extension'. */
  private static final String EXTENSION = "extension";

  /** Plugin parameter name 'extension'. */
  private static final String EXTENSION_NAME = "Extension";

  /** Plugin parameter 'extension'. */
  private static final PluginParameter PARAM_EXTENSION = new PluginParameter(EXTENSION, EXTENSION_NAME,
    PluginParameter.PluginParameterType.BOOLEAN, PARAM_VALUE_TRUE, false, false, "Check extension?");

  /** Plugin parameter ID 'matchOne'. */
  private static final String MATCH_ONE = "matchOne";

  /** Plugin parameter 'matchOne'. */
  private static final PluginParameter PARAM_MATCH_ONE = new PluginParameter(MATCH_ONE,
    "Match (at least) one Format type", PluginParameter.PluginParameterType.BOOLEAN, "false", false, false,
    "Don't create risk incidence(s) if at least one of the selected format types is found.");

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
    return "Check file format (Mimetype, PRONOM, Extension and Format designation) in the Format Registry. "
      + "If file format is not present in the Format Registry, it creates a new risk called "
      + "“Comprehensive representation information is missing for some files in the repository“ "
      + "and assigns the file to that risk in the Risk register.";
  }

  @Override
  public String getVersionImpl() {
    return VERSION;
  }

  @Override
  public Report execute(final IndexService index, final ModelService model, final StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    try {
      final Report report = PluginHelper.initPluginReport(this);

      final SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, liteList.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      final List<File> list = PluginHelper.transformLitesIntoObjects(model, index, this, report, jobPluginInfo,
        liteList);

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
    return Arrays.asList(PARAM_MIMETYPE, PARAM_PRONOM, PARAM_FORMAT_DESIGNATION, PARAM_EXTENSION, PARAM_MATCH_ONE);
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
   * Check the format designation?
   *
   * @return <code>true</code> if plugin should search for the format
   *         designation in the Format Registry, <code>false</code> otherwise.
   */
  private boolean checkFormatDesignation() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(FORMAT_DESIGNATION));
  }

  /**
   * Check the extension?
   *
   * @return <code>true</code> if plugin should search for the extension in the
   *         Format Registry, <code>false</code> otherwise.
   */
  private boolean checkExtension() {
    return PARAM_VALUE_TRUE.equalsIgnoreCase(getParameterValues().get(EXTENSION));
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

    final String fileId = IdUtils.getFileId(file);
    final Report fileReport = PluginHelper.initPluginReportItem(this, fileId, File.class, AIPState.ACTIVE);
    PluginHelper.updatePartialJobReport(this, model, index, fileReport, false);

    try {
      final FileFormat fileFormat = index.retrieve(IndexedFile.class, fileId).getFileFormat();
      final FileFormatResult result;
      if (matchAtLeastOneFormatType()) {
        result = new MatchOneResult(fileFormat, index);
      } else {
        result = new MatchAllResult(fileFormat, index);
      }

      if (result.isMissingAttributes()) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        fileReport.setPluginState(PluginState.FAILURE);
      } else if (result.isInRisk()) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        fileReport.setPluginState(PluginState.FAILURE);
        openRisk(file, fileReport, index, model);
      } else {
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        fileReport.setPluginState(PluginState.SUCCESS);
        mitigateRisk(file, fileReport, index, model);
      }
      addToReportDetails(fileReport, result.toString());

    } catch (final NotFoundException | GenericException e) {
      final String message = String.format("Error retrieving IndexedFile for File %s (%s)", file.getId(), fileId);
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
      final String message = String.format("Incidence of risk \"%s\" mitigated for File \"%s\"", RISK_ID, file.getId());
      addToReportDetails(fileReport, message);
      LOGGER.info(message);

    } catch (final NotFoundException e) {

      LOGGER.trace(e.getMessage(), e);
      final String message = String.format(
        "File \"%s\" doesn't have an unmitigated incidence of risk \"%s\". Nothing to mitigate.", file.getId(),
        RISK_ID);
      LOGGER.info(message);

    } catch (final GenericException e) {

      final String message = String.format(
        "An internal error occurred searching incidence of risk \"%s\" for File \"%s\". (Technical details: %s).",
        RISK_ID, file, e.getMessage());
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
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   */
  private void openRisk(final File file, final Report fileReport, final IndexService index, final ModelService model) {
    try {

      final RiskIncidence incidence = findUnmitigatedIncidence(file, index);
      model.updateRiskIncidence(incidence, true);
      final String message = String.format(
        "Unmitigated incidence of risk \"%s\" already exists for File \"%s\". Refreshing it.", RISK_ID, file.getId());
      LOGGER.info(message);

    } catch (final NotFoundException e) {
      LOGGER.trace(e.getMessage(), e);

      createRiskIncidence(file, fileReport, model);
      final String message = String.format("Unmitigated incidence of risk \"%s\" created for File \"%s\".", RISK_ID,
        file.getId());
      addToReportDetails(fileReport, message);
      LOGGER.info(message);

    } catch (final GenericException e) {

      final String message = String.format(
        "An internal error occurred searching incidence of risk \"%s\" for File \"%s\". (Technical details: %s).",
        RISK_ID, file.getId(), e.getMessage());
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
        new SimpleFilterParameter("riskId", RISK_ID), new SimpleFilterParameter("aipId", file.getAipId()),
        new SimpleFilterParameter("representationId", file.getRepresentationId()),
        new SimpleFilterParameter("fileId", file.getId())));
    // new SimpleFilterParameter("filePath", file.getPath())

    try {
      final List<RiskIncidence> results = index
        .find(RiskIncidence.class, filter, new Sorter(new SortParameter("detectedOn", true)), new Sublist(0, 1))
        .getResults();
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
   * Create a {@link RiskIncidence}.
   * 
   * @param file
   *          the {@link File}.
   * @param report
   *          the {@link Report}.
   * @param model
   *          the {@link ModelService}.
   */
  private void createRiskIncidence(final File file, final Report report, final ModelService model) {
    try {
      final Risk risk = PluginHelper.createRiskIfNotExists(model, 0, RISK_ID, getClass().getClassLoader());
      final RiskIncidence incidence = new RiskIncidence();
      incidence.setDetectedOn(new Date());
      incidence.setDetectedBy(this.getName());
      incidence.setRiskId(RISK_ID);
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
      final String message = String.format("Error creating risk %s incidence for File %s", RISK_ID, file.getId());
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
   * This interface is a {@link FileFormat} risk assessment result.
   */
  interface FileFormatResult {
    /**
     * Check if all needed attributes exist in {@link FileFormat}.
     *
     * @return <code>true</code> if all attributes exist, <code>false</code>
     *         otherwise.
     */
    boolean isMissingAttributes();

    /**
     * Check if a {@link FileFormat} is in risk. A {@link FileFormat} is in risk
     * if the Format registry doesn't have a {@link Format} with it's attributes
     * (format designation, mimetype, pronom).
     *
     * @return <code>true</code> if the {@link FileFormat} is in risk,
     *         <code>false</code> otherwise.
     */
    boolean isInRisk();

  }

  /**
   * This is an abstract implementation of a {@link FileFormatResult}.
   * 
   * @author Rui Castro <rui.castro@gmail.com>
   */
  abstract class AbstractResult implements FileFormatResult {
    /**
     * The {@link FileFormat}.
     */
    private final FileFormat fileFormat;

    /**
     * Constructor.
     * 
     * @param format
     *          the {@link FileFormat}.
     */
    AbstractResult(final FileFormat format) {
      this.fileFormat = format;
    }

    /**
     * The {@link List} of {@link FormatResult} matching this {@link FileFormat}
     * .
     * 
     * @return a {@link List<FormatResult>}.
     */
    abstract List<FormatResult> formatResults();

    @Override
    public boolean isMissingAttributes() {
      final boolean missingFormat = checkFormatDesignation()
        && (StringUtils.isBlank(this.fileFormat.getFormatDesignationName())
          || StringUtils.isBlank(this.fileFormat.getFormatDesignationVersion()));
      final boolean missingMimetype = checkMimetype() && StringUtils.isBlank(this.fileFormat.getMimeType());
      final boolean missingPronom = checkPronom() && StringUtils.isBlank(this.fileFormat.getPronom());
      final boolean missingExtension = checkExtension() && StringUtils.isBlank(this.fileFormat.getExtension());
      return missingFormat || missingMimetype || missingPronom || missingExtension;
    }

    @Override
    public boolean isInRisk() {
      return this.formatResults().isEmpty();
    }

    @Override
    public String toString() {
      String str = "";
      if (isMissingAttributes()) {
        str = "File does not have required information (Format designation, MIME type, PRONOM or Extension), "
          + "to be able to find Format representation information.";
      } else if (formatResults().isEmpty()) {
        str += getPreservationEventFailureMessage();
      } else {
        str += String.format("%s%n%n", getPreservationEventSuccessMessage());
        for (FormatResult result : this.formatResults()) {
          str += String.format("%s%n", result);
        }
      }
      return str;
    }

    /**
     * The {@link List} of {@link AttributeCheck} for this result.
     *
     * @return a {@link List<AttributeCheck>}.
     */
    List<AttributeCheck> attributeChecks() {
      return attributeChecks(true);
    }

    /**
     * The {@link List} of {@link AttributeCheck} for this result.
     * 
     * @param present
     *          default value for present attribute.
     * 
     * @return a {@link List<AttributeCheck>}.
     */
    List<AttributeCheck> attributeChecks(final boolean present) {
      final List<AttributeCheck> checks = new ArrayList<>();

      if (checkMimetype()) {
        checks.add(new AttributeCheck(MIMETYPE_NAME, fileFormat.getMimeType(), present,
          new SimpleFilterParameter(RodaConstants.FORMAT_MIMETYPES, fileFormat.getMimeType())));
      }

      if (checkPronom()) {
        checks.add(new AttributeCheck(PRONOM_NAME, fileFormat.getPronom(), present,
          new SimpleFilterParameter(RodaConstants.FORMAT_PRONOMS, fileFormat.getPronom())));
      }

      if (checkExtension()) {
        checks.add(new AttributeCheck(EXTENSION_NAME, fileFormat.getExtension(), present,
          new SimpleFilterParameter(RodaConstants.FORMAT_EXTENSIONS, fileFormat.getExtension())));
      }

      if (checkFormatDesignation()) {

        final FilterParameter mainName = new SimpleFilterParameter(RodaConstants.FORMAT_NAME,
          fileFormat.getFormatDesignationName());
        final FilterParameter alternativeName = new SimpleFilterParameter(RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS,
          fileFormat.getFormatDesignationName());
        final FilterParameter name = new OrFiltersParameters(Arrays.asList(mainName, alternativeName));
        final FilterParameter version = new SimpleFilterParameter(RodaConstants.FORMAT_VERSIONS,
          fileFormat.getFormatDesignationVersion());
        final FilterParameter nameAndVersion = new AndFiltersParameters(Arrays.asList(name, version));
        final FilterParameter nameAndVersionOrName = new OrFiltersParameters(Arrays.asList(nameAndVersion, name));

        final String value = String.format("%s\", \"%s", fileFormat.getFormatDesignationName(),
          fileFormat.getFormatDesignationVersion());

        checks.add(new AttributeCheck(FORMAT_DESIGNATION_NAME, value, present, nameAndVersionOrName));
      }

      return checks;
    }
  }

  /**
   * Implementation of {@link FileFormatResult} that has to match all
   * attributes.
   * 
   * @author Rui Castro <rui.castro@gmail.com>
   */
  class MatchAllResult extends AbstractResult {
    /**
     * List of {@link FormatResult}.
     */
    private List<FormatResult> formatResults = null;
    /**
     * The {@link IndexService}.
     */
    private final IndexService indexService;

    /**
     * Constructor.
     * 
     * @param format
     *          the {@link FileFormat}.
     * @param indexService
     *          the {@link IndexService}.
     */
    MatchAllResult(final FileFormat format, final IndexService indexService) {
      super(format);
      this.indexService = indexService;
    }

    @Override
    List<FormatResult> formatResults() {
      if (this.formatResults == null) {
        this.formatResults = new ArrayList<>();
        final List<FilterParameter> filterParams = new ArrayList<>();

        final List<AttributeCheck> checks = attributeChecks();
        checks.forEach(check -> filterParams.add(check.getFilterParameter()));

        final Iterator<Format> formats = this.indexService.findAll(Format.class, new Filter(filterParams)).iterator();
        formats.forEachRemaining(format -> this.formatResults.add(new FormatResult(format, checks)));
      }
      return this.formatResults;
    }

  }

  /**
   * Implementation of {@link FileFormatResult} that only needs to match one
   * attribute.
   *
   * @author Rui Castro <rui.castro@gmail.com>
   */
  class MatchOneResult extends AbstractResult {
    /**
     * List of {@link FormatResult}.
     */
    private List<FormatResult> formatResults = null;

    /**
     * The {@link IndexService}.
     */
    private final IndexService indexService;

    /**
     * Constructor.
     *
     * @param format
     *          the {@link FileFormat}.
     * @param indexService
     *          the {@link IndexService}.
     */
    MatchOneResult(final FileFormat format, final IndexService indexService) {
      super(format);
      this.indexService = indexService;
    }

    @Override
    List<FormatResult> formatResults() {
      if (this.formatResults == null) {
        this.formatResults = new ArrayList<>();
        for (AttributeCheck check : attributeChecks(true)) {
          final Iterator<Format> formats = this.indexService
            .findAll(Format.class, new Filter(check.getFilterParameter())).iterator();
          formats.forEachRemaining(
            format -> this.formatResults.add(new FormatResult(format, Collections.singletonList(check))));
        }
        consolidateResults();
      }
      return this.formatResults;
    }

    /**
     * Consolidate {@link List} of {@link FormatResult}. Merge repeated
     * {@link FormatResult}s and set default {@link AttributeCheck} (with
     * <code>present = false</code>) to all {@link FormatResult} and all
     * attributes.
     */
    private void consolidateResults() {
      final Map<String, FormatResult> map = new TreeMap<>();
      for (FormatResult result : this.formatResults) {
        if (!map.containsKey(result.id())) {
          map.put(result.id(), new FormatResult(result.format, attributeChecks(false)));
        }
        map.get(result.id()).merge(result);
      }
      this.formatResults.clear();
      this.formatResults.addAll(map.values());
    }

  }

  /**
   * This is a {@link FileFormat} match result.
   * 
   * @author Rui Castro <rui.castro@gmail.com>
   */
  class FormatResult {
    /**
     * The {@link Format}.
     */
    private final Format format;

    /**
     * The {@link Map} of {@link AttributeCheck} used to find the {@link Format}
     * .
     */
    private final Map<String, AttributeCheck> checks;

    /**
     * Constructor.
     * 
     * @param format
     *          the {@link Format}.
     * @param checks
     *          the {@link List<AttributeCheck>}.
     */
    FormatResult(final Format format, final List<AttributeCheck> checks) {
      this.format = format;
      this.checks = new TreeMap<>();
      checks.forEach(c -> this.checks.put(c.name, c));
    }

    /**
     * Return the unique identifier for this {@link FormatResult} which is the
     * same as the inner {@link Format}.
     * 
     * @return a {@link String} with the unique identifier.
     */
    String id() {
      return this.format.getId();
    }

    @Override
    public String toString() {
      String str = String.format("Format \"%s\" (%s)%n", this.format.getName(), this.format.getId());
      for (Map.Entry<String, AttributeCheck> entry : this.checks.entrySet()) {
        str += String.format("\t%s%n", entry.getValue());
      }
      return str;
    }

    /**
     * Merge another {@link FormatResult} checks into this checks.
     * 
     * @param result
     *          the {@link FormatResult} to merge.
     */
    void merge(final FormatResult result) {
      this.checks.putAll(result.checks);
    }
  }

  /**
   * This is a attribute that was used to match a {@link Format}.
   * 
   * @author Rui Castro <rui.castro@gmail.com>
   */
  class AttributeCheck {
    /**
     * The name.
     */
    private final String name;
    /**
     * The value.
     */
    private final String value;
    /**
     * Is it present?
     */
    private final boolean present;
    /**
     * The {@link FilterParameter} corresponding to this check.
     */
    private final FilterParameter filterParameter;

    /**
     * Constructor.
     * 
     * @param name
     *          The name.
     * @param value
     *          The value.
     * @param present
     *          Is it present?
     * @param filterParameter
     *          The {@link FilterParameter} corresponding to this check.
     */
    AttributeCheck(final String name, final String value, final boolean present,
      final FilterParameter filterParameter) {
      this.name = name;
      this.value = value;
      this.present = present;
      this.filterParameter = filterParameter;
    }

    FilterParameter getFilterParameter() {
      return filterParameter;
    }

    @Override
    public String toString() {
      return String.format("%s %s \"%s\"", this.present ? "has" : "does NOT have", this.name, this.value);
    }
  }
}
