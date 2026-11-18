import data from '../../../data/payments/PAY-API-18.js';
import { expectSuccess, expectTrue } from '../../../lib/assert.js';
import { awaitAuditRecords } from '../../audit.js';
import { paymentSteps } from '../steps.js';

export default {
  id: 'PAY-API-18',
  title: 'Аудит',
  profiles: ['functional'],
  run(context) {
    const steps = paymentSteps(context);
    const { management, ids } = context;
    const commandTypes = JSON.stringify(expectSuccess(management.fetchAuditCommandTypes(), 'audit command types'));
    const objectTypes = expectSuccess(management.fetchAuditObjectTypes(), 'audit object types');
    context.observe('object_types', JSON.stringify(objectTypes.result || objectTypes));
    expectTrue(data.commandTypes.every((type) => commandTypes.includes(type)), `command types: ${commandTypes}`);
    const templateId = steps.bindNewTemplate('audited', data.template, steps.scope());
    expectSuccess(management.removePaymentTemplate(templateId), 'remove template');
    const records = awaitAuditRecords(context, data, templateId);
    expectTrue(records.every((item) => item.initiator && item.insertTime), 'audit records have initiator and time');
  },
};
