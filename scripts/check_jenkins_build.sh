#!/usr/bin/env bash
# Nagios-style plugin to simulate checking a Jenkins job/build status.
# Usage:
#   check_jenkins_build.sh <jenkins_url_optional>
# If URL is provided, we check reachability. Otherwise, we simulate OK.

set -euo pipefail

URL=${1:-}

if [[ -z "$URL" ]]; then
  echo "JENKINS OK - simulated success"
  exit 0
fi

if curl -fsS --max-time 5 "$URL" >/dev/null; then
  echo "JENKINS OK - reachable: $URL"
  exit 0
else
  echo "JENKINS CRITICAL - cannot reach: $URL"
  exit 2
fi
