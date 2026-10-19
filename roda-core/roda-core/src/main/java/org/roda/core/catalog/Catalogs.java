/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.catalog;

import java.util.List;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.config.SpringContext;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.model.ModelService;
import org.roda.core.model.TransactionalModelService;
import org.roda.core.transaction.RODATransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to read the AIP catalog from code that only has a
 * {@link ModelService} at hand (plugins, helpers).
 * <p>
 * The catalog is only used when RODA transactions are active, because it is
 * only kept up to date in that mode. Otherwise {@link #reader(ModelService)}
 * returns empty and callers keep their previous (index based) behavior.
 * </p>
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public final class Catalogs {
  private static final Logger LOGGER = LoggerFactory.getLogger(Catalogs.class);

  private Catalogs() {
    // utility class
  }

  /**
   * @return the catalog view to use with {@code model}: the transaction's view
   *         when {@code model} belongs to a RODA transaction, the committed
   *         catalog otherwise, or empty when the catalog is not in use
   */
  public static Optional<CatalogReader> reader(ModelService model) {
    if (model instanceof TransactionalModelService transactionalModel) {
      return transactionalModel.getCatalogReader();
    }
    return service().filter(s -> isTransactionalModeActive()).map(Catalogs::committed);
  }

  public static Optional<CatalogService> service() {
    if (!SpringContext.isContextInitialized()) {
      return Optional.empty();
    }
    try {
      return Optional.of(SpringContext.getBean(CatalogService.class));
    } catch (RuntimeException e) {
      LOGGER.debug("Catalog service not available", e);
      return Optional.empty();
    }
  }

  public static boolean isTransactionalModeActive() {
    try {
      RODATransactionManager manager = RodaCoreFactory.getTransactionManager();
      return manager != null && manager.isInitialized();
    } catch (GenericException e) {
      return false;
    }
  }

  /** Reader over committed rows only. */
  public static CatalogReader committed(CatalogService catalog) {
    return new CatalogReader() {
      @Override
      public Optional<CatalogAip> get(String aipId) {
        return catalog.get(aipId);
      }

      @Override
      public List<CatalogAip> findByIngestSIPId(String sipId) {
        return catalog.findByIngestSIPId(sipId);
      }

      @Override
      public List<CatalogAip> findChildren(String parentId) {
        return catalog.findChildren(parentId);
      }

      @Override
      public List<CatalogAip> findGhosts(String jobId) {
        return catalog.findGhosts(jobId);
      }
    };
  }
}
