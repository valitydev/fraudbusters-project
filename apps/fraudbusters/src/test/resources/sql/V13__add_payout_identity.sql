-- Additive extension: existing withdrawals retain NULL for unavailable identity fields.
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS partyId Nullable(String);
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS walletId Nullable(String);
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS customerId Nullable(String);
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS userId Nullable(String);
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS ip Nullable(String);
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS fingerprint Nullable(String);
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS email Nullable(String);
ALTER TABLE fraud.withdrawal ADD COLUMN IF NOT EXISTS phone Nullable(String);
