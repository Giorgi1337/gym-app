#!/usr/bin/env bash
set -euo pipefail

case "${1:-up}" in
  up)
    ./gradlew :gym-crm-service:bootJar :trainer-workload-service:bootJar :discovery-service:bootJar
    docker compose up --build -d
    docker compose ps
    ;;
  status) docker compose ps ;;
  logs) docker compose logs -f main report ;;
  shell)
    docker compose exec "${2:?Usage: bash scripts/docker.sh shell <main|report>}" sh
    ;;
  down) docker compose down ;;
  *)
    echo "Usage: bash scripts/docker.sh {up|status|logs|shell <main|report>|down}" >&2
    exit 1
    ;;
esac
