/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.dips.DIPUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class DeleteRodaObjectPluginUtils {
  private static final String EVENT_DESCRIPTION = "The process of deleting an object of the repository";

  private DeleteRodaObjectPluginUtils() {
    // do nothing
  }

  public static void process(final IndexService index, final ModelService model, final Report report,
    final Job cachedJob, final JobPluginInfo jobPluginInfo, final Plugin<? extends IsRODAObject> plugin,
    final IsRODAObject object, final String details, final boolean dontCheckRelatives, final boolean doReport) {
    if (object instanceof AIP) {
      processAIP(index, model, report, jobPluginInfo, cachedJob, plugin, (AIP) object, details, dontCheckRelatives,
        doReport);
    } else if (object instanceof File) {
      processFile(index, model, report, jobPluginInfo, cachedJob, plugin, (File) object, details, doReport);
    } else if (object instanceof Representation) {
      processRepresentation(index, model, report, jobPluginInfo, cachedJob, plugin, (Representation) object, details,
        doReport);
    } else if (object instanceof Risk) {
      processRisk(index, model, report, jobPluginInfo, cachedJob, plugin, (Risk) object, doReport);
    } else if (object instanceof RepresentationInformation) {
      processRepresentationInformation(model, report, jobPluginInfo, cachedJob, plugin,
        (RepresentationInformation) object, doReport);
    } else if (object instanceof RiskIncidence) {
      processRiskIncidence(model, report, jobPluginInfo, cachedJob, plugin, (RiskIncidence) object, doReport);
    } else if (object instanceof DIP) {
      processDIP(model, report, jobPluginInfo, cachedJob, plugin, (DIP) object, doReport, true);
    } else if (object instanceof DIPFile) {
      processDIPFile(model, report, jobPluginInfo, cachedJob, plugin, (DIPFile) object, doReport);
    } else if (object instanceof TransferredResource transferredResource) {
      processTransferredResource(model, report, jobPluginInfo, cachedJob, plugin, transferredResource, doReport);
    }
  }

  public static void process(final IndexService index, final ModelService model, final Report report,
    final Job cachedJob, final JobPluginInfo jobPluginInfo, final Plugin<? extends IsRODAObject> plugin,
    final IsRODAObject object, final String details, final boolean dontCheckRelatives) {
    process(index, model, report, cachedJob, jobPluginInfo, plugin, object, details, dontCheckRelatives, true);
  }

  private static void updateReport(ModelService model, Report reportItem, JobPluginInfo jobPluginInfo, Report report,
    Job job, final Plugin<? extends IsRODAObject> plugin, String entityName, String entityId, String disposalType,
    String parentId, String details, final boolean doReport) {

    reportItem.setPluginState(PluginState.FAILURE);

    if (entityName.equals(AIP.class.getSimpleName())) {
      reportItem.addPluginDetails("Could not delete " + entityName + " [id: " + entityId
        + "] due to be associated to a disposal " + disposalType);
    } else {
      reportItem.addPluginDetails("Could not delete " + entityName + " [id: " + entityId + "] due to parent AIP [id: "
        + parentId + "] be associated to a disposal " + disposalType);
    }

    if (doReport) {
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
    }
    jobPluginInfo.incrementObjectsProcessed(reportItem.getPluginState());
    String outcomeText;

    if (entityName.equals(AIP.class.getSimpleName())) {
      outcomeText = entityName + " [id: " + entityId
        + "] has not been manually deleted due to be associated to a disposal " + disposalType;
    } else {
      outcomeText = entityName + " [id: " + entityId + "] has not been manually deleted due to parent AIP [id: "
        + parentId + "] be associated to a disposal " + disposalType;
    }

    List<LinkingIdentifier> sources = new ArrayList<>();
    sources.add(PluginHelper.getLinkingIdentifier(entityId, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

    model.createEvent(entityId, null, null, null, RodaConstants.PreservationEventType.DELETION, EVENT_DESCRIPTION,
      sources, null, reportItem.getPluginState(), outcomeText, details, job.getUsername(), true, null);
  }

  private static void processAIP(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job job, final Plugin<? extends IsRODAObject> plugin, AIP aip, final String details,
    final boolean dontCheckRelatives, final boolean doReport) {

    Report reportItem = PluginHelper.initPluginReportItem(plugin, aip.getId(), AIP.class, AIPState.ACTIVE);
    reportItem.setPluginState(PluginState.SUCCESS);

    if (StringUtils.isNotBlank(aip.getDisposalScheduleId())) {
      updateReport(model, reportItem, jobPluginInfo, report, job, plugin, AIP.class.getSimpleName(), aip.getId(),
        "schedule", null, details, doReport);
    } else if (aip.onHold()) {
      updateReport(model, reportItem, jobPluginInfo, report, job, plugin, AIP.class.getSimpleName(), aip.getId(),
        "hold", null, details, doReport);
    } else {
      if (!dontCheckRelatives) {
        try {
          Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
          index.execute(
            IndexedAIP.class, filter, Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID,
              RodaConstants.AIP_LEVEL, RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL),
            new IndexRunnable<IndexedAIP>() {
              @Override
              public void run(IndexedAIP item)
                throws GenericException, RequestNotValidException, AuthorizationDeniedException {
                PluginState state = PluginState.SUCCESS;
                try {
                  AIP childAip = model.retrieveAIP(item.getId());
                  processLinkedDIP(childAip, index, model, report, reportItem, jobPluginInfo, job, plugin);
                  model.deleteAIP(item.getId());
                } catch (NotFoundException e) {
                  state = PluginState.FAILURE;
                  reportItem.addPluginDetails("Could not delete AIP: " + e.getMessage());
                }

                String outcomeText;
                if (state.equals(PluginState.SUCCESS)) {
                  outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has been manually deleted");
                } else {
                  outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has not been manually deleted");
                }

                List<LinkingIdentifier> sources = new ArrayList<>();
                sources.add(
                  PluginHelper.getLinkingIdentifier(item.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

                model.createEvent(item.getId(), null, null, null, RodaConstants.PreservationEventType.DELETION,
                  EVENT_DESCRIPTION, sources, null, state, outcomeText, details, job.getUsername(), true, null);
              }
            }, e -> {
              reportItem.setPluginState(PluginState.FAILURE);
              reportItem.addPluginDetails("Could not delete sublevel AIPs: " + e.getMessage());
            });
        } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
          reportItem.setPluginState(PluginState.FAILURE);
          reportItem.addPluginDetails("Could not delete sublevel AIPs: " + e.getMessage());
        }
      }

      if (doReport) {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
      }
      jobPluginInfo.incrementObjectsProcessed(reportItem.getPluginState());

      IndexedAIP item = null;
      String outcomeText;

      try {
        item = index.retrieve(IndexedAIP.class, aip.getId(),
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_TITLE));
      } catch (NotFoundException | GenericException e) {
        // do nothing
      }

      try {
        processLinkedDIP(aip, index, model, report, reportItem, jobPluginInfo, job, plugin);
        model.deleteAIP(aip.getId());
        if (item != null) {
          outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has been manually deleted");
        } else {
          outcomeText = "Archival Information Package [id: " + aip.getId() + "] has been manually deleted";
        }
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        reportItem.setPluginState(PluginState.FAILURE);
        reportItem.addPluginDetails("Could not delete AIP: " + e.getMessage());
        if (item != null) {
          outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has not been manually deleted");
        } else {
          outcomeText = "Archival Information Package [id: " + aip.getId() + "] has not been manually deleted";
        }
      }

      List<LinkingIdentifier> sources = new ArrayList<>();
      sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

      model.createEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.DELETION, EVENT_DESCRIPTION,
        sources, null, reportItem.getPluginState(), outcomeText, details, job.getUsername(), true, null);
    }
  }

  private static void deleteFile(File file, IndexService index, ModelService model, Report report,
    JobPluginInfo jobPluginInfo, Report reportItem, final Plugin<? extends IsRODAObject> plugin, final String details,
    Job job) throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    PluginState state = PluginState.SUCCESS;
    try {
      processLinkedDIP(file, index, model, report, reportItem, jobPluginInfo, job, plugin);
      model.deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), job.getUsername(),
        true);
    } catch (NotFoundException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete FILE: " + e.getMessage());
    }

    List<LinkingIdentifier> sources = new ArrayList<>();
    sources.add(PluginHelper.getLinkingIdentifier(file.getAipId(), file.getRepresentationId(), file.getPath(),
      file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

    try {
      // removing related risk incidences
      Filter incidenceFilter = new Filter(
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, file.getAipId()),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, file.getRepresentationId()),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED,
          StringUtils.join(file.getPath(), RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED_SEPARATOR)),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_ID, file.getId()));
      deleteRelatedIncidences(model, index, incidenceFilter);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete file related incidences: " + e.getMessage());
    }

    // removing PREMIS file
    try {
      String pmId = URNUtils.getPremisPrefix(PreservationMetadata.PreservationMetadataType.FILE,
        RODAInstanceUtils.getLocalInstanceIdentifier()) + file.getId();
      model.deletePreservationMetadata(PreservationMetadata.PreservationMetadataType.FILE, file.getAipId(),
        file.getRepresentationId(), pmId, file.getPath(), false);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      reportItem.addPluginDetails("Could not delete associated PREMIS file: " + e.getMessage());
    }

    if (state.equals(PluginState.FAILURE)) {
      reportItem.addPluginDetails("The file '" + file.getId() + "' has been manually deleted.");
    }

    reportItem.setPluginState(state);

    model.createEvent(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      RodaConstants.PreservationEventType.DELETION, EVENT_DESCRIPTION,
        sources, null, state, reportItem.getPluginDetails(), details, job.getUsername(), true, null);

  }

  private static void processFile(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job job, final Plugin<? extends IsRODAObject> plugin, File file, final String details, final boolean doReport) {
    PluginState state = PluginState.SUCCESS;

    Report reportItem = PluginHelper.initPluginReportItem(plugin, IdUtils.getFileId(file), File.class);

    try {
      AIP retrievedAIP = model.retrieveAIP(file.getAipId());
      if (StringUtils.isNotBlank(retrievedAIP.getDisposalScheduleId())) {
        updateReport(model, reportItem, jobPluginInfo, report, job, plugin, File.class.getSimpleName(), file.getId(),
          "schedule", retrievedAIP.getId(), details, doReport);
      } else if (retrievedAIP.onHold()) {
        updateReport(model, reportItem, jobPluginInfo, report, job, plugin, File.class.getSimpleName(), file.getId(),
          "hold", retrievedAIP.getId(), details, doReport);
      } else {
        try {
          Filter filter = new Filter(
            new SimpleFilterParameter(RodaConstants.FILE_ANCESTORS_LIST, IdUtils.getFileId(file)));
          index.execute(
            IndexedFile.class, filter, Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.INDEX_ID,
              RodaConstants.FILE_AIP_ID, RodaConstants.FILE_PATH, RodaConstants.FILE_REPRESENTATION_ID),
            new IndexRunnable<IndexedFile>() {
              @Override
              public void run(IndexedFile item)
                throws GenericException, RequestNotValidException, AuthorizationDeniedException {

                try {
                  File childFile = model.retrieveFile(item.getAipId(), item.getRepresentationId(), item.getPath(),
                    item.getId());
                  deleteFile(childFile, index, model, report, jobPluginInfo, reportItem, plugin, details, job);

                } catch (NotFoundException e) {
                  reportItem.setPluginState(PluginState.FAILURE);
                  reportItem.addPluginDetails("Could not delete sublevel File: " + e.getMessage() + "\n");
                }

              }
            }, e -> {
              reportItem.setPluginState(PluginState.FAILURE);
              reportItem.addPluginDetails("Could not delete sublevel Files: " + e.getMessage());
            });
        } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
          reportItem.setPluginState(PluginState.FAILURE);
          reportItem.addPluginDetails("Could not delete sublevel Files: " + e.getMessage());
        }


        jobPluginInfo.incrementObjectsProcessed(reportItem.getPluginState());

        deleteFile(file, index, model, report, jobPluginInfo, reportItem, plugin, details, job);

        if (doReport) {
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete File: " + e.getMessage());
      if (doReport) {
        report.addReport(reportItem.setPluginState(state));
        PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
      }
      jobPluginInfo.incrementObjectsProcessed(state);
    }

  }

  private static void processRepresentation(IndexService index, ModelService model, Report report,
    JobPluginInfo jobPluginInfo, Job job, final Plugin<? extends IsRODAObject> plugin, Representation representation,
    final String details, final boolean doReport) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(plugin, representation.getId(), Representation.class);

    try {
      AIP retrievedAIP = model.retrieveAIP(representation.getAipId());
      if (StringUtils.isNotBlank(retrievedAIP.getDisposalScheduleId())) {
        updateReport(model, reportItem, jobPluginInfo, report, job, plugin, Representation.class.getSimpleName(),
          representation.getId(), "schedule", retrievedAIP.getId(), details, doReport);
      } else if (retrievedAIP.onHold()) {
        updateReport(model, reportItem, jobPluginInfo, report, job, plugin, Representation.class.getSimpleName(),
          representation.getId(), "hold", retrievedAIP.getId(), details, doReport);
      } else {
        try {
          processLinkedDIP(representation, index, model, report, reportItem, jobPluginInfo, job, plugin);
          model.deleteRepresentation(representation.getAipId(), representation.getId(), job.getUsername());
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
          state = PluginState.FAILURE;
          reportItem.addPluginDetails("Could not delete representation: " + e.getMessage());
        }

        try {
          // removing related risk incidences
          Filter incidenceFilter = new Filter(
            new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, representation.getAipId()),
            new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representation.getId()));
          deleteRelatedIncidences(model, index, incidenceFilter);
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
          state = PluginState.FAILURE;
          reportItem.addPluginDetails("Could not delete representation related incidences: " + e.getMessage());
        }

        // removing PREMIS file
        try {
          String pmId = URNUtils.getPremisPrefix(PreservationMetadata.PreservationMetadataType.REPRESENTATION,
            RODAInstanceUtils.getLocalInstanceIdentifier()) + representation.getId();
          model.deletePreservationMetadata(PreservationMetadata.PreservationMetadataType.REPRESENTATION,
            representation.getAipId(), representation.getId(), pmId, Collections.emptyList(), false);
        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
          reportItem.addPluginDetails("Could not delete associated PREMIS file: " + e.getMessage());
        }
        if (doReport) {
          report.addReport(reportItem.setPluginState(state));
          PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
        }

        jobPluginInfo.incrementObjectsProcessed(state);

        String outcomeText;
        if (state.equals(PluginState.SUCCESS)) {
          outcomeText = "The representation '" + representation.getId() + "' has been manually deleted.";
        } else {
          outcomeText = "The representation '" + representation.getId() + "' has not been manually deleted.";
        }

        List<LinkingIdentifier> sources = new ArrayList<>();
        sources.add(PluginHelper.getLinkingIdentifier(representation.getAipId(), representation.getId(),
          RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

        model.createEvent(representation.getAipId(), representation.getId(), null, null,
          RodaConstants.PreservationEventType.DELETION, EVENT_DESCRIPTION, sources, null, state, outcomeText, details,
          job.getUsername(), true, null);
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete representation: " + e.getMessage());
      if (doReport) {
        report.addReport(reportItem.setPluginState(state));
        PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
      }

      jobPluginInfo.incrementObjectsProcessed(state);
    }
  }

  private static void processRisk(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job job, final Plugin<? extends IsRODAObject> plugin, Risk risk, final boolean doReport) {
    Report reportItem = PluginHelper.initPluginReportItem(plugin, risk.getId(), Risk.class);
    PluginState state = PluginState.SUCCESS;

    try {
      Filter incidenceFilter = new Filter(
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISK_ID, risk.getId()));
      deleteRelatedIncidences(model, index, incidenceFilter);
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      reportItem.addPluginDetails("Could not delete representation related incidences: " + e.getMessage());
      state = PluginState.FAILURE;
    }

    try {
      model.deleteRisk(risk.getId(), true);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
    }
    if (doReport) {
      report.addReport(reportItem.setPluginState(state));
      PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
    }

    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private static void processRepresentationInformation(ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job job, final Plugin<? extends IsRODAObject> plugin, RepresentationInformation ri, final boolean doReport) {
    Report reportItem = PluginHelper.initPluginReportItem(plugin, ri.getId(), RepresentationInformation.class);
    PluginState state = PluginState.SUCCESS;

    try {
      model.deleteRepresentationInformation(ri.getId(), true);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
    }
    if (doReport) {
      report.addReport(reportItem.setPluginState(state));
      PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
    }

    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private static void deleteRelatedIncidences(ModelService model, IndexService index, Filter incidenceFilter)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Long incidenceCounter = index.count(RiskIncidence.class, incidenceFilter);
    IndexResult<RiskIncidence> incidences = index.find(RiskIncidence.class, incidenceFilter, Sorter.NONE,
      new Sublist(0, incidenceCounter.intValue()),
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCE_ID));

    for (RiskIncidence incidence : incidences.getResults()) {
      model.deleteRiskIncidence(incidence.getId(), false);
    }
  }

  private static void processRiskIncidence(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    final Plugin<? extends IsRODAObject> plugin, RiskIncidence incidence, final boolean doReport) {
    Report reportItem = PluginHelper.initPluginReportItem(plugin, incidence.getId(), RiskIncidence.class);
    PluginState state = PluginState.SUCCESS;

    try {
      model.deleteRiskIncidence(incidence.getId(), true);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
    }

    if (doReport) {
      report.addReport(reportItem.setPluginState(state));
      PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
    }
    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private static void processLinkedDIP(IsRODAObject object, IndexService index, ModelService model, Report report,
    Report reportItem, JobPluginInfo jobPluginInfo, Job job, Plugin<? extends IsRODAObject> plugin)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Filter dipFilter = new Filter();
    if (object instanceof AIP aip) {
      dipFilter.add(new SimpleFilterParameter(RodaConstants.DIP_ALL_AIP_UUIDS, aip.getId()));
    } else if (object instanceof Representation representation) {
      dipFilter.add(new SimpleFilterParameter(RodaConstants.DIP_ALL_REPRESENTATION_UUIDS,
        IdUtils.getRepresentationId(representation)));
    } else if (object instanceof File file) {
      dipFilter.add(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, IdUtils.getFileId(file)));
    } else {
      return;
    }

    try (IterableIndexResult<IndexedDIP> allDips = index.findAll(IndexedDIP.class, dipFilter, new ArrayList<>())) {
      for (IndexedDIP indexedDIP : allDips) {
        DIP dip = model.retrieveDIP(indexedDIP.getId());
        List<AIPLink> aipIds = dip.getAipIds();
        List<RepresentationLink> representationIds = dip.getRepresentationIds();
        List<FileLink> fileIds = dip.getFileIds();
        if (object instanceof AIP aip) {
          aipIds.removeIf(aipId -> aipId.getAipId().equals(aip.getId()));
          representationIds.removeIf(repId -> repId.getAipId().equals(aip.getId()));
          fileIds.removeIf(fileId -> fileId.getAipId().equals(aip.getId()));
        } else if (object instanceof Representation representation) {
          representationIds.removeIf(repId -> repId.getAipId().equals(representation.getAipId())
            && repId.getRepresentationId().equals(representation.getId()));
          fileIds.removeIf(fileId -> fileId.getRepresentationId().equals(representation.getId()));
        } else if (object instanceof File file) {
          fileIds.removeIf(fileId -> fileId.getAipId().equals(file.getAipId())
            && fileId.getRepresentationId().equals(file.getRepresentationId())
            && fileId.getPath().equals(file.getPath()) && fileId.getFileId().equals(file.getId()));
        }

        if (aipIds.isEmpty() && representationIds.isEmpty() && fileIds.isEmpty()) {
          processDIP(model, report, jobPluginInfo, job, plugin, dip, false, false);
        } else {
          model.updateDIP(dip);
        }
      }
    } catch (IOException e) {
      reportItem.addPluginDetails("Error removing object link in dip");
    }
  }

  private static void processDIP(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    final Plugin<? extends IsRODAObject> plugin, DIP dip, final boolean doReport, final boolean incrementObjects) {
    Optional<String> deletePlugin = DIPUtils.getDeletePlugin(dip);

    if (deletePlugin.isPresent()) {
      processDIPDelegated(model, report, jobPluginInfo, job, plugin, dip, doReport, deletePlugin.get());
    } else {
      PluginState state = PluginState.SUCCESS;
      Report reportItem = PluginHelper.initPluginReportItem(plugin, dip.getId(), DIP.class);

      try {
        model.deleteDIP(dip.getId());
      } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        reportItem.addPluginDetails("Could not delete DIP: " + e.getMessage());
      }
      if (doReport) {
        report.addReport(reportItem.setPluginState(state));
        PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
      }
      if (incrementObjects) {
        jobPluginInfo.incrementObjectsProcessed(state);
      }
    }
  }

  private static void processDIPDelegated(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    Plugin<? extends IsRODAObject> plugin, DIP dip, boolean doReport, String deletePlugin) {
    try {
      String requestUuid = plugin.getParameterValues().getOrDefault(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID,
        IdUtils.createUUID());
      Plugin<? extends IsRODAObject> externalDeletePlugin = RodaCoreFactory.getPluginManager().getPlugin(deletePlugin);
      Map<String, String> parameters = new HashMap<>(job.getPluginParameters());
      parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
      parameters.put(RodaConstants.PLUGIN_PARAMS_DO_REPORT, String.valueOf(doReport));
      parameters.put(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID, requestUuid);
      try {
        externalDeletePlugin.setParameterValues(parameters);
      } catch (InvalidParameterException e) {
        LoggerFactory.getLogger(DeleteRodaObjectPluginUtils.class).error("Error setting plugin parameters", e);
      }
      List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(model,
        Collections.singletonList(dip));
      externalDeletePlugin.execute(RodaCoreFactory.getIndexService(), model, lites);
    } catch (PluginException e) {
      Report reportItem = PluginHelper.initPluginReportItem(plugin, dip.getId(), DIP.class);
      PluginState state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete DIP using plugin " + deletePlugin + " reason: " + e.getMessage());
      if (doReport) {
        report.addReport(reportItem.setPluginState(state));
        PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
      }
      jobPluginInfo.incrementObjectsProcessed(state);
    }
  }

  private static void processDIPFile(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    final Plugin<? extends IsRODAObject> plugin, DIPFile dipFile, final boolean doReport) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(plugin, dipFile.getId(), DIPFile.class);

    try {
      model.deleteDIPFile(dipFile.getDipId(), dipFile.getPath(), dipFile.getId(), false);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete DIP file: " + e.getMessage());
    }

    if (doReport) {
      report.addReport(reportItem.setPluginState(state));
      PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
    }
    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private static void processTransferredResource(ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job job, final Plugin<? extends IsRODAObject> plugin, TransferredResource transferredResource,
    final boolean doReport) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(plugin, transferredResource.getId(),
      TransferredResource.class);

    try {
      model.deleteTransferredResource(transferredResource);
    } catch (GenericException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete transferred resource file: " + e.getMessage());
    }

    if (doReport) {
      report.addReport(reportItem.setPluginState(state));
      PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
    }
    jobPluginInfo.incrementObjectsProcessed(state);
  }
}
