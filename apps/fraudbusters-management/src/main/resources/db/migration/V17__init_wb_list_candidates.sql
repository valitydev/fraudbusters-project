-- af.wb_list_candidate_batch --
CREATE TABLE af.wb_list_candidate_batch
(
    id          CHARACTER VARYING NOT NULL,
    source      CHARACTER VARYING NOT NULL,
    insert_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),

    CONSTRAINT wb_list_candidate_batch_pkey PRIMARY KEY (id)
);

-- af.wb_list_candidate --
CREATE TABLE af.wb_list_candidate
(
    id          BIGSERIAL         NOT NULL,
    batch_id    CHARACTER VARYING NOT NULL,
    party_id    CHARACTER VARYING,
    shop_id     CHARACTER VARYING,
    list_type   af.list_type      NOT NULL,
    list_name   CHARACTER VARYING NOT NULL,
    value       CHARACTER VARYING NOT NULL,
    source      CHARACTER VARYING NOT NULL,
    approved    BOOLEAN           NOT NULL,
    insert_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
    update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),

    CONSTRAINT wb_list_candidate_pkey PRIMARY KEY (id),
    CONSTRAINT fk_list_candidate FOREIGN KEY (batch_id) REFERENCES af.wb_list_candidate_batch (id),
    UNIQUE (list_name, value)
);
