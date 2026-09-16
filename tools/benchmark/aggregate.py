"""Reduces a set of benchmark reports to a median and a spread, and compares two sets.

    python tools/benchmark/aggregate.py run/benchmark-sets/20260916-214500
    python tools/benchmark/aggregate.py <before> --compare <after>
    python tools/benchmark/aggregate.py <set> --ops --markdown

`bench.py` writes a set: `<set>/<node>/<profile>-<run>.json`. One report is a measurement; a set is
evidence. Every figure here is the median of the runs, followed by a spread, which is how far the
runs were apart as a share of that median::

    spread = (max - min) / median

That number is the point of the whole exercise. A difference between two sets means something when
it is larger than the spread of the runs behind it, and means nothing when it is not, so `--compare`
prints both side by side and says which of the two it is rather than leaving it to the eye.

Reports whose `status` is not `ok` are refused, as are runs recorded under a profiler or a debugger
(`environment.profiling`), and so is a comparison between sets measured on different processors or in
different terrain: `environment.cpu` and `environment.levelSeed` have to agree, or the answer is about
the machine rather than about the build.
"""

import argparse
import json
import pathlib
import statistics
import sys

DEFAULT_PLAYERS = 200


def tick(report, count, *path):
    """A field of the tick scenario for one player count, or None when the run had no such scenario."""
    for scenario in report.get("tickScenarios", []):
        if scenario.get("players") == count:
            value = scenario
            for key in path:
                value = value.get(key) if isinstance(value, dict) else None
                if value is None:
                    return None
            return value
    return None


def memory(report, key):
    return report.get("memory", {}).get(key)


def headline(count):
    """What a set is read down to by default: the aggregate cost and the two memory figures."""
    return [
        (f"ms/tick@{count}", lambda r: tick(r, count, "msPerTick", "mean")),
        (f"p99 ms@{count}", lambda r: tick(r, count, "msPerTick", "p99")),
        (f"us/player@{count}", lambda r: tick(r, count, "microsPerPlayerPerTick")),
        (f"B/player/tick@{count}", lambda r: tick(r, count, "allocatedBytesPerPlayerPerTick")),
        ("first touch B", lambda r: memory(r, "firstTouchBytesPerPlayer")),
        ("steady tick B", lambda r: memory(r, "steadyTickBytesPerPlayer")),
    ]


def operations(report):
    return {entry["name"]: entry for entry in report.get("interactions", [])}


def op_metrics(reports):
    """One pair of rows per interaction every report has: mean microseconds, then bytes."""
    names = None
    for report in reports:
        seen = set(operations(report))
        names = seen if names is None else (names & seen)
    metrics = []
    for name in sorted(names or []):
        metrics.append((name + " us", lambda r, n=name: operations(r)[n]["microsPerOp"]["mean"]))
        metrics.append((name + " B", lambda r, n=name: operations(r)[n]["bytesPerOp"]))
    return metrics


def load(directory):
    """Every report in a set, by node. A run that did not finish is refused rather than averaged in."""
    root = pathlib.Path(directory)
    if not root.is_dir():
        raise SystemExit(f"{root} is not a directory of reports; run bench.py first")
    sets = {}
    for node_dir in sorted(path for path in root.iterdir() if path.is_dir()):
        reports = []
        for file in sorted(node_dir.glob("*.json")):
            with file.open(encoding="utf-8") as handle:
                report = json.load(handle)
            if report.get("status") != "ok":
                raise SystemExit(f"{file} has status {report.get('status')}; "
                                 "a set with a failed run in it is not evidence")
            if report.get("environment", {}).get("profiling"):
                raise SystemExit(f"{file} was recorded with a profiler or a debugger attached, which "
                                 "makes every figure in it slower than the mod is; profile a run to "
                                 "read it, measure one to keep it, never the same run")
            reports.append(report)
        if reports:
            sets[node_dir.name] = reports
    if not sets:
        raise SystemExit(f"no reports under {root}")
    return sets


def environment(reports, key):
    return {report.get("environment", {}).get(key) for report in reports}


