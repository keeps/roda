# Ingest and job pipeline: bottleneck analysis

Scope: everything a SIP touches from job creation to cleanup — orchestration, ingest plugins and storage,
the RODA transaction layer, Solr indexing and the PostgreSQL job/report layer (including the uncommitted
switch to the normalized `db.jobs` schema). Branch `perf-optimize-jobs-reports`, working tree as of
2026-10-06.

Method: four independent code reviews (orchestrator, ingest/storage, indexing, database), the most
consequential claims re-checked by hand against the code, and the real-ingest benchmark
(`IngestDatabaseBenchmark`) as the only runtime evidence. Each finding is marked **verified** (re-checked
against the code) or **reported** (from a review, with file and line, not re-checked). Line numbers refer to
`roda-core/roda-core/src/main/java/org/roda/core/` unless a full path is given.

---

## 1. Summary

**The database is not where ingest time goes.** In the real-ingest benchmark (50 SIPs, 2 jobs, default
workflow), PostgreSQL executed 10,214 statements in about 285 ms, against 7.35 s of ingest wall time: roughly
4 %. The time goes into redundant indexing, per-write bookkeeping of the RODA transaction layer, repeated
storage I/O, and an orchestration model that leaves most workers idle for small and medium jobs. Several
paths also scale quadratically with job size, and nothing bounds the time a worker can spend waiting on Solr
or an external tool.

| # | Finding | Area | Severity | Grows with | Status |
|---|---|---|---|---|---|
| 1 | Every block re-reads all reports of the job in transactional mode | Transactions / DB | High | N² (job size) | **fixed** |
| 2 | AIPs and representations are deleted and fully re-indexed several times per ingest | Indexing | High | files × steps | verified |
| 3 | Solr retries with no deadline: ~10 min of sleeping per document, per worker | Indexing / robustness | High | documents during an outage | verified |
| 4 | Each model/storage write costs several separate DB transactions plus a lock round trip | Transactions | High | writes | reported |
| 5 | Replacing a report step deletes by `seq` alone, which no index leads with | DB (new layer) | High | total step rows × steps | **fixed** |
| 6 | Ingest parallelism capped at `ceil(N / block_size)`; steps serialized within a block | Orchestration | High | job size | reported |
| 7 | `aip.json` is read and rewritten on almost every write | Storage | High | files × steps + events | reported |
| 8 | SIP content is copied three times (unzip → staging → main) instead of moved | Storage | High | SIP bytes | reported |
| 9 | One transaction per block: a single failing SIP can roll back up to 99 good ones | Transactions | High (throughput) | block size | reported |
| 10 | Every progress message is a serialized read + DB write + Solr add on one actor | Orchestration | Medium | blocks × steps | reported |
| 11 | No batching anywhere in Solr writes; heavy `toSolrDocument` (JAXB context per call) | Indexing | Medium-high | documents | partly verified |
| 12 | External tools (Siegfried HTTP, ClamAV process) have no timeouts | Robustness | Medium | — (unbounded) | verified |
| 13 | `deleteByQuery` on the ingest hot path in SolrCloud | Indexing | High | AIPs, representations | reported |
| 14 | Transaction log `changeStatus` loads every operation of the transaction | DB | High | operations per transaction | verified (runtime effect unconfirmed) |

Correctness issues found on the way are listed in section 8.

---

## 2. What the benchmarks show, and what they cannot

`IngestDatabaseBenchmark` (real ingest through the orchestrator, default workflow, 50 copies of a 42 KB
E-ARK SIP, 2 concurrent jobs, block size 10), median of 3 runs:

| | Old code (`d28afff7e`) | Normalized layer + caches + direct counter updates |
|---|---|---|
| Ingest wall time | 7.05 s | 7.35 s |
| PostgreSQL statements / exec time | 6,221 / 217 ms | 10,214 / 285 ms |
| WAL written | 1.98 MB | 0.73 MB |
| `job_reports` rows rewritten | 800 | 200 |

Blind spots — the benchmark cannot see several of the findings below:

- **No RODA transactions.** Tests run in `NodeType.TEST`, where `RodaCoreFactory` does not start the
  transaction manager, so `PekkoWorkerActor` calls `plugin.execute` directly. Findings 1, 4, 9 and 14 never
  ran.
- **Tiny SIPs and tables.** One 820-byte file per SIP hides I/O costs (findings 7, 8) and table-size effects
  (finding 5: the step table never held more than a few hundred rows).
