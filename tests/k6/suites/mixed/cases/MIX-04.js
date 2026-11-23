import data from '../../../data/mixed/MIX-04.js';
import { expectEqual, expectSuccess } from '../../../lib/assert.js';
import { paymentSteps } from '../../payments/steps.js';
import { payoutSteps } from '../../payouts/steps.js';

export default {
  id: 'MIX-04',
  title: 'Конфигурации платежей и выплат не пересекаются',
  profiles: ['functional'],
  run(context) {
    const payments = paymentSteps(context);
    const payouts = payoutSteps(context);
    const { ids, management, payouts: payoutApi } = context;
    const { expected } = data;
    const templateId = ids.name('shared');
    payments.saveTemplate(templateId, data.paymentTemplate);
    payouts.saveTemplate(templateId, data.payoutTemplate);
    expectEqual(expectSuccess(management.filterPaymentTemplates({ searchValue: templateId, size: 10 }), 'payment search')
      .result.map((item) => item.template), [data.paymentTemplate.trim()], 'payment search returns the payment template');
    expectEqual(expectSuccess(payoutApi.filterPayoutTemplates({ searchValue: templateId, size: 10 }), 'payout search')
      .result.map((item) => item.template), [data.payoutTemplate.trim()], 'payout search returns the payout template');

    const partyId = ids.name('party');
    const shop = { partyId, shopId: ids.name('shop'), cardToken: ids.name('payment-card') };
    const wallet = { partyId, walletId: ids.name('wallet'), cardToken: ids.name('payout-card') };
    payments.bindTemplate(templateId, shop);
    payouts.bindTemplate(templateId, wallet);
    payments.awaitReady('payment template is applied', shop);
    payouts.awaitReady('payout template is applied', wallet);
    payments.awaitDecision('payment template is the payment one', () => payments.payment({
      ...shop, amount: data.paymentOnlyAmount }), expected.paymentOnlyAmount.payment);
    payouts.expectDecision(payouts.payout({ ...wallet, amount: data.paymentOnlyAmount }),
      expected.paymentOnlyAmount.payout, 'payout inspection does not use the payment template');

    payments.addListRow('black', 'CARD_TOKEN', shop.cardToken, shop);
    payouts.addListRow('black', 'CARD_TOKEN', wallet.cardToken, wallet);
    payments.awaitDecision('payment black list', () => payments.payment(shop), expected.ownList);
    payouts.awaitDecision('payout black list', () => payouts.payout(wallet), expected.ownList);
    payments.expectDecision(payments.payment({ ...shop, cardToken: wallet.cardToken }), expected.otherList,
      'payout black list does not touch payments');
    payouts.expectDecision(payouts.payout({ ...wallet, cardToken: shop.cardToken }), expected.otherList,
      'payment black list does not touch payouts');
  },
};
