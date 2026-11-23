import data from '../../../data/payouts/PO-FL-03.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-03',
  title: 'Каскад шаблонов и групп выплат',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { ids } = context;
    const { expected } = data;
    const template = (role) => steps.saveTemplate(ids.name(role), data.templates[role]);
    const [accept, decline, notify] = ['accept', 'decline', 'notify'].map(template);
    const group = (role, templateId) => steps.saveGroup(ids.name(`group-${role}`), [[templateId, 1]]);
    const partyId = ids.name('party');
    const wallet = { partyId, walletId: ids.name('wallet-1') };
    const otherWallet = { partyId, walletId: ids.name('wallet-2') };

    steps.bindTemplate(accept, wallet);
    steps.awaitDecision('only party+wallet template', () => steps.payout(wallet), expected.onlyWalletTemplate);
    const partyTemplate = steps.bindNewTemplate('party-template', data.templates.decline, { partyId });
    steps.awaitDecision('party template before party+wallet template', () => steps.payout(wallet),
      expected.partyTemplateBeforeWalletTemplate);
    steps.saveTemplate(partyTemplate, data.templates.noDecision);
    steps.awaitDecision('party template without decision falls through', () => steps.payout(wallet),
      expected.partyWithoutDecisionFallsThrough);
    steps.saveTemplate(partyTemplate, data.templates.decline);
    steps.awaitDecision('party template for another wallet', () => steps.payout(otherWallet),
      expected.partyTemplateForOtherWallet);

    const groups = { partyId: ids.name('party-groups'), walletId: wallet.walletId };
    steps.bindGroup(group('party', decline), { partyId: groups.partyId });
    steps.bindGroup(group('wallet', accept), groups);
    steps.awaitDecision('party group before party+wallet group', () => steps.payout(groups),
      expected.partyGroupBeforeWalletGroup);

    const walletGroup = { partyId: ids.name('party-wallet-group'), walletId: wallet.walletId };
    steps.bindGroup(group('wallet-2', accept), walletGroup);
    steps.bindTemplate(decline, { partyId: walletGroup.partyId });
    steps.awaitDecision('party+wallet group before party template', () => steps.payout(walletGroup),
      expected.walletGroupBeforePartyTemplate);

    const notified = { partyId: ids.name('party-notify'), walletId: wallet.walletId };
    steps.bindTemplate(notify, { partyId: notified.partyId });
    steps.bindTemplate(decline, notified);
    steps.awaitDecision('notify does not stop the cascade', () => steps.payout(notified),
      expected.notifyDoesNotStopCascade);
  },
};
