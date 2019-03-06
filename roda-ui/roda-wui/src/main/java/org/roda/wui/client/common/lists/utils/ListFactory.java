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

    if (actualClass.equals(AIP.class.getName()) || actualClass.equals(IndexedAIP.class.getName())) {
      options = new AsyncTableCellOptions<>(IndexedAIP.class, listIdPrefix + "_AIP");
      listConstructor = () -> new ConfigurableAsyncTableCell<>();
    } else if (actualClass.equals(Representation.class.getName())
      || actualClass.equals(IndexedRepresentation.class.getName())) {
      options = new AsyncTableCellOptions<>(IndexedRepresentation.class, listIdPrefix + "_representation");
      listConstructor = () -> new ConfigurableAsyncTableCell<>();
    } else if (actualClass.equals(File.class.getName()) || actualClass.equals(IndexedFile.class.getName())) {
      options = new AsyncTableCellOptions<>(IndexedFile.class, listIdPrefix + "_simpleFile");
      listConstructor = () -> new ConfigurableAsyncTableCell<>();
    } else if (actualClass.equals(RepresentationInformation.class.getName())) {
      options = new AsyncTableCellOptions<>(RepresentationInformation.class,
        listIdPrefix + "_representationInformation");
      listConstructor = () -> new RepresentationInformationList();
    } else if (actualClass.equals(IndexedRisk.class.getName()) || actualClass.equals(Risk.class.getName())) {
      options = new AsyncTableCellOptions<>(IndexedRisk.class, listIdPrefix + "_risk");
      listConstructor = () -> new RiskList();
    } else if (actualClass.equals(RiskIncidence.class.getName())) {
      options = new AsyncTableCellOptions<>(RiskIncidence.class, listIdPrefix + "_riskIncidence");
      listConstructor = () -> new RiskIncidenceList();
    } else if (actualClass.equals(Job.class.getName())) {
      options = new AsyncTableCellOptions<>(Job.class, listIdPrefix + "_job");
      listConstructor = () -> new JobList();
    } else if (actualClass.equals(Report.class.getName()) || actualClass.equals(IndexedReport.class.getName())) {
      options = new AsyncTableCellOptions<>(IndexedReport.class, listIdPrefix + "_simpleJob");
      listConstructor = () -> new SimpleJobReportList();
    } else if (actualClass.equals(TransferredResource.class.getName())) {
      options = new AsyncTableCellOptions<>(TransferredResource.class, listIdPrefix + "_transferredResource");
      listConstructor = () -> new TransferredResourceList();
    } else if (actualClass.equals(Notification.class.getName())) {
      options = new AsyncTableCellOptions<>(Notification.class, listIdPrefix + "_notification");
      listConstructor = () -> new NotificationList();
    } else if (actualClass.equals(LogEntry.class.getName())) {
      options = new AsyncTableCellOptions<>(LogEntry.class, listIdPrefix + "_logEntry");
      listConstructor = () -> new LogEntryList();
    } else if (actualClass.equals(RODAMember.class.getName())) {
      options = new AsyncTableCellOptions<>(RODAMember.class, listIdPrefix + "_rodaMember");
      listConstructor = () -> new RodaMemberList();
    } else if (actualClass.equals(DIP.class.getName()) || actualClass.equals(IndexedDIP.class.getName())) {
      options = new AsyncTableCellOptions<>(IndexedDIP.class, listIdPrefix + "_DIP");
      listConstructor = () -> new DIPList();
    } else if (actualClass.equals(DIPFile.class.getName())) {
      options = new AsyncTableCellOptions<>(DIPFile.class, listIdPrefix + "_DIPFile");
      listConstructor = () -> new DIPFileList();
    } else {
      return null;
    }

    options.withFilter(filter);
    options.withInitialPageSize(pageSize);
    options.withPageSizeIncrement(incrementPage);

    if (actualClass.equals(AIP.class.getName()) || actualClass.equals(IndexedAIP.class.getName())) {
      options.withSummary(messages.selectAipSearchResults());
      options.withJustActive(LastSelectedItemsSingleton.getInstance().isSelectedJustActive());
    } else if (actualClass.equals(Representation.class.getName())
      || actualClass.equals(IndexedRepresentation.class.getName())) {
      options.withSummary(messages.selectRepresentationSearchResults());
      options.withJustActive(LastSelectedItemsSingleton.getInstance().isSelectedJustActive());
    } else if (actualClass.equals(File.class.getName()) || actualClass.equals(IndexedFile.class.getName())) {
      options.withSummary(messages.selectFileSearchResults());
      options.withJustActive(LastSelectedItemsSingleton.getInstance().isSelectedJustActive());
    } else {
      options.withSummary(title);
    }

    return (ListBuilder<? extends IsIndexed>) new ListBuilder(listConstructor, options);
  }

}
