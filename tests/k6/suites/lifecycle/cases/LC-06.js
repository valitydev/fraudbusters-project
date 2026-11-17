import data from '../../../data/lifecycle/LC-06.js';
import { payoutSteps } from '../../payouts/steps.js';

export default {
  id: 'LC-06',
  title: 'Шаблон и список выплат после перезапуска',
  since: 'payouts',
  prepare(context) {
    const steps = payoutSteps(context);
    const scope = steps.scope();
    steps.bindNewTemplate('template', data.template, { partyId: scope.partyId });
    steps.addListRow('black', 'CARD_TOKEN', context.ids.resolve(data.listedCard), { partyId: scope.partyId });
    steps.awaitDecision('listed card', () => steps.withdrawal({ ...scope, cardToken: data.listedCard }),
      data.expected.listedCard);
  },
  verify(context) {
    const steps = payoutSteps(context);
    const scope = steps.scope();
    steps.awaitDecision('listed card restored', () => steps.withdrawal({ ...scope, cardToken: data.listedCard }),
      data.expected.listedCard);
    steps.expectDecision(steps.withdrawal({ ...scope, cardToken: '{{other-card}}' }), data.expected.otherCard,
      'other card');
  },
};
