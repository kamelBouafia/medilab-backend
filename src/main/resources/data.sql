-- LABS
INSERT INTO labs (name) VALUES ('Main Lab');
INSERT INTO labs (name) VALUES ('Branch Lab');

-- STAFF_USERS
-- The password for all users is 'password'
INSERT INTO staff_users (name, username, password, role, lab_id) VALUES ('Alice', 'alice', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Technician', 1);
INSERT INTO staff_users (name, username, password, role, lab_id) VALUES ('Manager', 'manager', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Manager', 1);
INSERT INTO staff_users (name, username, password, role, lab_id) VALUES ('Technician', 'technician', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Technician', 1);
INSERT INTO staff_users (name, username, password, role, lab_id) VALUES ('Bob', 'bob', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Technician', 2);
INSERT INTO staff_users (name, username, password, role, lab_id) VALUES ('BranchManager', 'branchmanager', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Manager', 2);

-- PATIENTS
INSERT INTO patients (name, username, dob, gender, contact, created_by_id, lab_id)
VALUES ('John Doe', 'john.doe', '1990-01-01', 'Male', 'john@example.com', 1, 1);
INSERT INTO patients (name, username, dob, gender, contact, created_by_id, lab_id)
VALUES ('Jane Smith', 'jane.smith', '1985-05-15', 'Female', 'jane@example.com', 4, 2);

-- LAB_TESTS
INSERT INTO lab_tests (name, category, price, lab_id)
VALUES
    ('Complete Blood Count (CBC)', 'Hematology', 25.00, 1),
    ('Blood Glucose', 'Chemistry', 35.00, 1),
    ('Urinalysis', 'Urinalysis', 20.00, 2),
    ('Lipid Panel', 'Chemistry', 40.00, 2);

-- REQUISITIONS
INSERT INTO requisitions (patient_id, doctor_name, date, status, created_by_id, lab_id)
VALUES (1, 'Dr. Smith', CURRENT_TIMESTAMP, 'PROCESSING', 1, 1);
INSERT INTO requisitions (patient_id, doctor_name, date, status, created_by_id, lab_id)
VALUES (2, 'Dr. Jones', CURRENT_TIMESTAMP, 'PROCESSING', 4, 2);

-- REQUISITION_TESTS
INSERT INTO requisition_tests (requisition_id, test_id)
VALUES (1, 1), (1, 2);
INSERT INTO requisition_tests (requisition_id, test_id)
VALUES (2, 3), (2, 4);

-- INVENTORY
INSERT INTO inventory (name, category, quantity, low_stock_threshold, supplier, added_by_id, lab_id)
VALUES ('Test Tubes', 'Consumables', 100, 10, 'LabSupplier', 1, 1);
INSERT INTO inventory (name, category, quantity, low_stock_threshold, supplier, added_by_id, lab_id)
VALUES ('Microscope Slides', 'Consumables', 200, 20, 'BioSupply', 4, 2);
