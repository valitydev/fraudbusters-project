-- Keep legacy payment rows and their keys intact.
CREATE TABLE wb_list.payout_row (
    id CHARACTER VARYING NOT NULL,
    value CHARACTER VARYING NOT NULL,
    CONSTRAINT payout_row_pkey PRIMARY KEY (id)
);
