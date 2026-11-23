CREATE TABLE IF NOT EXISTS fraud.withdrawal_check ON CLUSTER '{cluster}' (
    timestamp Date,
    eventTimeHour UInt64,
    eventTime UInt64,
    id String,
    partyId String,
    walletId String,
    amount UInt64,
    currency String,
    cardToken Nullable(String),
    bin Nullable(String),
    lastDigits Nullable(String),
    paymentSystem Nullable(String),
    issuerCountry Nullable(String),
    customerId Nullable(String),
    userId Nullable(String),
    ip Nullable(String),
    fingerprint Nullable(String),
    email Nullable(String),
    phone Nullable(String),
    resultStatus String,
    checkedRule Nullable(String),
    checkedTemplate String
) ENGINE = ReplicatedMergeTree('/clickhouse/{cluster}/tables/{shard}/fraud/withdrawal_check', '{replica}')
PARTITION BY toYYYYMM(timestamp)
ORDER BY (eventTimeHour, partyId, walletId, resultStatus, id);