- **One machine, everything co-located.** Solr, PostgreSQL, ClamAV and Siegfried run in local containers;
  network latency, which multiplies every per-document round trip, is absent. One run also showed Solr
  stalling under memory pressure, which turned into a 30-minute hang (finding 3).

A benchmark with the transaction manager on, larger SIPs and many files per representation would be needed to
quantify most of the high-severity items.

---

## 3. Orchestration and job execution

### 3.1 Parallelism is capped by blocks, and steps run in sequence within a block — high, reported

- `resources/config/roda-core.properties:151-156`: `block_size = 100`, `nr_of_jobs_workers = 8`,
  `max_jobs_in_parallel = 8`. Blocks are cut by count in `plugins/orchestrate/PekkoEmbeddedPluginOrchestrator.java:232-250`.
- `plugins/base/ingest/v2/DefaultIngestPlugin.java:176-181` runs every step over the whole block before the
  next step starts; `IngestStepsUtils.recalculateAIPsList` (line 157) reloads every AIP after each step.
- A job of up to 100 SIPs runs on one worker while the other seven idle; one slow SIP holds up the other 99 at
  every step. Wall time ≈ `ceil(N / (block_size × workers))` × the serial time of one block.
- Fix: size ingest blocks per job (`ceil(N / workers)`, clamped); the per-plugin override
  `core.orchestrator.block_size.<FQCN>` exists but is static. Longer term, pipeline steps per SIP.

### 3.2 Progress updates are serialized read-modify-writes — medium, reported

- `PekkoJobStateInfoActor.java:198-205` → `JobsHelper.updateJobInformation` → `retrieveJob`,
  `updateJobStats` (one DB `UPDATE` since this branch) and a full Solr add of the job document
  (`DefaultModelService.java:2989-3001` → `SolrUtils.create2`).
- About `S + 6` messages per block for ingest (S ≈ 10–15 steps). One actor per job, one at a time; state
  changes and stop requests queue behind them, and any Solr slowness (finding 3) blocks the job's mailbox.
- The sender also reads the job only for `parallelism` and `priority`
  (`PekkoEmbeddedPluginOrchestrator.java:578-590`).
- Fix: keep counters in memory in the state actor and flush on a timer (latest value wins) and on final
  state; put priority/parallelism in the message; index the job document asynchronously.

### 3.3 Shared fixed thread pool, blocking lock waits — medium-high, reported

- All jobs' workers share `io-2-dispatcher` (`resources/config/orchestrator/application.conf`), whose pool
  stays at its core size (3 × cores, 8–64) because the queue is unbounded.
- Lock acquisition parks a pool thread in `Await.result` for up to `lock_request_timeout` (600 s)
  (`PekkoEmbeddedPluginOrchestrator.java:614-617`), so contention in one job starves others.
- Fix: size the dispatcher explicitly; make lock acquisition non-blocking or move it to a dedicated blocking
  dispatcher.

### 3.4 Blocks are pushed up front, round-robin — medium-high, reported

- `PekkoJobStateInfoActor.java:239-243` forwards every block immediately to a `RoundRobinPool`. Heavy blocks
  create stragglers while other workers idle; for "all objects" jobs every block sits in mailboxes at once
  (O(N) memory).
- Fix: pull-based dispatch (send the next block on `PluginExecuteIsDone`) with a bounded number in flight.

### 3.5 Lock manager scans everything on every message — medium, reported

- `PekkoJobsManager.java:156-160` runs `handleTick` after every message; it iterates all waiting requests
  (433-448) and all held locks (542-551), on a single pinned thread. Ingest takes one lock round trip per
  block, per step and per created AIP.
- Locks expire after 600 s without refresh (`LockInfo.releaseLockDueToExpire`), so a block longer than ten
  minutes can lose its locks while still working; expired waiting requests get no reply.
- Fix: sweep timeouts only on the scheduled tick, index waiters by object, heartbeat locks of running
  blocks.

### 3.6 Smaller items — reported

- `retrieveJob` called about `3S + 13` times per block (`PluginHelper.processObjects` 159/225/317 and
  others); with this branch each call is two primary-key reads plus a copy of the job's source-id list
  (`JobEntityMapper.java:229`), O(N) per call for LIST jobs.
- `processJobPluginInformation` re-aggregates one entry per block on every update (O(B²·S) per job; only
  significant with small blocks).
- `PluginHelper.java:358-368` re-parses the whole outcome→source JSON map for every report item
  (O(block²) per step).
