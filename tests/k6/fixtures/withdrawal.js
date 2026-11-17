import { TYPE } from '../lib/thrift.js';

export function buildWithdrawal({
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

export function withdrawalContextFields(withdrawal) {
  const clientIdentity = withdrawal.customerId
    ? field(9, TYPE.STRUCT, [stringField(1, withdrawal.customerId), stringField(2, withdrawal.userId)])
    : undefined;
  const body = [
    field(1, TYPE.STRUCT, [
      field(1, TYPE.I64, withdrawal.amount),
      field(2, TYPE.STRUCT, [stringField(1, withdrawal.currency)]),
    ]),
    stringField(5, withdrawal.createdAt),
    field(2, TYPE.STRUCT, [field(1, TYPE.STRUCT, [
      stringField(1, withdrawal.cardToken),
      field(14, TYPE.STRUCT, [stringField(1, withdrawal.paymentSystem)]),
      stringField(3, withdrawal.bin),
      stringField(4, withdrawal.lastDigits),
    ])]),
    field(7, TYPE.STRUCT, [stringField(1, withdrawal.phone), stringField(2, withdrawal.email)]),
    field(8, TYPE.STRUCT, [stringField(1, withdrawal.ip), stringField(2, withdrawal.fingerprint)]),
    clientIdentity,
  ].filter(Boolean);
  return [
    field(1, TYPE.STRUCT, [field(1, TYPE.STRUCT, [stringField(1, withdrawal.partyId)])]),
    field(2, TYPE.STRUCT, [stringField(1, withdrawal.walletId)]),
    stringField(3, withdrawal.id),
    { id: 4, ...struct(body) },
  ];
}

export function withdrawalRecord(withdrawal, status, errorCode) {
  return {
    id: withdrawal.id,
    eventTime: new Date().toISOString(),
    paymentResource: {
      type: 'BankCard',
      cardToken: withdrawal.cardToken,
      bin: withdrawal.bin,
      lastDigits: withdrawal.lastDigits,
      paymentSystem: withdrawal.paymentSystem,
      countryCode: 'RUS',
      bankName: 'k6 bank',
      cardType: 'debit',
    },
    account: { id: withdrawal.walletId, currency: withdrawal.currency },
    cash: { amount: withdrawal.amount, currency: withdrawal.currency },
    provider: { id: 'k6-provider', terminalId: withdrawal.terminalId, country: withdrawal.providerCountry },
    status,
    error: errorCode ? { errorCode, errorMessage: 'k6' } : undefined,
    partyId: withdrawal.partyId,
    walletId: withdrawal.walletId,
    customer: {
      customerId: withdrawal.customerId,
      userId: withdrawal.userId,
      device: { ip: withdrawal.ip, fingerprint: withdrawal.fingerprint },
      contact: { email: withdrawal.email, phone: withdrawal.phone },
    },
  };
}
