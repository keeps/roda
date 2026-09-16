# Migration Guide for PostgreSQL database

> [!IMPORTANT]
> **Applies to** any PostgreSQL-backed RODA installation being upgraded to 6.4.0 from any 6.x version up to 6.3.1.

## Why this migration is needed

Up to 6.3.1 the Flyway migration scripts under `db/migration` named the `public` schema explicitly in every statement. In 6.4.0 that is gone: the `public.` prefix, the `SELECT pg_catalog.set_config('search_path', '', false)` statement and the `ALTER TABLE ... OWNER TO admin` statements were all removed, and the schema is taken from the database connection instead. However two consequences follow, and together they are the reason for this guide.

**The schema must now be stated on the connection.** Nothing in the scripts says where to write any more. If the JDBC URL does not name a schema, PostgreSQL falls back to its own `search_path`, and the migrations are applied wherever that happens to point — not necessarily where your data lives.

**The flyway scripts changed content, so their checksums changed.** Flyway records a checksum for every migration it applies and compares it against the script it finds on the next start. Where they differ it refuses to run, and the application does not start until the recorded checksums are realigned.

Doing this properly is what keeps the database and the application in step. Skipping it leads to one of two outcomes: the application refuses to start, which is **noisy and safe**; or migrations are applied to a schema that is not yours, which is **silent and not safe** — the database ends up split across two schemas, the application fails on a later and less obvious error, and every subsequent upgrade fails with it.

> [!NOTE]
> No step in this guide deletes data. Step 3 relocates tables within the same database, step 4 writes only to Flyway's own history table, and a migration that fails is rolled back in full by PostgreSQL.

## Which installations are affected

| Your installation | Error that could appear |
| --- | --- |
| On `public` — the default — and running normally | Nothing; 6.3.x starts and works |
| On a dedicated schema, database user **without** rights on `public` | 6.3.x refuses to start: `permission denied for schema public`, SQL state 42501 |
| On a dedicated schema, database user **with** rights on `public` | On 6.3.0 the migrations report success and startup fails on `Schema validation: missing table`; on 6.3.1 it fails earlier, on `relation "public.jobs" does not exist` |
| Never started a 6.3.x release, so the database has no Flyway history at all | Nothing; Flyway was not part of 6.2.x |

> [!TIP]
> **Step 1 - Triage** confirms which of these you are, from the database itself rather than from the version you believe you are on. That distinction matters: Flyway creates its history table on the first start of a 6.3.x release, successful or not. An installation that came from 6.2.x but has already tried 6.3.x once — even if it failed and rolled back — is no longer the last row of this table. **Only the database can tell you which row you are.**
>
> Follow the steps in order; the triage tells you which of the conditional ones apply to you.

## Reading this guide — `public` is the default

RODA's default schema is `public`, and that is where the Flyway history table and all RODA data belong unless your deployment was deliberately set up otherwise. **If you never configured a schema, yours is `public`.**

The guide writes `<schema>` wherever the value depends on the deployment. Read it as `public` throughout unless step 1 tells you otherwise. For that common case there is nothing to substitute and nothing to decide: 6.4.0 ships with `currentSchema=public` already set, so step 2 is a confirmation rather than a change, and step 3 never applies.

Only installations deliberately deployed into a dedicated schema read `<schema>` as anything else, and step 1 identifies them unambiguously.

---

## 0. Before you start

Take a database backup. Steps 3 and 4 write to the database.

Stop the RODA application. Leave it stopped until step 5.

---

## 1. Triage

**Everything else depends on this step.** Run both queries as a user that can read the whole database (the database owner), so that objects outside your RODA schema are visible.

Answer from what the queries return, not from the version you think you are upgrading from. The two can disagree: a single start of any 6.3.x release is enough to create the history table, whether or not the application then came up.

**1a. Where are the tables?**

```sql
SELECT schemaname, tablename
FROM pg_tables
WHERE tablename IN ('jobs', 'flyway_schema_history',
                    'disposal_confirmations', 'disposal_confirmation_aip_entry',
                    'aip_entry_disposal_hold_ids', 'aip_entry_disposal_hold_trans_ids')
ORDER BY schemaname, tablename;
```

