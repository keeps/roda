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

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractAIPComponentsPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TikaFullTextPlugin<T extends IsRODAObject> extends AbstractAIPComponentsPlugin<T> {
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
    return "Feature extraction (Apache Tika)";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "The Apache Tika tool extracts technical metadata and text from over a thousand different file types (such as PPT, XLS, and PDF). \nThe task updates "
      + "PREMIS objects metadata in the entity to store the results of the characterization process. A PREMIS event is also recorded "
      + "after the task is run.\nFor more information on this tool, please visit https://tika.apache.org";
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

  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<AIP> list, Job job) throws PluginException {

    try {
      for (AIP aip : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);
        LOGGER.debug("Processing AIP {}", aip.getId());
        String outcomeDetailExtension = "";
        List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

        try {
          for (Representation representation : aip.getRepresentations()) {
            LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());

            CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
              representation.getId(), true);

            for (OptionalWithCause<File> oFile : allFiles) {
              if (oFile.isPresent()) {
                File file = oFile.get();

                LinkingIdentifier tikaResult = TikaFullTextPluginUtils.runTikaFullTextOnFile(index, model, storage,
                  file, doFeatureExtraction, doFulltextExtraction);
                sources.add(tikaResult);
              } else {
                LOGGER.error("Cannot process File", oFile.getCause());
              }
            }

            model.notifyRepresentationUpdated(representation);
          }

          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(PluginState.SUCCESS);
        } catch (Exception e) {
          outcomeDetailExtension = e.getMessage();
          LOGGER.error("Error running Tika on AIP {}", aip.getId(), e);
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
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);

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

    return report;
  }

  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException {

    try {
      for (Representation representation : list) {
        LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), representation.getAipId());
        Report reportItem = PluginHelper.initPluginReportItem(this, representation.getId(), Representation.class,
          AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);
        List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();
        String outcomeDetailExtension = "";

        try {
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
            representation.getId(), true);

          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();

              LinkingIdentifier tikaResult = TikaFullTextPluginUtils.runTikaFullTextOnFile(index, model, storage, file,
                doFeatureExtraction, doFulltextExtraction);
              sources.add(tikaResult);
            } else {
              LOGGER.error("Cannot process File", oFile.getCause());
            }
          }

          model.notifyRepresentationUpdated(representation);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(PluginState.SUCCESS);
        } catch (Exception e) {
          outcomeDetailExtension = e.getMessage();
          LOGGER.error("Error running Tika on Representation {}: {}", representation.getId(), e.getMessage());
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
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);

        try {
          List<LinkingIdentifier> outcomes = null;
          boolean notify = true;
          PluginHelper.createPluginEvent(this, representation.getAipId(), representation.getId(), model, index, sources,
            outcomes, reportItem.getPluginState(), outcomeDetailExtension, notify);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating preservation event", e);
        }
      }

    } catch (ClassCastException e) {
      LOGGER.error("Trying to execute an Representation-only plugin with other objects");
      jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
    }

    return report;
  }

  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException {

    List<RepresentationLink> representationsToUpdate = new ArrayList<RepresentationLink>();

    for (File file : list) {
      LOGGER.debug("Processing file {} of representation {} of AIP {}", file.getId(), file.getRepresentationId(),
        file.getAipId());
      Report reportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class, AIPState.INGEST_PROCESSING);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);
      List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();
      String outcomeDetailExtension = "";

      try {
        LinkingIdentifier tikaResult = TikaFullTextPluginUtils.runTikaFullTextOnFile(index, model, storage, file,
          doFeatureExtraction, doFulltextExtraction);
        sources.add(tikaResult);

        RepresentationLink link = new RepresentationLink(file.getAipId(), file.getRepresentationId());
        if (!representationsToUpdate.contains(link)) {
          representationsToUpdate.add(link);
        }
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);

      } catch (Exception e) {
        outcomeDetailExtension = e.getMessage();
        LOGGER.error("Error running Tika on File {}: {}", file.getId(), e.getMessage());
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
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);

      try {
        List<LinkingIdentifier> outcomes = null;
        boolean notify = true;
        PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          model, index, sources, outcomes, reportItem.getPluginState(), outcomeDetailExtension, notify);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating preservation event", e);
      }
    }

    for (RepresentationLink link : representationsToUpdate) {
      try {
        Representation representation = model.retrieveRepresentation(link.getAipId(), link.getRepresentationId());
        model.notifyRepresentationUpdated(representation);
      } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
        LOGGER.error("Error updating representation after running Tika plugin");
      }
    }

    return report;
  }

  @Override
  public Plugin<T> cloneMe() {
    TikaFullTextPlugin<T> tikaPlugin = new TikaFullTextPlugin<T>();
    try {
      tikaPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing {} init", TikaFullTextPlugin.class.getName(), e);
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    return (List) list;
  }

}
