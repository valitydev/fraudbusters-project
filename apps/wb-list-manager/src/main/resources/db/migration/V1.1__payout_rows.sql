-- Keep legacy payment rows and their keys intact.
CREATE TABLE wb_list.payout_row (
    id CHARACTER VARYING PRIMARY KEY,
    value CHARACTER VARYING NOT NULL
);