The schema holding `jobs` is your RODA schema. It is referred to below as `<schema>` — for most installations `public`, for installations deployed into a dedicated schema its name.

**1b. What does the Flyway history say?**

> [!IMPORTANT]
> Skip this query only if `flyway_schema_history` did not appear in **1a**. If it did appear, run it — no matter which version you started from.

```sql
SELECT installed_rank, version, description, type, checksum, success
FROM <schema>.flyway_schema_history
ORDER BY installed_rank;
```

> [!CAUTION]
> Read the rows in order and stop at the first one that matches. **The first row takes precedence**.

| What you observed in step 1 | Step 2 | Step 3 (move tables) | Step 4 (repair) |
| --- | --- | --- | --- |
| Any `disposal_*` or `aip_entry_disposal_*` table sits in a schema other than `<schema>` | **yes** | **yes** | **yes** |
| History holds one or more rows of type `SQL` | **yes** | no | **yes** |
| History holds only the `BASELINE` row, `checksum` empty | **yes** | no | no |
| `flyway_schema_history` does not exist | **yes** | no | no |

---

## 2. Set the schema on the datasource URL

**If your RODA is on `public`** — the common case — 6.4.0 already ships this value and there is nothing to change. Confirm it and move on:

```text
jdbc:postgresql://<host>:5432/roda_core_db?currentSchema=public
```

**If step 1 placed your tables in a dedicated schema**, set that name instead, and use the same value everywhere this guide writes `<schema>`:

```text
jdbc:postgresql://<host>:5432/roda_core_db?currentSchema=<schema>
```

Either way the value must name the schema that already holds your tables. Do not point it at a different one: nothing is moved, and Flyway would apply every migration into the new schema.

The release ships **`currentSchema=public` as its default**. Container deployments override the datasource URL through the `SPRING_DATASOURCE_URL` environment variable, and environment variables take precedence over the `application.properties` packaged inside the JAR — so the value must be set there, and editing the properties file has no effect. No image needs to be rebuilt.

Set this in your deployment configuration now. **You will confirm the value that actually reached the application in step 6**, once it is running.

---

## 3. Move tables left in another schema — conditional

> [!NOTE]
> Applies **only if any `disposal_*` or `aip_entry_disposal_*` table sits in a schema other than `<schema>`**.

This happens when RODA was deployed into a dedicated schema while connecting as a user privileged enough to write to `public`: the `V2` script, which named `public` explicitly, succeeded into the wrong schema and was recorded as successful.

Symptoms, if you want to confirm before acting: on 6.3.0 the log reads `Successfully applied 1 migration to schema "<schema>"` and the application still fails, on `Schema validation: missing table [aip_entry_disposal_hold_ids]`; on 6.3.1 it fails earlier, with `relation "public.jobs" does not exist` while applying `V3`.

Move each affected table. Indexes, constraints and identity sequences move with the table.

```sql
ALTER TABLE public.disposal_confirmations              SET SCHEMA <schema>;
ALTER TABLE public.disposal_confirmation_aip_entry     SET SCHEMA <schema>;
ALTER TABLE public.aip_entry_disposal_hold_ids         SET SCHEMA <schema>;
ALTER TABLE public.aip_entry_disposal_hold_trans_ids   SET SCHEMA <schema>;
```

Run only the statements for tables step 1a actually reported in the wrong schema. Re-run query 1a afterwards: every table listed must now be in `<schema>`.

Steps 3 and 4 touch different things — this one moves tables, step 4 writes only to the history table — so their order between themselves does not matter. Both must be done before starting the application: on startup Hibernate validates every table it expects against the single schema the connection resolves, and any table left behind fails that check.

---

## 4. Realign the Flyway history — conditional

> [!NOTE]
> Applies **only if flyway history holds one or more rows of type `SQL`.**

The migration scripts changed content, so their checksums changed. Flyway compares the checksum of each applied migration against the script it now finds, and refuses to start when they differ. `repair` command rewrites the stored checksums to match. Every row of type `SQL` is realigned; how many there are depends on your history. `repair` writes only to `flyway_schema_history` — it does not re-run any migration and **does not touch your data**.

