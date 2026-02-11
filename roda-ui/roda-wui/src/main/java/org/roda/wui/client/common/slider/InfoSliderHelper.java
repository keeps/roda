/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class InfoSliderHelper {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private InfoSliderHelper() {
    // do nothing
  }

  public static Map<String, Widget> getRepresentationInfoDetailsMap(BrowseRepresentationResponse response) {
    Map<String, Widget> values = new HashMap<>();
    IndexedRepresentation representation = response.getIndexedRepresentation();

    values.put(messages.representationId(), createIdHTML(response));

    if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())) {
      values.put(messages.aipCreated(), new InlineHTML(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getCreatedOn()), representation.getCreatedBy())));
    }

    if (representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      values.put(messages.aipUpdated(), new InlineHTML(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getUpdatedOn()), representation.getUpdatedBy())));
    }

    if (StringUtils.isNotBlank(representation.getType())) {
      values.put(messages.representationType(), createRepresentationTypeHTML(response));
    }

    return values;
  }

  public static Map<String, Widget> getAipInfoDetailsMap(BrowseAIPResponse response) {
    Map<String, Widget> values = new HashMap<>();
    IndexedAIP aip = response.getIndexedAIP();

    values.put(messages.itemId(), createIdHTML(response));

    if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy())) {
      values.put(messages.aipCreated(),
        new InlineHTML(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getCreatedOn()), aip.getCreatedBy())));
    }

    if (aip.getUpdatedOn() != null && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      values.put(messages.aipUpdated(),
        new InlineHTML(messages.dateCreatedOrUpdated(Humanize.formatDateTime(aip.getUpdatedOn()), aip.getUpdatedBy())));
    }

    if (StringUtils.isNotBlank(aip.getLevel())) {
      values.put(messages.aipLevel(), createAipLevelHTML(response));
    }

    if (StringUtils.isNotBlank(aip.getType())) {
      values.put(messages.aipType(), createAipTypeHTML(response));
    }

    if (!aip.getIngestSIPIds().isEmpty()) {
      FlowPanel sipIds = new FlowPanel();
      for (String ingestSIPId : aip.getIngestSIPIds()) {
        sipIds.add(new HTMLPanel("p", ingestSIPId));
      }
      values.put(messages.sipId(), sipIds);
    }

    if (response.getIndexedAIP().getIngestJobId() != null && !response.getIndexedAIP().getIngestJobId().isEmpty()) {
      FlowPanel jobIdsList = new FlowPanel();
      jobIdsList.addStyleName("slider-info-entry-value-aip-ingest-jobs");

      jobIdsList.add(new HTMLPanel("p", response.getIndexedAIP().getIngestJobId()));

      // If some error occures during the retrieval of the job information, we still
      // want to show the job id
      values.put(messages.processIdTitle(), jobIdsList);

      Services service = new Services("Retrieve AIP jobs information", "get");

      List<String> jobIds = new ArrayList<>();

      jobIds.add(response.getIndexedAIP().getIngestJobId());
      jobIds.addAll(response.getIndexedAIP().getIngestUpdateJobIds());

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

    return values;
  }

  public static Map<String, Widget> getFileInfoDetailsMap(IndexedFile file, List<String> riRules) {
    Map<String, Widget> values = new HashMap<>();

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), createIdHTML(riRules, fileName, file.getUUID()));

      if (file.getSize() > 0) {
        values.put(messages.viewRepresentationInfoSize(),
          new InlineHTML(SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize()))));
      }

      if (file.getFileFormat() != null) {
        FileFormat fileFormat = file.getFileFormat();

        if (StringUtils.isNotBlank(fileFormat.getExtension())) {
          values.put(messages.viewRepresentationInfoExtension(),
            createExtensionHTML(riRules, fileFormat.getExtension()));
        }

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), createMimetypeHTML(riRules, fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(),
            createFormatDesignationHTML(riRules, fileFormat.getFormatDesignation()));
        }

        if (StringUtils.isNotBlank(fileFormat.getPronom())) {
          values.put(messages.viewRepresentationInfoPronom(), createPronomHTML(riRules, fileFormat.getPronom()));
        }
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationName())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationName(),
          createCreatingApplicationNameHTML(riRules, file.getCreatingApplicationName()));
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationVersion())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationVersion(),
          createCreatingApplicationVersionHTML(riRules, file.getCreatingApplicationVersion()));
      }

      if (StringUtils.isNotBlank(file.getDateCreatedByApplication())) {
        values.put(messages.viewRepresentationInfoDateCreatedByApplication(),
          new InlineHTML(SafeHtmlUtils.fromString(file.getDateCreatedByApplication())));
      }

      if (file.getHash() != null && !file.getHash().isEmpty()) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        boolean first = true;
        for (String hash : file.getHash()) {
          if (first) {
            first = false;
          } else {
            b.append(SafeHtmlUtils.fromSafeConstant("<br/>"));
          }
          b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
          b.append(SafeHtmlUtils.fromString(hash));
          b.append(SafeHtmlUtils.fromSafeConstant("</small>"));
        }
        values.put(messages.viewRepresentationInfoHash(), new InlineHTML(b.toSafeHtml()));
      }
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      if (file.isReference()) {
        b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
        b.append(SafeHtmlUtils.fromString(file.getReferenceURL()));
        b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

        values.put(messages.viewRepresentationInfoStoragePath(), new InlineHTML(b.toSafeHtml()));
      } else {
        if (file.getStoragePath() != null) {
          b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
          b.append(SafeHtmlUtils.fromString(file.getStoragePath()));
          b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

          values.put(messages.viewRepresentationInfoStoragePath(), new InlineHTML(b.toSafeHtml()));
        }
      }
    }
    return values;
  }

  private static FlowPanel createExtensionHTML(List<String> representationInformationFields, String extension) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_EXTENSION, extension);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(extension),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_EXTENSION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createMimetypeHTML(List<String> representationInformationFields, String mimetype) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_MIMETYPE, mimetype);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(mimetype),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_FORMAT_MIMETYPE),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createPronomHTML(List<String> representationInformationFields, String pronom) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.FILE_PRONOM, pronom);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(pronom),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_PRONOM),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createFormatDesignationHTML(List<String> representationInformationFields,
    String designation) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_FORMAT_DESIGNATION, designation);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(designation),
      riFilter, panel, representationInformationFields.contains(RodaConstants.FILE_FORMAT_DESIGNATION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createCreatingApplicationNameHTML(List<String> representationInformationFields,
    String createApplicationName) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_NAME, createApplicationName);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(createApplicationName), riFilter, panel,
      representationInformationFields.contains(RodaConstants.FILE_CREATING_APPLICATION_NAME),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createCreatingApplicationVersionHTML(List<String> representationInformationFields,
    String createApplicationVersion) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_FILE, RodaConstants.FILE_CREATING_APPLICATION_VERSION, createApplicationVersion);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(createApplicationVersion), riFilter, panel,
      representationInformationFields.contains(RodaConstants.FILE_CREATING_APPLICATION_VERSION),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(List<String> representationInformationFields, String filename, String uuid) {
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_FILE, RodaConstants.INDEX_UUID, uuid);
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(filename),
      riFilter, panel, representationInformationFields.contains(RodaConstants.INDEX_UUID), "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.INDEX_UUID, aip.getId());

    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getId()),
      riFilter, panel, response.getRepresentationInformationFields().contains(RodaConstants.INDEX_UUID),
      "browseFileInformationIcon");
    return panel;
  }

  private static FlowPanel createIdHTML(BrowseRepresentationResponse response) {
    IndexedRepresentation representation = response.getIndexedRepresentation();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.INDEX_UUID, representation.getUUID());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getId()), riFilter, panel,
      response.getRiRules().contains(RodaConstants.INDEX_UUID));

    return panel;
  }

  private static FlowPanel createAipTypeHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();
    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_TYPE, aip.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(SafeHtmlUtils.fromString(aip.getType()),
      riFilter, panel, response.getRepresentationInformationFields().contains(RodaConstants.AIP_TYPE));
    return panel;
  }

  private static FlowPanel createAipLevelHTML(BrowseAIPResponse response) {
    IndexedAIP aip = response.getIndexedAIP();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils
      .createRepresentationInformationFilter(RodaConstants.INDEX_AIP, RodaConstants.AIP_LEVEL, aip.getLevel());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(DescriptionLevelUtils.getElementLevelLabel(aip.getLevel())), riFilter, panel,
      response.getRepresentationInformationFields().contains(RodaConstants.AIP_LEVEL));

    return panel;
  }

  private static FlowPanel createRepresentationTypeHTML(BrowseRepresentationResponse response) {
    IndexedRepresentation representation = response.getIndexedRepresentation();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.REPRESENTATION_TYPE, representation.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getType()), riFilter, panel,
      response.getRiRules().contains(RodaConstants.REPRESENTATION_TYPE));

    return panel;
  }
}
