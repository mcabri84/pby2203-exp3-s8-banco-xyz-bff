#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/preparar_entorno.py
mvn -B -ntp -DskipTests package
docker compose -p banco-xyz-s8 build
docker compose -p banco-xyz-s8 up -d --wait --wait-timeout 420
