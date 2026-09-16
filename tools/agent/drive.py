"""Sends requests to a running game's agent queue and prints the answers.

    python tools/agent/drive.py run/<node>/agent/server requests.jsonl
    echo '{"command": "probe"}' | python tools/agent/drive.py run/<node>/agent/server -

The queue itself is two text files, so none of this is required: appending a line to `in.jsonl` and
reading `out.jsonl` is the whole protocol, and that is what an agent with only file tools does. This
exists so a person, or a script, does not have to match answers to requests by hand. It waits for
`ready.json` before writing, gives every request an id if it has none, and waits for the answer to
each one, so what it prints is in the order asked rather than the order the game finished.

Requests may carry an ``expect`` object and relational ``checks``. They are evaluated against the
reply, so a shell gets exit 1 when the game answered the wrong value as well as when it refused or
failed to answer a request::

    {"command":"client.state","expect":{"result.thirst":7}}
    {"command":"client.hud","checks":[
      {"left":"result.bar.bottom","op":"le","right":"result.food.top"}
    ]}
"""

import argparse
import json
import pathlib
import sys
import time

READY = "ready.json"
IN = "in.jsonl"
OUT = "out.jsonl"


class MissingPath(Exception):
    pass


def value_at(value, path):
    """Reads a dotted path from a JSON value; numeric components index arrays."""
    current = value
    for component in path.split(".") if path else []:
        try:
            if isinstance(current, list):
                current = current[int(component)]
            else:
                current = current[component]
        except (KeyError, IndexError, TypeError, ValueError):
            raise MissingPath(path)
    return current


def comparison(left, operation, right, tolerance=None):
    if operation == "eq":
        return left == right
    if operation == "ne":
        return left != right
    if operation == "lt":
        return left < right
    if operation == "le":
        return left <= right
    if operation == "gt":
        return left > right
    if operation == "ge":
        return left >= right
    if operation == "contains":
        return right in left
    if operation == "not_contains":
        return right not in left
    if operation == "within":
        if tolerance is None:
            raise ValueError("the 'within' operation needs 'tolerance'")
        return abs(left - right) <= tolerance
    raise ValueError("unknown assertion operation %r" % operation)


def assertions(request, answer):
    """Returns human-readable failures for the expectations attached to one request."""
    failures = []
    expected = request.get("expect", {})
    if not isinstance(expected, dict):
        return ["expect must be an object of dotted reply paths to exact values"]
    for path, wanted in expected.items():
        try:
            actual = value_at(answer, path)
        except MissingPath:
            failures.append("%s is missing (expected %r)" % (path, wanted))
            continue
        if actual != wanted:
            failures.append("%s: expected %r, got %r" % (path, wanted, actual))

    checks = request.get("checks", [])
    if not isinstance(checks, list):
        return failures + ["checks must be an array"]
    for number, check in enumerate(checks, start=1):
        if not isinstance(check, dict) or "left" not in check or "op" not in check:
            failures.append("check %d needs left and op" % number)
            continue
        try:
            left = value_at(answer, check["left"])
            if "right" in check:
                right = value_at(answer, check["right"])
                right_label = check["right"]
            elif "value" in check:
                right = check["value"]
                right_label = repr(right)
            else:
                failures.append("check %d needs right or value" % number)
                continue
            operation = check["op"]
            if not comparison(left, operation, right, check.get("tolerance")):
                failures.append("check %d failed: %s (%r) %s %s (%r)" %
                                (number, check["left"], left, operation, right_label, right))
        except MissingPath as error:
            failures.append("check %d path is missing: %s" % (number, error))
        except (TypeError, ValueError) as error:
            failures.append("check %d is invalid: %s" % (number, error))
    return failures


