CREATE TABLE sp_assignment
(
    id         UUID NOT NULL,
    user_id    UUID NOT NULL,
    spot_id    UUID NOT NULL,
    start_date date NOT NULL,
    end_date   date,
    note       VARCHAR(255),
    CONSTRAINT pk_sp_assignment PRIMARY KEY (id)
);

CREATE INDEX idx_assignment_spot_end ON sp_assignment (spot_id, end_date);

CREATE INDEX idx_assignment_spot_start ON sp_assignment (spot_id, start_date);

ALTER TABLE sp_assignment
    ADD CONSTRAINT FK_SP_ASSIGNMENT_ON_SPOT FOREIGN KEY (spot_id) REFERENCES sp_spot (id);

ALTER TABLE sp_assignment
    ADD CONSTRAINT FK_SP_ASSIGNMENT_ON_USER FOREIGN KEY (user_id) REFERENCES sp_user (id);