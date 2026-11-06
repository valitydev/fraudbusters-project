-- Payout configuration is projected separately; all existing payment tables remain intact.
CREATE TABLE af.payout_template (
    id VARCHAR PRIMARY KEY,
    template TEXT NOT NULL
);

CREATE TABLE af.payout_group (
    group_id VARCHAR NOT NULL,
    template_id VARCHAR NOT NULL,
    priority BIGINT NOT NULL,
    PRIMARY KEY (group_id, template_id)
);

CREATE TABLE af.payout_reference (
    party_id VARCHAR NOT NULL DEFAULT '',
    wallet_id VARCHAR NOT NULL DEFAULT '',
    template_id VARCHAR NOT NULL,
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (party_id, wallet_id, is_global),
    CHECK (wallet_id = '' OR party_id <> ''),
    CHECK (NOT is_global OR (party_id = '' AND wallet_id = ''))
);

CREATE TABLE af.payout_group_reference (
    party_id VARCHAR NOT NULL DEFAULT '',
    wallet_id VARCHAR NOT NULL DEFAULT '',
    group_id VARCHAR NOT NULL,
    PRIMARY KEY (party_id, wallet_id),
    CHECK (wallet_id = '' OR party_id <> '')
);

CREATE TABLE af.payout_list_record (
    party_id VARCHAR NOT NULL DEFAULT '',
    wallet_id VARCHAR NOT NULL DEFAULT '',
    list_type af.list_type NOT NULL,
    list_name VARCHAR NOT NULL,
    value VARCHAR NOT NULL,
    row_info TEXT,
    PRIMARY KEY (party_id, wallet_id, list_type, list_name, value),
    CHECK (wallet_id = '' OR party_id <> '')
);
