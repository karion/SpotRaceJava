CREATE TABLE sp_location
(
    id          UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_sp_location PRIMARY KEY (id)
);

CREATE TABLE sp_spot
(
    id          UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    location_id UUID         NOT NULL,
    CONSTRAINT pk_sp_spot PRIMARY KEY (id)
);

ALTER TABLE sp_spot
    ADD CONSTRAINT FK_SP_SPOT_ON_LOCATION FOREIGN KEY (location_id) REFERENCES sp_location (id);
