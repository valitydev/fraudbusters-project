import { TYPE } from '../lib/thrift.js';

export function buildPayout({
  id, partyId, walletId, amount = 5000, currency = 'RUB', cardToken, bin = '400000', lastDigits = '0000',
  paymentSystem = 'visa', email, phone = '70000000000', ip = '10.0.0.2', fingerprint, customerId, userId,
  terminalId = 'k6-terminal', providerCountry = 'RUS',
}) {
  return {
    id, partyId, walletId, amount, currency, cardToken, bin, lastDigits, paymentSystem, email, phone, ip,
    fingerprint, customerId, userId, terminalId, providerCountry, createdAt: new Date().toISOString(),
  };
}

const struct = (fields) => ({ type: TYPE.STRUCT, value: fields });
const field = (id, type, value) => ({ id, type, value });
const stringField = (id, value) => field(id, TYPE.STRING, value);

export function payoutContextFields(payout) {
  const clientIdentity = payout.customerId
    ? field(9, TYPE.STRUCT, [stringField(1, payout.customerId), stringField(2, payout.userId)])
    : undefined;
  const body = [
    field(1, TYPE.STRUCT, [
      field(1, TYPE.I64, payout.amount),
      field(2, TYPE.STRUCT, [stringField(1, payout.currency)]),
    ]),
    stringField(5, payout.createdAt),
    field(2, TYPE.STRUCT, [field(1, TYPE.STRUCT, [
      stringField(1, payout.cardToken),
      field(14, TYPE.STRUCT, [stringField(1, payout.paymentSystem)]),
      stringField(3, payout.bin),
      stringField(4, payout.lastDigits),
    ])]),
    field(7, TYPE.STRUCT, [stringField(1, payout.phone), stringField(2, payout.email)]),
    field(8, TYPE.STRUCT, [stringField(1, payout.ip), stringField(2, payout.fingerprint)]),
    clientIdentity,
  ].filter(Boolean);
  return [
    field(1, TYPE.STRUCT, [field(1, TYPE.STRUCT, [stringField(1, payout.partyId)])]),
    field(2, TYPE.STRUCT, [stringField(1, payout.walletId)]),
    stringField(3, payout.id),
    { id: 4, ...struct(body) },
  ];
}

export function payoutRecord(payout, status, errorCode) {
  return {
    id: payout.id,
    eventTime: new Date().toISOString(),
    paymentResource: {
      type: 'BankCard',
      cardToken: payout.cardToken,
      bin: payout.bin,
      lastDigits: payout.lastDigits,
      paymentSystem: payout.paymentSystem,
      countryCode: 'RUS',
      bankName: 'k6 bank',
      cardType: 'debit',
    },
    account: { id: payout.walletId, currency: payout.currency },
    cash: { amount: payout.amount, currency: payout.currency },
    provider: { id: 'k6-provider', terminalId: payout.terminalId, country: payout.providerCountry },
    status,
    error: errorCode ? { errorCode, errorMessage: 'k6' } : undefined,
    partyId: payout.partyId,
    walletId: payout.walletId,
    customer: {
      customerId: payout.customerId,
      userId: payout.userId,
      device: { ip: payout.ip, fingerprint: payout.fingerprint },
      contact: { email: payout.email, phone: payout.phone },
    },
  };
}
