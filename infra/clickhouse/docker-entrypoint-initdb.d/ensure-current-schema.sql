-- Local schema additions from the owning service migrations.
-- V5__add_fields.sql
ALTER TABLE fraud.events_unique ADD COLUMN IF NOT EXISTS payerType String;
ALTER TABLE fraud.events_unique ADD COLUMN IF NOT EXISTS tokenProvider String;

ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS payerType String;
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS tokenProvider String;

ALTER TABLE fraud.refund ADD COLUMN IF NOT EXISTS payerType String;
ALTER TABLE fraud.refund ADD COLUMN IF NOT EXISTS tokenProvider String;

ALTER TABLE fraud.chargeback ADD COLUMN IF NOT EXISTS payerType String;
ALTER TABLE fraud.chargeback ADD COLUMN IF NOT EXISTS tokenProvider String;

-- V6__add_result_fields_payment.sql
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS checkedTemplate String;
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS checkedRule String;
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS resultStatus String;

ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS checkedResultsJson String;

-- V7__add_fields.sql
ALTER TABLE fraud.events_unique ADD COLUMN IF NOT EXISTS mobile UInt8;
ALTER TABLE fraud.events_unique ADD COLUMN IF NOT EXISTS recurrent UInt8;

ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS mobile UInt8;
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS recurrent UInt8;

-- V9__add_phone_category_card.sql
ALTER TABLE fraud.fraud_payment ADD COLUMN IF NOT EXISTS phone String;
ALTER TABLE fraud.fraud_payment ADD COLUMN IF NOT EXISTS cardCategory String;

ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS phone String;
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS cardCategory String;

-- V10__add_id_inspect_result.sql
ALTER TABLE fraud.events_unique ADD COLUMN IF NOT EXISTS id String;

-- V12__add_client_identity.sql
ALTER TABLE fraud.events_unique ADD COLUMN IF NOT EXISTS customerId Nullable(String);
ALTER TABLE fraud.events_unique ADD COLUMN IF NOT EXISTS userId Nullable(String);
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS customerId Nullable(String);
ALTER TABLE fraud.payment ADD COLUMN IF NOT EXISTS userId Nullable(String);
ALTER TABLE fraud.refund ADD COLUMN IF NOT EXISTS customerId Nullable(String);
ALTER TABLE fraud.refund ADD COLUMN IF NOT EXISTS userId Nullable(String);
ALTER TABLE fraud.chargeback ADD COLUMN IF NOT EXISTS customerId Nullable(String);
ALTER TABLE fraud.chargeback ADD COLUMN IF NOT EXISTS userId Nullable(String);
ALTER TABLE fraud.fraud_payment ADD COLUMN IF NOT EXISTS customerId Nullable(String);
ALTER TABLE fraud.fraud_payment ADD COLUMN IF NOT EXISTS userId Nullable(String);
