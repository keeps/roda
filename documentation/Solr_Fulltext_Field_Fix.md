# Solr `fulltext` field fix in the `File` collection (#3733 / #3734)

## What changed

The `fulltext` field in the `File` Solr collection was `stored=false`. Solr's
atomic/partial update (`{"set": ...}`) rebuilds a document from its stored
fields before resubmitting it, so a non-stored, non-docValues field is
silently dropped during that rebuild. This wiped `fulltext` (and the
`search` copyField terms derived from it) on any partial update to a file
document — e.g. an AIP move or permission propagation.

The fix makes `fulltext` stored (the default), so it survives partial
updates and is retrievable via `fl=fulltext`.

**This schema change does not apply automatically to an already-bootstrapped
Solr collection.** `SolrBootstrapUtils.bootstrapCollection` only logs a
warning when a field's attributes are out of date — it never issues a
replace-field call.

## Who must act

- **You use the full-text extraction feature:** a full reindex of the `File`
  collection is **mandatory** after applying the schema fix. Without it,
  previously indexed files keep an empty/stale `fulltext` field.
- **You don't use full-text extraction:** applying the schema fix is still
  recommended, but reindexing the `File` collection is **optional**.

### How to check if full-text extraction is enabled

Full-text extraction is provided by an optional plugin (`TikaFullTextPlugin`)
distributed separately from the open-source core, and it is opt-in per
ingest job (disabled by default).

If the plugin is not installed, or the option was never enabled on any
ingest job, the `fulltext` field will be empty for your files and the
reindex is optional.

You can also check directly whether the field has ever been populated:

- **Solr:** query the `File` collection with `fulltext:*` — any hits mean
  extraction has been used.
- **RODA UI:** go to *Search*, filter by files, open *Advanced search* and
  pick the **Full-text** field. Search for a word you know appears in a
  document you expect to have been text-extracted — if it's found via this
  field, extraction data exists in your repository.

## Steps

### 1. Fix the live schema

```bash
curl -X POST -H 'Content-type:application/json' \
  http://SOLR_HOST:8983/solr/File/schema \
  -d '{
    "replace-field": {
      "name": "fulltext",
      "type": "text",
      "multiValued": false,
      "stored": true
    }
  }'
```

### 2. Verify in Solr Admin UI

- Open `http://SOLR_HOST:8983/solr/#/File/schema` (or `#/~cloud` → File →
  Schema, depending on Solr version).
- Search for the `fulltext` field and confirm **Stored: true**.
- Optionally query `http://SOLR_HOST:8983/solr/File/select?q=*:*&fl=fulltext&rows=1`
  and confirm it returns content for a document known to have full text.

### 3. Reindex the `File` collection

Trigger a full reindex of files (RODA UI: *Administration → Actions log /
Index maintenance → Reindex files*), so already-indexed documents get
`fulltext` repopulated.

- **Mandatory** if full-text search is enabled in your instance.
- **Optional** otherwise.

> **Note:** reindexing the `File` collection can take a long time depending
> on the size of the repository. Also, since `fulltext` is now stored, the
> index will occupy more disk space than before — check available storage
> before starting this operation.   