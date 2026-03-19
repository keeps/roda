/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.confirmation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.confirmation.DestroyedSelectionState;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationPluginUtils {

  private DisposalConfirmationPluginUtils() {
  }

  public static DisposalConfirmation getDisposalConfirmation(String confirmationId, String title, long storageSize,
    Set<String> disposalHolds, Set<String> disposalSchedules, long numberOfAIPs, Map<String, String> extraFields) {

    DisposalConfirmation confirmationMetadata = new DisposalConfirmation();
    confirmationMetadata.setId(confirmationId);
    confirmationMetadata.setTitle(title);
    confirmationMetadata.setSize(storageSize);
    confirmationMetadata.addDisposalHoldIds(disposalHolds);
    confirmationMetadata.addDisposalScheduleIds(disposalSchedules);
    confirmationMetadata.setNumberOfAIPs(numberOfAIPs);
    confirmationMetadata.setExtraFields(extraFields);

    return confirmationMetadata;
  }

  public static DisposalConfirmationAIPEntry getAIPEntryFromAIP(IndexService indexService, AIP aip,
    String topAncestorId, DestroyedSelectionState destroyedSelectionState, Set<String> disposalSchedules,
    Set<String> disposalHolds, Set<String> disposalHoldTransitives)
    throws GenericException, RequestNotValidException, NotFoundException {

    IndexedAIP indexedAIP = indexService.retrieve(IndexedAIP.class, aip.getId(),
      Arrays.asList(RodaConstants.AIP_LEVEL, RodaConstants.AIP_TITLE, RodaConstants.AIP_OVERDUE_DATE));

    return createDisposalConfirmationAIPEntry(indexService, indexedAIP, aip, topAncestorId, destroyedSelectionState,
      disposalSchedules, disposalHolds, disposalHoldTransitives);
  }

  public static DisposalConfirmationAIPEntry getAIPEntryFromAIP(IndexService indexService, AIP aip,
    DestroyedSelectionState destroyedSelectionState, Set<String> disposalSchedules, Set<String> disposalHolds,
    Set<String> disposalHoldTransitives) throws GenericException, RequestNotValidException, NotFoundException {

    return getAIPEntryFromAIP(indexService, aip, null, destroyedSelectionState, disposalSchedules, disposalHolds,
      disposalHoldTransitives);
  }

  private static DisposalConfirmationAIPEntry createDisposalConfirmationAIPEntry(IndexService indexService,
    final IndexedAIP indexedAIP, final AIP aip, String topAncestorId, DestroyedSelectionState destroyedSelectionState,
    Set<String> disposalSchedules, Set<String> disposalHolds, Set<String> disposalHoldTransitives)
    throws GenericException, RequestNotValidException {
    DisposalConfirmationAIPEntry entry = new DisposalConfirmationAIPEntry();

    entry.setAipId(aip.getId());
    entry.setAipLevel(indexedAIP.getLevel());
    entry.setAipTitle(indexedAIP.getTitle());
    entry.setParentId(indexedAIP.getParentID());
    entry.setDestroyedTransitiveSource(topAncestorId);
    entry.setDestroyedSelection(destroyedSelectionState);
    entry.setAipCreationDate(indexedAIP.getCreatedOn());
    entry.setAipOverdueDate(indexedAIP.getOverdueDate());

    entry.setAipDisposalScheduleId(indexedAIP.getDisposalScheduleId());
    disposalSchedules.add(indexedAIP.getDisposalScheduleId());

    entry.setAipDisposalHoldIds(getDisposalHoldIds(aip.getHolds()));
    disposalHolds.addAll(entry.getAipDisposalHoldIds());

    List<String> collect = aip.getTransitiveHolds().stream().map(DisposalTransitiveHoldAIPMetadata::getId)
      .collect(Collectors.toList());
    entry.setAipDisposalHoldTransitiveIds(collect);
    disposalHoldTransitives.addAll(collect);

    getStorageSizeInBytesForAIP(indexService, aip.getId(), entry);

    return entry;
  }

  private static List<String> getDisposalHoldIds(List<DisposalHoldAIPMetadata> disposalHoldAssociation) {
    List<String> holdIds = new ArrayList<>();

    if (disposalHoldAssociation != null) {
      for (DisposalHoldAIPMetadata holdAssociation : disposalHoldAssociation) {
        holdIds.add(holdAssociation.getId());
      }
    }

    return holdIds;
  }

  private static void getStorageSizeInBytesForAIP(IndexService indexService, String aipId,
    DisposalConfirmationAIPEntry entry) throws GenericException, RequestNotValidException {

    long totalSize = 0L;
    long totalOfDataFiles = 0L;

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));

    IterableIndexResult<IndexedRepresentation> all = indexService.findAll(IndexedRepresentation.class, filter, null,
      true,
      Arrays.asList(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES));

    for (IndexedRepresentation representation : all) {
      totalSize += representation.getSizeInBytes();
      totalOfDataFiles += representation.getNumberOfDataFiles();
    }

    entry.setAipSize(totalSize);
    entry.setAipNumberOfFiles(totalOfDataFiles);
  }
}
