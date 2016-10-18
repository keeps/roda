/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

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
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
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

  private boolean doFeatureExtraction = true;
  private boolean doFulltextExtraction = false;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, "Feature extraction", PluginParameterType.BOOLEAN, "true",
      true, false,
      "Perform feature extraction from files. This will extract properties such as number of pages, width, height, colour space, etc."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, "Full text extraction", PluginParameterType.BOOLEAN, "true",
      true, false,
      "Extracts full text from document/textual files. Extracted text is used to perform full-text searching on the catalogue."));
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
    return "AIP feature extraction (Apache Tika)";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "The Apache Tika tool extracts technical metadata and text from over a thousand different file types (such as PPT, XLS, and PDF). \nThe task updates PREMIS objects metadata in the Archival Information Package (AIP) to store the results of the characterization process. A PREMIS event is also recorded after the task is run.\nFor more information on this tool, please visit https://tika.apache.org";
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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

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

      try {
        for (AIP aip : list) {
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class,
            AIPState.INGEST_PROCESSING);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
          LOGGER.debug("Processing AIP {}", aip.getId());
          String outcomeDetailExtension = "";
          List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

          try {
            for (Representation representation : aip.getRepresentations()) {
              LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());
              Pair<Report, List<LinkingIdentifier>> tikaResult = TikaFullTextPluginUtils
                .runTikaFullTextOnRepresentation(reportItem, index, model, storage, aip, representation,
                  doFeatureExtraction, doFulltextExtraction);
              reportItem = tikaResult.getFirst();
              sources.addAll(tikaResult.getSecond());
              model.notifyRepresentationUpdated(representation);
            }

            jobPluginInfo.incrementObjectsProcessedWithSuccess();
            reportItem.setPluginState(PluginState.SUCCESS);
          } catch (Exception e) {
            outcomeDetailExtension = e.getMessage();
            LOGGER.error("Error running Tika on AIP " + aip.getId() + ": " + e.getMessage());
            if (reportItem != null) {
              String details = reportItem.getPluginDetails();
              if (details == null) {
                details = "";
              }
              details += e.getMessage();
              reportItem.setPluginDetails(details).setPluginState(PluginState.FAILURE);
            } else {
              LOGGER.error("Error running Apache Tika", e);
            }

            jobPluginInfo.incrementObjectsProcessedWithFailure();
          }

          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);

          try {
            List<LinkingIdentifier> outcomes = null;
            boolean notify = true;
            PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, outcomes,
              reportItem.getPluginState(), outcomeDetailExtension, notify);
          } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
            | AuthorizationDeniedException | AlreadyExistsException e) {
            LOGGER.error("Error creating preservation event", e);
          }
        }
      } catch (ClassCastException e) {
        LOGGER.error("Trying to execute an AIP-only plugin with other objects");
        jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
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
    return "Successfully extracted technical metadata and/or full text from file(s). "
      + "The results of extraction are stored under [REPRESENTATION_ID]/metadata/other/ApacheTika.";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