- End-of-job work (flush of all reports, a classpath scan for notifications at `PluginHelper.java:1846`)
  runs on the state actor before the job releases its slot.
- `PekkoJobsManager.java:112-115` creates `max_jobs_in_parallel - 2` job actors but admits
  `max_jobs_in_parallel` jobs.

---

## 4. Ingest plugins, storage and the RODA transaction layer

What one data file of a new SIP costs on the default workflow (reported): written 3 times (unzip, staging,
commit copy), read in full by the transaction copies, ClamAV (`--stream`), fixity (MD5 + SHA-1 + SHA-256)
and Siegfried; `aip.json` rewritten at least 4 times; its Solr document rebuilt 4–5 times.

### 4.1 Report scan per block in transactional mode — high, verified — **fixed**

Fixed: `RODATransactionManagerUtils.getReportsForTransaction` now uses
`ModelService.listJobReportsByTransaction`, which reads only the transaction's reports
(`JobDatabaseService.findReportsByTransaction`), backed by an index on `(job_id, transaction_id)`
(created by `V6__normalize_jobs_and_job_reports.sql`). On a 20,000-report job with 12 steps each, the per-block
read went from 240,000 step rows (65 ms) to 1,200 (0.74 ms).


- `transaction/RODATransactionManagerUtils.java:44-58` (`getReportsForTransaction`) is called after every
  block (`RODATransactionManager.java:111`). It loads every report of the job through `listJobReports` and
  filters by transaction id in Java.
- O(blocks × reports) = O(N² / block_size) per job. Pre-existing (the old layer read every report row with
  its JSON steps); with the normalized layer each report also loads its step rows, so it is heavier.
- Fix: query by `(job_id, transaction_id)` with an index on `job_reports.transaction_id`, or have the
  transactional model service remember which reports it wrote.

### 4.2 Each write costs several DB transactions plus a lock round trip — high, reported

- `transaction/TransactionLogService.java:118/127` (model operations) and `158/173` (storage operations):
  each operation is an INSERT in its own transaction, then a `findById` + `save` in a second one to mark it
  done. `@ManyToOne` defaults to eager, so each `findById` joins `transaction_log`.
- `DefaultTransactionalStorageService.java:287-312` adds a "deleted path?" query per binary write;
  `TransactionalModelOperationRegistry.java:166-183` logs two model operations per file; every non-read
  operation acquires a lock through the single `JobsManager` actor.
- Estimate: about 13 DB round trips for one `createFile`.
- Fix: buffer operation logs and write them in batches, or write once with the final state; bulk
  `UPDATE` instead of load-and-save; lazy `@ManyToOne`; cache the transaction's deleted paths; lock each
  AIP once per transaction.

### 4.3 `aip.json` read-modify-write on almost every write — high, reported

- `model/DefaultModelService.java`: `createFile` → `changeRepresentationUpdateOn` (1289-1308);
  `createPreservationMetadata` (1984-1987); `updatePreservationMetadata` (2026-2028);
  `createOrUpdateOtherMetadata` (2284-2288); all end in `updateAIPMetadata` (340-352), a full JSON rewrite.
- Inside a transaction each rewrite adds existence checks and three log statements, and with
  `notify=true` an atomic Solr update of the same AIP document. All writes to one AIP serialize behind it.
- Fix: update `updatedOn`/`updatedBy` once per step or at the end of ingest; bulk APIs that write
  `aip.json` once.

### 4.4 Three copies instead of moves — high, reported

- `storage/fs/FSPathContentPayload.java:42` (unzip dir → staging) and commit
  `DefaultTransactionalStorageService.java:803-812` → `StorageServiceUtils.java:152` (staging → main) both
  use `Files.copy`; both sources are deleted afterwards.
- About 3× the SIP size in sequential I/O (4× with the submission copy).
- Fix: `Files.move` (atomic rename) when on the same filesystem, falling back to copy; unzip straight into
  staging.

### 4.5 One transaction per block — high for throughput, reported

- `RODATransactionManager.java:109-117` rolls back the whole block if any report is a FAILURE whose plugin is
  not listed in `parameter.skip_rollback_on_validation_failure` (`RODATransactionManagerUtils.java:125-127`);
  nothing in the repository sets that parameter.
- One invalid or infected SIP can discard the staged work of up to 99 others. Worth confirming with a test
  (one bad SIP in a block of good ones).
- Fix: one transaction per SIP inside ingest, or set the skip list for ingest plugins.

### 4.6 Other storage findings — reported