def load(source):
    """The requests to send, from a file or standard input, one JSON object per line."""
    text = sys.stdin.read() if source == "-" else pathlib.Path(source).read_text(encoding="utf-8")
    requests = []
    ids = set()
    for number, line in enumerate(text.splitlines(), start=1):
        line = line.strip()
        if not line or line.startswith("//"):
            continue
        try:
            request = json.loads(line)
        except ValueError as error:
            raise SystemExit("%s:%d is not JSON: %s" % (source, number, error))
        request.setdefault("id", "r%d" % len(requests))
        if request["id"] in ids:
            raise SystemExit("%s:%d repeats request id %r" % (source, number, request["id"]))
        ids.add(request["id"])
        requests.append(request)
    return requests


def wait_for_ready(queue, timeout, since):
    """Waits for a game started after `since` to open the queue, in epoch milliseconds.

    Waiting for the file alone is the mistake this exists to avoid. A game takes most of a minute to
    come up, and for all of it the previous run's `ready.json` is still in the directory saying the
    queue is open; a request written then goes into the file the new run is about to rotate away, and
    is never answered. So the stamp the game writes has to be newer than the moment the wait began.
    """
    ready = queue / READY
    deadline = time.time() + timeout
    while time.time() < deadline:
        if ready.is_file() and ready.stat().st_size > 0:
            try:
                about = json.loads(ready.read_text(encoding="utf-8"))
            except ValueError:
                about = None          # being written this instant; look again
            if about is not None and about.get("startedAt", 0) >= since:
                time.sleep(0.2)
                return about
        time.sleep(0.2)
    raise SystemExit("no %s newer than the start of this command in %s after %.0f s; "
                     "is the game starting?" % (READY, queue, timeout))


def replies(queue, offset):
    """Every complete line of out.jsonl after `offset`, with the new offset."""
    out = queue / OUT
    if not out.is_file():
        return [], offset
    data = out.read_bytes()
    end = data.rfind(b"\n") + 1
    if end <= offset:
        return [], offset
    lines = data[offset:end].decode("utf-8").splitlines()
    return [json.loads(line) for line in lines if line.strip()], end


def drive(queue, requests, timeout):
    out = queue / OUT
    offset = out.stat().st_size if out.is_file() else 0
    with (queue / IN).open("a", encoding="utf-8", newline="\n") as handle:
        for request in requests:
            handle.write(json.dumps(request) + "\n")
        handle.flush()

    wanted = [request["id"] for request in requests]
    answers = {}
    deadline = time.time() + timeout
    while wanted and time.time() < deadline:
        found, offset = replies(queue, offset)
        for answer in found:
            answers[answer.get("id")] = answer
            if answer.get("id") in wanted:
                wanted.remove(answer["id"])
        if wanted:
            time.sleep(0.1)

    failed = False
    for request in requests:
        answer = answers.get(request["id"])
        if answer is None:
            failed = True
            print(json.dumps({"id": request["id"], "command": request.get("command"), "ok": False,
                              "error": "unanswered after %.0f s" % timeout}))
            continue
        if not answer.get("ok"):
            failed = True
        assertion_failures = assertions(request, answer)
        if assertion_failures:
            failed = True
            answer = dict(answer)
            answer["assertionErrors"] = assertion_failures
        print(json.dumps(answer))
    return failed


def main():
    started = time.time() * 1000.0
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("queue", help="the queue directory, run/<node>/agent/<name>")
    parser.add_argument("requests", help="a file of requests, one JSON object per line, or - for stdin")
    parser.add_argument("--ready", type=float, default=0.0,
                        help="seconds to wait for a game started from now on to open the queue; "
                             "leave it off for a game that is already running")
    parser.add_argument("--timeout", type=float, default=120.0,
                        help="seconds to wait for the last answer (default 120)")
    arguments = parser.parse_args()

    queue = pathlib.Path(arguments.queue)
    if arguments.ready:
        about = wait_for_ready(queue, arguments.ready, started)
        print(json.dumps({"ready": about}))
    elif not (queue / READY).is_file():
        raise SystemExit("%s has no %s; the game has not opened this queue" % (queue, READY))

    requests = load(arguments.requests)
    if not requests:
        raise SystemExit("no requests to send")
    sys.exit(1 if drive(queue, requests, arguments.timeout) else 0)


if __name__ == "__main__":
    main()
