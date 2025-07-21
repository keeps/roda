/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.utils;

import java.util.function.Supplier;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.JobList;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.lists.NotificationList;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.client.common.lists.RodaMemberList;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.lists.TransferredResourceList;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class ListFactory {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public ListBuilder<? extends IsIndexed> getListBuilder(String listIdPrefix, String actualClass, String title,
    Filter filter, int pageSize, int incrementPage) {

    AsyncTableCellOptions<? extends IsIndexed> options;
    Supplier<AsyncTableCell<? extends IsIndexed>> listConstructor;

    if (AIP.class.getName().equals(actualClass) || IndexedAIP.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(IndexedAIP.class, listIdPrefix + "_AIP");
      listConstructor = () -> new ConfigurableAsyncTableCell<>();
    } else if (Representation.class.getName().equals(actualClass)
      || IndexedRepresentation.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(IndexedRepresentation.class, listIdPrefix + "_representation");
      listConstructor = () -> new ConfigurableAsyncTableCell<>();
    } else if (File.class.getName().equals(actualClass) || IndexedFile.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(IndexedFile.class, listIdPrefix + "_simpleFile");
      listConstructor = () -> new ConfigurableAsyncTableCell<>();
    } else if (RepresentationInformation.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(RepresentationInformation.class,
        listIdPrefix + "_representationInformation");
      listConstructor = () -> new RepresentationInformationList();
    } else if (IndexedRisk.class.getName().equals(actualClass) || Risk.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(IndexedRisk.class, listIdPrefix + "_risk");
      listConstructor = () -> new RiskList();
    } else if (RiskIncidence.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(RiskIncidence.class, listIdPrefix + "_riskIncidence");
      listConstructor = () -> new RiskIncidenceList();
    } else if (Job.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(IndexedJob.class, listIdPrefix + "_job");
      listConstructor = () -> new JobList();
    } else if (Report.class.getName().equals(actualClass) || IndexedReport.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(IndexedReport.class, listIdPrefix + "_simpleJob");
      listConstructor = () -> new SimpleJobReportList();
    } else if (TransferredResource.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(TransferredResource.class, listIdPrefix + "_transferredResource");
      listConstructor = () -> new TransferredResourceList();
    } else if (Notification.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(Notification.class, listIdPrefix + "_notification");
      listConstructor = () -> new NotificationList();
    } else if (LogEntry.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(LogEntry.class, listIdPrefix + "_logEntry");
      listConstructor = () -> new LogEntryList();
    } else if (RODAMember.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(RODAMember.class, listIdPrefix + "_rodaMember");
      listConstructor = () -> new RodaMemberList();
    } else if (DIP.class.getName().equals(actualClass) || IndexedDIP.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(IndexedDIP.class, listIdPrefix + "_DIP");
      listConstructor = () -> new DIPList();
    } else if (DIPFile.class.getName().equals(actualClass)) {
      options = new AsyncTableCellOptions<>(DIPFile.class, listIdPrefix + "_DIPFile");
      listConstructor = () -> new DIPFileList();
    } else {
      return null;
    }

    options.withFilter(filter);
    options.withInitialPageSize(pageSize);
    options.withPageSizeIncrement(incrementPage);

    if (AIP.class.getName().equals(actualClass) || IndexedAIP.class.getName().equals(actualClass)) {
      options.withSummary(messages.selectAipSearchResults());
      options.withJustActive(LastSelectedItemsSingleton.getInstance().isSelectedJustActive());
    } else if (Representation.class.getName().equals(actualClass)
      || IndexedRepresentation.class.getName().equals(actualClass)) {
      options.withSummary(messages.selectRepresentationSearchResults());
      options.withJustActive(LastSelectedItemsSingleton.getInstance().isSelectedJustActive());
    } else if (File.class.getName().equals(actualClass) || IndexedFile.class.getName().equals(actualClass)) {
      options.withSummary(messages.selectFileSearchResults());
      options.withJustActive(LastSelectedItemsSingleton.getInstance().isSelectedJustActive());
    } else {
      options.withSummary(title);
    }

    return (ListBuilder<? extends IsIndexed>) new ListBuilder(listConstructor, options);
  }

}
