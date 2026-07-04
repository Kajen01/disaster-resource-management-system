#!/usr/bin/env bash

# Exit on error
set -e

echo "=== Seeding DRMS Mock Data ==="

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Verify docker containers are running
if ! docker ps | grep -q "compose-mysql-user-1"; then
    echo "Error: compose-mysql-user-1 container is not running."
    echo "Please start the system using: docker compose -f infra/compose/docker-compose.yml up -d"
    exit 1
fi

echo "Seeding user_service_db on compose-mysql-user-1..."
docker exec -i compose-mysql-user-1 mysql -uroot -proot user_service_db < "$DIR/seed/user.sql"

echo "Seeding shelter_service_db on compose-mysql-shelter-1..."
docker exec -i compose-mysql-shelter-1 mysql -uroot -proot shelter_service_db < "$DIR/seed/shelter.sql"

echo "Seeding resource_service_db on compose-mysql-resource-1..."
docker exec -i compose-mysql-resource-1 mysql -uroot -proot resource_service_db < "$DIR/seed/resource.sql"

echo "Seeding sharing_service_db on compose-mysql-sharing-1..."
docker exec -i compose-mysql-sharing-1 mysql -uroot -proot sharing_service_db < "$DIR/seed/sharing.sql"

echo "=== Mock Data Successfully Seeded! ==="
