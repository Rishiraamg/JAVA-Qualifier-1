#!/usr/bin/env bash
set -euo pipefail
mvn -q -DskipTests package
java -jar target/hrx-webhook-sql-1.0.0.jar
