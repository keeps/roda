/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
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
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;

public class SearchFilters {
  public static Filter defaultFilter(String actualClass) {
    if (actualClass.equals(AIP.class.getName()) || actualClass.equals(IndexedAIP.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, "*"));
    } else if (actualClass.equals(Representation.class.getName())
      || actualClass.equals(IndexedRepresentation.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.REPRESENTATION_SEARCH, "*"));
    } else if (actualClass.equals(File.class.getName()) || actualClass.equals(IndexedFile.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.FILE_SEARCH, "*"));
    } else if (actualClass.equals(Format.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.FORMAT_SEARCH, "*"));
    } else if (actualClass.equals(IndexedRisk.class.getName()) || actualClass.equals(Risk.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.RISK_SEARCH, "*"));
    } else if (actualClass.equals(RiskIncidence.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.RISK_INCIDENCE_SEARCH, "*"));
    } else if (actualClass.equals(Job.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.JOB_SEARCH, "*"));
    } else if (actualClass.equals(Report.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.JOB_REPORT_SEARCH, "*"));
    } else if (actualClass.equals(TransferredResource.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_SEARCH, "*"));
    } else if (actualClass.equals(Notification.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.NOTIFICATION_SEARCH, "*"));
    } else if (actualClass.equals(LogEntry.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.LOG_SEARCH, "*"));
    } else if (actualClass.equals(RODAMember.class.getName())) {
      return new Filter(new BasicSearchFilterParameter(RodaConstants.MEMBERS_SEARCH, "*"));
    } else {
      return Filter.NULL;
    }
  }

  public static String allFilter(String actualClass) {
    if (actualClass.equals(AIP.class.getName()) || actualClass.equals(IndexedAIP.class.getName())) {
      return RodaConstants.AIP_SEARCH;
    } else if (actualClass.equals(Representation.class.getName())
      || actualClass.equals(IndexedRepresentation.class.getName())) {
      return RodaConstants.REPRESENTATION_SEARCH;
    } else if (actualClass.equals(File.class.getName()) || actualClass.equals(IndexedFile.class.getName())) {
      return RodaConstants.FILE_SEARCH;
    } else if (actualClass.equals(Format.class.getName())) {
      return RodaConstants.FORMAT_SEARCH;
    } else if (actualClass.equals(IndexedRisk.class.getName()) || actualClass.equals(Risk.class.getName())) {
      return RodaConstants.RISK_SEARCH;
    } else if (actualClass.equals(RiskIncidence.class.getName())) {
      return RodaConstants.RISK_INCIDENCE_SEARCH;
    } else if (actualClass.equals(Job.class.getName())) {
      return RodaConstants.JOB_SEARCH;
    } else if (actualClass.equals(Report.class.getName())) {
      return RodaConstants.JOB_REPORT_SEARCH;
    } else if (actualClass.equals(TransferredResource.class.getName())) {
      return RodaConstants.TRANSFERRED_RESOURCE_SEARCH;
    } else if (actualClass.equals(Notification.class.getName())) {
      return RodaConstants.NOTIFICATION_SEARCH;
    } else if (actualClass.equals(LogEntry.class.getName())) {
      return RodaConstants.LOG_SEARCH;
    } else if (actualClass.equals(RODAMember.class.getName())) {
      return RodaConstants.MEMBERS_SEARCH;
    } else {
      return "";
    }
  }
}
