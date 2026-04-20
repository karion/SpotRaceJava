ALTER TABLE sp_user
    ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT 'temp_hash';
