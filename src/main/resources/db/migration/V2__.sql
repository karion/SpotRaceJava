ALTER TABLE spotracer_user
    ADD COLUMN passwordHash VARCHAR(255) NOT NULL DEFAULT 'temp_hash';
