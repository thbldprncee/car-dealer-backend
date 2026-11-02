"""Turns load-tests/results/<arch>/ into Markdown tables (printed to stdout)."""
import csv
import json
import re
import sys
from pathlib import Path

RESULTS = Path(__file__).resolve().parent / "results"
ARCHS = ["monolith", "microservices"]
# Containers that run application code, per architecture (databases are reported separately).
APPS = {"monolith": ["app"], "microservices": ["order-service", "storage-service"]}
DBS = {"monolith": ["db"], "microservices": ["order-db", "storage-db"]}


def ms(value):
    return "—" if value is None else f"{value:.0f}" if value >= 10 else f"{value:.1f}"


def load(path):
    return json.loads(path.read_text())["metrics"] if path.exists() else None


def stats(path, arch):
    """Average and peak CPU (sum over the given containers) and peak memory while k6 ran."""
    if not path.exists():
        return {}
    samples = {}
    for row in csv.DictReader(path.open()):
        name = re.sub(r"^lt-(mono|micro)-(.*)-\d+$", r"\2", row["container"])
        cpu = float(row["cpu_percent"].rstrip("%") or 0)
        mem = row["mem"].split("/")[0].strip()
        mib = float(mem[:-3]) * (1024 if mem.endswith("GiB") else 1)
        samples.setdefault(row["time"], {})[name] = (cpu, mib)
    result = {}
    for group, names in (("app", APPS[arch]), ("db", DBS[arch])):
        cpus = [sum(s[n][0] for n in names if n in s) for s in samples.values()]
        mems = [sum(s[n][1] for n in names if n in s) for s in samples.values()]
        # Skip the first and last samples: k6 starts and stops between them.
        core = cpus[1:-1] or cpus
        result[group] = (sum(core) / len(core) if core else 0, max(cpus or [0]), max(mems or [0]))
    return result


def rate_of(path):
    return int(re.search(r"-(\d+)$", path.stem).group(1))


def read_table():
    print("### Read: GET a car by id\n")
    print("| Architecture | Path | Target, req/s | Done, req/s | p50, ms | p95, ms | p99, ms | Errors | Dropped | App CPU avg / peak | DB CPU avg |")
    print("|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|")
    for arch in ARCHS:
        for path in sorted((RESULTS / arch).glob("read-*.json"), key=lambda p: (p.stem.split("-")[1], rate_of(p))):
            m = load(path)
            route = path.stem.split("-")[1]
            label = {"direct": "storage-service" if arch == "microservices" else "app",
                     "grpc": "order-service → gRPC → storage"}[route]
            d = m.get("http_req_duration{name:GET car}", m["http_req_duration"])
            reqs = m["http_reqs"]
            failed = m["http_req_failed"]["value"] * 100
            dropped = m.get("dropped_iterations", {}).get("count", 0)
            duration = reqs["count"] / reqs["rate"] if reqs["rate"] else 0
            done = (reqs["count"] - 1) / duration if duration else 0
            s = stats(path.with_suffix("").with_suffix(".stats.csv"), arch)
            app = s.get("app", (0, 0, 0)); db = s.get("db", (0, 0, 0))
            print(f"| {arch} | {label} | {rate_of(path)} | {done:.0f} | {ms(d['med'])} | {ms(d['p(95)'])} | "
                  f"{ms(d['p(99)'])} | {failed:.2f}% | {dropped} | {app[0]:.0f}% / {app[1]:.0f}% | {db[0]:.0f}% |")
    print("\nCPU is in Docker's units: 100% = one core; every service is limited to 2 cores (200%).\n")


def purchase_table():
    print("### Write: buying an in-stock car\n")
    print("| Architecture | Target, flows/s | Done, flows/s | OK | Sync part p50 / p95, ms | create p95 | pay p95 | Saga p50 / p95, ms | Saga max, ms | Not finished | App CPU avg / peak | DB CPU avg |")
    print("|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|")
    for arch in ARCHS:
        for path in sorted((RESULTS / arch).glob("purchase-*.json"), key=rate_of):
            m = load(path)
            ok = m["purchase_ok"]
            total = ok["passes"] + ok["fails"]
            sync = m.get("purchase_sync_duration", {})
            create = m["http_req_duration{name:create}"]["p(95)"]
            pay = m["http_req_duration{name:pay}"]["p(95)"]
            duration = m["iteration_duration"]
            iters = m["iterations"]
            saga_file = path.with_suffix("").with_suffix(".saga.csv")
            saga = "—"; saga_max = "—"; pending = "—"
            if saga_file.exists():
                row = next(csv.DictReader(saga_file.open()))
                saga = f"{row['saga_p50_ms']} / {row['saga_p95_ms']}"
                saga_max = row["saga_max_ms"]
                pending = row["still_paid"]
            s = stats(path.with_suffix("").with_suffix(".stats.csv"), arch)
            app = s.get("app", (0, 0, 0)); db = s.get("db", (0, 0, 0))
            print(f"| {arch} | {rate_of(path)} | {iters['rate']:.1f} | {ok['passes']}/{total} | "
                  f"{ms(sync.get('med'))} / {ms(sync.get('p(95)'))} | {ms(create)} | {ms(pay)} | {saga} | {saga_max} | {pending} | "
                  f"{app[0]:.0f}% / {app[1]:.0f}% | {db[0]:.0f}% |")
    print("\nMonolith: the sync part ends with `ready-for-delivery`, so the order is done when it returns.")
    print("Microservices: the sync part ends with `pay`; the order then waits for the saga (measured in order_db).\n")


def memory_table():
    print("### Memory (peak, MiB)\n")
    print("| Architecture | Application containers | Databases |")
    print("|---|---:|---:|")
    for arch in ARCHS:
        peaks = [stats(p, arch) for p in (RESULTS / arch).glob("*.stats.csv")]
        if peaks:
            print(f"| {arch} | {max(p['app'][2] for p in peaks):.0f} | {max(p['db'][2] for p in peaks):.0f} |")
    print()


if __name__ == "__main__":
    if not RESULTS.exists():
        sys.exit("No results yet: run load-tests/run.sh first")
    read_table()
    purchase_table()
    memory_table()
