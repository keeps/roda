/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.schedule;

import java.util.Date;

import org.roda.core.data.v2.disposal.metadata.DisposalScheduleAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveScheduleAIPMetadata;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.IndexedAIP;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DisposalSchedulePluginUtils {
  private DisposalSchedulePluginUtils() {
    // empty constructor (private)
  }

  public static DisposalScheduleAIPMetadata createDisposalScheduleAIPMetadata(DisposalSchedule disposalSchedule,
    String associatedBy, AIPDisposalScheduleAssociationType flow) {
    DisposalScheduleAIPMetadata disposalScheduleAIPMetadata = new DisposalScheduleAIPMetadata();

    disposalScheduleAIPMetadata.setId(disposalSchedule.getId());
    disposalScheduleAIPMetadata.setAssociatedBy(associatedBy);
    disposalScheduleAIPMetadata.setAssociatedOn(new Date());
    disposalScheduleAIPMetadata.setAssociationType(flow);

    return disposalScheduleAIPMetadata;
  }

  public static DisposalTransitiveScheduleAIPMetadata createTransitiveDisposalScheduleAIPMetadata(
    DisposalSchedule disposalSchedule, AIP aip, IndexedAIP aipParentIndexed) {
    DisposalTransitiveScheduleAIPMetadata disposalTransitiveSchedule = new DisposalTransitiveScheduleAIPMetadata();
    disposalTransitiveSchedule.setAipId(aip.getId());
    disposalTransitiveSchedule.setActionCode(disposalSchedule.getActionCode());
    disposalTransitiveSchedule.setRetentionPeriodDuration(aipParentIndexed.getRetentionPeriodDuration());
    disposalTransitiveSchedule.setOverDueDate(aipParentIndexed.getRetentionPeriodStartDate());
    return disposalTransitiveSchedule;
  }
}
