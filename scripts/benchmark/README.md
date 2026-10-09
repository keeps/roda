# Job / job report database benchmarks

Two benchmarks in `roda-core/roda-core-tests` (package `org.roda.core.benchmark`) measure how the job
tables (`jobs`, `job_reports`, `job_report_steps`, ...) are used. Use them to compare database behaviour
before and after a code change:

- **`JobDatabaseBenchmark`** (TestNG group `benchmark`) drives the `ModelService` job/report calls
  directly with a synthetic, ingest-like workload. It is fast and isolates the database layer.
- **`IngestDatabaseBenchmark`** (TestNG group `benchmark-ingest`) runs real SIP ingests through the plugin
  orchestrator, so reports are written exactly as in production: each ingest step appends its report item
  as RUNNING and then replaces it with its outcome (`PluginHelper.updatePartialJobReport`).

Both run against the Testcontainers infrastructure started by the test suite (PostgreSQL with
`pg_stat_statements` preloaded, plus Solr, ClamAV and Siegfried for the ingest benchmark), so they only need
a reachable Docker daemon. They run RODA in a temporary home and refuse to start otherwise, so they never
write into a real installation such as `~/.roda`. Their groups are not part of the regular (`all`/`travis`)
test runs.

## Running

```bash
# once, and after changing roda-common / roda-core
mvn install -Pcore -DskipTests

# baseline (e.g. on master, or before your change); repeat 2-3 times to gauge noise
mvn -pl roda-core/roda-core-tests test -Dtestng.groups=benchmark -Dbenchmark.label=baseline

# candidate (with your change; re-run the install above first)
mvn -pl roda-core/roda-core-tests test -Dtestng.groups=benchmark -Dbenchmark.label=candidate

# compare (medians when several files are given per side)
scripts/benchmark/compare_job_db_benchmark.py \
  -b roda-core/roda-core-tests/target/benchmark/job-db-benchmark-baseline-*.json \
  -c roda-core/roda-core-tests/target/benchmark/job-db-benchmark-candidate-*.json
```

Add `--markdown` to get tables ready to paste in a PR.

### Real ingests

```bash
mvn -pl roda-core/roda-core-tests test -Dtestng.groups=benchmark-ingest -Dbenchmark.label=ingest-baseline
```

Copies one E-ARK SIP from the test corpora `sips` times (as transferred resources) and ingests them in
`jobs` concurrent ingest jobs, then runs the flush cleanup once (phases `ingest` and `cleanup`). Wall times
include everything an ingest does (storage, index, ClamAV, Siegfried), so the database counters are what
isolate the database layer. Each result records the jobs' final state and counters, to check that compared
runs ingested the same way.

Parameters (`-Dbenchmark.<name>`): `sips` (50), `jobs` (2), `ingestPlugin` (`configurable`, the default
workflow; `minimal` skips virus check, format identification and disposal rules), `virusCheck` (true),
`formatIdentification` (true), `blockSize` (10, SIPs per orchestrator worker block), `sipFile`
(`e-ark-sip-2.1.0-with-custom-representation-type.zip`), `warmupSips` (2), `jobTimeoutSeconds` (1800), plus
`label` (`ingest-<git branch>`), `outputDir`, `topStatements` and `statsFlushWaitMillis`.

To benchmark code from another commit without touching your working tree, check it out in a git worktree,
copy in `org/roda/core/benchmark/*`, `TestContainersManager.java` and the test `application.properties`,
and run it as a reactor build there (no `install`), writing next to your other results:

```bash
git worktree add --detach /tmp/roda-baseline <commit>
# copy the files above into /tmp/roda-baseline, then:
(cd /tmp/roda-baseline && mvn -pl roda-core/roda-core-tests -am test -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtestng.groups=benchmark-ingest -Dbenchmark.label=ingest-baseline \
  -Dbenchmark.outputDir=$PWD/roda-core/roda-core-tests/target/benchmark)
```

The tables measured are those that exist in the schema being benchmarked, so older schemas work too.

### Dashboard

```bash
scripts/benchmark/serve_job_db_benchmark.py --open   # http://127.0.0.1:8765/
```

A local page, with no dependencies, for picking baseline and candidate runs (grouped by label, medians
across runs). It shows headline numbers, per-phase bars, the % change of every metric, latency per
operation, the full metric table, and the top SQL statements. It re-reads the results directory on
"Reload runs". Use `--dir` for another results directory and `--port` to change the port.

Results go to `target/benchmark`, which `mvn clean` deletes. Pass `-Dbenchmark.outputDir=...` to keep them
elsewhere.

## Synthetic workload (`JobDatabaseBenchmark`)

Phases (each one measured separately):

| Phase | What it does |
|---|---|
| `create-jobs` | `createJob` for every job (state STARTED) |
| `ingest` | per SIP (all jobs' SIPs interleaved, `threads` workers): create the report, then per step `retrieveJobReport` → `addReport` → `createOrUpdateJobReport`; at `idChangeStep` the outcome id changes (AIP created), replacing the report row; every `jobUpdateEvery` report updates the job is retrieved and saved |
| `read` | `listJobReports`, `retrieveJobReport` and `retrieveJob`, as the UI does on a running job |
| `finish` | job → COMPLETED (flushes job + reports to storage, marks the row as flushed) |
| `cleanup` | one `JobFlushCleanupTask` run (removes flushed jobs and reports) |

Parameters (`-Dbenchmark.<name>`): `jobs` (10), `sipsPerJob` (100), `steps` (12), `threads` (8),
`idChangeStep` (3), `jobUpdateEvery` (10), `detailsBytes` (256, plugin details per step), `readsPerJob` (50),
`warmupJobs` (2), `warmupSipsPerJob` (20), `topStatements` (20), `statsFlushWaitMillis` (1500), `seed` (42),
`label` (git branch), `outputDir` (`target/benchmark`).

## What is measured (per phase)

- **Latency** per operation (count, mean, p50, p95, p99, max), and wall time / throughput.
- **Hibernate**: prepared statements, transactions, entity loads/inserts/updates/deletes.
- **PostgreSQL**: tuples inserted/updated/HOT-updated/deleted, seq/index scans, dead tuples, heap/TOAST/index
  size, row count and average row size, WAL bytes, and `pg_stat_statements` totals plus the top statements
  (calls, time, rows, buffers, WAL).

Statement counts, tuples written, WAL and table sizes are nearly deterministic for a given workload, so they are the
most reliable way to tell whether a change helped. Latencies vary from run to run, so compare medians of
several runs.
