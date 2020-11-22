package org.roda.core.plugins.plugins.internal.disposal.schedule;

import java.util.Date;

import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalScheduleAIPMetadata;

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
}
