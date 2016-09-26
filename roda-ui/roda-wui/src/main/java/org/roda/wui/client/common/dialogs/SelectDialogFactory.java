/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
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

public class SelectDialogFactory {

  public <T extends IsIndexed> DefaultSelectDialog getSelectDialog(Class<T> actualClass, String title, Filter filter,
    boolean selectable) throws NotFoundException {
    return (DefaultSelectDialog) getSelectDialog(actualClass.getName(), title, filter, selectable);
  }

  public DefaultSelectDialog getSelectDialog(String actualClass, String title, Filter filter, boolean selectable)
    throws NotFoundException {
    if (actualClass.equals(AIP.class.getName()) || actualClass.equals(IndexedAIP.class.getName())) {
      boolean justActive = true;
      return new SelectAipDialog(title, filter, justActive, false, selectable);
    } else if (actualClass.equals(Representation.class.getName())
      || actualClass.equals(IndexedRepresentation.class.getName())) {
      boolean justActive = true;
      return new SelectRepresentationDialog(title, filter, justActive, false, selectable);
    } else if (actualClass.equals(File.class.getName()) || actualClass.equals(IndexedFile.class.getName())) {
      boolean justActive = true;
      return new SelectFileDialog(title, filter, justActive, false, selectable);
    } else if (actualClass.equals(Format.class.getName())) {
      return new SelectFormatDialog(title, filter, selectable);
    } else if (actualClass.equals(IndexedRisk.class.getName()) || actualClass.equals(Risk.class.getName())) {
      return new SelectRiskDialog(title, filter, selectable);
    } else if (actualClass.equals(Job.class.getName())) {
      return new SelectJobDialog(title, filter, selectable);
    } else if (actualClass.equals(Report.class.getName())) {
      return new SelectReportDialog(title, filter, selectable);
    } else if (actualClass.equals(TransferredResource.class.getName())) {
      return new SelectTransferResourceDialog(title, filter, selectable);
    } else if (actualClass.equals(Notification.class.getName())) {
      return new SelectNotificationDialog(title, filter, selectable);
    } else if (actualClass.equals(LogEntry.class.getName())) {
      return new SelectLogEntryDialog(title, filter, selectable);
    } else {
      throw new NotFoundException();
    }
  }

}
