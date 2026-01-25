-- Add patient_price column to agreement_test_prices table
ALTER TABLE agreement_test_prices ADD COLUMN patient_price DECIMAL(10, 2);
COMMENT ON COLUMN agreement_test_prices.patient_price IS 'The price the requesting lab intends to charge its patients for this test';
