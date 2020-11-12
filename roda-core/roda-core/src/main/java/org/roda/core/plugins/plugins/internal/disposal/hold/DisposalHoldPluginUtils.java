package org.roda.core.plugins.plugins.internal.disposal.hold;

import static org.roda.core.data.common.RodaConstants.PreservationEventType.POLICY_ASSIGNMENT;

import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.model.ModelService;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldPluginUtils {

  public static DisposalHoldAssociation createDisposalHoldAssociation(String disposalHoldId, String associatedBy) {
    DisposalHoldAssociation disposalHoldAssociation = new DisposalHoldAssociation();
    disposalHoldAssociation.setAssociatedOn(new Date());
    disposalHoldAssociation.setAssociatedBy(associatedBy);
    disposalHoldAssociation.setId(disposalHoldId);

    return disposalHoldAssociation;
  }

  public static void liftDisposalHoldFromAIP(ModelService model, PluginState state, AIP aip,
    String disposalHoldId, Job cachedJob, Report reportItem) {
    for (DisposalHoldAssociation association : aip.getDisposalHoldAssociation()) {
      if (disposalHoldId.equals(association.getId()) && association.getLiftedOn() == null) {
        String outcomeLiftText = liftHold(association, cachedJob.getUsername(), aip.getId(), reportItem);

        model.createEvent(aip.getId(), null, null, null, POLICY_ASSIGNMENT,
            LiftDisposalHoldFromAIPPlugin.getStaticName(), null, null, state, outcomeLiftText, "",
            cachedJob.getUsername(), true);
      }
    }
  }

  public static void liftAllDisposalHoldsFromAIP(ModelService model, PluginState state, AIP aip, Job cachedJob,
    Report reportItem) {
    for (DisposalHoldAssociation association : aip.getDisposalHoldAssociation()) {
      if (association.getLiftedOn() == null) {
        String outcomeLiftText = liftHold(association, cachedJob.getUsername(), aip.getId(), reportItem);

        model.createEvent(aip.getId(), null, null, null, POLICY_ASSIGNMENT,
          LiftDisposalHoldFromAIPPlugin.getStaticName(), null, null, state, outcomeLiftText, "",
          cachedJob.getUsername(), true);
      }
    }
  }

  private static String liftHold(DisposalHoldAssociation association, String liftedBy, String aipId,
    Report reportItem) {
    association.setLiftedOn(new Date());
    association.setLiftedBy(liftedBy);

    // Uses cache to retrieve the disposal hold
    DisposalHold liftedHold = RodaCoreFactory.getDisposalHold(association.getId());
    String outcomeLiftText;
    if (liftedHold == null) {
      outcomeLiftText = "Disposal hold '" + association.getId() + "' was successfully lifted from AIP '" + aipId + "'";
      reportItem.addPluginDetails("Disposal hold '" + association.getId() + "' was successfully lifted from AIP\n");
    } else {
      outcomeLiftText = "Disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
        + ") was successfully lifted from AIP '" + aipId + "'";
      reportItem.addPluginDetails("Disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
        + ") was successfully lifted from AIP\n");
    }

    return outcomeLiftText;
  }
}
