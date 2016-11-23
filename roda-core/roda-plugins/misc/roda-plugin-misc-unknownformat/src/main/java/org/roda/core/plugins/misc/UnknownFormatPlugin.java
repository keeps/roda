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
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.filter.Filter;
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
 * Plugin identifies risk of files with unknown formats.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class UnknownFormatPlugin extends AbstractPlugin<File> {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(UnknownFormatPlugin.class);

  /** Plugin version. */
  private static final String VERSION = "1.0";

  /** <i>Unknown mimetype</i> risk ID. */
  private static final String UNKNOWN_MIMETYPE_RISK_ID = "urn:unknownmimetype:r1";

  /** <i>Unknown PRONON ID</i> risk ID. */
  private static final String UNKNOWN_PRONOM_RISK_ID = "urn:unknownpronom:r1";

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
    return "Unknown format risk assessor";
  }

  @Override
  public String getDescription() {
    return "Check file format (Mimetype and PRONOM) in the Format Registry. "
      + "If File Format is not present in the Format Registry, it creates a new risk called "
      + "“<Mimetype/PRONOM> is unknown“ " + "and assigns the file to that risk in the Risk register.";
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
    return new UnknownFormatPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return Arrays.asList(PARAM_MIMETYPE, PARAM_PRONOM);
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
    return "Searched for file format in Format Registry.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File format is known.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "File format is unknown.";
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
   * @param report
   *          the {@link Report}.
   */
  private void executeOnFile(final File file, final IndexService index, final ModelService model,
    final JobPluginInfo jobPluginInfo, final Report report) {
    LOGGER.debug("Processing File {}", file.getId());

    final String fileUUID = IdUtils.getFileId(file);
    final Report reportItem = PluginHelper.initPluginReportItem(this, fileUUID, File.class, AIPState.ACTIVE);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);

    try {
      final FileFormat fileFormat = index.retrieve(IndexedFile.class, fileUUID).getFileFormat();
      boolean fileOk = true;

      final String mimetype = fileFormat.getMimeType();
      if (checkMimetype() && StringUtils.isNotBlank(mimetype)
        && !findFileFormat(index, RodaConstants.FORMAT_MIMETYPES, mimetype)) {
        createIncidence(model, file, UNKNOWN_MIMETYPE_RISK_ID, reportItem);
        fileOk = false;
      }
      final String pronom = fileFormat.getPronom();
      if (checkPronom() && StringUtils.isNotBlank(pronom)
        && !findFileFormat(index, RodaConstants.FORMAT_PRONOMS, pronom)) {
        createIncidence(model, file, UNKNOWN_PRONOM_RISK_ID, reportItem);
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

    try {
      PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
        model, index, null, null, reportItem.getPluginState(), "", true);
    } catch (final RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.error("Could not create plugin event for file " + file.getId(), e);
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
  }

  /**
   * Check if a Format exists with the specified attribute.
   * 
   * @param index
   *          the {@link IndexService}.
   * @param key
   *          the attribute key.
   * @param value
   *          the attribute value.
   * @return <code>true</code> if a {@link Format} exists with the specified
   *         key/value pair, <code>true</code> otherwise.
   */
  private boolean findFileFormat(final IndexService index, final String key, final String value) {
    try {
      return index.count(Format.class, new Filter(new SimpleFilterParameter(key, value))) > 0;
    } catch (final RequestNotValidException | GenericException e) {
      LOGGER.error(String.format("Error searching for %s '%s'.", key, value), e);
      return false;
    }
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
      model.createRiskIncidence(incidence, false);
    } catch (final RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      final String message = String.format("Error creating risk %s incidence for File %s", riskId, file.getId());
      report.setPluginState(PluginState.FAILURE).setPluginDetails(message);
      LOGGER.error(message, e);
    }
  }

}
