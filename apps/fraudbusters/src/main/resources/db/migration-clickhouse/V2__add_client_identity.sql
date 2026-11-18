ALTER TABLE fraud.events_unique ON CLUSTER '{cluster}'
    ADD COLUMN IF NOT EXISTS customerId Nullable(String),
    ADD COLUMN IF NOT EXISTS userId Nullable(String);

ALTER TABLE fraud.payment ON CLUSTER '{cluster}'
    ADD COLUMN IF NOT EXISTS customerId Nullable(String),
    ADD COLUMN IF NOT EXISTS userId Nullable(String);

ALTER TABLE fraud.refund ON CLUSTER '{cluster}'
    ADD COLUMN IF NOT EXISTS customerId Nullable(String),
    ADD COLUMN IF NOT EXISTS userId Nullable(String);

ALTER TABLE fraud.chargeback ON CLUSTER '{cluster}'
    ADD COLUMN IF NOT EXISTS customerId Nullable(String),
    ADD COLUMN IF NOT EXISTS userId Nullable(String);

ALTER TABLE fraud.fraud_payment ON CLUSTER '{cluster}'
    ADD COLUMN IF NOT EXISTS customerId Nullable(String),
    ADD COLUMN IF NOT EXISTS userId Nullable(String);
