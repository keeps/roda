package org.roda.core.plugins.plugins.internal.disposal.hold;

import static org.roda.core.data.common.RodaConstants.PreservationEventType.POLICY_ASSIGNMENT;

import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalTransitiveHoldAIPMetadata;
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

  public static void addDisposalHoldAIPMetadata(AIP aip, String disposalHoldId, String associatedBy, DisposalTransitiveHoldAIPMetadata transitiveHold) {
    DisposalAIPMetadata disposal;
    if (aip.getDisposal() != null) {
      disposal = aip.getDisposal();
    } else {
      disposal = new DisposalAIPMetadata();
      aip.setDisposal(disposal);
    }

    DisposalHoldAIPMetadata disposalHoldAIPMetadata = disposal.findHold(disposalHoldId);
    if(disposalHoldAIPMetadata == null) {
      disposalHoldAIPMetadata = new DisposalHoldAIPMetadata();
      disposalHoldAIPMetadata.setId(disposalHoldId);
      disposalHoldAIPMetadata.setAssociatedOn(new Date());
      disposalHoldAIPMetadata.setAssociatedBy(associatedBy);
      disposal.addDisposalHold(disposalHoldAIPMetadata);
    }
    if (transitiveHold != null) {
      if(disposalHoldAIPMetadata.findTransitiveAip(transitiveHold.getAipId()) == null){
        disposalHoldAIPMetadata.addTransitiveAip(transitiveHold);
      }
    }
  }

  public static void liftDisposalHoldFromAIP(ModelService model, PluginState state, AIP aip, String disposalHoldId,
    Job cachedJob, Report reportItem) {
    for (DisposalHoldAssociation association : aip.getDisposalHoldAssociation()) {
      if (disposalHoldId.equals(association.getId()) && association.getLiftedOn() == null) {
        String outcomeLiftText = liftHold(association, cachedJob.getUsername(), aip.getId(), reportItem);

        model.createEvent(aip.getId(), null, null, null, POLICY_ASSIGNMENT,
          LiftDisposalHoldFromAIPPlugin.getStaticName(), null, null, state, outcomeLiftText, "",
          cachedJob.getUsername(), true);
      }
    }
  }

  public static void liftTransitiveDisposalHoldFromAIP(ModelService model, PluginState state, AIP aip,
    String disposalHoldId, Job cachedJob, Report reportItem,
    List<DisposalHoldAssociation> transitiveDisposalHoldAssociationList) {

    for (DisposalHoldAssociation disposalHoldAssociation : transitiveDisposalHoldAssociationList) {
      aip.getTransitiveDisposalHoldAssociation().removeIf(d -> d.getId().equals(disposalHoldAssociation.getId()));
    }

    for (DisposalHoldAssociation association : transitiveDisposalHoldAssociationList) {
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

  public static void disassociateAllDisposalHoldsFromAIP(ModelService model, PluginState state, AIP aip, Job cachedJob,
    Report reportItem) {
    DisposalAIPMetadata disposal = aip.getDisposal();
    if (disposal != null) {
      for (DisposalHoldAIPMetadata disposalHoldAIPMetadata : disposal.getHolds()) {
        String outcomeLiftText = disassociateDisposalHoldFromAIP(disposalHoldAIPMetadata, aip.getId(), reportItem);
        model.createEvent(aip.getId(), null, null, null, POLICY_ASSIGNMENT,
          LiftDisposalHoldFromAIPPlugin.getStaticName(), null, null, state, outcomeLiftText, "",
          cachedJob.getUsername(), true);
      }
    }
  }

  private static String disassociateDisposalHoldFromAIP(DisposalHoldAIPMetadata disposalHoldAIPMetadata, String aipId,
    Report reportItem) {
    String outcomeLiftText;
    DisposalHold disposalHold = RodaCoreFactory.getDisposalHold(disposalHoldAIPMetadata.getId());
    if (disposalHold == null) {
      outcomeLiftText = "Disposal hold '" + disposalHoldAIPMetadata.getId() + "' was successfully lifted from AIP '"
        + aipId + "'";
      reportItem
        .addPluginDetails("Disposal hold '" + disposalHoldAIPMetadata.getId() + "' was successfully lifted from AIP\n");
    } else {
      outcomeLiftText = "Disposal hold '" + disposalHold.getTitle() + "' (" + disposalHold.getId()
        + ") was successfully lifted from AIP '" + aipId + "'";
      reportItem.addPluginDetails("Disposal hold '" + disposalHold.getTitle() + "' (" + disposalHold.getId()
        + ") was successfully lifted from AIP\n");
    }
    return outcomeLiftText;
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
