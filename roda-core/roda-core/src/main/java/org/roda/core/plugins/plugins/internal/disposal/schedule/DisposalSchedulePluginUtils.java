/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.internal.disposal.schedule;

import java.util.Date;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalScheduleAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalTransitiveScheduleAIPMetadata;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DisposalSchedulePluginUtils {
  public static DisposalScheduleAIPMetadata createDisposalScheduleAIPMetadata(DisposalSchedule disposalSchedule,
    String associatedBy, AIPDisposalScheduleAssociationType flow) {
    DisposalScheduleAIPMetadata disposalScheduleAIPMetadata = new DisposalScheduleAIPMetadata();

    disposalScheduleAIPMetadata.setId(disposalSchedule.getId());
    disposalScheduleAIPMetadata.setAssociatedBy(associatedBy);
    disposalScheduleAIPMetadata.setAssociatedOn(new Date());
    disposalScheduleAIPMetadata.setAssociationType(flow);

    return disposalScheduleAIPMetadata;
  }

  public static DisposalTransitiveScheduleAIPMetadata createTransitiveDisposalScheduleAIPMetadata(DisposalSchedule disposalSchedule, AIP aip, IndexedAIP aipParentIndexed) {
    DisposalTransitiveScheduleAIPMetadata disposalTransitiveSchedule = new DisposalTransitiveScheduleAIPMetadata();
    disposalTransitiveSchedule.setAipId(aip.getId());
    disposalTransitiveSchedule.setActionCode(disposalSchedule.getActionCode());
    disposalTransitiveSchedule.setRetentionPeriodDuration(aipParentIndexed.getRetentionPeriodDuration());
    disposalTransitiveSchedule.setOverDueDate(aipParentIndexed.getRetentionPeriodStartDate());
    return disposalTransitiveSchedule;
  }
}
