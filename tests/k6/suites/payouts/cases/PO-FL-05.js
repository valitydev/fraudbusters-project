import data from '../../../data/payouts/PO-FL-05.js';
import { expectEqual, expectSuccess } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { countInfoFor } from '../../../lib/time.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-05',
  title: 'Списки выплат и их область действия',
  profiles: ['functional', 'smoke'],
  run(context) {
    const steps = payoutSteps(context);
    const { ids, payouts, connection } = context;
    const cardToken = ids.name('card');
    const partyA = ids.name('party-a');
    const partyB = ids.name('party-b');
    const wallet1 = ids.name('wallet-1');
    const wallet2 = ids.name('wallet-2');
    const at = (partyId, walletId) => () => steps.withdrawal({ partyId, walletId, cardToken });
    const { black, white, grey } = data;

    const blackTemplate = steps.saveTemplate(ids.name('black'), black.template);
    [partyA, partyB].forEach((partyId) => steps.bindTemplate(blackTemplate, { partyId }));
    steps.awaitDecision('A/1 before lists', at(partyA, wallet1), black.expected.beforeLists);
    steps.awaitDecision('B/1 before lists', at(partyB, wallet1), black.expected.beforeLists);
    steps.addListRow('black', 'CARD_TOKEN', cardToken, { partyId: partyA, walletId: wallet1 });
    steps.awaitDecision('black row of A/1', at(partyA, wallet1), black.expected.listed);
    steps.expectDecision(at(partyA, wallet2)(), black.expected.notListed, 'black row of A/1 does not touch A/2');
    steps.expectDecision(at(partyB, wallet1)(), black.expected.notListed, 'black row of A/1 does not touch B/1');
    const walletRow = steps.awaitListRow('black', 'CARD_TOKEN', cardToken, { partyId: partyA, walletId: wallet1 });
    expectSuccess(payouts.removePayoutListRow(walletRow.id), 'remove wallet row');
    steps.awaitDecision('wallet row removed', at(partyA, wallet1), black.expected.removed);
    steps.addListRow('black', 'CARD_TOKEN', cardToken, { partyId: partyA });
    steps.awaitDecision('black row of party A, wallet 2', at(partyA, wallet2), black.expected.listed);
    steps.expectDecision(at(partyA, wallet1)(), black.expected.listed, 'black row of party A, wallet 1');
    steps.expectDecision(at(partyB, wallet1)(), black.expected.notListed, 'black row of party A does not touch B');

    const whiteScope = { partyId: ids.name('party-white'), walletId: wallet1 };
    const email = `${ids.prefix}-white@k6.test`;
    steps.bindNewTemplate('white', white.template, { partyId: whiteScope.partyId });
    steps.awaitReady('white template is applied', whiteScope, white.ready);
    steps.expectDecision(steps.withdrawal({ ...whiteScope, email }), white.expected.notListed, 'not in white list');
    steps.addListRow('white', 'EMAIL', email, { partyId: whiteScope.partyId });
    steps.awaitDecision('email in white list', () => steps.withdrawal({ ...whiteScope, email }), white.expected.listed);

    const greyCard = { partyId: ids.name('party-grey'), walletId: wallet1, cardToken: ids.name('grey-card') };
    const { count, fromMs, toMs } = grey.countInfo;
    steps.bindNewTemplate('grey', grey.template, { partyId: greyCard.partyId });
    steps.awaitReady('grey template is applied', greyCard, grey.ready);
    steps.addListRow('grey', 'CARD_TOKEN', greyCard.cardToken, { partyId: greyCard.partyId },
      countInfoFor(count, fromMs, toMs));
    const first = steps.awaitDecision('card in grey list', () => steps.withdrawal(greyCard), grey.expected.listed);
    steps.loadAndAwait([first]);
    const after = waitFor(connection, 'grey list limit is exhausted', () => {
      const decision = steps.inspect(steps.withdrawal(greyCard));
      return { done: decision !== grey.expected.listed, value: decision, observed: decision };
    });
    context.observe('grey_decision_after_limit', after);
    expectEqual(after, grey.expected.afterLimit, 'decision after the grey list limit');
  },
};
