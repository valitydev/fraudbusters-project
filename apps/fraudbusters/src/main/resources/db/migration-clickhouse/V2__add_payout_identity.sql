ALTER TABLE fraud.withdrawal ON CLUSTER '{cluster}'
    ADD COLUMN IF NOT EXISTS partyId Nullable(String),
    ADD COLUMN IF NOT EXISTS walletId Nullable(String),
    ADD COLUMN IF NOT EXISTS customerId Nullable(String),
    ADD COLUMN IF NOT EXISTS userId Nullable(String),
    ADD COLUMN IF NOT EXISTS ip Nullable(String),
    ADD COLUMN IF NOT EXISTS fingerprint Nullable(String),
    ADD COLUMN IF NOT EXISTS email Nullable(String),
    ADD COLUMN IF NOT EXISTS phone Nullable(String);
