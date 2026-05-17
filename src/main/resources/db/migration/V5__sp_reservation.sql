CREATE TABLE sp_reservation
(
    id UUID NOT NULL PRIMARY KEY,

    user_id UUID NOT NULL,
    spot_id UUID NOT NULL,

    date DATE NOT NULL,

    CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id)
            REFERENCES sp_user (id),

    CONSTRAINT fk_reservation_spot
        FOREIGN KEY (spot_id)
            REFERENCES sp_spot (id),

    CONSTRAINT uk_reservation_spot_date
        UNIQUE (spot_id, date)
);

CREATE INDEX idx_reservation_user_id
    ON sp_reservation (user_id);

CREATE INDEX idx_reservation_spot_id
    ON sp_reservation (spot_id);

CREATE INDEX idx_reservation_date
    ON sp_reservation (date);