package org.roda.core.plugins.plugins.internal.disposal.hold;

import static org.roda.core.data.common.RodaConstants.PreservationEventType.POLICY_ASSIGNMENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldState;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldPluginUtils {

  public static void addDisposalHoldAIPMetadata(AIP aip, String disposalHoldId, String associatedBy) {
    DisposalAIPMetadata disposal;
    if (aip.getDisposal() != null) {
      disposal = aip.getDisposal();
    } else {
      disposal = new DisposalAIPMetadata();
      aip.setDisposal(disposal);
    }

    DisposalHoldAIPMetadata disposalHoldAIPMetadata = disposal.findHold(disposalHoldId);
    if (disposalHoldAIPMetadata == null) {
      disposalHoldAIPMetadata = new DisposalHoldAIPMetadata();
      disposal.addDisposalHold(disposalHoldAIPMetadata);
      disposalHoldAIPMetadata.setId(disposalHoldId);
      disposalHoldAIPMetadata.setAssociatedOn(new Date());
      disposalHoldAIPMetadata.setAssociatedBy(associatedBy);
    }
  }

  public static void addTransitiveDisposalHoldAIPMetadata(AIP aip, String disposalHoldId, String fromAIP) {
    DisposalAIPMetadata disposal;
    if (aip.getDisposal() != null) {
      disposal = aip.getDisposal();
    } else {
      disposal = new DisposalAIPMetadata();
    }
    aip.setDisposal(disposal);

    DisposalTransitiveHoldAIPMetadata disposalTransitiveHoldAIPMetadata = disposal.findTransitiveHold(disposalHoldId);
    if (disposalTransitiveHoldAIPMetadata == null) {
      disposalTransitiveHoldAIPMetadata = new DisposalTransitiveHoldAIPMetadata();
      disposal.addTransitiveHold(disposalTransitiveHoldAIPMetadata);
    }

    disposalTransitiveHoldAIPMetadata.setId(disposalHoldId);
    disposalTransitiveHoldAIPMetadata.addFromAip(fromAIP);
  }

  public static String liftDisposalHoldFromAIP(AIP aip, String disposalHoldId, Report reportItem) {
    String outcomeLiftText = "Cannot find disposal hold " + disposalHoldId + " on AIP " + aip.getId();
    DisposalAIPMetadata disposal = aip.getDisposal();
    if (disposal != null) {
      List<DisposalHoldAIPMetadata> holds = disposal.getHolds();
      for (DisposalHoldAIPMetadata hold : new ArrayList<>(holds)) {
        if (disposalHoldId.equals(hold.getId())) {
          outcomeLiftText = liftHold(hold, disposal, aip.getId(), reportItem);
          break;
        }
      }
    }
    return outcomeLiftText;
  }

  public static String liftTransitiveDisposalHoldFromAIP(AIP aip, String disposalHoldId, Report reportItem) {
    DisposalAIPMetadata disposal = aip.getDisposal();
    String outcomeLiftText = "Cannot find transitive disposal hold " + disposalHoldId + " on AIP " + aip.getId();
    if (disposal != null) {
      List<DisposalTransitiveHoldAIPMetadata> transitiveHolds = disposal.getTransitiveHolds();
      for (DisposalTransitiveHoldAIPMetadata transitiveHold : new ArrayList<>(transitiveHolds)) {
        if (disposalHoldId.equals(transitiveHold.getId())) {
          outcomeLiftText = liftTransitiveHold(transitiveHold, disposal, aip.getId(), reportItem);
          break;
        }
      }
    }
    return outcomeLiftText;
  }

  public static void disassociateAllDisposalHoldsFromAIP(ModelService model, PluginState state, AIP aip, Job cachedJob,
    Report reportItem) {
    DisposalAIPMetadata disposal = aip.getDisposal();
    if (disposal != null) {
      for (DisposalHoldAIPMetadata disposalHoldAIPMetadata : new ArrayList<>(disposal.getHolds())) {
        String outcomeLiftText = disassociateDisposalHoldFromAIP(disposalHoldAIPMetadata.getId(), aip, reportItem);
        model.createEvent(aip.getId(), null, null, null, POLICY_ASSIGNMENT,
          LiftDisposalHoldFromAIPPlugin.getStaticName(), null, null, state, outcomeLiftText, "",
          cachedJob.getUsername(), true);
      }
    }
  }

  public static String disassociateDisposalHoldFromAIP(String disposalHoldAIPMetadataID, AIP aip, Report reportItem) {
    String outcomeLiftText;
    DisposalHold disposalHold = RodaCoreFactory.getDisposalHold(disposalHoldAIPMetadataID);
    if (disposalHold.getState().equals(DisposalHoldState.LIFTED)) {
      outcomeLiftText = "Disposal hold '" + disposalHoldAIPMetadataID
        + "' is lifted and cannot be disassociated from aip '" + aip.getId() + "'";
    } else {
      aip.removeDisposalHold(disposalHoldAIPMetadataID);
      if (disposalHold == null) {
        outcomeLiftText = "Disposal hold '" + disposalHoldAIPMetadataID + "' was successfully disassociated from AIP '"
          + aip.getId() + "'";
        reportItem.addPluginDetails(
          "Disposal hold '" + disposalHoldAIPMetadataID + "' was successfully disassociated from AIP\n");
      } else {
        outcomeLiftText = "Disposal hold '" + disposalHold.getTitle() + "' (" + disposalHold.getId()
          + ") was successfully disassociated from AIP '" + aip.getId() + "'";
        reportItem.addPluginDetails("Disposal hold '" + disposalHold.getTitle() + "' (" + disposalHold.getId()
          + ") was successfully disassociated from AIP\n");
      }
    }
    return outcomeLiftText;
  }

  public static String disassociateTransitiveDisposalHoldFromAIP(String disposalHoldAIPMetadataID, AIP aip,
    Report reportItem) {
    String outcomeLiftText;
    DisposalHold disposalHold = RodaCoreFactory.getDisposalHold(disposalHoldAIPMetadataID);
    if (disposalHold.getState().equals(DisposalHoldState.LIFTED)) {
      outcomeLiftText = "Transitive Disposal hold '" + disposalHoldAIPMetadataID
        + "' is lifted and cannot be disassociated from aip '" + aip.getId() + "'";
    } else {
      aip.removeTransitiveHold(disposalHoldAIPMetadataID);
      if (disposalHold == null) {
        outcomeLiftText = "Transitive disposal hold '" + disposalHoldAIPMetadataID
          + "' was successfully disassociated from AIP '" + aip.getId() + "'";
        reportItem.addPluginDetails(
          "Transitive disposal hold '" + disposalHoldAIPMetadataID + "' was successfully disassociated from AIP\n");
      } else {
        outcomeLiftText = "Transitive disposal hold '" + disposalHold.getTitle() + "' (" + disposalHold.getId()
          + ") was successfully disassociated from AIP '" + aip.getId() + "'";
        reportItem.addPluginDetails("Transitive disposal hold '" + disposalHold.getTitle() + "' ("
          + disposalHold.getId() + ") was successfully disassociated from AIP\n");
      }
    }
    return outcomeLiftText;
  }

  private static String liftTransitiveHold(DisposalTransitiveHoldAIPMetadata transitiveHold,
    DisposalAIPMetadata disposal, String aipId, Report reportItem) {
    DisposalHold liftedHold = RodaCoreFactory.getDisposalHold(transitiveHold.getId());
    String outcomeLiftText;
    if (liftedHold == null) {
      outcomeLiftText = "Transitive Disposal hold '" + transitiveHold.getId() + "' was successfully lifted from AIP '"
        + aipId + "'";
      reportItem.addPluginDetails(
        "Transitive Disposal hold '" + transitiveHold.getId() + "' was successfully lifted from AIP\n");
    } else {
      outcomeLiftText = "Transitive Disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
        + ") was successfully lifted from AIP '" + aipId + "'";
      reportItem.addPluginDetails("Transitive Disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
        + ") was successfully lifted from AIP\n");
    }

    return outcomeLiftText;
  }

  private static String liftHold(DisposalHoldAIPMetadata disposalHold, DisposalAIPMetadata disposal, String aipId,
    Report reportItem) {

    // disposal.getHolds().remove(disposalHold);
    DisposalHold liftedHold = RodaCoreFactory.getDisposalHold(disposalHold.getId());
    String outcomeLiftText;
    if (liftedHold == null) {
      outcomeLiftText = "Disposal hold '" + disposalHold.getId() + "' was successfully lifted from AIP '" + aipId + "'";
      reportItem.addPluginDetails("Disposal hold '" + disposalHold.getId() + "' was successfully lifted from AIP\n");
    } else {
      outcomeLiftText = "Disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
        + ") was successfully lifted from AIP '" + aipId + "'";
      reportItem.addPluginDetails("Disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
        + ") was successfully lifted from AIP\n");
    }

    return outcomeLiftText;
  }

  public static IterableIndexResult<IndexedAIP> getTransitivesHoldsAIPs(IndexService index, String aipId)
    throws NotFoundException, GenericException, RequestNotValidException {
    final IndexedAIP indexedAip = index.retrieve(IndexedAIP.class, aipId, new ArrayList<>());
    List<FilterParameter> ancestorsList = new ArrayList<>();
    for (String ancestor : indexedAip.getAncestors()) {
      ancestorsList.add(new SimpleFilterParameter(RodaConstants.INDEX_UUID, ancestor));
    }
    ancestorsList.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipId));
    Filter ancestorsFilter = new Filter(new OrFiltersParameters(ancestorsList));

    return index.findAll(IndexedAIP.class, ancestorsFilter, false,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID));
  }
}
