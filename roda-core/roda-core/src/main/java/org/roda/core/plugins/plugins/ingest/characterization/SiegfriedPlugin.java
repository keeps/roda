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
import java.util.List;
import java.util.Map;

import org.roda.core.common.IdUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
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

public class SiegfriedPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPlugin.class);
  public static final String FILE_SUFFIX = ".json";

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "File format identification (Siegfried)";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Identifies the file format and version of data files included in Information Packages using the Siegfried tool (a signature-based file format "
      + "identification tool that supports PRONOM identifiers and Mimetypes).\nThe task updates PREMIS objects metadata in the Information Package to store "
      + "the results of format identification. A PREMIS event is also recorded after the task is run.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return SiegfriedPluginUtils.getVersion();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, liteList.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      Job job = PluginHelper.getJob(this, model);
      List<T> list = PluginHelper.transformLitesIntoObjects(model, index, this, report, jobPluginInfo, liteList, job);

      if (!list.isEmpty()) {
        if (list.get(0) instanceof AIP) {
          report = executeOnAIP(index, model, storage, report, jobPluginInfo, (List<AIP>) list, job);
        } else if (list.get(0) instanceof Representation) {
          report = executeOnRepresentation(index, model, storage, report, jobPluginInfo, (List<Representation>) list,
            job);
        } else if (list.get(0) instanceof File) {
          report = executeOnFile(index, model, storage, report, jobPluginInfo, (List<File>) list, job);
        }
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException | AuthorizationDeniedException | NotFoundException | GenericException
      | RequestNotValidException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<AIP> list, Job job) throws PluginException {
    try {
      for (AIP aip : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);

        LOGGER.debug("Processing AIP {}", aip.getId());
        List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();
        try {

          for (Representation representation : aip.getRepresentations()) {
            LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());
            sources.addAll(SiegfriedPluginUtils.runSiegfriedOnRepresentation(this, model, representation));
            model.notifyRepresentationUpdated(representation);
          }

          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(PluginState.SUCCESS);
        } catch (PluginException | NotFoundException | GenericException | RequestNotValidException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error running Siegfried " + aip.getId() + ": " + e.getMessage(), e);

          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Error running Siegfried " + aip.getId() + ": " + e.getMessage());
        }

        try {
          List<LinkingIdentifier> outcomes = null;
          boolean notify = true;
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, outcomes,
            reportItem.getPluginState(), "", notify);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: " + e.getMessage(), e);
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
      }
    } catch (ClassCastException e) {
      LOGGER.error("Trying to execute an AIP-only plugin with other objects");
      jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
    }

    return report;
  }

  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException {

    for (Representation representation : list) {
      List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class, AIPState.ACTIVE);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);
      LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), representation.getAipId());
      try {
        sources.addAll(SiegfriedPluginUtils.runSiegfriedOnRepresentation(this, model, representation));
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (PluginException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error running Siegfried " + representation.getAipId() + ": " + e.getMessage(), e);

        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error running Siegfried " + representation.getAipId() + ": " + e.getMessage());
      }

      try {
        List<LinkingIdentifier> outcomes = null;
        boolean notify = true;
        PluginHelper.createPluginEvent(this, representation.getAipId(), representation.getId(), model, index, sources,
          outcomes, reportItem.getPluginState(), "", notify);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: " + e.getMessage(), e);
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
    }

    return report;
  }

  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException {

    for (File file : list) {
      List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class, AIPState.ACTIVE);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);
      LOGGER.debug("Processing file {} from representation {} of AIP {}", file.getId(), file.getRepresentationId(),
        file.getAipId());

      try {
        // FIXME 20161117 nvieira this should be done with a single file
        sources.addAll(SiegfriedPluginUtils.runSiegfriedOnFile(this, model, file));
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (PluginException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error running Siegfried on file " + file.getId() + ": " + e.getMessage(), e);

        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error running Siegfried on file " + file.getId() + ": " + e.getMessage());
      }

      try {
        List<LinkingIdentifier> outcomes = null;
        boolean notify = true;
        PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          model, index, sources, outcomes, reportItem.getPluginState(), "", notify);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: " + e.getMessage(), e);
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
    }

    return report;
  }

  @Override
  public Plugin<T> cloneMe() {
    SiegfriedPlugin<T> siegfriedPlugin = new SiegfriedPlugin<T>();
    try {
      siegfriedPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing " + SiegfriedPlugin.class.getName() + "init", e);
    }
    return siegfriedPlugin;
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
    return PreservationEventType.FORMAT_IDENTIFICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Identified the object's file formats and versions using Siegfried.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File formats were identified and recorded in PREMIS objects.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to identify file formats in the package.";
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
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    return (List) list;
  }

}