- **O(N×M) listing merge**: `storage/StorageServiceUtils.java:281-283` scans all seen paths with
  `toString()` for every resource; every listing also queries the transaction's DELETE operations
  (`DefaultTransactionalStorageService.java:148,239`). Fix: a `HashSet` of normalized paths.
- **Copy-on-write of whole files and AIPs for updates**: `updateBinaryContent` (390-398) copies the old
  file to staging before overwriting it; `getDirectAccess` (545-551) copies the whole AIP into staging when
  anything under it changed (Antivirus, Siegfried). Affects UPDATE SIPs.
- **Fixity recomputed for every file** (`PremisSkeletonPluginUtils.java:107-114`), even when the METS
  declares checksums; plus a per-file `notifyFileCreated` that the representation reindex overwrites.
- **Siegfried post-processing per file**: two PREMIS parses, three writes, two `aip.json` rewrites and one
  atomic update of the AIP document (`SiegfriedPluginUtils.java:243-273`); the whole HTTP response is
  buffered as a string.
- **AIP retrieved twice per step**, and agent existence checked on storage for every event
  (`PluginHelper.createPluginEvent` 1205).
- **AutoAccept** sends one atomic update per file and per event (`IndexModelObserver.java:481-585`).

---

## 5. Solr indexing

### 5.1 Delete-then-recreate updates, repeated several times per ingest — high, verified

- `index/IndexModelObserver.java:416-420`: `aipUpdated` = `aipDeleted` + `aipCreated`;
  `representationUpdated` (983-986) and `fileUpdated` (1036-1041) do the same. `aipCreated` builds the AIP
  document twice (line 130, and again through `indexRetentionPeriod` at 338/346).
- Triggers during one ingest: SIP→AIP (`EARKSIP2ToAIPPluginUtils.java:99-101`, the new AIP goes through
  `updateAIP`), PremisSkeleton (per-file `notifyFileCreated`, then `notifyRepresentationUpdated`),
  Siegfried (`notifyRepresentationUpdated` again), disposal rules (`updateAIP`).
- Each file document is sent in full at least 4 times plus one atomic update; each rebuild re-reads and
  re-parses PREMIS and technical metadata from storage.
- Fix: rely on overwrite by unique key instead of deleting first; drop the per-file notify before a
  representation reindex; build the AIP document once; ideally index once at the end of each step or at
  transaction commit.

### 5.2 Retries with no deadline — high, verified

- `index/utils/RetryPolicyBuilder.java:41-53`: backoff from 1 s doubling up to 180 s, 10 retries — about
  615 s of sleeping per operation in production, applied to every single add, update, delete and commit.
- The loops over representations, files and events (`IndexModelObserver.java:202-212, 258-282`) keep going
  after a failure, so an AIP with 100 files can hold a worker for hours during a Solr outage; progress
  updates go through the same path on the job's actor.
- Observed in the benchmark: Solr stopped answering, and both ingest jobs sat in STARTED until the 30-minute
  job timeout.
- `IndexResultIterator` (35-36, 81-104) separately retries a page 100 times with 10 s sleeps, including
  `RequestNotValidException`, which can never succeed.
- Fix: a shared circuit breaker plus an overall deadline; a short client request timeout; stop an object's
  loop on the first `SolrRetryException`; do not retry non-transient errors.

### 5.3 `deleteByQuery` on the hot path — high, reported

- `aipDeleted` (866-869) issues 3 and `representationDeleted` (996-999) 2 delete-by-query calls; in
  SolrCloud these are broadcast to all shards and serialize against concurrent updates. Ingest triggers them
  for an AIP that has nothing indexed yet, then twice more per representation.
- Fix: skip for new AIPs; otherwise delete by ID lists or compute what disappeared.

### 5.4 No batching, and expensive document building — medium-high, partly verified

- Every write is a single-document HTTP request (`SolrUtils.create2` 1494-1518, `SolrUtils.update`
  1551-1564).
- **Verified:** `common/PremisV3Utils.java` builds a new `JAXBContext` on every parse (lines 747, 759, 1232,
  1369, 1399, 1429, 1459) — once per file document and per event document. JAXB contexts are expensive and
  thread-safe; they should be static.
- Reported: `XMLInputFactory`/`XMLReader` created per document (`SolrUtils.java:863, 909`;
  `RodaUtils.java:214`); compiled XSLTs expire from the cache one minute after being written
  (`RodaUtils.java:78-79`); a configuration stream opened per technical-metadata type is never closed
  (`SolrUtils.java:897`).
