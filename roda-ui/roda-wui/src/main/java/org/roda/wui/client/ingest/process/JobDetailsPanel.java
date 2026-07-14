package org.roda.wui.client.ingest.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.search.SearchPreFilterUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.management.distributed.ShowDistributedInstance;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.client.services.DistributedInstancesRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobDetailsPanel extends GenericMetadataCardPanel<IndexedJob> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final boolean isIngest;
  private final boolean isJobInFinalState;
  private final String username;

  private final SelectedItems<?> cachedSourceObjects;
  private final boolean sourceObjectsFetched;
  private final Consumer<SelectedItems<?>> sourceObjectsCallback;

  public JobDetailsPanel(IndexedJob job, String username, boolean isIngest, boolean isJobInFinalState,
    SelectedItems<?> cachedSourceObjects, boolean sourceObjectsFetched,
    Consumer<SelectedItems<?>> sourceObjectsCallback) {
    this.isIngest = isIngest;
    this.username = username;
    this.isJobInFinalState = isJobInFinalState;
    this.cachedSourceObjects = cachedSourceObjects;
    this.sourceObjectsFetched = sourceObjectsFetched;
    this.sourceObjectsCallback = sourceObjectsCallback;
    setData(job);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedJob data) {
    return null;
  }

  @Override
  protected void buildFields(IndexedJob data) {
    addSeparator(messages.processTitle());

    buildInstanceArea(data);

    buildField(messages.jobName()).withValue(data.getName()).build();
    buildField(messages.jobCreator()).withValue(username).build();

    buildField(messages.joOrchestration())
      .withMultipleHtmlBadges(List.of(HtmlSnippetUtils.getJobPriorityHtml(data.getPriority(), true),
        HtmlSnippetUtils.getJobParallelismTypeHtml(data.getParallelism(), true)))
      .build();

    buildSourceObjects(data);

    buildField(messages.jobStartDate()).withValue(Humanize.formatDateTime(data.getStartDate())).build();

    if (data.getEndDate() != null) {
      buildField(messages.jobEndDate()).withValue(Humanize.formatDateTime(data.getEndDate())).build();
    }

    buildField(messages.jobDuration())
      .withValue(Humanize.durationInDHMS(data.getStartDate(), data.getEndDate(), Humanize.DHMSFormat.LONG)).build();

    buildField(messages.jobStatus()).withHtml(HtmlSnippetUtils.getJobStateHtml(data.getState(), data.getJobStats()))
      .build();

    buildInstanceJobScheduleArea(data);

    buildField(messages.jobStateDetails()).withValue(data.getStateDetails()).build();

    buildProgressField(data);

    buildAttachments(data.getId(), data.getAttachmentsList());
  }

  private void buildInstanceArea(IndexedJob job) {
    if (job.getInstanceId() != null) {
      String distributedMode = ConfigurationManager.getStringWithDefault(
        RodaConstants.DEFAULT_DISTRIBUTED_MODE_TYPE.name(), RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY);

      if (distributedMode.equals(RodaConstants.DistributedModeType.CENTRAL.name())) {
        // Synchronously reserve space
        FlowPanel placeholder = new FlowPanel();
        buildField(messages.distributedInstanceLabel()).withWidget(placeholder).build();

        Services services = new Services("Retrieve distributed instance", "retrieve");
        services.distributedInstanceResource(s -> s.getDistributedInstance(job.getInstanceId()))
          .whenComplete((distributedInstance, throwable) -> {
            if (throwable != null) {
              services.distributedInstanceResource(DistributedInstancesRestService::getLocalInstance)
                .whenComplete((localInstance, throwable1) -> {
                  placeholder.add(new Label(localInstance.getName()));
                });
            } else {
              Anchor link = new Anchor(distributedInstance.getName());
              link.addStyleName("btn-link cursor-pointer"); // Add your hyperlink styles here
              link.addClickHandler(
                event -> HistoryUtils.newHistory(ShowDistributedInstance.RESOLVER, job.getInstanceId()));
              placeholder.add(link);
            }
          });
      }
    }
  }

  private void buildInstanceJobScheduleArea(IndexedJob job) {
    String distributedMode = ConfigurationManager.getStringWithDefault(
      RodaConstants.DEFAULT_DISTRIBUTED_MODE_TYPE.name(), RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY);

    if (distributedMode.equals(RodaConstants.DistributedModeType.LOCAL.name())
      && Job.JOB_STATE.SCHEDULED.equals(job.getState())) {

      // Synchronously reserve space
      FlowPanel placeholder = new FlowPanel();
      buildField(messages.reportScheduleInfo()).withWidget(placeholder).build();

      Services services = new Services("Retrieve job schedule info", "retrieve");
      services.configurationsResource(s -> s.retrieveCronValue(LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((stringResponse, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            String description = stringResponse.getValue();
            if (StringUtils.isNotBlank(description)) {
              placeholder.add(new Label(description));
            }
          }
        });
    }
  }

  private void buildSourceObjects(IndexedJob job) {
    // 1. Create a placeholder to hold the spot in the DOM immediately
    FlowPanel placeholder = new FlowPanel();
    buildField(messages.showJobSourceObjects()).withWidget(placeholder).build();

    // 2. If data is already cached, render it synchronously to avoid flickering
    if (sourceObjectsFetched) {
      if (isIngest) {
        showIngestSourceObjects(placeholder, job.getUUID(), cachedSourceObjects);
      } else {
        showActionSourceObjects(placeholder, job.getUUID(), cachedSourceObjects);
      }
    } else {
      // 3. Make the async call only once
      Services services = new Services("get job from model", "get");
      services.jobsResource(s -> s.getJobFromModel(job.getId())).whenComplete((modelJob, error) -> {
        SelectedItems<?> selected = modelJob != null ? modelJob.getSourceObjects() : null;

        // Send data back to ShowJobV2 to cache it for the next Timer tick
        if (sourceObjectsCallback != null) {
          sourceObjectsCallback.accept(selected);
        }

        if (modelJob != null) {
          if (isIngest) {
            showIngestSourceObjects(placeholder, job.getUUID(), selected);
          } else {
            showActionSourceObjects(placeholder, job.getUUID(), selected);
          }
        }
      });
    }
  }

  // Notice how this now receives the 'placeholder' container and adds widgets to
  // it
  private void showIngestSourceObjects(FlowPanel container, final String jobUUID, final SelectedItems<?> selected) {
    container.clear();
    if (selected != null) {
      if (ClientSelectedItemsUtils.isEmpty(selected) && isJobInFinalState) {
        container.add(new Label(messages.noItemsToDisplay(messages.someOfAObject(selected.getSelectedClass()))));
      } else if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();
        final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, jobUUID));

        FlowPanel downloadLink = buildDownloadLink(
          messages.sourceObjectList(ids.size(), messages.someOfAObject(selected.getSelectedClass())), event -> {
            Services services = new Services("Retrieve export limit", "retrieve");
            services.configurationsResource(ConfigurationRestService::retrieveExportLimit)
              .whenComplete((longResponse, throwable) -> {
                if (throwable != null) {
                  AsyncCallbackUtils.defaultFailureTreatment(throwable);
                } else {
                  Toast.showInfo(messages.exportListTitle(),
                    messages.exportListMessage(longResponse.getResult().intValue()));
                  RestUtils.requestCSVExport(IndexedJob.class, filter, Sorter.NONE,
                    new Sublist(0, longResponse.getResult().intValue()), Facets.NONE, true, false, "job.csv");
                }
              });
          });
        container.add(downloadLink);
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
        container.add(new HTML(SearchPreFilterUtils.getFilterText(filter, selected.getSelectedClass())));
      } else if (selected instanceof SelectedItemsAll) {
        container.add(new Label(messages.allOfAObject(selected.getSelectedClass())));
      }
    }
  }

  // Receives the 'placeholder' container and adds widgets to it
  private void showActionSourceObjects(FlowPanel container, final String jobUUID, final SelectedItems<?> selected) {
    container.clear();
    if (selected != null) {
      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();

        if (ids.isEmpty()) {
          container.add(new Label(messages.someOfAObject(selected.getSelectedClass())));
        } else {
          final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, jobUUID));

          FlowPanel downloadLink = buildDownloadLink(
            messages.sourceObjectList(ids.size(), messages.someOfAObject(selected.getSelectedClass())), event -> {
              Services services = new Services("Retrieve export limit", "retrieve");
              services.configurationsResource(ConfigurationRestService::retrieveExportLimit)
                .whenComplete((longResponse, throwable) -> {
                  if (throwable != null) {
                    AsyncCallbackUtils.defaultFailureTreatment(throwable);
                  } else {
                    Toast.showInfo(messages.exportListTitle(),
                      messages.exportListMessage(longResponse.getResult().intValue()));
                    RestUtils.requestCSVExport(IndexedJob.class, filter, Sorter.NONE,
                      new Sublist(0, longResponse.getResult().intValue()), Facets.NONE, true, false, "job.csv");
                  }
                });
            });
          container.add(downloadLink);
        }
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
        container.add(new HTML(SearchPreFilterUtils.getFilterText(filter, selected.getSelectedClass())));
      } else if (selected instanceof SelectedItemsAll) {
        String text;

        if (AIP.class.getName().equals(selected.getSelectedClass())
          || IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          text = messages.allIntellectualEntities();
        } else if (Representation.class.getName().equals(selected.getSelectedClass())
          || IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          text = messages.allRepresentations();
        } else if (File.class.getName().equals(selected.getSelectedClass())
          || IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          text = messages.allFiles();
        } else {
          text = messages.allOfAObject(selected.getSelectedClass());
        }

        container.add(new Label(text));
      } else if (selected instanceof SelectedItemsNone) {
        container.add(new HTMLPanel("span", SafeHtmlUtils.htmlEscape(messages.noObjectsSelected())));
      }
    }
  }

  private void buildAttachments(String jobId, List<String> attachments) {
    if (!attachments.isEmpty()) {
      addSeparator(messages.showAttachments());

      FlowPanel list = new FlowPanel();
      list.addStyleName("generic-multiline");

      for (String attachment : attachments) {
        FlowPanel container = buildDownloadLink(attachment,
          RestUtils.createJobAttachmentDownloadUri(jobId, attachment));
        list.add(container);
      }

      buildField(messages.showAttachments()).withWidget(list).build();
    }
  }

  private FlowPanel buildDownloadLink(String description, ClickHandler clickHandler) {
    FlowPanel container = new FlowPanel();
    container.addStyleName("download-resource-container");

    InlineLabel descriptionLabel = new InlineLabel(description);
    descriptionLabel.addStyleName("resource-description");

    Anchor downloadLink = new Anchor();
    downloadLink.setHTML(messages.downloadButton() + " <i class='fas fa-link' />");
    downloadLink.addStyleName("resource-download-link");
    downloadLink.setHref("javascript:void(0);");
    downloadLink.addClickHandler(clickHandler);

    container.add(descriptionLabel);
    container.add(downloadLink);

    return container;
  }

  private FlowPanel buildDownloadLink(String description, SafeUri href) {
    FlowPanel container = new FlowPanel();
    container.addStyleName("download-resource-container");

    InlineLabel descriptionLabel = new InlineLabel(description);
    descriptionLabel.addStyleName("resource-description");

    Anchor downloadLink = new Anchor();
    downloadLink.setHTML(messages.downloadButton() + " <i class='fas fa-link' />");
    downloadLink.addStyleName("resource-download-link");
    downloadLink.setHref(href);

    container.add(descriptionLabel);
    container.add(downloadLink);

    return container;
  }

  private void buildProgressField(IndexedJob job) {

    List<SafeHtml> items = new ArrayList<>();

    // set counters
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-default'>"));
    b.append(messages.showJobProgressCompletionPercentage(job.getJobStats().getCompletionPercentage()));
    b.append(SafeHtmlUtils.fromSafeConstant("</span>"));

    items.add(b.toSafeHtml());

    b = new SafeHtmlBuilder();
    if (job.getJobStats().getSourceObjectsCount() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-default'>"));
      b.append(messages.showJobProgressTotalCount(job.getJobStats().getSourceObjectsCount()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
      items.add(b.toSafeHtml());
    }

    b = new SafeHtmlBuilder();
    if (job.getJobStats().getSourceObjectsProcessedWithSuccess() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-success'>"));
      b.append(messages.showJobProgressSuccessfulCount(job.getJobStats().getSourceObjectsProcessedWithSuccess()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
      items.add(b.toSafeHtml());
    }

    b = new SafeHtmlBuilder();
    if (job.getJobStats().getSourceObjectsProcessedWithPartialSuccess() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-warning'>"));
      b.append(messages
        .showJobProgressPartialSuccessfulCount(job.getJobStats().getSourceObjectsProcessedWithPartialSuccess()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
      items.add(b.toSafeHtml());
    }

    b = new SafeHtmlBuilder();
    if (job.getJobStats().getSourceObjectsProcessedWithFailure() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-danger'>"));
      b.append(messages.showJobProgressFailedCount(job.getJobStats().getSourceObjectsProcessedWithFailure()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
      items.add(b.toSafeHtml());
    }

    b = new SafeHtmlBuilder();
    if (job.getJobStats().getSourceObjectsProcessedWithSkipped() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-warning'>"));
      b.append(messages.showJobProgressSkippedCount(job.getJobStats().getSourceObjectsProcessedWithSkipped()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
      items.add(b.toSafeHtml());
    }

    b = new SafeHtmlBuilder();
    if (job.getJobStats().getSourceObjectsBeingProcessed() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-info'>"));
      b.append(messages.showJobProgressProcessingCount(job.getJobStats().getSourceObjectsBeingProcessed()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
      items.add(b.toSafeHtml());
    }

    b = new SafeHtmlBuilder();
    if (job.getJobStats().getSourceObjectsWaitingToBeProcessed() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-warning'>"));
      b.append(messages.showJobProgressWaitingCount(job.getJobStats().getSourceObjectsWaitingToBeProcessed()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
      items.add(b.toSafeHtml());
    }

    buildField(messages.jobProgress()).withMultipleHtmlBadges(items).build();
  }
}