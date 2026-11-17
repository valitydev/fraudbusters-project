import data from '../../../data/payouts/PO-FL-12.js';
import { expectOutcomes } from '../../../lib/assert.js';
import { paymentSteps } from '../../payments/steps.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-12',
  title: 'Функции истории платежей в правиле выплаты',
  profiles: ['functional'],
  run(context) {
    const payouts = payoutSteps(context);
    const payments = paymentSteps(context);
    const { ids } = context;
    const walletId = ids.name('wallet');
    const variants = data.variants.map((variant) => ({ ...variant, partyId: ids.name(`party-${variant.role}`) }));
    variants.forEach((variant) => payouts.bindNewTemplate(variant.role, variant.template, { partyId: variant.partyId }));
    expectOutcomes(context, variants.map((variant) => {
      const withdrawal = { partyId: variant.partyId, walletId, ...variant.withdrawal };
      payouts.awaitReady(`${variant.role} is applied`, withdrawal);
      payments.loadAndAwait(variant.payments.map((overrides) => payments.payment({
        partyId: variant.partyId, shopId: ids.name('shop'), ...overrides,
      })));
      return { name: variant.role, actual: payouts.inspect(payouts.withdrawal(withdrawal)), expected: data.expected };
    }));
  },
};
