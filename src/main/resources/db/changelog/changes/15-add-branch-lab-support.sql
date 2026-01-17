-- Add parent_lab_id to labs table to support branch labs hierarchy
ALTER TABLE labs ADD COLUMN parent_lab_id BIGINT;

-- Add foreign key constraint
ALTER TABLE labs 
ADD CONSTRAINT fk_labs_parent_lab 
FOREIGN KEY (parent_lab_id) REFERENCES labs(id);

-- Commentary: 
-- If parent_lab_id is NULL, the lab is either a standalone lab or a Main Lab that can have branches.
-- If parent_lab_id is NOT NULL, the lab is a Branch Lab.
