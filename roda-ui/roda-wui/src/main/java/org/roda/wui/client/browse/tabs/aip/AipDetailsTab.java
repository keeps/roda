package org.roda.wui.client.browse.tabs.aip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.widgets.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class AipDetailsTab extends GenericMetadataCardPanel<BrowseAIPResponse> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public AipDetailsTab(BrowseAIPResponse aip) {
    setData(aip);
  }

  @Override
  protected FlowPanel createHeaderWidget(BrowseAIPResponse data) {
    return null;
  }

  @Override
  protected void buildFields(BrowseAIPResponse data) {
    IndexedAIP aip = data.getIndexedAIP();

    addSeparator(messages.detailsAIP());
    buildField(messages.itemId()).withWidget(createIdHTML(data)).build();
    buildField(messages.aipCreated())
      .withValue(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getCreatedOn()), aip.getCreatedBy()))
      .build();
    buildField(messages.aipUpdated())
      .withValue(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getUpdatedOn()), aip.getUpdatedBy()))
      .build();
    buildField(messages.aipLevel()).withWidget(createAipLevelHTML(data)).build();
    buildField(messages.aipType()).withWidget(createAipTypeHTML(data)).build();

    if (!aip.getIngestSIPIds().isEmpty()) {
      addSeparator(messages.detailsIngest());

      FlowPanel sipIds = new FlowPanel();
      sipIds.addStyleName("generic-multiline");
      for (String ingestSIPId : aip.getIngestSIPIds()) {
        sipIds.add(new HTMLPanel("span", ingestSIPId));
      }

      buildField(messages.sipId()).withWidget(sipIds).build();
    }

    boolean canAccessJobs = PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_JOB);
    if (aip.getIngestJobId() != null && !aip.getIngestJobId().isEmpty() && canAccessJobs) {
      FlowPanel jobIdsList = new FlowPanel();
      jobIdsList.addStyleName("generic-multiline");

      jobIdsList.add(new HTMLPanel("span", aip.getIngestJobId()));

      // If some error occurs during the retrieval of the job information, we still
      // want to show the job id
      buildField(messages.processIdTitle()).withWidget(jobIdsList).build();

      Services service = new Services("Retrieve AIP jobs information", "get");

      List<String> jobIds = new ArrayList<>();

      jobIds.add(aip.getIngestJobId());
      jobIds.addAll(aip.getIngestUpdateJobIds());

      List<CompletableFuture<Job>> futures = jobIds.stream()
        .map(jobId -> service.jobsResource(s -> s.getJobFromModel(jobId))).collect(Collectors.toList());

      CompletableFuture<List<Job>> futureIngestJobs = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream().map(CompletableFuture::join) // join each individual Job future here
          .collect(Collectors.toList()));

      CompletableFuture.allOf(futureIngestJobs).thenApply(v -> {
        List<Job> jobs = futureIngestJobs.join();
        FlowPanel tempJobIdsList = new FlowPanel();
        if (jobs != null && !jobs.isEmpty()) {
          jobs.forEach(job -> {
            Anchor jobIdentifier = new Anchor();
            SafeHtmlBuilder b = new SafeHtmlBuilder();
            jobIdentifier.setHTML(b.appendEscaped(job.getName()).appendHtmlConstant(" <span class='details-date'>")
              .appendHtmlConstant(Humanize.formatDateTime(job.getStartDate())).appendHtmlConstant("</span>")
              .toSafeHtml());
            jobIdentifier.setHref(HistoryUtils.createHistoryHashLink(ShowJob.RESOLVER, job.getId(),
              RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, aip.getId()));
            jobIdentifier.setStyleName("value");
            jobIdentifier.addStyleName("details-anchor");
            tempJobIdsList.add(jobIdentifier);
          });
        }
        return tempJobIdsList;
      }).whenComplete((value, throwable) -> {

        if (throwable != null) {
          Toast.showError("Error fetching AIP jobs information");
        } else {
          jobIdsList.clear();
          jobIdsList.add(value);
        }
      });
    }
  }

  private FlowPanel createAipLevelHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();

    if (aip.getLevel() == null) {
      return null;
    }

    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_LEVEL, aip.getLevel());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(DescriptionLevelUtils.getElementLevelLabel(aip.getLevel())), riFilter, panel,
      response.getRepresentationInformationFields().contains(RodaConstants.AIP_LEVEL));

    return panel;
  }

  private FlowPanel createAipTypeHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_TYPE, aip.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getType()),
      riFilter, panel, response.getRepresentationInformationFields().contains(RodaConstants.AIP_TYPE));
    return panel;
  }

  private FlowPanel createIdHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.INDEX_UUID, aip.getId());

    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getId()),
      riFilter, panel, response.getRepresentationInformationFields().contains(RodaConstants.INDEX_UUID),
      "browseFileInformationIcon");
    return panel;
  }
}
