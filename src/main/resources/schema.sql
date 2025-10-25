CREATE TABLE labs (
                      id VARCHAR(255) PRIMARY KEY,
                      name VARCHAR(255) NOT NULL
);

CREATE TABLE staff_users (
                             id VARCHAR(255) PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             role VARCHAR(50) NOT NULL,
                             lab_id VARCHAR(255) NOT NULL,
                             FOREIGN KEY (lab_id) REFERENCES labs(id)
);

CREATE TABLE patients (
                          id VARCHAR(255) PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          dob DATE NOT NULL,
                          gender VARCHAR(50) NOT NULL,
                          contact VARCHAR(255),
                          created_by_id VARCHAR(255) NOT NULL,
                          lab_id VARCHAR(255) NOT NULL,
                          FOREIGN KEY (lab_id) REFERENCES labs(id),
                          FOREIGN KEY (created_by_id) REFERENCES staff_users(id)
);

CREATE TABLE lab_tests (
                           id VARCHAR(255) PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           category VARCHAR(255),
                           lab_id VARCHAR(255) NOT NULL,
                           FOREIGN KEY (lab_id) REFERENCES labs(id)
);

CREATE TABLE requisitions (
                              id VARCHAR(255) PRIMARY KEY,
                              patient_id VARCHAR(255) NOT NULL,
                              doctor_name VARCHAR(255),
                              date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              status VARCHAR(50),
                              created_by_id VARCHAR(255) NOT NULL,
                              lab_id VARCHAR(255) NOT NULL,
                              FOREIGN KEY (patient_id) REFERENCES patients(id),
                              FOREIGN KEY (created_by_id) REFERENCES staff_users(id),
                              FOREIGN KEY (lab_id) REFERENCES labs(id)
);

CREATE TABLE requisition_tests (
                                   requisition_id VARCHAR(255),
                                   test_id VARCHAR(255),
                                   PRIMARY KEY (requisition_id, test_id),
                                   FOREIGN KEY (requisition_id) REFERENCES requisitions(id),
                                   FOREIGN KEY (test_id) REFERENCES lab_tests(id)
);

CREATE TABLE inventory (
                           id VARCHAR(255) PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           category VARCHAR(255),
                           quantity INT DEFAULT 0,
                           low_stock_threshold INT DEFAULT 0,
                           supplier VARCHAR(255),
                           added_by_id VARCHAR(255) NOT NULL,
                           lab_id VARCHAR(255) NOT NULL,
                           FOREIGN KEY (added_by_id) REFERENCES staff_users(id),
                           FOREIGN KEY (lab_id) REFERENCES labs(id)
);
