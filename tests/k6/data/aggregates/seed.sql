-- Payment history of the aggregates suite: {rows} payments spread over the last {days} days.
-- Entities follow data/aggregates/shape.json and the same formulas as suites/aggregates/traffic.js:
--   card       floor(r^3 * cards): a few hot cards and a long tail;
--   home       mix(card, salt) = ((card * 2654435761 + salt) % 2^32) / 2^32, the stable party, shop, email and
--              fingerprint of a card; a payment uses them with the home* shares, otherwise random ones;
--   party      floor(mix(card, 0)^2 * parties): large and small merchants.
INSERT INTO fraud.payment (timestamp, eventTime, eventTimeHour, id, email, ip, fingerprint, bin, lastDigits, cardToken,
    paymentSystem, paymentTool, terminal, providerId, bankCountry, partyId, shopId, amount, currency, status,
    errorReason, errorCode, paymentCountry, payerType, tokenProvider, mobile, recurrent, phone)
SELECT
    toDate(eventTime),
    eventTime,
    intDiv(eventTime, 3600) * 3600 * 1000,
    concat('seed-payment-', toString(number)),
    concat('seed-', toString(email), '@seed.test'),
    IPv4NumToString(toUInt32(167772160 + toUInt64(pow(r_ip, 2) * {ips:UInt32}))),
    concat('seed-fp-', toString(fingerprint)),
    '400000',
    leftPad(toString(card % 10000), 4, '0'),
    concat('seed-card-', toString(card)),
    'visa',
    'bank_card',
    'seed-terminal',
    'seed-provider',
    'RUS',
    concat('seed-party-', leftPad(toString(party), 2, '0')),
    concat('seed-shop-', leftPad(toString(party), 2, '0'), '-', leftPad(toString(shop), 2, '0')),
    toUInt64(exp(log(100) + r_amount * (log(300000) - log(100)))),
    'RUB',
    status,
    if(status = 'failed', 'seed', ''),
    if(status = 'failed', ['insufficient_funds', 'insufficient_funds', 'card_expired', 'rejected_by_issuer',
        'preauthorization_failed', 'timeout'][1 + toUInt8(r_error * 6)], ''),
    'RUS',
    'payment_resource',
    'UNKNOWN',
    0,
    0,
    '70000000000'
FROM
(
    SELECT
        number,
        card,
        toUInt64(toUnixTimestamp(now()) - toUInt64(cityHash64(number, 1) / pow(2, 64) * {days:UInt32} * 86400))
            AS eventTime,
        cityHash64(number, 3) / pow(2, 64) < {homeShop:Float64} AS home,
        if(home, toUInt64(pow(((card * 2654435761) % 4294967296) / 4294967296, 2) * {parties:UInt32}),
            toUInt64(cityHash64(number, 4) / pow(2, 64) * {parties:UInt32})) AS party,
        if(home, toUInt64(((card * 2654435761 + 7) % 4294967296) / 4294967296 * {shops:UInt32}),
            toUInt64(cityHash64(number, 5) / pow(2, 64) * {shops:UInt32})) AS shop,
        if(cityHash64(number, 6) / pow(2, 64) < {homeEmail:Float64}, card % {emails:UInt32},
            toUInt64(cityHash64(number, 7) / pow(2, 64) * {emails:UInt32})) AS email,
        if(cityHash64(number, 8) / pow(2, 64) < {homeFingerprint:Float64}, card % {fingerprints:UInt32},
            toUInt64(cityHash64(number, 9) / pow(2, 64) * {fingerprints:UInt32})) AS fingerprint,
        cityHash64(number, 10) / pow(2, 64) AS r_ip,
        cityHash64(number, 11) / pow(2, 64) AS r_amount,
        cityHash64(number, 13) / pow(2, 64) AS r_error,
        multiIf(cityHash64(number, 12) / pow(2, 64) AS r_status < 0.70, 'captured', r_status < 0.88, 'failed',
            r_status < 0.94, 'processed', r_status < 0.98, 'pending', 'cancelled') AS status
    FROM
    (
        SELECT number, toUInt64(pow(cityHash64(number, 2) / pow(2, 64), 3) * {cards:UInt32}) AS card
        FROM numbers({rows:UInt64})
    )
);
