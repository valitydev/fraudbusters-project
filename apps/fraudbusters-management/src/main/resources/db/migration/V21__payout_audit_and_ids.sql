-- Payout configuration keeps who changed it and when, as the payment tables do.
ALTER TABLE af.payout_template ADD COLUMN last_update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
ALTER TABLE af.payout_template ADD COLUMN modified_by_user CHARACTER VARYING;

ALTER TABLE af.payout_group ADD COLUMN last_update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
ALTER TABLE af.payout_group ADD COLUMN modified_by_user CHARACTER VARYING;

ALTER TABLE af.payout_reference ADD COLUMN last_update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
ALTER TABLE af.payout_reference ADD COLUMN modified_by_user CHARACTER VARYING;

ALTER TABLE af.payout_group_reference ADD COLUMN last_update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
ALTER TABLE af.payout_group_reference ADD COLUMN modified_by_user CHARACTER VARYING;

ALTER TABLE af.payout_list_record ADD COLUMN insert_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc');
ALTER TABLE af.payout_list_record ADD COLUMN created_by_user CHARACTER VARYING;

-- Rows are paged and removed by id, so list values never reach a URL; the id comes from the application.
ALTER TABLE af.payout_reference ADD COLUMN id CHARACTER VARYING NOT NULL;
ALTER TABLE af.payout_reference ADD CONSTRAINT payout_reference_uniq_id UNIQUE (id);

ALTER TABLE af.payout_group_reference ADD COLUMN id CHARACTER VARYING NOT NULL;
ALTER TABLE af.payout_group_reference ADD CONSTRAINT payout_group_reference_uniq_id UNIQUE (id);

ALTER TABLE af.payout_list_record ADD COLUMN id CHARACTER VARYING NOT NULL;
ALTER TABLE af.payout_list_record ADD CONSTRAINT payout_list_record_uniq_id UNIQUE (id);