Extract the migration scripts from the release JAR. Use the JAR, not a source checkout: checksums are computed over file bytes, and even line endings differ.

```bash
mkdir -p /tmp/roda-migration
unzip -j roda-wui-<version>.jar 'BOOT-INF/classes/db/migration/*.sql' -d /tmp/roda-migration
```

Keep the password out of your shell history:

```bash
read -s FLYWAY_PASSWORD && export FLYWAY_PASSWORD
```

Run the repair with the Flyway image pinned to the version the application embeds:

```bash
docker run --rm --network <network> \
  -v /tmp/roda-migration:/flyway/sql:ro \
  -e FLYWAY_PASSWORD \
  flyway/flyway:12.4.0 \
  -url="jdbc:postgresql://<host>:5432/roda_core_db?currentSchema=<schema>" \
  -user=<user> \
  -locations=filesystem:/flyway/sql \
  repair
```

`<network>` lets the Flyway container reach the database. Under Docker Compose it is usually `<project>_default` — list them with `docker network ls`. For an external database, omit `--network` and use the real host.

The output names each realigned migration and ends with a success line:

```text
Repair of failed migration in Schema History table "<schema>"."flyway_schema_history" not necessary. No failed migration detected.
Repairing Schema History table for version 2 (Description: add disposal confirmations aux, Type: SQL, Checksum: 1378294059)  ...
Successfully repaired schema history table "<schema>"."flyway_schema_history"
```

The first line is informational: it reports that no *failed* migration was found, which is the normal case — PostgreSQL rolls a failed migration back in full and leaves no row behind.

---

## 5. Start the application

Deploy 6.4.0 and start it.

---

## 6. Verify

**1. On the running container**, confirm the value that actually reached the application:

```bash
docker compose exec roda env | grep SPRING_DATASOURCE_URL
```

It must name the schema you identified in step 1.

**2. In the startup log**, Flyway names the schema it is working on. Which lines you see depends on whether any migration was still pending.

If migrations were pending, it applies them:

```text
Current version of schema "<schema>": 2
Migrating schema "<schema>" to version "3 - add job flushed at"
Successfully applied N migrations to schema "<schema>", now at version v4
```

If your installation was already at version 4 and only needed step 4, there is nothing left to apply and Flyway says so:

```text
Current version of schema "<schema>": 4
Schema "<schema>" is up to date. No migration necessary.
```

Both outputs are correct. What matters is that the schema named in them is yours, and that no line reports a failure.

**3. In the database**, the history must end at version 4 with every row successful:

```sql
SELECT installed_rank, version, description, type, checksum, success
FROM <schema>.flyway_schema_history
ORDER BY installed_rank;
```

Every row of type `SQL` carries a checksum and `success` true. The `BASELINE` row, if present, has no checksum — that is expected. Re-run query 1a. Every table must be in `<schema>`.

---

## 7. Troubleshoot

If it still fails, check the following cases:

* **`permission denied for schema public` (SQL state 42501)** — the datasource URL did not reach the application, so the old default is still in effect, or the value names the wrong schema. Read it off the running container as shown in step 6.

* **`Validate failed: Migrations have failed validation`** — step 4 was skipped or ran against a different schema than the one in step 2, or the scripts came from a source checkout instead of the release JAR. Flyway lists one block per mismatched migration, each naming the checksum applied to the database, the one resolved locally, and `run repair to update the schema history`. Nothing was written: Flyway validates before it migrates.

  ```text
  Migration checksum mismatch for migration version 2
  -> Applied to database : 2125797564
  -> Resolved locally    : 1378294059
  Either revert the changes to the migration, or run repair to update the schema history.
  ```

* **`Schema validation: missing table [<name>]`** — the migrations applied to a different schema than the one holding your data, or tables are still split across schemas. Re-run query 1a and see step 3.

* **`relation "public.jobs" does not exist`** — a pre-6.4.0 script is still being applied. Confirm the deployed version is 6.4.0. An installation that reached this error also has tables outside its schema: re-run query 1a and see step 3.

A failed startup does not mean nothing changed: the migration that failed is rolled back, but earlier ones stay applied. Re-run query 1a and the history query to see where it stopped, and restore from the backup taken in step 0 only if you cannot explain what you find.
