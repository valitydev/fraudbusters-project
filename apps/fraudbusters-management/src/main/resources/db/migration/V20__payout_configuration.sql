-- Payout configuration is projected separately; all existing payment tables remain intact.
CREATE TABLE af.payout_template (
    id VARCHAR NOT NULL,
    template TEXT NOT NULL,
    CONSTRAINT payout_template_pkey PRIMARY KEY (id)
);

CREATE TABLE af.payout_group (
    group_id VARCHAR NOT NULL,
    template_id VARCHAR NOT NULL,
    priority BIGINT NOT NULL,
    CONSTRAINT payout_group_pkey PRIMARY KEY (group_id, template_id)
);

CREATE TABLE af.payout_reference (
    party_id VARCHAR NOT NULL DEFAULT '',
    wallet_id VARCHAR NOT NULL DEFAULT '',
    template_id VARCHAR NOT NULL,
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT payout_reference_pkey PRIMARY KEY (party_id, wallet_id, is_global),
    CONSTRAINT payout_reference_wallet_requires_party_check CHECK (wallet_id = '' OR party_id <> ''),
    CONSTRAINT payout_reference_global_without_scope_check CHECK (NOT is_global OR (party_id = '' AND wallet_id = ''))
);

CREATE TABLE af.payout_group_reference (
    party_id VARCHAR NOT NULL DEFAULT '',
    wallet_id VARCHAR NOT NULL DEFAULT '',
    group_id VARCHAR NOT NULL,
    CONSTRAINT payout_group_reference_pkey PRIMARY KEY (party_id, wallet_id),
    CONSTRAINT payout_group_reference_wallet_requires_party_check CHECK (wallet_id = '' OR party_id <> '')
);

CREATE TABLE af.payout_list_record (
    party_id VARCHAR NOT NULL DEFAULT '',
    wallet_id VARCHAR NOT NULL DEFAULT '',
    list_type af.list_type NOT NULL,
    list_name VARCHAR NOT NULL,
    value VARCHAR NOT NULL,
    row_info TEXT,
    CONSTRAINT payout_list_record_pkey PRIMARY KEY (party_id, wallet_id, list_type, list_name, value),
    CONSTRAINT payout_list_record_wallet_requires_party_check CHECK (wallet_id = '' OR party_id <> '')
);