def reduce_metric(reports, read):
    """The median of one figure over the runs, and how far apart the runs were, as a share of it."""
    values = [read(report) for report in reports]
    values = [float(value) for value in values if isinstance(value, (int, float))]
    if not values:
        return None, None
    median = statistics.median(values)
    spread = (max(values) - min(values)) / median if median else 0.0
    return median, spread


def figure(median, spread):
    return "-" if median is None else f"{median:.4g} +/-{spread * 100:.0f}%"


def table(rows, markdown):
    widths = [max(len(str(row[column])) for row in rows) for column in range(len(rows[0]))]
    lines = []
    for index, row in enumerate(rows):
        cells = [str(cell).ljust(widths[column]) for column, cell in enumerate(row)]
        lines.append(("| " + " | ".join(cells) + " |") if markdown else "  ".join(cells).rstrip())
        if markdown and index == 0:
            lines.append("|" + "|".join("-" * (width + 2) for width in widths) + "|")
    return "\n".join(lines)


def label_of(reports):
    """One line saying what these runs were, so a table is never read out of context."""
    cpu = sorted(str(value) for value in environment(reports, "cpu"))
    seed = sorted(str(value) for value in environment(reports, "levelSeed"))
    return f"{len(reports)} runs, {cpu[0]}, seed {seed[0]}"


def describe(sets, metrics_for, markdown):
    for node, reports in sets.items():
        print(f"\n{node}: {label_of(reports)}")
        rows = [["metric", "median", "spread"]]
        for name, read in metrics_for(reports):
            median, spread = reduce_metric(reports, read)
            rows.append([name, "-" if median is None else f"{median:.4g}",
                         "-" if median is None else f"{spread * 100:.0f}%"])
        print(table(rows, markdown))


def compare(before, after, metrics_for, markdown):
    """Two sets side by side. A change counts only when it is larger than the runs' own spread."""
    for node in sorted(set(before) & set(after)):
        left, right = before[node], after[node]
        for key in ("cpu", "levelSeed"):
            values = environment(left, key) | environment(right, key)
            if len(values) > 1:
                raise SystemExit(f"{node}: the two sets disagree on environment.{key} ({values}); "
                                 "they were measured on different machines or in different terrain")
        print(f"\n{node}: {len(left)} runs before, {len(right)} after")
        rows = [["metric", "before", "after", "change", "verdict"]]
        for name, read in metrics_for(left + right):
            median_before, spread_before = reduce_metric(left, read)
            median_after, spread_after = reduce_metric(right, read)
            if not median_before or median_after is None:
                rows.append([name, figure(median_before, spread_before),
                             figure(median_after, spread_after), "-", "-"])
                continue
            change = (median_after - median_before) / median_before
            noise = max(spread_before, spread_after)
            verdict = "noise" if abs(change) <= noise else ("worse" if change > 0 else "better")
            rows.append([name, figure(median_before, spread_before), figure(median_after, spread_after),
                         f"{change * 100:+.0f}%", verdict])
        print(table(rows, markdown))
    only_one = set(before) ^ set(after)
    if only_one:
        print("\nnodes in only one of the two sets, skipped: " + ", ".join(sorted(only_one)), file=sys.stderr)


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("set", help="a directory bench.py wrote")
    parser.add_argument("--compare", help="a second set, measured after the change")
    parser.add_argument("--ops", action="store_true",
                        help="every interaction rather than the headline figures")
    parser.add_argument("--players", type=int, default=DEFAULT_PLAYERS,
                        help=f"which tick scenario the headline figures come from (default {DEFAULT_PLAYERS})")
    parser.add_argument("--markdown", action="store_true", help="tables a document can take as they are")
    options = parser.parse_args()

    metrics_for = op_metrics if options.ops else (lambda reports: headline(options.players))
    first = load(options.set)
    if options.compare:
        compare(first, load(options.compare), metrics_for, options.markdown)
    else:
        describe(first, metrics_for, options.markdown)
    return 0


if __name__ == "__main__":
    sys.exit(main())
