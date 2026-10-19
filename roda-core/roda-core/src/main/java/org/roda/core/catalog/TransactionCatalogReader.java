/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Catalog view of a RODA transaction: committed rows combined with the AIPs the
 * transaction has written, read from its own model (staging first). Nothing is
 * written to the database; changes made by other transactions that haven't
 * committed yet are never visible.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransactionCatalogReader implements CatalogReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCatalogReader.class);

  private final CatalogService catalog;
  private final ModelService transactionModel;
  private final Supplier<Set<String>> writtenAIPIds;
  // AIPs written by the transaction, as they are now in its model
  private final Map<String, Optional<CatalogAip>> overlay = new ConcurrentHashMap<>();

  public TransactionCatalogReader(CatalogService catalog, ModelService transactionModel,
    Supplier<Set<String>> writtenAIPIds) {
    this.catalog = catalog;
    this.transactionModel = transactionModel;
    this.writtenAIPIds = writtenAIPIds;
  }

  /** Called whenever the transaction writes the AIP. */
  public void invalidate(String aipId) {
    overlay.remove(aipId);
  }

  @Override
  public Optional<CatalogAip> get(String aipId) {
    if (writtenAIPIds.get().contains(aipId)) {
      return written(aipId);
    }
    return catalog.get(aipId);
  }

  @Override
  public List<CatalogAip> findByIngestSIPId(String sipId) {
    return combine(catalog.findByIngestSIPId(sipId), aip -> aip.hasIngestSIPId(sipId));
  }

  @Override
  public List<CatalogAip> findChildren(String parentId) {
    return combine(catalog.findChildren(parentId), aip -> parentId.equals(aip.parentId()));
  }

  @Override
  public List<CatalogAip> findGhosts(String jobId) {
    return combine(catalog.findGhosts(jobId), aip -> aip.ghost() && (jobId == null || jobId.equals(aip.ingestJobId())));
  }

  private List<CatalogAip> combine(List<CatalogAip> committed, Predicate<CatalogAip> matches) {
    Set<String> written = writtenAIPIds.get();
    List<CatalogAip> result = new ArrayList<>();
    for (CatalogAip aip : committed) {
      if (!written.contains(aip.id())) {
        result.add(aip);
      }
    }
    for (String aipId : written) {
      written(aipId).filter(matches).ifPresent(result::add);
    }
    return result;
  }

  private Optional<CatalogAip> written(String aipId) {
    return overlay.computeIfAbsent(aipId, id -> {
      try {
        return Optional.of(CatalogAip.fromAIP(transactionModel.retrieveAIP(id)));
      } catch (NotFoundException e) {
        return Optional.empty();
      } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
        LOGGER.warn("Could not read AIP {} written by the transaction", id, e);
        return Optional.empty();
      }
    });
  }
}
