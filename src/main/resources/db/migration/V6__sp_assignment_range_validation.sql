CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE sp_assignment
ADD CONSTRAINT ex_sp_assignment_no_overlap
EXCLUDE USING gist (
    spot_id WITH =,
    daterange(
        start_date,
        end_date,
        '[]'
    ) WITH &&
);