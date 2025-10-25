-- LABS
INSERT INTO labs (id, name) VALUES (1, 'Lab A');

-- STAFF_USERS
INSERT INTO staff_users (id, name, role, lab_id) VALUES (1, 'Alice', 'Technician', 1);

-- PATIENTS
INSERT INTO patients (id, name, dob, gender, contact, created_by_id, lab_id)
VALUES (1, 'John Doe', '1990-01-01', 'Male', 'john@example.com', 1, 1);

-- LAB_TESTS
INSERT INTO lab_tests (id, name, category, lab_id)
VALUES
    (1, 'Complete Blood Count (CBC)', 'Hematology', 1),
    (2, 'Blood Glucose', 'Chemistry', 1);

-- REQUISITIONS
INSERT INTO requisitions (id, patient_id, doctor_name, date, status, created_by_id, lab_id)
VALUES (1, 1, 'Dr. Smith', CURRENT_TIMESTAMP, 'PROCESSING', 1, 1);

INSERT INTO requisitions (id, patient_id, doctor_name, date, status, created_by_id, lab_id)
VALUES (2, 1, 'Dr. Smith', CURRENT_TIMESTAMP, 'PROCESSING', 1, 1);

-- REQUISITION_TESTS
INSERT INTO requisition_tests (requisition_id, test_id)
VALUES (1, 1), (1, 2);

-- INVENTORY
INSERT INTO inventory (id, name, category, quantity, low_stock_threshold, supplier, added_by_id, lab_id)
VALUES (1, 'Test Tubes', 'Consumables', 100, 10, 'LabSupplier', 1, 1);
