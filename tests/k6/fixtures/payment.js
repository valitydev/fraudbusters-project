export function buildPayment({
  id, partyId, shopId, amount = 5000, currency = 'RUB', ip = '10.0.0.1', email, phone = '70000000000',
  fingerprint, cardToken, userId, customerId, payerType = 'payment_resource', tokenMobileProvider,
  terminalId = 'k6-terminal', providerCountry = 'RUS', bin = '400000', lastDigits = '0000', bankCountry = 'RUS',
}) {
  return {
    id,
    customer: {
      customerId,
      userId,
      name: 'k6 test',
      device: { ip, fingerprint },
      contact: { email, phone },
    },
    cash: { amount, currency },
    paymentResource: {
      type: 'BankCard',
      bin,
      lastDigits,
      countryCode: bankCountry,
      cardToken,
      bankName: 'k6 bank',
      cardType: 'debit',
      paymentSystem: 'visa',
    },
    merchant: {
      id: partyId,
      shop: { id: shopId, name: 'k6 shop', category: 'k6', location: 'k6' },
    },
    provider: { id: 'k6-provider', terminalId, country: providerCountry },
    payerType,
    tokenMobileProvider,
    createdAt: new Date().toISOString(),
    description: 'k6 regression',
  };
}

export function managementPayment({
  id, partyId, shopId, amount = 5000, currency = 'RUB', cardToken, email, ip = '10.0.0.1', fingerprint,
  eventTime, status = 'captured',
}) {
  return {
    id,
    paymentId: id,
    eventTime,
    merchantInfo: { partyId, shopId },
    amount,
    currency,
    cardToken,
    bin: '400000',
    lastDigits: '0000',
    paymentSystem: 'visa',
    paymentCountry: 'RUS',
    status,
    payerType: 'payment_resource',
    mobile: false,
    recurrent: false,
    clientInfo: { email, ip, fingerprint },
    provider: { providerId: 'k6-provider', terminalId: 'k6-terminal', country: 'RUS' },
  };
}

export function paymentChange(payment, paymentStatus, error) {
  return { payment, paymentStatus, eventTime: new Date().toISOString(), error };
}

function operationBase(payment) {
  return {
    paymentId: payment.id,
    eventTime: new Date().toISOString(),
    merchant: payment.merchant,
    paymentResource: payment.paymentResource,
    cash: payment.cash,
    provider: payment.provider,
    customer: payment.customer,
    payerType: payment.payerType,
  };
}

export function buildRefund(payment, id, status = 'succeeded') {
  return { id, ...operationBase(payment), status };
}

export function buildChargeback(payment, id, status = 'accepted') {
  return { id, ...operationBase(payment), status, category: 'fraud', chargebackCode: 'k6' };
}
