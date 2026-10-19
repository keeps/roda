/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.catalog;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Committed structural information of AIPs (table {@code catalog_aip}). Only
 * written when data is committed to the main storage: at the end of a RODA
 * transaction, by writes made outside a transaction, and by the rebuild job.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
public class CatalogService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogService.class);

  private static final String COLUMNS = "id, parent_id, state, ghost, ingest_job_id, ingest_update_job_ids, "
    + "ingest_sip_ids, instance_id, updated_on";
  private static final String UPSERT = "INSERT INTO catalog_aip (" + COLUMNS
    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET parent_id = EXCLUDED.parent_id, "
    + "state = EXCLUDED.state, ghost = EXCLUDED.ghost, ingest_job_id = EXCLUDED.ingest_job_id, "
    + "ingest_update_job_ids = EXCLUDED.ingest_update_job_ids, ingest_sip_ids = EXCLUDED.ingest_sip_ids, "
    + "instance_id = EXCLUDED.instance_id, updated_on = EXCLUDED.updated_on";

  private static final RowMapper<CatalogAip> ROW_MAPPER = (rs, rowNum) -> new CatalogAip(rs.getString("id"),
    rs.getString("parent_id"), AIPState.valueOf(rs.getString("state")), rs.getBoolean("ghost"),
    rs.getString("ingest_job_id"), toList(rs, "ingest_update_job_ids"), toList(rs, "ingest_sip_ids"),
    rs.getString("instance_id"), rs.getTimestamp("updated_on").toInstant());

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;

  public CatalogService(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  public Optional<CatalogAip> get(String aipId) {
    return jdbcTemplate.query("SELECT " + COLUMNS + " FROM catalog_aip WHERE id = ?", ROW_MAPPER, aipId).stream()
      .findFirst();
  }

  public List<CatalogAip> findByIngestSIPId(String sipId) {
    return jdbcTemplate.query("SELECT " + COLUMNS + " FROM catalog_aip WHERE ingest_sip_ids @> ARRAY[?]::text[]",
      ROW_MAPPER, sipId);
  }

  public List<CatalogAip> findChildren(String parentId) {
    return jdbcTemplate.query("SELECT " + COLUMNS + " FROM catalog_aip WHERE parent_id = ?", ROW_MAPPER, parentId);
  }

  public List<CatalogAip> findGhosts(String jobId) {
    if (jobId == null) {
      return jdbcTemplate.query("SELECT " + COLUMNS + " FROM catalog_aip WHERE ghost", ROW_MAPPER);
    }
    return jdbcTemplate.query("SELECT " + COLUMNS + " FROM catalog_aip WHERE ghost AND ingest_job_id = ?",
      ROW_MAPPER, jobId);
  }

  public long count() {
    Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM catalog_aip", Long.class);
    return count == null ? 0 : count;
  }

  public void upsert(CatalogAip aip) {
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(UPSERT);
      ps.setString(1, aip.id());
      ps.setString(2, aip.parentId());
      ps.setString(3, aip.state().name());
      ps.setBoolean(4, aip.ghost());
      ps.setString(5, aip.ingestJobId());
      ps.setArray(6, connection.createArrayOf("text", aip.ingestUpdateJobIds().toArray()));
      ps.setArray(7, connection.createArrayOf("text", aip.ingestSIPIds().toArray()));
      ps.setString(8, aip.instanceId());
      ps.setTimestamp(9, Timestamp.from(aip.updatedOn()));
      return ps;
    });
  }

  public void delete(String aipId) {
    jdbcTemplate.update("DELETE FROM catalog_aip WHERE id = ?", aipId);
  }

  /**
   * Makes the catalog rows of the given AIPs match the main storage, in a single
   * database transaction: AIPs that exist are inserted or updated, AIPs that no
   * longer exist are removed.
   */
  public void synchronize(Collection<String> aipIds, ModelService mainModel) {
    if (aipIds.isEmpty()) {
      return;
    }
    transactionTemplate.executeWithoutResult(status -> {
      for (String aipId : aipIds) {
        synchronizeOne(aipId, mainModel);
      }
    });
  }

  private void synchronizeOne(String aipId, ModelService mainModel) {
    try {
      upsert(CatalogAip.fromAIP(mainModel.retrieveAIP(aipId)));
    } catch (NotFoundException e) {
      delete(aipId);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      throw new IllegalStateException("Could not read AIP " + aipId + " to update the catalog", e);
    }
  }

  /**
   * Rebuilds the whole catalog from the main storage.
   *
   * @return number of AIPs in the catalog after the rebuild
   */
  public long rebuild(ModelService mainModel)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, NotFoundException {
    Set<String> seen = new HashSet<>();
    try (CloseableIterable<OptionalWithCause<AIP>> aips = mainModel.listAIPs()) {
      List<CatalogAip> batch = new ArrayList<>();
      for (OptionalWithCause<AIP> aip : aips) {
        if (aip.isPresent()) {
          seen.add(aip.get().getId());
          batch.add(CatalogAip.fromAIP(aip.get()));
          if (batch.size() >= 500) {
            upsertAll(batch);
            batch.clear();
          }
        } else {
          LOGGER.warn("Could not read AIP while rebuilding the catalog", aip.getCause());
        }
      }
      upsertAll(batch);
    } catch (java.io.IOException e) {
      throw new GenericException("Error listing AIPs to rebuild the catalog", e);
    }

    List<String> orphans = jdbcTemplate.queryForList("SELECT id FROM catalog_aip", String.class).stream()
      .filter(id -> !seen.contains(id)).toList();
    orphans.forEach(this::delete);
    LOGGER.info("Catalog rebuilt: {} AIPs, {} orphan rows removed", seen.size(), orphans.size());
    return seen.size();
  }

  private void upsertAll(List<CatalogAip> batch) {
    if (!batch.isEmpty()) {
      transactionTemplate.executeWithoutResult(status -> batch.forEach(this::upsert));
    }
  }

  /**
   * Compares the catalog with the main storage without changing anything.
   *
   * @return descriptions of the differences found (empty when consistent)
   */
  public List<String> verify(ModelService mainModel)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, NotFoundException {
    List<String> differences = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    try (CloseableIterable<OptionalWithCause<AIP>> aips = mainModel.listAIPs()) {
      for (OptionalWithCause<AIP> aip : aips) {
        if (aip.isPresent()) {
          CatalogAip expected = CatalogAip.fromAIP(aip.get());
          seen.add(expected.id());
          Optional<CatalogAip> actual = get(expected.id());
          if (actual.isEmpty()) {
            differences.add("missing " + expected.id());
          } else if (!sameStructure(expected, actual.get())) {
            differences.add("different " + expected.id() + ": expected " + expected + " but was " + actual.get());
          }
        }
      }
    } catch (java.io.IOException e) {
      throw new GenericException("Error listing AIPs to verify the catalog", e);
    }
    jdbcTemplate.queryForList("SELECT id FROM catalog_aip", String.class).stream().filter(id -> !seen.contains(id))
      .forEach(id -> differences.add("orphan " + id));
    return differences;
  }

  private static boolean sameStructure(CatalogAip a, CatalogAip b) {
    return a.id().equals(b.id()) && java.util.Objects.equals(a.parentId(), b.parentId()) && a.state() == b.state()
      && a.ghost() == b.ghost() && java.util.Objects.equals(a.ingestJobId(), b.ingestJobId())
      && a.ingestUpdateJobIds().equals(b.ingestUpdateJobIds()) && a.ingestSIPIds().equals(b.ingestSIPIds())
      && java.util.Objects.equals(a.instanceId(), b.instanceId());
  }

  private static List<String> toList(ResultSet rs, String column) throws SQLException {
    Array array = rs.getArray(column);
    if (array == null) {
      return List.of();
    }
    return List.copyOf(Arrays.asList((String[]) array.getArray()));
  }
}
