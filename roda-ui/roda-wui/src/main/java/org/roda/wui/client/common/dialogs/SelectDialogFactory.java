/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
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

public class SelectDialogFactory {

  public <T extends IsIndexed> DefaultSelectDialog getSelectDialog(Class<T> actualClass, String title, Filter filter)
    throws NotFoundException {
    return getSelectDialog(actualClass.getName(), title, filter);
  }

  public DefaultSelectDialog getSelectDialog(String actualClass, String title, Filter filter)
    throws NotFoundException {
    if (actualClass.equals(AIP.class.getName()) || actualClass.equals(IndexedAIP.class.getName())) {
      return new SelectAipDialog(title, filter, true);
    } else if (actualClass.equals(Representation.class.getName())
      || actualClass.equals(IndexedRepresentation.class.getName())) {
      return new SelectRepresentationDialog(title, filter, true);
    } else if (actualClass.equals(File.class.getName()) || actualClass.equals(IndexedFile.class.getName())) {
      return new SelectFileDialog(title, filter, true);
    } else if (actualClass.equals(RepresentationInformation.class.getName())) {
      return new SelectRepresentationInformationDialog(title, filter);
    } else if (actualClass.equals(IndexedRisk.class.getName()) || actualClass.equals(Risk.class.getName())) {
      return new SelectRiskDialog(title, filter);
    } else if (actualClass.equals(Job.class.getName())) {
      return new SelectJobDialog(title, filter);
    } else if (actualClass.equals(Report.class.getName()) || actualClass.equals(IndexedReport.class.getName())) {
      return new SelectReportDialog(title, filter);
    } else if (actualClass.equals(TransferredResource.class.getName())) {
      return new SelectTransferResourceDialog(title, filter);
    } else if (actualClass.equals(Notification.class.getName())) {
      return new SelectNotificationDialog(title, filter);
    } else if (actualClass.equals(LogEntry.class.getName())) {
      return new SelectLogEntryDialog(title, filter);
    } else {
      throw new NotFoundException();
    }
  }

}
