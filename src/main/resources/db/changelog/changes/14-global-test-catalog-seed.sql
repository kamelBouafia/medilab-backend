-- liquibase formatted sql
-- changeset ak_bo:14-global-test-catalog-seed runOnChange:true

-- =====================================================
-- HEMATOLOGY TESTS
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('CBC', 'HEMATOLOGY', 'panel', 'Complete Blood Count - comprehensive blood analysis', NULL, NULL, NULL, NULL),
('WBC', 'HEMATOLOGY', 'x10^9/L', 'White Blood Cell Count', 4.5, 11.0, 2.0, 30.0),
('RBC', 'HEMATOLOGY', 'x10^12/L', 'Red Blood Cell Count', 4.5, 5.5, 3.0, 7.0),
('HGB', 'HEMATOLOGY', 'g/dL', 'Hemoglobin', 12.0, 17.5, 7.0, 20.0),
('HCT', 'HEMATOLOGY', '%', 'Hematocrit', 36.0, 50.0, 20.0, 60.0),
('PLT', 'HEMATOLOGY', 'x10^9/L', 'Platelet Count', 150.0, 400.0, 50.0, 1000.0),
('MCV', 'HEMATOLOGY', 'fL', 'Mean Corpuscular Volume', 80.0, 100.0, 70.0, 115.0),
('MCH', 'HEMATOLOGY', 'pg', 'Mean Corpuscular Hemoglobin', 27.0, 33.0, 24.0, 36.0),
('MCHC', 'HEMATOLOGY', 'g/dL', 'Mean Corpuscular Hemoglobin Concentration', 32.0, 36.0, 28.0, 38.0),
('RDW', 'HEMATOLOGY', '%', 'Red Cell Distribution Width', 11.5, 14.5, 10.0, 20.0),
('ESR', 'HEMATOLOGY', 'mm/hr', 'Erythrocyte Sedimentation Rate', 0.0, 20.0, NULL, 100.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- CHEMISTRY / METABOLIC TESTS
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('GLU', 'CHEMISTRY', 'mg/dL', 'Glucose (Fasting)', 70.0, 100.0, 40.0, 500.0),
('GLU-R', 'CHEMISTRY', 'mg/dL', 'Glucose (Random)', 70.0, 140.0, 40.0, 500.0),
('HBA1C', 'CHEMISTRY', '%', 'Hemoglobin A1c', 4.0, 5.6, NULL, 14.0),
('BUN', 'CHEMISTRY', 'mg/dL', 'Blood Urea Nitrogen', 7.0, 20.0, 2.0, 100.0),
('CREAT', 'CHEMISTRY', 'mg/dL', 'Creatinine', 0.7, 1.3, 0.4, 10.0),
('eGFR', 'CHEMISTRY', 'mL/min/1.73m2', 'Estimated Glomerular Filtration Rate', 90.0, 120.0, 15.0, NULL),
('UA', 'CHEMISTRY', 'mg/dL', 'Uric Acid', 3.5, 7.2, 2.0, 12.0),
('NA', 'CHEMISTRY', 'mEq/L', 'Sodium', 136.0, 145.0, 120.0, 160.0),
('K', 'CHEMISTRY', 'mEq/L', 'Potassium', 3.5, 5.0, 2.5, 6.5),
('CL', 'CHEMISTRY', 'mEq/L', 'Chloride', 98.0, 106.0, 80.0, 120.0),
('CO2', 'CHEMISTRY', 'mEq/L', 'Carbon Dioxide (Bicarbonate)', 23.0, 29.0, 10.0, 40.0),
('CA', 'CHEMISTRY', 'mg/dL', 'Calcium', 8.6, 10.3, 6.0, 14.0),
('MG', 'CHEMISTRY', 'mg/dL', 'Magnesium', 1.7, 2.2, 1.0, 4.0),
('PHOS', 'CHEMISTRY', 'mg/dL', 'Phosphorus', 2.5, 4.5, 1.0, 8.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- LIVER FUNCTION TESTS
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('AST', 'CHEMISTRY', 'U/L', 'Aspartate Aminotransferase (SGOT)', 10.0, 40.0, NULL, 1000.0),
('ALT', 'CHEMISTRY', 'U/L', 'Alanine Aminotransferase (SGPT)', 7.0, 56.0, NULL, 1000.0),
('ALP', 'CHEMISTRY', 'U/L', 'Alkaline Phosphatase', 44.0, 147.0, NULL, 1000.0),
('GGT', 'CHEMISTRY', 'U/L', 'Gamma-Glutamyl Transferase', 9.0, 48.0, NULL, 500.0),
('TBIL', 'CHEMISTRY', 'mg/dL', 'Total Bilirubin', 0.1, 1.2, NULL, 15.0),
('DBIL', 'CHEMISTRY', 'mg/dL', 'Direct Bilirubin', 0.0, 0.3, NULL, 10.0),
('ALB', 'CHEMISTRY', 'g/dL', 'Albumin', 3.5, 5.0, 2.0, 6.0),
('TP', 'CHEMISTRY', 'g/dL', 'Total Protein', 6.0, 8.3, 4.0, 10.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- LIPID PANEL
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('CHOL', 'CHEMISTRY', 'mg/dL', 'Total Cholesterol', 0.0, 200.0, NULL, 400.0),
('HDL', 'CHEMISTRY', 'mg/dL', 'HDL Cholesterol (Good)', 40.0, 60.0, 20.0, NULL),
('LDL', 'CHEMISTRY', 'mg/dL', 'LDL Cholesterol (Bad)', 0.0, 100.0, NULL, 250.0),
('TRIG', 'CHEMISTRY', 'mg/dL', 'Triglycerides', 0.0, 150.0, NULL, 500.0),
('VLDL', 'CHEMISTRY', 'mg/dL', 'VLDL Cholesterol', 5.0, 40.0, NULL, 100.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- THYROID FUNCTION
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('TSH', 'CHEMISTRY', 'mIU/L', 'Thyroid Stimulating Hormone', 0.4, 4.0, 0.1, 50.0),
('T3', 'CHEMISTRY', 'ng/dL', 'Triiodothyronine', 80.0, 200.0, 40.0, 400.0),
('T4', 'CHEMISTRY', 'mcg/dL', 'Thyroxine', 4.5, 12.0, 2.0, 20.0),
('FT4', 'CHEMISTRY', 'ng/dL', 'Free Thyroxine', 0.8, 1.8, 0.4, 5.0),
('FT3', 'CHEMISTRY', 'pg/mL', 'Free Triiodothyronine', 2.3, 4.2, 1.0, 8.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- COAGULATION
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('PT', 'HEMATOLOGY', 'seconds', 'Prothrombin Time', 11.0, 13.5, 8.0, 30.0),
('INR', 'HEMATOLOGY', 'ratio', 'International Normalized Ratio', 0.8, 1.2, 0.5, 5.0),
('PTT', 'HEMATOLOGY', 'seconds', 'Partial Thromboplastin Time', 25.0, 35.0, 20.0, 100.0),
('APTT', 'HEMATOLOGY', 'seconds', 'Activated Partial Thromboplastin Time', 25.0, 35.0, 20.0, 100.0),
('FIB', 'HEMATOLOGY', 'mg/dL', 'Fibrinogen', 200.0, 400.0, 100.0, 700.0),
('DIMER', 'HEMATOLOGY', 'ng/mL', 'D-Dimer', 0.0, 500.0, NULL, 5000.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- URINALYSIS
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('UA-PH', 'URINALYSIS', 'pH', 'Urine pH', 4.5, 8.0, 4.0, 9.0),
('UA-SG', 'URINALYSIS', 'sg', 'Urine Specific Gravity', 1.005, 1.030, 1.001, 1.040),
('UA-PRO', 'URINALYSIS', 'mg/dL', 'Urine Protein', 0.0, 14.0, NULL, 300.0),
('UA-GLU', 'URINALYSIS', 'mg/dL', 'Urine Glucose', 0.0, 15.0, NULL, 1000.0),
('UA-KET', 'URINALYSIS', 'mg/dL', 'Urine Ketones', 0.0, 0.0, NULL, NULL),
('UA-BIL', 'URINALYSIS', 'mg/dL', 'Urine Bilirubin', 0.0, 0.0, NULL, NULL),
('UA-BLD', 'URINALYSIS', 'cells/hpf', 'Urine Blood', 0.0, 0.0, NULL, NULL),
('UA-NIT', 'URINALYSIS', 'result', 'Urine Nitrite', NULL, NULL, NULL, NULL),
('UA-WBC', 'URINALYSIS', 'cells/hpf', 'Urine WBC', 0.0, 5.0, NULL, 50.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- CARDIAC MARKERS
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('TROP-I', 'CHEMISTRY', 'ng/mL', 'Troponin I', 0.0, 0.04, NULL, 0.5),
('TROP-T', 'CHEMISTRY', 'ng/mL', 'Troponin T', 0.0, 0.01, NULL, 0.1),
('BNP', 'CHEMISTRY', 'pg/mL', 'B-type Natriuretic Peptide', 0.0, 100.0, NULL, 500.0),
('CK', 'CHEMISTRY', 'U/L', 'Creatine Kinase', 30.0, 200.0, NULL, 1000.0),
('CK-MB', 'CHEMISTRY', 'ng/mL', 'Creatine Kinase-MB', 0.0, 5.0, NULL, 25.0),
('LDH', 'CHEMISTRY', 'U/L', 'Lactate Dehydrogenase', 140.0, 280.0, NULL, 1000.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- INFLAMMATORY MARKERS
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('CRP', 'CHEMISTRY', 'mg/L', 'C-Reactive Protein', 0.0, 3.0, NULL, 100.0),
('HS-CRP', 'CHEMISTRY', 'mg/L', 'High-Sensitivity CRP', 0.0, 1.0, NULL, 10.0),
('RF', 'IMMUNOLOGY', 'IU/mL', 'Rheumatoid Factor', 0.0, 14.0, NULL, 100.0),
('ANA', 'IMMUNOLOGY', 'titer', 'Antinuclear Antibody', NULL, NULL, NULL, NULL),
('FERR', 'CHEMISTRY', 'ng/mL', 'Ferritin', 12.0, 300.0, 5.0, 1000.0),
('IRON', 'CHEMISTRY', 'mcg/dL', 'Serum Iron', 60.0, 170.0, 30.0, 300.0),
('TIBC', 'CHEMISTRY', 'mcg/dL', 'Total Iron Binding Capacity', 250.0, 370.0, 150.0, 500.0)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- VITAMINS
-- =====================================================
INSERT INTO global_test_catalog (code, category, default_unit, description, default_min_val, default_max_val, default_critical_min, default_critical_max)
VALUES 
('VIT-D', 'CHEMISTRY', 'ng/mL', 'Vitamin D (25-Hydroxy)', 30.0, 100.0, 10.0, 150.0),
('VIT-B12', 'CHEMISTRY', 'pg/mL', 'Vitamin B12', 200.0, 900.0, 100.0, 2000.0),
('FOLATE', 'CHEMISTRY', 'ng/mL', 'Folate (Folic Acid)', 3.0, 17.0, 2.0, NULL)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- MULTILINGUAL NAMES (English)
-- =====================================================
INSERT INTO global_test_names (test_id, language_code, name)
SELECT id, 'en', 
    CASE code
        WHEN 'CBC' THEN 'Complete Blood Count'
        WHEN 'WBC' THEN 'White Blood Cell Count'
        WHEN 'RBC' THEN 'Red Blood Cell Count'
        WHEN 'HGB' THEN 'Hemoglobin'
        WHEN 'HCT' THEN 'Hematocrit'
        WHEN 'PLT' THEN 'Platelet Count'
        WHEN 'MCV' THEN 'Mean Corpuscular Volume'
        WHEN 'MCH' THEN 'Mean Corpuscular Hemoglobin'
        WHEN 'MCHC' THEN 'Mean Corpuscular Hemoglobin Concentration'
        WHEN 'RDW' THEN 'Red Cell Distribution Width'
        WHEN 'ESR' THEN 'Erythrocyte Sedimentation Rate'
        WHEN 'GLU' THEN 'Glucose (Fasting)'
        WHEN 'GLU-R' THEN 'Glucose (Random)'
        WHEN 'HBA1C' THEN 'Hemoglobin A1c'
        WHEN 'BUN' THEN 'Blood Urea Nitrogen'
        WHEN 'CREAT' THEN 'Creatinine'
        WHEN 'eGFR' THEN 'Estimated GFR'
        WHEN 'UA' THEN 'Uric Acid'
        WHEN 'NA' THEN 'Sodium'
        WHEN 'K' THEN 'Potassium'
        WHEN 'CL' THEN 'Chloride'
        WHEN 'CO2' THEN 'Carbon Dioxide'
        WHEN 'CA' THEN 'Calcium'
        WHEN 'MG' THEN 'Magnesium'
        WHEN 'PHOS' THEN 'Phosphorus'
        WHEN 'AST' THEN 'AST (SGOT)'
        WHEN 'ALT' THEN 'ALT (SGPT)'
        WHEN 'ALP' THEN 'Alkaline Phosphatase'
        WHEN 'GGT' THEN 'GGT'
        WHEN 'TBIL' THEN 'Total Bilirubin'
        WHEN 'DBIL' THEN 'Direct Bilirubin'
        WHEN 'ALB' THEN 'Albumin'
        WHEN 'TP' THEN 'Total Protein'
        WHEN 'CHOL' THEN 'Total Cholesterol'
        WHEN 'HDL' THEN 'HDL Cholesterol'
        WHEN 'LDL' THEN 'LDL Cholesterol'
        WHEN 'TRIG' THEN 'Triglycerides'
        WHEN 'VLDL' THEN 'VLDL Cholesterol'
        WHEN 'TSH' THEN 'TSH'
        WHEN 'T3' THEN 'T3'
        WHEN 'T4' THEN 'T4'
        WHEN 'FT4' THEN 'Free T4'
        WHEN 'FT3' THEN 'Free T3'
        WHEN 'PT' THEN 'Prothrombin Time'
        WHEN 'INR' THEN 'INR'
        WHEN 'PTT' THEN 'Partial Thromboplastin Time'
        WHEN 'APTT' THEN 'Activated PTT'
        WHEN 'FIB' THEN 'Fibrinogen'
        WHEN 'DIMER' THEN 'D-Dimer'
        WHEN 'TROP-I' THEN 'Troponin I'
        WHEN 'TROP-T' THEN 'Troponin T'
        WHEN 'BNP' THEN 'BNP'
        WHEN 'CK' THEN 'Creatine Kinase'
        WHEN 'CK-MB' THEN 'CK-MB'
        WHEN 'LDH' THEN 'LDH'
        WHEN 'CRP' THEN 'C-Reactive Protein'
        WHEN 'HS-CRP' THEN 'High-Sensitivity CRP'
        WHEN 'RF' THEN 'Rheumatoid Factor'
        WHEN 'ANA' THEN 'ANA'
        WHEN 'FERR' THEN 'Ferritin'
        WHEN 'IRON' THEN 'Serum Iron'
        WHEN 'TIBC' THEN 'TIBC'
        WHEN 'VIT-D' THEN 'Vitamin D'
        WHEN 'VIT-B12' THEN 'Vitamin B12'
        WHEN 'FOLATE' THEN 'Folate'
        ELSE description
    END
FROM global_test_catalog
ON CONFLICT (test_id, language_code) DO NOTHING;

-- =====================================================
-- MULTILINGUAL NAMES (French)
-- =====================================================
INSERT INTO global_test_names (test_id, language_code, name)
SELECT id, 'fr', 
    CASE code
        WHEN 'CBC' THEN 'Hémogramme Complet'
        WHEN 'WBC' THEN 'Leucocytes'
        WHEN 'RBC' THEN 'Érythrocytes'
        WHEN 'HGB' THEN 'Hémoglobine'
        WHEN 'HCT' THEN 'Hématocrite'
        WHEN 'PLT' THEN 'Plaquettes'
        WHEN 'GLU' THEN 'Glycémie à jeun'
        WHEN 'CREAT' THEN 'Créatinine'
        WHEN 'NA' THEN 'Sodium'
        WHEN 'K' THEN 'Potassium'
        WHEN 'CHOL' THEN 'Cholestérol Total'
        WHEN 'HDL' THEN 'Cholestérol HDL'
        WHEN 'LDL' THEN 'Cholestérol LDL'
        WHEN 'TRIG' THEN 'Triglycérides'
        WHEN 'TSH' THEN 'TSH'
        WHEN 'VIT-D' THEN 'Vitamine D'
        ELSE description
    END
FROM global_test_catalog
ON CONFLICT (test_id, language_code) DO NOTHING;

-- =====================================================
-- MULTILINGUAL NAMES (Arabic)
-- =====================================================
INSERT INTO global_test_names (test_id, language_code, name)
SELECT id, 'ar', 
    CASE code
        WHEN 'CBC' THEN 'تعداد الدم الكامل'
        WHEN 'WBC' THEN 'خلايا الدم البيضاء'
        WHEN 'RBC' THEN 'خلايا الدم الحمراء'
        WHEN 'HGB' THEN 'الهيموغلوبين'
        WHEN 'PLT' THEN 'الصفائح الدموية'
        WHEN 'GLU' THEN 'سكر الدم صائم'
        WHEN 'CREAT' THEN 'الكرياتينين'
        WHEN 'NA' THEN 'الصوديوم'
        WHEN 'K' THEN 'البوتاسيوم'
        WHEN 'CHOL' THEN 'الكوليسترول الكلي'
        WHEN 'TSH' THEN 'هرمون الغدة الدرقية'
        WHEN 'VIT-D' THEN 'فيتامين د'
        ELSE description
    END
FROM global_test_catalog
ON CONFLICT (test_id, language_code) DO NOTHING;

-- =====================================================
-- MULTILINGUAL NAMES (Spanish)
-- =====================================================
INSERT INTO global_test_names (test_id, language_code, name)
SELECT id, 'es', 
    CASE code
        WHEN 'CBC' THEN 'Hemograma Completo'
        WHEN 'WBC' THEN 'Leucocitos'
        WHEN 'RBC' THEN 'Eritrocitos'
        WHEN 'HGB' THEN 'Hemoglobina'
        WHEN 'PLT' THEN 'Plaquetas'
        WHEN 'GLU' THEN 'Glucosa en Ayunas'
        WHEN 'CREAT' THEN 'Creatinina'
        WHEN 'NA' THEN 'Sodio'
        WHEN 'K' THEN 'Potasio'
        WHEN 'CHOL' THEN 'Colesterol Total'
        WHEN 'HDL' THEN 'Colesterol HDL'
        WHEN 'LDL' THEN 'Colesterol LDL'
        WHEN 'TRIG' THEN 'Triglicéridos'
        WHEN 'TSH' THEN 'TSH'
        WHEN 'VIT-D' THEN 'Vitamina D'
        ELSE description
    END
FROM global_test_catalog
ON CONFLICT (test_id, language_code) DO NOTHING;
