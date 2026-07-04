USE resource_service_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE resource_batches;
TRUNCATE TABLE stock_reservations;
SET FOREIGN_KEY_CHECKS = 1;

-- Seeding resource batches
-- Admin custody batches (shelter_id = null)
INSERT INTO resource_batches (id, shelter_id, donor_email, resource_type, resource_name, unit, quantity_received, quantity_available, expiry_date, received_at, source_donation_ref) VALUES
(1, NULL, 'donor01@gmail.com', 'FOOD', 'Rice 10kg', 'bags', 100, 20, '2026-12-31', NOW(), 'DON-20260701-DONOR1'),
(2, NULL, 'donor02@gmail.com', 'WATER', 'Water Bottled 1.5L', 'boxes', 50, 10, NULL, NOW(), 'DON-20260701-DONOR2'),
(3, NULL, 'donor03@gmail.com', 'MEDICINE', 'Paracetamol 500mg', 'packs', 200, 200, '2027-06-30', NOW(), 'DON-20260701-DONOR3'),
-- Shelter batches transferred from Admin or peer shelters
(4, 1, 'donor01@gmail.com', 'FOOD', 'Rice 10kg', 'bags', 50, 10, '2026-12-31', NOW(), 'DON-20260701-DONOR1'),
(5, 2, 'donor01@gmail.com', 'FOOD', 'Rice 10kg', 'bags', 30, 20, '2026-12-31', NOW(), 'DON-20260701-DONOR1'),
(6, 3, 'donor02@gmail.com', 'WATER', 'Water Bottled 1.5L', 'boxes', 40, 15, NULL, NOW(), 'DON-20260701-DONOR2'),
(7, 4, 'donor01@gmail.com', 'FOOD', 'Rice 10kg', 'bags', 10, 10, '2026-12-31', NOW(), 'DON-20260701-DONOR1');

-- Seeding stock reservations
INSERT INTO stock_reservations (id, batch_id, source_shelter_id, target_shelter_id, resource_type, resource_name, unit, reserved_quantity, reference_number, status, created_at, release_reason) VALUES
(1, 1, 0, 1, 'FOOD', 'Rice 10kg', 'bags', 50, 'ADMIN-TRF-101', 'COMPLETED', NOW(), NULL),
(2, 1, 0, 2, 'FOOD', 'Rice 10kg', 'bags', 30, 'ADMIN-TRF-102', 'COMPLETED', NOW(), NULL),
(3, 2, 0, 3, 'WATER', 'Water Bottled 1.5L', 'boxes', 40, 'ADMIN-TRF-103', 'COMPLETED', NOW(), NULL),
(4, 5, 2, 4, 'FOOD', 'Rice 10kg', 'bags', 10, 'EXCESS-REQ-201', 'COMPLETED', NOW(), NULL);
