#!/usr/bin/env sh
set -eu

project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$project_root"

./gradlew clean build --no-daemon --stacktrace

echo "Build complete. Release JARs:"
find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' -print
