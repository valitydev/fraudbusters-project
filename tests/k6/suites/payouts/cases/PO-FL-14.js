import data from '../../../data/payouts/PO-FL-14.js';
import { expectTrue } from '../../../lib/assert.js';
import { payoutSteps } from '../steps.js';

export default {
  id: 'PO-FL-14',
  title: 'Списки выплат по полям PARTY_ID, WALLET_ID, CURRENCY, USER_ID, именованный список, countryBy',
  profiles: ['functional'],
  run(context) {
    const steps = payoutSteps(context);
    const { ids } = context;
    const base = { partyId: ids.name('party'), walletId: ids.name('wallet') };
    const blockedParty = { ...base, partyId: ids.name('party-blocked') };
    const templateId = steps.saveTemplate(ids.name('template'), data.template);
    [base, blockedParty].forEach((scope) => steps.bindTemplate(templateId, { partyId: scope.partyId }));
    [base, blockedParty].forEach((scope) => steps.awaitReady(`template applied to ${scope.partyId}`, scope, data.ready));
    ids.resolve(data.rows).forEach((row) => steps.addListRow(row.type, row.name, row.value, row.scope));
    const mismatches = data.scenarios.map((scenario) => {
      try {
        steps.awaitDecision(scenario.name, () => steps.withdrawal({ ...base, ...scenario.withdrawal }), scenario.expected);
        return undefined;
      } catch (error) {
        return `${scenario.name}: ${error.message}`;
      }
    }).filter(Boolean);
    expectTrue(mismatches.length === 0, mismatches.join('; '));
  },
};