- Fix: send documents in lists per representation; static JAXB contexts and factories; size-based XSLT
  cache.

### 5.5 Other indexing findings

- **Soft commit every 2 s, no cache warming** — verified for the soft commit
  (`resources/config/index/common/conf/solrconfig.xml:317-319`); reported: all caches use
  `autowarmCount="0"`. During ingest each collection opens a new cold searcher every 2 s.
- **Explicit commits in the ingest path** — reported: Siegfried risk incidences commit with
  `waitSearcher=true` per incidence (`SiegfriedPluginUtils.java:290, 317`), plus `index.commitAIPs()` on 5
  collections in `DefaultIngestPlugin.afterAllExecute`, which runs per block.
- **Job report saves do two full-document realtime gets** for labels (`JobReportCollection.java:174-180` →
  `SolrUtils.getObjectLabel` 1703-1706), with no field list.
- **N+1 storage reads in the observer**: `model.retrieveAIP` per event (`IndexModelObserver.java:222-238`)
  and ancestor walks per file notification (`SolrUtils.getAncestors` 1791-1809).
- **Index reads return every stored field** in `JobsHelper.getObjectsFromIndex` (550-557), `getFiles`,
  `getRepresentations`.

---

## 6. PostgreSQL

### 6.1 New job/report layer (this branch)

Fixed: step writes (delete by primary key, steps compared through a narrow projection, job check inside
the save), flush page by page, cleanup in bounded transactions without `RETURNING`, report caches per
job and capped, running jobs read without N+1, report listings streamed page by page, and flushed jobs
listed only from storage. Replacing a step on a table of 2.4M step rows went from 83 ms (sequential
scan) to 0.03 ms. Still open: JDBC batching and the second transaction for plugin registration.


- **Step replace deletes by `seq` alone — high, verified.** `JobReportStep`'s id is `seq`
  (`roda-common/.../v2/db/jobs/JobReportStep.java:44-47`), so `em.remove` issues
  `DELETE FROM job_report_steps WHERE seq = ?`; V6's only index is the primary key `(report_pk, seq)`, so
  each delete scans the whole table of all running jobs' steps. Happens once per plugin step per object
  (`updatePartialJobReport(..., true)`). Fix: delete with
  `WHERE report_pk = :pk AND seq >= :firstSeq` in one statement.
- **Each step update reads the steps twice and uses three transactions — medium.** `retrieveJobReport`
  reads them to rebuild the report, `jobExists` is a separate transaction, and `saveReport` reads them again
  to diff (`JobDatabaseService` `findReport`, `saveReport`, `syncSteps`). Fix: `appendStep` /
  `replaceLastStep` operations that write without reading, and drop the separate `jobExists`.
- **Flush loads the whole job into memory — medium.** `flushJobToStorage` → `findReports` loads all
  reports, steps and original ids of the job at once, and fills the original-id cache just before the rows
  are deleted. Fix: keyset pagination by `pk`, no cache fills in bulk reads.
- **Cleanup batches by job count, not row count — medium.** One large job becomes one multi-million-row
  transaction; `RETURNING pk, id` materializes every report id only to clear caches. Fix: chunked deletes
  per job; caches keyed by job so they can be dropped without `RETURNING`.
- **In-memory caches have no bound — medium.** `reportPks`, `reportOriginalIds`, `jobChildren` (the whole
  LIST of each running job) grow per report until cleanup; `findAllReports` fills them for every report.
  Fix: bounded caches (e.g. Caffeine), keyed per job; no caching on bulk paths.
- **`findAllJobs` is N+1 and `findAllReports` loads whole tables — medium.** Used by `list`/`listLite`
  (`DefaultModelService.java:4200-4283`), e.g. for reindexing. Flushed-but-not-cleaned jobs are returned twice
  (from the database and from storage).
- **No JDBC batching — low/medium.** No `hibernate.jdbc.batch_size`/`order_inserts`, and IDENTITY ids
  disable insert batching anyway.
- **Plugin registration uses a second transaction while the first holds its connection — low.** Cold caches
  at job start can make each worker hold two pool connections.

### 6.2 Transaction log and other tables

- **`changeStatus` loads every operation of the transaction — high, verified (runtime effect unconfirmed).**
  `repository/transaction/TransactionLogRepository.java:29` overrides `findById` with an entity graph over two
  `List` collections; `TransactionLogService.changeStatus` (91-97) uses it twice per commit and twice per
  rollback. That either multiplies rows (storage ops × model ops) or fails as multiple bag fetch; SQL
  logging would settle which. Fix: a bulk `UPDATE` of status; remove the override.
