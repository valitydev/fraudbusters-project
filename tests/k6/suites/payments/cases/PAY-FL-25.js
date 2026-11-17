import data from '../../../data/payments/PAY-FL-25.js';
import { expectEqual, expectSuccess } from '../../../lib/assert.js';
import { ANY_STATUS } from '../../../lib/http.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-FL-25',
  title: 'Проверка пользователя /inspect-user',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { ids, inspection } = context;
    const partyId = ids.name('party');
    const shopId = (role) => ids.name(`shop-${role}`);
    steps.applyTemplates(data.shops.map((shop) => ({ ...shop, scope: { partyId, shopId: shopId(shop.role) } })));
    const merchants = data.shops.map((shop) => ({
      id: partyId, shop: { id: shopId(shop.role), name: 'k6 shop', category: 'k6', location: 'k6' },
    }));
    const response = inspection.inspectUser({ customer: ids.resolve(data.customer), merchants }, ANY_STATUS);
    context.observe('status', response.status);
    const blocked = (expectSuccess(response, 'inspect user').blockedMerchants || [])
      .map((merchant) => merchant.shop && merchant.shop.id);
    context.observe('amount_rule_blocks_shop', blocked.includes(shopId(data.observedShop)));
    expectEqual(blocked.filter((id) => id !== shopId(data.observedShop)), data.expected.blockedShops.map(shopId),
      'blocked merchants');
  },
};
