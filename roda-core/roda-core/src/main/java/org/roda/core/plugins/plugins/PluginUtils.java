/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.roda.core.data.Attribute;
import org.roda.core.data.Report;
import org.roda.core.data.ReportItem;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.JobReport;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.fs.FSUtils;
import org.w3c.util.DateParser;

public final class PluginUtils {

  private PluginUtils() {
  }

  public static Report createPluginReport(Plugin<?> plugin) {
    Report report = new Report();
    report.setType(Report.TYPE_PLUGIN_REPORT);
    report.setTitle("Report of plugin " + plugin.getName());

    report.addAttribute(new Attribute("Agent name", plugin.getName()))
      .addAttribute(new Attribute("Agent version", plugin.getVersion()))
      .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));

    return report;
  }

  public static ReportItem createPluginReportItem(TransferredResource transferredResource, Plugin<?> plugin) {
    return createPluginReportItem(plugin, "Extract SIP", null, transferredResource.getId());
  }

  public static ReportItem createPluginReportItem(Plugin<?> plugin, String title, String itemId, String otherId) {
    ReportItem reportItem = new ReportItem(title);
    if (itemId != null) {
      reportItem.setItemId(itemId);
    }
    if (otherId != null) {
      reportItem.setOtherId(otherId);
    }
    reportItem.addAttribute(new Attribute("Agent name", plugin.getName()))
      .addAttribute(new Attribute("Agent version", plugin.getVersion()))
      .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));
    return reportItem;
  }

  public static String getJobId(Map<String, String> pluginParameters) {
    return pluginParameters.get(RodaConstants.PLUGIN_PARAMS_JOB_ID);
  }

  public static Job getJobFromIndex(IndexService index, Map<String, String> pluginParameters)
    throws NotFoundException, GenericException {
    return index.retrieve(Job.class, getJobId(pluginParameters));
  }
  //
  // public static List<AIP> getJobAIPs(IndexService index, Map<String, String>
  // pluginParameters)
  // throws IndexServiceException, NotFoundException {
  // // FIXME this should be a constant and the maximum number of objects sent
  // // to a plugin
  // int maxAips = 200;
  // IndexResult<AIP> aipsFromIndex = index.find(AIP.class,
  // new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID,
  // new ArrayList<>(getJobFromIndex(index,
  // pluginParameters).getObjectIdsToAipIds().values()))),
  // null, new Sublist(0, maxAips));
  // return aipsFromIndex.getResults();
  //
  // }

  public static void createJobReport(ModelService model, String jobId, String objectId) {
    JobReport jobReport = new JobReport();
    jobReport.setId(UUID.randomUUID().toString());
    jobReport.setJobId(jobId);
    jobReport.setObjectId(objectId);
    Date currentDate = new Date();
    jobReport.setDateCreated(currentDate);
    jobReport.setDateUpdated(currentDate);
    Report report = new Report();
    jobReport.setReport(report);

    model.createJobReport(jobReport);
  }

  public static void createJobReport(ModelService model, Plugin<?> plugin, ReportItem reportItem,
    PluginState pluginState, String jobId) {
    JobReport jobReport = new JobReport();
    jobReport.setId(UUID.randomUUID().toString());
    jobReport.setJobId(jobId);
    jobReport.setAipId(reportItem.getItemId());
    jobReport.setObjectId(reportItem.getOtherId());
    Date currentDate = new Date();
    jobReport.setDateCreated(currentDate);
    jobReport.setDateUpdated(currentDate);
    jobReport.setLastPluginRan(plugin.getClass().getName());
    jobReport.setLastPluginRanState(pluginState);

    Report report = new Report();
    report.addItem(reportItem);
    jobReport.setReport(report);

    model.createJobReport(jobReport);

  }

  public static void updateJobReport(ModelService model, IndexService index, Plugin<?> plugin, ReportItem reportItem,
    PluginState pluginState, String jobId, String aipId)
      throws NotFoundException, GenericException, RequestNotValidException {

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, jobId));
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_AIP_ID, aipId));
    Sorter sorter = null;
    Sublist sublist = new Sublist();
    IndexResult<JobReport> find = index.find(JobReport.class, filter, sorter, sublist);
    if (!find.getResults().isEmpty()) {

      JobReport jobReport = find.getResults().get(0);

      jobReport.setLastPluginRan(plugin.getClass().getName());
      jobReport.setLastPluginRanState(pluginState);
      jobReport.getReport().addItem(reportItem);
      jobReport.setDateUpdated(new Date());

      model.updateJobReport(jobReport);
    } else {
      // FIXME log error
    }

  }

  public static EventPreservationObject createPluginEvent(String aipID, String representationID, ModelService model,
    String type, String details, String agentRole, String agentID, List<String> objectIDs, String outcome,
    String detailNote, String detailExtension) throws PremisMetadataException, IOException, RequestNotValidException,
      NotFoundException, GenericException, AuthorizationDeniedException {
    EventPreservationObject epo = new EventPreservationObject();
    epo.setDatetime(new Date());
    epo.setEventType(type);
    epo.setEventDetail(details);
    epo.setAgentRole(agentRole);
    String name = UUID.randomUUID().toString();
    epo.setId(name);
    epo.setAgentID(agentID);
    epo.setObjectIDs(objectIDs.toArray(new String[objectIDs.size()]));
    epo.setOutcome(outcome);
    epo.setOutcomeDetailNote(detailNote);
    epo.setOutcomeDetailExtension(detailExtension);
    byte[] serializedPremisEvent = new PremisEventHelper(epo).saveToByteArray();
    Path file = Files.createTempFile("preservation", ".xml");
    Files.copy(new ByteArrayInputStream(serializedPremisEvent), file, StandardCopyOption.REPLACE_EXISTING);
    Binary resource = (Binary) FSUtils.convertPathToResource(file.getParent(), file);
    if (representationID == null) { // "AIP Event"
      model.createPreservationMetadata(aipID, name, resource);
    } else { // "Representation Event"
      model.createPreservationMetadata(aipID, representationID, name, resource);
    }
    return epo;
  }
}
