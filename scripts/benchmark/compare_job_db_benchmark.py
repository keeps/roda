#!/usr/bin/env python3
"""Compare JobDatabaseBenchmark results (before vs. after a code change).

Usage:
  compare_job_db_benchmark.py -b BASELINE.json [...] -c CANDIDATE.json [...] [--markdown] [--all-latencies]

When several files are given for a side (repeated runs), the median of each
metric is used, which dampens run-to-run noise. Lower is better for every
metric except ops/s.
"""
import argparse
import json
import statistics
import sys

PHASE_METRICS = [
    ("wall ms", ("wallMs",)),
    ("ops/s", ("opsPerSec",)),
    ("hibernate statements", ("hibernate", "preparedStatements")),
    ("hibernate transactions", ("hibernate", "transactions")),
    ("hibernate entity loads", ("hibernate", "entityLoads")),
    ("hibernate entity updates", ("hibernate", "entityUpdates")),
    ("pg statements (all)", ("statementTotals", "all", "calls")),
    ("pg exec ms (all)", ("statementTotals", "all", "total_exec_ms")),
    ("pg blocks dirtied", ("statementTotals", "all", "shared_blks_dirtied")),
    ("WAL bytes", ("walBytes",)),
]

TABLE_METRICS = [
    ("tuples inserted", "tuplesInserted"),
    ("tuples updated", "tuplesUpdated"),
    ("tuples HOT updated", "tuplesHotUpdated"),
    ("tuples deleted", "tuplesDeleted"),
    ("seq scans", "seqScans"),
    ("dead tuples after", "deadTuplesAfter"),
    ("avg row bytes after", "avgRowBytesAfter"),
    ("total bytes after", "totalBytesAfter"),
    ("TOAST bytes after", "toastBytesAfter"),
]

LATENCY_METRICS = ["p50Ms", "p95Ms", "p99Ms"]
HIGHER_IS_BETTER = {"ops/s"}


def load(paths):
    runs = []
    for path in paths:
        with open(path, encoding="utf-8") as f:
            runs.append(json.load(f))
    return runs


def dig(obj, keys):
    for key in keys:
        if not isinstance(obj, dict) or key not in obj:
            return None
        obj = obj[key]
    return obj


def median(values):
    values = [v for v in values if isinstance(v, (int, float))]
    return statistics.median(values) if values else None


def phase_of(run, name):
    return next((p for p in run["phases"] if p["name"] == name), None)


def fmt(value):
    if value is None:
        return "-"
    if isinstance(value, float) and not value.is_integer():
        return f"{value:,.3f}" if abs(value) < 100 else f"{value:,.1f}"
    return f"{int(value):,}"


def delta(label, base, cand):
    if base is None or cand is None:
        return "", ""
    if base == 0:
        return ("" if cand == 0 else "new"), ""
    pct = (cand - base) / abs(base) * 100
    if abs(pct) < 0.5:
        return f"{pct:+.1f}%", ""
    better = (pct > 0) if label in HIGHER_IS_BETTER else (pct < 0)
    return f"{pct:+.1f}%", ("better" if better else "worse")


class Table:
    def __init__(self, markdown):
        self.markdown = markdown
        self.rows = []

    def add(self, label, base, cand):
        pct, verdict = delta(label.strip(), base, cand)
        self.rows.append((label, fmt(base), fmt(cand), pct, verdict))

    def render(self, title):
        out = []
        header = ("metric", "baseline", "candidate", "delta", "")
        if self.markdown:
            out.append(f"\n#### {title}\n")
            out.append("| " + " | ".join(header) + " |")
            out.append("|---|--:|--:|--:|---|")
            for row in self.rows:
                out.append("| " + " | ".join(row) + " |")
        else:
            out.append(f"\n== {title}")
            widths = [max(len(str(r[i])) for r in self.rows + [header]) for i in range(len(header))]
            for row in [header] + self.rows:
                cells = [str(row[0]).ljust(widths[0])] + [str(c).rjust(w) for c, w in zip(row[1:4], widths[1:4])]
                out.append("  ".join(cells) + "  " + row[4])
        return "\n".join(out)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("-b", "--baseline", nargs="+", required=True, help="baseline result file(s)")
    parser.add_argument("-c", "--candidate", nargs="+", required=True, help="candidate result file(s)")
    parser.add_argument("--markdown", action="store_true", help="output markdown tables (e.g. for a PR)")
    parser.add_argument("--all-latencies", action="store_true", help="also include p99 latencies")
    args = parser.parse_args()

    baseline, candidate = load(args.baseline), load(args.candidate)

    def describe(runs):
        return ", ".join(f"{r['label']}@{r['git'].get('commit')}{'*' if r['git'].get('dirty') else ''}"
                         for r in runs)

    print(f"baseline:  {describe(baseline)} ({len(baseline)} run(s), medians)")
    print(f"candidate: {describe(candidate)} ({len(candidate)} run(s), medians)")
    configs = {json.dumps(r["config"], sort_keys=True) for r in baseline + candidate}
    if len(configs) > 1:
        print("WARNING: runs were made with different workload configurations:", file=sys.stderr)
        for c in sorted(configs):
            print("  " + c, file=sys.stderr)

    phase_names = [p["name"] for p in baseline[0]["phases"]]
    for name in phase_names:
        base_phases = [p for p in (phase_of(r, name) for r in baseline) if p]
        cand_phases = [p for p in (phase_of(r, name) for r in candidate) if p]
        if not cand_phases:
            continue
        table = Table(args.markdown)

        for label, keys in PHASE_METRICS:
            table.add(label, median([dig(p, keys) for p in base_phases]),
                      median([dig(p, keys) for p in cand_phases]))

        operations = sorted(set().union(*(p["latency"].keys() for p in base_phases + cand_phases)))
        latencies = LATENCY_METRICS if args.all_latencies else LATENCY_METRICS[:2]
        for op in operations:
            for metric in latencies:
                table.add(f"{op} {metric[:-2]} ms", median([dig(p, ("latency", op, metric)) for p in base_phases]),
                          median([dig(p, ("latency", op, metric)) for p in cand_phases]))

        tables = sorted(set().union(*(p["tables"].keys() for p in base_phases + cand_phases)))
        for tbl in tables:
            for label, key in TABLE_METRICS:
                table.add(f"{tbl}: {label}", median([dig(p, ("tables", tbl, key)) for p in base_phases]),
                          median([dig(p, ("tables", tbl, key)) for p in cand_phases]))

        print(table.render(f"phase: {name}"))


if __name__ == "__main__":
    main()
