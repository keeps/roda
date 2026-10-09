#!/usr/bin/env python3
"""Serve a local dashboard comparing JobDatabaseBenchmark runs (baseline vs. candidate).

Usage:
  serve_job_db_benchmark.py [--dir RESULTS_DIR] [--port 8765] [--open]

Then open http://127.0.0.1:8765/. The results directory is re-read on every
request, so new runs show up after a page refresh. Only listens on localhost.
"""
import argparse
import json
import os
import sys
import webbrowser
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import unquote, urlparse

HERE = os.path.dirname(os.path.abspath(__file__))
DASHBOARD = os.path.join(HERE, "job_db_benchmark_dashboard.html")
DEFAULT_DIR = os.path.normpath(os.path.join(HERE, "..", "..", "roda-core", "roda-core-tests", "target", "benchmark"))
PREFIX = "job-db-benchmark-"


def list_runs(results_dir):
    runs = []
    if not os.path.isdir(results_dir):
        return runs
    for name in sorted(os.listdir(results_dir)):
        if not (name.startswith(PREFIX) and name.endswith(".json")):
            continue
        try:
            with open(os.path.join(results_dir, name), encoding="utf-8") as f:
                data = json.load(f)
        except (OSError, ValueError) as e:
            print(f"skipping {name}: {e}", file=sys.stderr)
            continue
        runs.append({
            "file": name,
            "label": data.get("label"),
            "timestamp": data.get("timestamp"),
            "git": data.get("git", {}),
            "config": data.get("config", {}),
        })
    return runs


def make_handler(results_dir):
    class Handler(SimpleHTTPRequestHandler):
        def _send(self, status, body, content_type):
            payload = body.encode("utf-8") if isinstance(body, str) else body
            self.send_response(status)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(payload)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(payload)

        def do_GET(self):
            path = urlparse(self.path).path
            if path in ("/", "/index.html"):
                with open(DASHBOARD, "rb") as f:
                    self._send(200, f.read(), "text/html; charset=utf-8")
            elif path == "/api/runs":
                self._send(200, json.dumps(list_runs(results_dir)), "application/json")
            elif path.startswith("/api/runs/"):
                name = unquote(path[len("/api/runs/"):])
                # only plain file names from the results directory
                if name != os.path.basename(name) or not name.startswith(PREFIX) or not name.endswith(".json"):
                    self._send(400, '{"error": "invalid run name"}', "application/json")
                    return
                file_path = os.path.join(results_dir, name)
                if not os.path.isfile(file_path):
                    self._send(404, '{"error": "run not found"}', "application/json")
                    return
                with open(file_path, "rb") as f:
                    self._send(200, f.read(), "application/json")
            else:
                self._send(404, "not found", "text/plain")

        def log_message(self, fmt, *args):
            pass

    return Handler


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--dir", default=DEFAULT_DIR, help=f"results directory (default: {DEFAULT_DIR})")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--open", action="store_true", help="open the dashboard in a browser")
    args = parser.parse_args()

    results_dir = os.path.abspath(args.dir)
    server = ThreadingHTTPServer(("127.0.0.1", args.port), make_handler(results_dir))
    url = f"http://127.0.0.1:{args.port}/"
    print(f"Serving {len(list_runs(results_dir))} run(s) from {results_dir}")
    print(f"Dashboard: {url}  (Ctrl+C to stop)")
    if args.open:
        webbrowser.open(url)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
