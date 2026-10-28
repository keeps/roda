# Ingest via REST API

External systems can submit a **Submission Information Package (SIP)** to RODA and start its ingest in a **single API call**, optionally asking RODA to call a **webhook** when the ingest finishes.

Previously this took two calls: uploading the SIP to the transfer area and then creating an ingest job for it. That way still works and is unchanged.

## Authentication

The call can be made without a browser session, using one of:

- **Access token**: create an access token for the user in *Administration* > *Users* > *Manage Access tokens*, exchange it for a temporary token and send it in the `Authorization` header:

  ```bash
  TOKEN=$(curl -s -X POST https://roda.example.com/api/v2/members/token \
    -H 'Content-Type: text/plain' -d "$ACCESS_TOKEN" | jq -r .token)
  ```

  ```bash
  curl -H "Authorization: Bearer $TOKEN" ...
  ```

- **HTTP Basic authentication**: `curl -u username:password ...`

The user needs permission both to create transferred resources and to create jobs.

## Submitting a SIP

`POST /api/v2/transfers/create/resource/ingest`, sent as `multipart/form-data`:

| Field | Required | Description |
|-------|----------|-------------|
| `resource` | yes | The SIP file |
| `webhook` | no | URL that RODA calls when the ingest job finishes (see [Webhook](#webhook)) |
| `parameters` | no | Ingest parameters, as a JSON object with string values, e.g. `{"parameter.do_auto_accept": "true"}` |
| `name` | no | Job name, shown in *Ingest* > *Process* (defaults to `API Ingest workflow`) |
| `plugin` | no | Ingest plugin (defaults to `org.roda.core.plugins.base.ingest.v2.ConfigurableIngestPlugin`) |
| `parent-uuid` | no | Folder of the transfer area where the SIP is stored (defaults to the root) |

Example:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  -F resource=@sip.zip \
  -F webhook=https://integration.example.com/roda/callback \
  -F 'parameters={"parameter.do_auto_accept":"true"}' \
  -F name="Nightly submission" \
  https://roda.example.com/api/v2/transfers/create/resource/ingest
```

The response is the created job. Its `id` can be used to follow the ingest in `GET /api/v2/jobs/{id}`, or in *Ingest* > *Process*.

### Manual review or automatic acceptance

As with an ingest started in the web interface, the ingest parameters decide what happens to the SIP:

- `"parameter.do_auto_accept": "true"`: the resulting AIPs go directly to the repository;
- `"parameter.do_auto_accept": "false"`: the job finishes with the AIPs waiting for appraisal in *Ingest* > *Appraisal*.

### Responses

| Status | Meaning |
|--------|---------|
| `201` | Ingest job created |
| `400` | Invalid webhook, plugin or parameters. Nothing was stored |
| `401` / `403` | Missing credentials, or user without permission to upload or create jobs |
| `409` | A file with the same name already exists in the transfer area (or in the given `parent-uuid`) |

## Webhook

When the job finishes, RODA sends an HTTP `POST` to the webhook with the job as JSON (the same content returned by `GET /api/v2/jobs/{id}`), including its final `state` and `jobStats`. A successful HTTP response is enough for the notification to be considered delivered. If `"parameter.notification_when_failed": "true"` is sent in `parameters`, the webhook is only called when the ingest fails.

For security reasons, a webhook is only accepted if:

1. it matches one of the destinations allowed by the administrator (same scheme, host and port), and
2. it does not resolve to an internal or private address, such as `localhost`, `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16` or `169.254.169.254`.

Otherwise the request is rejected immediately with `400` and nothing is stored. The same checks are repeated when the notification is sent, and redirects are not followed.

The webhook can only be given in the `webhook` field: the `ingest.http_notification.endpoint` parameter is rejected in `parameters`.

## Configuration

The allowed webhook destinations are set in `roda-core.properties`. The list is **empty by default**, which rejects every webhook (submissions without a webhook still work). Repeat the key to allow several destinations:

```properties
core.notification.webhook.allowed_destinations = https://integration.example.com
core.notification.webhook.allowed_destinations = https://other.example.com:8443
```

This list only applies to the `webhook` field of this call. HTTP notifications configured in other ways keep working as before, without these restrictions:

- the `ingest.configurable.http_endpoint` property, used by default by every ingest (including calls to this endpoint without a `webhook`);
- the `ingest.http_notification.endpoint` parameter of a job created in `POST /api/v2/jobs`.
