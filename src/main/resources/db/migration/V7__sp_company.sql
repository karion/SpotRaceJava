CREATE TABLE sp_company
(
    id UUID NOT NULL PRIMARY KEY,

    name VARCHAR(250) NOT NULL CHECK (length(trim(name)) > 0),
    normalized_name VARCHAR(250) NOT NULL CHECK (length(trim(normalized_name)) > 0),

    active BOOLEAN NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX uq_company__normalized_name_active
ON sp_company (normalized_name)
WHERE active = true;
