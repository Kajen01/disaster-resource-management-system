USE sharing_service_db;

CREATE TABLE IF NOT EXISTS excess_notifications (
	id bigint NOT NULL AUTO_INCREMENT,
	shelter_id bigint DEFAULT NULL,
	batch_id bigint DEFAULT NULL,
	source_donation_ref varchar(255) DEFAULT NULL,
	resource_type varchar(255) DEFAULT NULL,
	resource_name varchar(255) DEFAULT NULL,
	unit varchar(255) DEFAULT NULL,
	quantity int NOT NULL,
	status enum('OPEN','RESOLVED') DEFAULT NULL,
	created_at datetime(6) DEFAULT NULL,
	PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS excess_requests (
	id bigint NOT NULL AUTO_INCREMENT,
	excess_notification_id bigint DEFAULT NULL,
	requesting_shelter_id bigint DEFAULT NULL,
	quantity int NOT NULL,
	status enum('PENDING','APPROVED','REJECTED') DEFAULT NULL,
	created_at datetime(6) DEFAULT NULL,
	PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE shortage_requests;
TRUNCATE TABLE excess_notifications;
TRUNCATE TABLE excess_requests;
TRUNCATE TABLE transfers;
TRUNCATE TABLE donation_traces;
TRUNCATE TABLE transparency_timeline_events;
SET FOREIGN_KEY_CHECKS = 1;

-- Shortage Requests
INSERT INTO shortage_requests (id, shelter_id, resource_type, resource_name, unit, required_quantity, shortage_quantity, justification, status, created_at) VALUES
(1, 1, 'FOOD', 'Rice 10kg', 'bags', 50, 0, 'Heavy flooding in Colombo North area requires immediate food relief.', 'FULFILLED', NOW()),
(2, 3, 'WATER', 'Water Bottled 1.5L', 'boxes', 40, 0, 'Water contamination in Kandy district camp sources.', 'FULFILLED', NOW()),
(3, 4, 'CLOTHING', 'Blankets', 'pcs', 100, 100, 'Cold night temperatures in Jaffna relief area.', 'OPEN', NOW());

-- Excess Notifications
INSERT INTO excess_notifications (id, shelter_id, batch_id, source_donation_ref, resource_type, resource_name, unit, quantity, status, created_at) VALUES
(1, 2, 5, 'DON-20260701-DONOR1', 'FOOD', 'Rice 10kg', 'bags', 10, 'OPEN', NOW()),
(2, 3, 6, 'DON-20260701-DONOR2', 'WATER', 'Water Bottled 1.5L', 'boxes', 5, 'OPEN', NOW());

-- Excess Requests
INSERT INTO excess_requests (id, excess_notification_id, requesting_shelter_id, quantity, status, created_at) VALUES
(1, 1, 4, 10, 'APPROVED', NOW()),
(2, 2, 1, 5, 'PENDING', NOW());

-- Transfers
INSERT INTO transfers (id, shortage_request_id, source_shelter_id, target_shelter_id, reservation_id, source_batch_id, donation_ref, resource_type, resource_name, unit, quantity, status, created_at) VALUES
(1, 1, 0, 1, 1, 1, 'DON-20260701-DONOR1', 'FOOD', 'Rice 10kg', 'bags', 50, 'COMPLETED', NOW()),
(2, NULL, 0, 2, 2, 1, 'DON-20260701-DONOR1', 'FOOD', 'Rice 10kg', 'bags', 30, 'COMPLETED', NOW()),
(3, 2, 0, 3, 3, 2, 'DON-20260701-DONOR2', 'WATER', 'Water Bottled 1.5L', 'boxes', 40, 'COMPLETED', NOW()),
(4, NULL, 2, 4, 4, 5, 'DON-20260701-DONOR1', 'FOOD', 'Rice 10kg', 'bags', 10, 'COMPLETED', NOW());

-- Donation Traces
INSERT INTO donation_traces (id, donation_ref, transfer_id, source_shelter_id, destination_shelter_id, resource_type, resource_name, quantity, recorded_at) VALUES
(1, 'DON-20260701-DONOR1', 1, 0, 1, 'FOOD', 'Rice 10kg', 50, NOW()),
(2, 'DON-20260701-DONOR1', 2, 0, 2, 'FOOD', 'Rice 10kg', 30, NOW()),
(3, 'DON-20260701-DONOR2', 3, 0, 3, 'WATER', 'Water Bottled 1.5L', 40, NOW()),
(4, 'DON-20260701-DONOR1', 4, 2, 4, 'FOOD', 'Rice 10kg', 10, NOW());

-- Transparency Timeline Events
INSERT INTO transparency_timeline_events (id, donation_ref, transfer_id, event_type, details, occurred_at) VALUES
(1, 'DON-20260701-DONOR1', NULL, 'DONATION_LOGGED', 'Donation of 100 units of Rice 10kg registered by Admin from donor donor01@gmail.com', NOW()),
(2, 'DON-20260701-DONOR1', 1, 'TRANSFER_DISPATCHED', 'Transfer dispatched from Admin to shelter Colombo North Shelter', NOW()),
(3, 'DON-20260701-DONOR1', 1, 'TRANSFER_COMPLETED', 'Transfer confirmed received at shelter Colombo North Shelter', NOW()),
(4, 'DON-20260701-DONOR1', 2, 'TRANSFER_DISPATCHED', 'Transfer dispatched from Admin to shelter Galle Relief Camp', NOW()),
(5, 'DON-20260701-DONOR1', 2, 'TRANSFER_COMPLETED', 'Transfer confirmed received at shelter Galle Relief Camp', NOW()),
(6, 'DON-20260701-DONOR1', 4, 'TRANSFER_DISPATCHED', 'Transfer dispatched from shelter Galle Relief Camp to shelter Jaffna Community Shelter', NOW()),
(7, 'DON-20260701-DONOR1', 4, 'TRANSFER_COMPLETED', 'Transfer confirmed received at shelter Jaffna Community Shelter', NOW()),

(8, 'DON-20260701-DONOR2', NULL, 'DONATION_LOGGED', 'Donation of 50 units of Water Bottled 1.5L registered by Admin from donor donor02@gmail.com', NOW()),
(9, 'DON-20260701-DONOR2', 3, 'TRANSFER_DISPATCHED', 'Transfer dispatched from Admin to shelter Kandy Safe Haven', NOW()),
(10, 'DON-20260701-DONOR2', 3, 'TRANSFER_COMPLETED', 'Transfer confirmed received at shelter Kandy Safe Haven', NOW()),

(11, 'DON-20260701-DONOR3', NULL, 'DONATION_LOGGED', 'Donation of 200 units of Paracetamol 500mg registered by Admin from donor donor03@gmail.com', NOW());
