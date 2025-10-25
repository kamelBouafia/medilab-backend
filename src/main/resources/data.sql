-- LAB
INSERT INTO labs (id, name) VALUES ('labA', 'Lab A');

-- STAFF
INSERT INTO staff_users (id, name, role, lab_id) VALUES ('S001', 'Alice', 'Technician', 'labA');

-- PATIENT
INSERT INTO patients (id, name, dob, gender, contact, created_by_id, lab_id)
VALUES ('P001', 'John Doe', '1990-01-01', 'Male', 'john@example.com', 'S001', 'labA');

-- LAB TESTS
INSERT INTO lab_tests (id, name, category, lab_id)
VALUES
    ('T01', 'Complete Blood Count (CBC)', 'Hematology', 'labA'),
    ('T02', 'Blood Glucose', 'Chemistry', 'labA');

-- REQUISITION
INSERT INTO requisitions (id, patient_id, doctor_name, date, status, created_by_id, lab_id)
VALUES ('R1001', 'P001', 'Dr. Smith', CURRENT_TIMESTAMP, 'Completed', 'S001', 'labA');

-- REQUISITION TESTS
INSERT INTO requisition_tests (requisition_id, test_id)
VALUES ('R1001', 'T01'), ('R1001', 'T02');

-- INVENTORY (optional)
INSERT INTO inventory (id, name, category, quantity, low_stock_threshold, supplier, added_by_id, lab_id)
VALUES ('I001', 'Test Tubes', 'Consumables', 100, 10, 'LabSupplier', 'S001', 'labA');
