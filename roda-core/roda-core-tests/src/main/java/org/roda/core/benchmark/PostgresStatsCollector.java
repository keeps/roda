/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.benchmark;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects PostgreSQL-side statistics for a fixed set of tables, over a
 * dedicated JDBC connection (outside the application connection pool, so that
 * its own queries neither compete for pool connections nor show up as
 * application statements).
 *
 * Collected data:
 * <ul>
 * <li>table activity (pg_stat_user_tables): inserted/updated/HOT-updated/deleted
 * tuples, sequential and index scans, live and dead tuples, autovacuum runs</li>
 * <li>table storage: heap, TOAST and index sizes, row count and average row
 * size</li>
 * <li>WAL bytes generated (from the WAL insert position)</li>
 * <li>per-statement statistics (pg_stat_statements), if the extension is
 * preloaded</li>
 * </ul>
 *
 * Note that table activity counters of other backends are only published when
 * those backends flush their pending statistics (at the latest when they
 * disconnect), so callers must make the application pool close its connections
 * before taking a snapshot.
 *
 * @author RODA Development Team
 */
public class PostgresStatsCollector implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(PostgresStatsCollector.class);

  /**
   * Statements issued by this collector itself, to be left out of the
   * pg_stat_statements results.
   */
  private static final String NOT_COLLECTOR_STATEMENT = "query NOT ILIKE '%pg\\_stat%'"
    + " AND query NOT ILIKE '%pg\\_class%'"
    + " AND query NOT ILIKE '%pg\\_wal\\_lsn%' AND query NOT ILIKE '%pg\\_column\\_size%'"
    + " AND query NOT ILIKE '%pg\\_indexes%' AND query NOT ILIKE '%pg\\_settings%'"
    + " AND query NOT ILIKE 'vacuum%' AND query NOT ILIKE 'create extension%'";

  private static final String CURRENT_DB = "dbid = (SELECT oid FROM pg_database WHERE datname = current_database())";

  private final Connection connection;
  private final List<String> tables;
  private final boolean statementsAvailable;

  public PostgresStatsCollector(String url, String username, String password, List<String> tables)
    throws SQLException {
    this.connection = DriverManager.getConnection(url, username, password);
    this.connection.setAutoCommit(true);
    this.tables = existingTables(tables);
    this.statementsAvailable = initStatementsExtension();
  }

  /**
   * @return the given tables that exist, in the given order (so the same
   *         benchmark also runs against an older schema)
   */
  private List<String> existingTables(List<String> wanted) throws SQLException {
    List<String> existing = new ArrayList<>();
    try (PreparedStatement statement = connection
      .prepareStatement("SELECT 1 FROM pg_tables WHERE schemaname = current_schema() AND tablename = ?")) {
      for (String table : wanted) {
        statement.setString(1, table);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            existing.add(table);
          }
        }
      }
    }
    if (existing.size() < wanted.size()) {
      LOGGER.info("Not measuring tables absent from this schema: {}",
        wanted.stream().filter(t -> !existing.contains(t)).toList());
    }
    return existing;
  }

  private boolean initStatementsExtension() {
    try (Statement statement = connection.createStatement()) {
      statement.execute("CREATE EXTENSION IF NOT EXISTS pg_stat_statements");
      statement.executeQuery("SELECT 1 FROM pg_stat_statements LIMIT 1").close();
      return true;
    } catch (SQLException e) {
      LOGGER.warn("pg_stat_statements is not available (is it in shared_preload_libraries?); "
        + "per-statement statistics will not be collected: {}", e.getMessage());
      return false;
    }
  }

  public boolean isStatementsAvailable() {
    return statementsAvailable;
  }

  /**
   * Empties the given tables and rewrites them (VACUUM FULL), so that every run
   * starts from the same physical state.
   */
  public void resetTables() throws SQLException {
    try (Statement statement = connection.createStatement()) {
      for (String table : tables) {
        statement.execute("DELETE FROM " + table);
        statement.execute("VACUUM (FULL, ANALYZE) " + table);
      }
    }
  }

  public void resetStatements() throws SQLException {
    if (statementsAvailable) {
      try (Statement statement = connection.createStatement()) {
        statement.execute("SELECT pg_stat_statements_reset()");
      }
    }
  }

  public Snapshot snapshot() throws SQLException {
    Snapshot snapshot = new Snapshot();
    try (Statement statement = connection.createStatement()) {
      statement.execute("SELECT pg_stat_clear_snapshot()");
      try (ResultSet rs = statement
        .executeQuery("SELECT pg_wal_lsn_diff(pg_current_wal_insert_lsn(), '0/0')::bigint")) {
        rs.next();
        snapshot.walBytes = rs.getLong(1);
      }
    }

    String sql = "SELECT s.relname, s.n_tup_ins, s.n_tup_upd, s.n_tup_hot_upd, s.n_tup_del, s.seq_scan,"
      + " s.seq_tup_read, COALESCE(s.idx_scan, 0) AS idx_scan, s.n_live_tup, s.n_dead_tup, s.autovacuum_count,"
      + " pg_total_relation_size(c.oid) AS total_bytes, pg_relation_size(c.oid) AS heap_bytes,"
      + " pg_indexes_size(c.oid) AS index_bytes,"
      + " COALESCE(pg_total_relation_size(NULLIF(c.reltoastrelid, 0)), 0) AS toast_bytes"
      + " FROM pg_stat_user_tables s JOIN pg_class c ON c.oid = s.relid WHERE s.relname = ANY(?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      Array names = connection.createArrayOf("text", tables.toArray());
      statement.setArray(1, names);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          TableStats stats = new TableStats();
          stats.tuplesInserted = rs.getLong("n_tup_ins");
          stats.tuplesUpdated = rs.getLong("n_tup_upd");
          stats.tuplesHotUpdated = rs.getLong("n_tup_hot_upd");
          stats.tuplesDeleted = rs.getLong("n_tup_del");
          stats.seqScans = rs.getLong("seq_scan");
          stats.seqTuplesRead = rs.getLong("seq_tup_read");
          stats.indexScans = rs.getLong("idx_scan");
          stats.liveTuples = rs.getLong("n_live_tup");
          stats.deadTuples = rs.getLong("n_dead_tup");
          stats.autovacuumCount = rs.getLong("autovacuum_count");
          stats.totalBytes = rs.getLong("total_bytes");
          stats.heapBytes = rs.getLong("heap_bytes");
          stats.indexBytes = rs.getLong("index_bytes");
          stats.toastBytes = rs.getLong("toast_bytes");
          snapshot.tables.put(rs.getString("relname"), stats);
        }
      }
    }

    for (String table : tables) {
      TableStats stats = snapshot.tables.computeIfAbsent(table, k -> new TableStats());
      try (Statement statement = connection.createStatement();
        ResultSet rs = statement
          .executeQuery("SELECT count(*), COALESCE(avg(pg_column_size(t.*)), 0) FROM " + table + " t")) {
        rs.next();
        stats.rowCount = rs.getLong(1);
        stats.avgRowBytes = Math.round(rs.getDouble(2));
      }
    }
    return snapshot;
  }

  /**
   * @return the most expensive statements (by total execution time) touching
   *         the benchmarked tables, since the last
   *         {@link #resetStatements()}
   */
  public List<Map<String, Object>> topStatements(int limit) throws SQLException {
    if (!statementsAvailable) {
      return List.of();
    }
    String sql = "SELECT query, calls, round(total_exec_time::numeric, 3) AS total_exec_ms,"
      + " round(mean_exec_time::numeric, 4) AS mean_exec_ms, round(max_exec_time::numeric, 3) AS max_exec_ms,"
      + " rows, shared_blks_hit, shared_blks_read, shared_blks_dirtied, shared_blks_written, wal_records,"
      + " wal_fpi, wal_bytes::bigint AS wal_bytes FROM pg_stat_statements WHERE " + CURRENT_DB + " AND "
      + NOT_COLLECTOR_STATEMENT + " AND (" + tablesFilter() + ") ORDER BY total_exec_time DESC LIMIT ?";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, limit);
      try (ResultSet rs = statement.executeQuery()) {
        return toRows(rs);
      }
    }
  }

  /**
   * @return aggregated pg_stat_statements totals since the last
   *         {@link #resetStatements()}, both for the statements touching the
   *         benchmarked tables ("benchmarkTables") and for every application
   *         statement, including transaction control such as COMMIT ("all")
   */
  public Map<String, Object> statementTotals() throws SQLException {
    Map<String, Object> ret = new LinkedHashMap<>();
    if (!statementsAvailable) {
      return ret;
    }
    String select = "SELECT COALESCE(sum(calls), 0)::bigint AS calls,"
      + " round(COALESCE(sum(total_exec_time), 0)::numeric, 3) AS total_exec_ms,"
      + " COALESCE(sum(rows), 0)::bigint AS rows, COALESCE(sum(shared_blks_hit), 0)::bigint AS shared_blks_hit,"
      + " COALESCE(sum(shared_blks_read), 0)::bigint AS shared_blks_read,"
      + " COALESCE(sum(shared_blks_dirtied), 0)::bigint AS shared_blks_dirtied,"
      + " COALESCE(sum(wal_bytes), 0)::bigint AS wal_bytes FROM pg_stat_statements WHERE " + CURRENT_DB + " AND "
      + NOT_COLLECTOR_STATEMENT;
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery(select + " AND (" + tablesFilter() + ")")) {
        ret.put("benchmarkTables", toRows(rs).get(0));
      }
      try (ResultSet rs = statement.executeQuery(select)) {
        ret.put("all", toRows(rs).get(0));
      }
    }
    return ret;
  }

  public Map<String, Object> environment() throws SQLException {
    Map<String, Object> ret = new LinkedHashMap<>();
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery("SELECT version()")) {
        rs.next();
        ret.put("version", rs.getString(1));
      }
      try (ResultSet rs = statement.executeQuery("SELECT name, setting, unit FROM pg_settings WHERE name IN"
        + " ('shared_buffers', 'work_mem', 'synchronous_commit', 'fsync', 'wal_level', 'max_connections',"
        + " 'autovacuum', 'autovacuum_vacuum_scale_factor', 'checkpoint_timeout', 'max_wal_size') ORDER BY name")) {
        Map<String, String> settings = new LinkedHashMap<>();
        while (rs.next()) {
          String unit = rs.getString("unit");
          settings.put(rs.getString("name"), rs.getString("setting") + (unit == null ? "" : " " + unit));
        }
        ret.put("settings", settings);
      }
    }
    try (PreparedStatement statement = connection.prepareStatement(
      "SELECT tablename, indexname, indexdef FROM pg_indexes WHERE tablename = ANY(?) ORDER BY tablename, indexname")) {
      statement.setArray(1, connection.createArrayOf("text", tables.toArray()));
      try (ResultSet rs = statement.executeQuery()) {
        ret.put("indexes", toRows(rs));
      }
    }
    return ret;
  }

  private String tablesFilter() {
    List<String> conditions = new ArrayList<>();
    for (String table : tables) {
      conditions.add("query ~* '\\m" + table + "\\M'");
    }
    return String.join(" OR ", conditions);
  }

  private static List<Map<String, Object>> toRows(ResultSet rs) throws SQLException {
    List<Map<String, Object>> rows = new ArrayList<>();
    ResultSetMetaData metaData = rs.getMetaData();
    while (rs.next()) {
      Map<String, Object> row = new LinkedHashMap<>();
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        row.put(metaData.getColumnLabel(i), rs.getObject(i));
      }
      rows.add(row);
    }
    return rows;
  }

  @Override
  public void close() throws SQLException {
    connection.close();
  }

  /**
   * Point-in-time statistics; differences between two snapshots give the
   * activity of a benchmark phase.
   */
  public static class Snapshot {
    private long walBytes;
    private final Map<String, TableStats> tables = new LinkedHashMap<>();

    public long getWalBytes() {
      return walBytes;
    }

    public Map<String, TableStats> getTables() {
      return tables;
    }
  }

  /**
   * Statistics of a single table.
   */
  public static class TableStats {
    private long tuplesInserted;
    private long tuplesUpdated;
    private long tuplesHotUpdated;
    private long tuplesDeleted;
    private long seqScans;
    private long seqTuplesRead;
    private long indexScans;
    private long liveTuples;
    private long deadTuples;
    private long autovacuumCount;
    private long totalBytes;
    private long heapBytes;
    private long indexBytes;
    private long toastBytes;
    private long rowCount;
    private long avgRowBytes;

    /**
     * @return the activity counters accumulated between {@code before} and this
     *         snapshot, plus this snapshot's state (dead tuples, sizes, rows)
     */
    public Map<String, Object> diff(TableStats before) {
      TableStats base = before == null ? new TableStats() : before;
      Map<String, Object> ret = new LinkedHashMap<>();
      ret.put("tuplesInserted", tuplesInserted - base.tuplesInserted);
      ret.put("tuplesUpdated", tuplesUpdated - base.tuplesUpdated);
      ret.put("tuplesHotUpdated", tuplesHotUpdated - base.tuplesHotUpdated);
      ret.put("tuplesDeleted", tuplesDeleted - base.tuplesDeleted);
      ret.put("seqScans", seqScans - base.seqScans);
      ret.put("seqTuplesRead", seqTuplesRead - base.seqTuplesRead);
      ret.put("indexScans", indexScans - base.indexScans);
      ret.put("autovacuumRuns", autovacuumCount - base.autovacuumCount);
      ret.put("liveTuplesAfter", liveTuples);
      ret.put("deadTuplesAfter", deadTuples);
      ret.put("rowsAfter", rowCount);
      ret.put("avgRowBytesAfter", avgRowBytes);
      ret.put("totalBytesAfter", totalBytes);
      ret.put("heapBytesAfter", heapBytes);
      ret.put("toastBytesAfter", toastBytes);
      ret.put("indexBytesAfter", indexBytes);
      return ret;
    }
  }
}
