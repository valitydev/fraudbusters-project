RENAME TABLE fraud.withdrawal TO fraud.payout,
    fraud.withdrawal_check TO fraud.payout_check
    ON CLUSTER '{cluster}';
