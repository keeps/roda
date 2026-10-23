# Solr shards per collection

## What changed

RODA used to create every Solr collection with the same number of shards,
taken from the `SOLR_NUM_SHARDS` environment variable. With the reference
setting of 4 shards, the 20 collections made 80 cores on a single node, even
though most collections hold a few hundred documents and only a handful grow
with the repository.

The number of shards can now be set per group of collections, and per
collection, in `roda-core.properties`:

| Property | Meaning |
|----------|---------|
| `core.solr.collections.large` | Collections that grow with the repository (repeat the key for each) |
| `core.solr.collections.small` | Collections that stay small (repeat the key for each) |
| `core.solr.shards.large` | Number of shards of the large collections |
| `core.solr.shards.small` | Number of shards of the small collections |
| `core.solr.shards.collection.<Name>` | Number of shards of one collection, e.g. `core.solr.shards.collection.File` |

A collection takes its own value, else that of the group it is listed in,
else `SOLR_NUM_SHARDS`, else 1.

By default the large collections are `AIP`, `Representation`, `File`,
`PreservationEvent`, `ActionLog` and `JobReport`; all the others are small.
The shard counts are not set by default, so **an installation that only sets
`SOLR_NUM_SHARDS` keeps its current behaviour**.

Like any RODA property, each one can also be set as an environment variable
(or Java system property), e.g. in a Docker Compose file:

```yaml
environment:
  - RODA_CORE_SOLR_SHARDS_LARGE=4
  - RODA_CORE_SOLR_SHARDS_SMALL=1
  - RODA_CORE_SOLR_SHARDS_COLLECTION_FILE=8
  # lists are comma-separated when set this way
  - RODA_CORE_SOLR_COLLECTIONS_LARGE=AIP,Representation,File,PreservationEvent,ActionLog,JobReport
```

The reference deployments (`deploys/standalone`) now set 4 shards for the
large collections and 1 for the small ones, i.e. 38 cores instead of 80.

### Audit log grouping

The audit log list groups the entries of each request. Solr only groups
documents within a shard, so with more than one shard in the `ActionLog`
collection a request could show up once per shard. New audit log entries now
get an id prefixed by the id of their request (`<request>!<entry>`), which
makes Solr keep every entry of a request on the same shard. This needs no
schema change nor reindex.

## Who must act

**The number of shards is only applied when a collection is created.** RODA
creates missing collections at startup and never changes the shards of an
existing one.

- **New installations:** nothing to do besides setting the properties (or
  environment variables) before the first startup.
- **Existing installations:** nothing changes unless you recreate
  collections. This is only worthwhile where the number of shards matters,
  e.g. many small collections with several shards on a single node, or a
  large collection that needs more shards. Recreating a collection means
  reindexing it, which may take long for the large ones.
- **Audit log:** entries written before this change keep their id and may
  still show up more than once per request in the audit log list. Entries
  written afterwards are grouped correctly.

You can check the current number of shards of each collection in the Solr
Admin UI (*Cloud → Graph*) or with:

```bash
curl 'http://SOLR_HOST:8983/solr/admin/collections?action=CLUSTERSTATUS'
```

## Steps to recreate a collection

### 1. Set the number of shards

Set the properties (or environment variables) described above.

### 2. Stop RODA and delete the collection

```bash
curl 'http://SOLR_HOST:8983/solr/admin/collections?action=DELETE&name=COLLECTION'
```

### 3. Start RODA

RODA creates the missing collection with the configured number of shards;
the log shows `Creating SOLR collection COLLECTION with N shards and M
replicas`.

### 4. Reindex the collection

Run the matching reindex action (RODA UI: *Administration → Actions*), e.g.
*Rebuild AIP index* for `AIP`, `Representation` and `File`, or *Rebuild all
indexes* when several collections were recreated.
