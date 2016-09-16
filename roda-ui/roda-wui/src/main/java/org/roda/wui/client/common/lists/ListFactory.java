/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class ListFactory {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public <T extends IsIndexed> BasicAsyncTableCell<T> getList(Class<T> actualClass, String title, Filter filter,
    int pageSize, int incrementPage) throws RODAException {
    return (BasicAsyncTableCell<T>) getList(actualClass.getName(), title, filter, pageSize, incrementPage);
  }

  public BasicAsyncTableCell<?> getList(String actualClass, String title, Filter filter, int pageSize,
    int incrementPage) throws RODAException {
    if (actualClass.equals(AIP.class.getName()) || actualClass.equals(IndexedAIP.class.getName())) {
      return new AIPList(filter, true, null, messages.selectAipSearchResults(), false, pageSize, incrementPage);
    } else if (actualClass.equals(Representation.class.getName())
      || actualClass.equals(IndexedRepresentation.class.getName())) {
      return new RepresentationList(filter, true, null, messages.selectRepresentationSearchResults(), false, pageSize,
        incrementPage);
    } else if (actualClass.equals(File.class.getName()) || actualClass.equals(IndexedFile.class.getName())) {
      return new SimpleFileList(filter, true, null, messages.selectFileSearchResults(), false, pageSize, incrementPage);
    } else if (actualClass.equals(Format.class.getName())) {
      return new FormatList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(Agent.class.getName())) {
      return new AgentList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(IndexedRisk.class.getName()) || actualClass.equals(Risk.class.getName())) {
      return new RiskList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(RiskIncidence.class.getName())) {
      return new RiskIncidenceList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(Job.class.getName())) {
      return new JobList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(Report.class.getName())) {
      return new SimpleJobReportList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(TransferredResource.class.getName())) {
      return new TransferredResourceList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(Notification.class.getName())) {
      return new NotificationList(filter, null, title, false, pageSize, incrementPage);
    } else if (actualClass.equals(LogEntry.class.getName())) {
      return new LogEntryList(filter, null, title, false, pageSize, incrementPage);
    } else {
      throw new RODAException();
    }
  }

}
