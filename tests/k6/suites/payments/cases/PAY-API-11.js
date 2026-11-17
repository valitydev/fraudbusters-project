import data from '../../../data/payments/PAY-API-11.js';
import { expectEqual, expectSuccess } from '../../../lib/assert.js';
import { waitFor } from '../../../lib/poll.js';
import { paymentSteps } from '../steps.js';

const randomPrivateIp = () => [10, 1 + Math.floor(Math.random() * 250), Math.floor(Math.random() * 250),
  1 + Math.floor(Math.random() * 250)].join('.');

export default {
  id: 'PAY-API-11',
  title: 'Списки: кандидаты из чарджбэков',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids, connection } = context;
    const { partyId, shopId } = steps.scope();
    const client = { ...ids.resolve(data.client), ip: randomPrivateIp() };
    expectSuccess(management.insertListCandidates([{
      ...data.chargeback,
      id: ids.unique('chargeback'),
      paymentId: ids.unique('payment'),
      eventTime: new Date().toISOString(),
      merchantInfo: { partyId, shopId },
      clientInfo: client,
    }]), 'insert list candidates');
    const candidateOf = (field) => waitFor(connection, `candidate ${client[field]}`, () => {
      const found = (expectSuccess(management.filterListCandidates({ searchValue: client[field], size: 10 }),
        'filter candidates').result || []).find((item) => item.value === client[field] && item.partyId === partyId);
      return { done: Boolean(found), value: found };
    });
    const candidates = { email: candidateOf('email'), fingerprint: candidateOf('fingerprint'), ip: candidateOf('ip') };
    expectEqual({ email: candidates.email.listName, ip: candidates.ip.listName, listType: candidates.email.listType },
      data.expected, 'candidates from the chargeback');
    expectEqual(candidates.email.shopId, shopId, 'shop of the candidate');
    context.observe('fingerprint_candidate_list_name', candidates.fingerprint.listName);
    const batches = expectSuccess(management.filterCandidateBatches({ size: 100, sortOrder: 'DESC' }), 'batches');
    context.observe('batches_found', (batches.result || []).length > 0);
    expectSuccess(management.approveListCandidates(data.approve.map((field) => Number(candidates[field].id))),
      'approve candidates');
    steps.awaitListRow('black', candidates.email.listName, client.email);
    expectEqual(steps.findListRows('black', candidates.ip.listName, client.ip).length, 0,
      'not approved candidate is not in the list');
    waitFor(connection, 'approved fingerprint row', () => {
      const listName = ['FINGERPRINT', 'FINGERPRRINT'].find((name) => steps.findListRows('black', name,
        client.fingerprint).length > 0);
      context.observe('approved_fingerprint_list', listName || 'none');
      return { done: Boolean(listName) };
    });
  },
};