- **Transaction cleanup deletes row by row with no limit, and never purges rolled-back transactions —
  medium, reported** (`TransactionLogService.java:99-105`, `TransactionCleanupTask.java:97-116`; no index on
  `transaction_log.status`).
- **Disposal confirmation tables lack indexes on `job_id` and their FK columns — medium, reported** (V2
  migration), and `findByJobId` fetches two collections at once.
- **Open-in-view is on by default — medium, needs confirming.** `spring.jpa.open-in-view` is not set, so web
  requests that touch the job tables may hold a pool connection until they finish.
- **Prefix `LIKE` on storage paths** (`TransactionalStoragePathRepository.findModificationsUnderStoragePath`)
  cannot use the btree under a non-C collation; it returns full rows where an `EXISTS` would do.

---

## 7. Robustness: unbounded waits

These are not throughput costs in normal operation, but they turn a slow dependency into a stuck pipeline:

- Solr retries: about 10 minutes of sleeping per document (5.2).
- `roda-common/roda-common-utils/.../util/HTTPUtility.java:38, 53`: `openConnection()` with no connect or read
  timeout (Siegfried server) — verified.
- `roda-common/roda-common-utils/.../util/CommandUtility.java:92`: `process.waitFor()` with no timeout
  (ClamAV) — verified.
- Lock waits of up to 600 s on shared pool threads (3.3), and locks that expire under long blocks (3.5).

A stalled dependency holds the worker, its AIP locks and its open RODA transaction until something gives
way.

---

## 8. Correctness issues found on the way

- **Ancestors of moved AIPs' descendants are never updated — verified.**
  `IndexModelObserver.java:811` passes `aip.getId()` where it should pass `item.getId()`: the moved AIP's
  own ancestors are overwritten once per descendant, and descendants keep stale ancestors.
- **Reports written in the flush window are lost** (this branch and before): a report saved after
  `flushJobToStorage` read the reports, but before cleanup, still passes `jobExists`, is written only to the
  database, and is deleted by the cleanup task.
- **`childrenFingerprint` is a 32-bit `Objects.hash`** (`JobEntityMapper.java:130-139`): a collision would
  silently skip a needed child-row sync. Low probability; a stronger hash or an explicit dirty flag removes
  it.
- **Shared mutable state across actor threads**: `runningJobs`, `stoppingJobs`, `inErrorJobs`
  (`PekkoEmbeddedPluginOrchestrator.java:100-115`) are plain collections; `JobInfoUpdated` carries a live
  `JobPluginInfo` the worker keeps mutating.
- **Typing**: disposal repositories declare `JpaRepository<…, String>` for `Long` ids.

---

## 9. Suggested order of work

Quick and contained (each measurable with the existing benchmarks, the first two only once the benchmark
runs with transactions on):

1. Delete report steps by `(report_pk, seq)` (6.1) — this branch's own regression risk.
2. Query transaction reports by `(job_id, transaction_id)` with an index (4.1) — removes an N² path.
3. Bulk `UPDATE` for transaction status and operation state; drop the `findById` entity-graph override
   (6.2, 4.2).
4. Static JAXB contexts and XML factories; size-based XSLT cache (5.4).
5. Timeouts on Siegfried/ClamAV calls and a deadline/circuit breaker on Solr retries (section 7).
6. Fix `IndexModelObserver.java:811` (section 8).

Medium effort, large expected gain:

7. Stop the delete-and-reindex pattern: overwrite by key, no per-file notify before a representation reindex,
   one AIP document build (5.1, 5.3).
8. Batch Solr writes per representation (5.4) and remove explicit commits from the ingest path (5.5).
9. Throttle job progress persistence and indexing (3.2).
10. Adaptive ingest block size (3.1); one transaction per SIP inside ingest (4.5).

Structural:

11. Write `aip.json` once per step (4.3), move instead of copy (4.4), reuse METS checksums (4.6).
12. Pull-based block dispatch, non-blocking locks, explicit dispatcher sizing (3.3–3.5).
13. Index at transaction commit, de-duplicated (5.1).

Before tackling 2–5 and 7–13, extend `IngestDatabaseBenchmark` with the transaction manager on, larger SIPs
and many files per representation, and record Solr request counts alongside the database counters; the
current benchmark cannot see most of these costs.
