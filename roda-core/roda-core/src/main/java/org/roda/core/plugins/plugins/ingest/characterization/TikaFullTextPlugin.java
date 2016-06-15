/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TikaFullTextPlugin extends AbstractPlugin<AIP> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TikaFullTextPlugin.class);

  public static final String FILE_SUFFIX_FULLTEXT = ".fulltext.txt";
  public static final String FILE_SUFFIX_METADATA = ".metadata.xml";

  private boolean createsPluginEvent = true;
  private boolean doFeatureExtraction = true;
  private boolean doFulltextExtraction = false;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT, "Creates plugin event",
        PluginParameterType.BOOLEAN, "true", true, false, "Creates plugin event after executing"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, "Do feature extraction",
        PluginParameterType.BOOLEAN, "true", true, false, "Does Tika feature extraction"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, "Do full text extraction",
        PluginParameterType.BOOLEAN, "true", true, false, "Does Tika full text extraction"));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Feature and/or full-text extraction";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Extraction of technical metadata and/or full-text using Apache Tika";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // updates the flag responsible to allow plugin event creation
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT)) {
      createsPluginEvent = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION)) {
      doFeatureExtraction = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION)) {
      doFulltextExtraction = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (AIP aip : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
        LOGGER.debug("Processing AIP {}", aip.getId());
        Exception ex = null;
        try {
          for (Representation representation : aip.getRepresentations()) {
            LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());
            reportItem = TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(reportItem, index, model, storage, aip,
              representation, doFeatureExtraction, doFulltextExtraction);
            model.notifyRepresentationUpdated(representation);
          }

          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(PluginState.SUCCESS);
        } catch (Exception e) {
          ex = e;
          LOGGER.error("Error running Tika on AIP " + aip.getId() + ": " + e.getMessage());
          if (reportItem != null) {
            String details = reportItem.getPluginDetails();
            if (details == null) {
              details = "";
            }
            details += e.getMessage();
            reportItem.setPluginDetails(details);
          } else {
            LOGGER.error("Error running Apache Tika", e);
          }

          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Error running Tika on AIP " + aip.getId() + ": " + e.getMessage());
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);

        if (createsPluginEvent) {
          try {
            boolean notify = true;
            String outcomeDetailExtension = "";
            if (ex != null) {
              outcomeDetailExtension = ex.getMessage();
            }
            PluginHelper.createPluginEvent(this, aip.getId(), model, index, reportItem.getPluginState(),
              outcomeDetailExtension, notify);
          } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
            | AuthorizationDeniedException | AlreadyExistsException e) {
            LOGGER.error("Error creating preservation event", e);
          }
        }
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
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
    TikaFullTextPlugin tikaPlugin = new TikaFullTextPlugin();
    try {
      tikaPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing " + TikaFullTextPlugin.class.getName() + "init", e);
    }
    return tikaPlugin;
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
    return PreservationEventType.METADATA_EXTRACTION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Extraction of technical metadata using Apache Tika.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Successfully extracted technical metadata from file.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to extract technical metadata from file.";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_FEATURE_EXTRACTION);
  }

}
