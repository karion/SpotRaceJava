CREATE TABLE spotracer_user
(
    id        UUID         NOT NULL,
    email     VARCHAR(255) NOT NULL,
    firstname VARCHAR(255),
    lastname  VARCHAR(255),
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE spotracer_user_roles
(
    user_id UUID NOT NULL,
    role    VARCHAR(255)
);

ALTER TABLE spotracer_user
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE spotracer_user_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES spotracer_user (id);