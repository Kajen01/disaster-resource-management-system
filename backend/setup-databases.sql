-- ============================================================
-- DRMS - MySQL Setup Script
-- Run this as root to create all service users and databases
-- ============================================================

-- ============================================================
-- Command: cmd /c "mysql -u root -p < backend\setup-databases.sql"
-- Root password for the Docker Compose MySQL containers: root
-- If you use a separate local MySQL instance, replace the password as needed.
-- ============================================================

-- User Service
CREATE USER IF NOT EXISTS 'user_service'@'localhost' IDENTIFIED BY 'user_service';
CREATE DATABASE IF NOT EXISTS user_service_db;
GRANT ALL PRIVILEGES ON user_service_db.* TO 'user_service'@'localhost';

-- Shelter Service
CREATE USER IF NOT EXISTS 'shelter_service'@'localhost' IDENTIFIED BY 'shelter_service';
CREATE DATABASE IF NOT EXISTS shelter_service_db;
GRANT ALL PRIVILEGES ON shelter_service_db.* TO 'shelter_service'@'localhost';

-- Resource Service
CREATE USER IF NOT EXISTS 'resource_service'@'localhost' IDENTIFIED BY 'resource_service';
CREATE DATABASE IF NOT EXISTS resource_service_db;
GRANT ALL PRIVILEGES ON resource_service_db.* TO 'resource_service'@'localhost';

-- Sharing Transparency Service
CREATE USER IF NOT EXISTS 'sharing_service'@'localhost' IDENTIFIED BY 'sharing_service';
CREATE DATABASE IF NOT EXISTS sharing_service_db;
GRANT ALL PRIVILEGES ON sharing_service_db.* TO 'sharing_service'@'localhost';

FLUSH PRIVILEGES;

-- Verify all users were created
SELECT User, Host FROM mysql.user
WHERE User IN ('user_service', 'shelter_service', 'resource_service', 'sharing_service');
