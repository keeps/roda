/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.hold;

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
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.metadata.DisposalAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
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
        String outcomeLiftText = disassociateDisposalHoldFromAIP(disposalHoldAIPMetadata.getId(), aip, reportItem)
          .getSecond();
        model.createEvent(aip.getId(), null, null, null, POLICY_ASSIGNMENT, LiftDisposalHoldPlugin.getStaticName(),
          null, null, state, outcomeLiftText, "", cachedJob.getUsername(), true, null);
      }
    }
  }

  public static Pair<Boolean, String> disassociateDisposalHoldFromAIP(String disposalHoldAIPMetadataID, AIP aip,
    Report reportItem) {
    boolean lifted;
    String outcomeLiftText;
    DisposalHold disposalHold = RodaCoreFactory.getDisposalHold(disposalHoldAIPMetadataID);
    if (disposalHold != null && disposalHold.getState().equals(DisposalHoldState.LIFTED)) {
      lifted = false;
      outcomeLiftText = "Disposal hold '" + disposalHoldAIPMetadataID
        + "' is lifted and cannot be disassociated from aip '" + aip.getId() + "'";
    } else {
      aip.removeDisposalHold(disposalHoldAIPMetadataID);
      lifted = true;
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
    return new Pair<>(lifted, outcomeLiftText);
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
      outcomeLiftText = "Transitive disposal hold '" + transitiveHold.getId() + "' was successfully lifted from AIP '"
        + aipId + "'";
      reportItem.addPluginDetails(
        "Transitive disposal hold '" + transitiveHold.getId() + "' was successfully lifted from AIP\n");
    } else {
      outcomeLiftText = "Transitive disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
        + ") was successfully lifted from AIP '" + aipId + "'";
      reportItem.addPluginDetails("Transitive disposal hold '" + liftedHold.getTitle() + "' (" + liftedHold.getId()
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
