USE shelter_service_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE shelters;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO shelters (id, name, district, address_line1, address_line2, contact_name, contact_phone, manager_user_id, status, capacity, occupancy) VALUES
(1, 'Colombo North Shelter', 'Colombo', '123 Kandy Road', 'Kadawatha', 'Alice Johnson', '0771234567', 5, 'ACTIVE', 50, 30),
(2, 'Galle Relief Camp', 'Galle', '45 Marine Drive', 'Galle Fort', 'Bob Williams', '0777654321', 6, 'ACTIVE', 75, 60),
(3, 'Kandy Safe Haven', 'Kandy', '88 Peradeniya Road', 'Kandy', 'Carol Davis', '0779998888', 7, 'ACTIVE', 60, 45),
(4, 'Jaffna Community Shelter', 'Jaffna', '10 Point Pedro Road', 'Jaffna', 'Emma Wilson', '0775554444', 8, 'ACTIVE', 40, 25);
