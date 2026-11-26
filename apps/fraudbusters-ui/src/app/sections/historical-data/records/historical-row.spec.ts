import { toHistoricalCsv } from './historical-csv';
import { HISTORICAL_KINDS } from './historical-kinds';
import {
    chargebackRow,
    formatPaymentTool,
    inspectResultRow,
    maskedCard,
    payoutRow,
    statusTone,
} from '../../../shared/components/historical-table/historical-row';

describe('historical rows', () => {
    it('drops the template and rule when no rule was checked', () => {
        const row = inspectResultRow(
            {
                payment: { id: 'p1', merchantInfo: { partyId: 'party' } },
                checkedTemplate: 'RULE_NOT_CHECKED',
                ruleChecked: '0',
            },
            0
        );
        expect(row.template).toBe('');
        expect(row.rule).toBe('');
        expect(row.partyId).toBe('party');
    });

    it('shows only details that have a value', () => {
        const row = chargebackRow(
            {
                id: 'cb1',
                paymentId: 'p1',
                clientInfo: { fingerprint: '-', ip: '' },
                provider: { terminalId: 'term-1' },
            },
            0
        );
        expect(row.details.map((item) => item.label)).toEqual(['Payment ID', 'Terminal']);
    });

    it('describes payout destinations and cards', () => {
        const row = payoutRow(
            {
                id: 'wd1',
                eventTime: new Date(),
                amount: 10,
                currency: 'RUB',
                status: 'succeeded',
                destinationType: 'bank_card',
                paymentSystem: 'VISA',
                bin: '424242',
                lastDigits: '4242',
            },
            0
        );
        expect(row.method).toBe('Bank card · VISA · 424242');
        expect(row.details.find((item) => item.label === 'Card')?.value).toBe('424242 •••• 4242');
        expect(maskedCard('', '')).toBe('');
    });

    it('colours statuses by meaning', () => {
        expect(statusTone('decline_and_notify')).toBe('danger');
        expect(statusTone('captured')).toBe('success');
        expect(statusTone('three_ds')).toBe('info');
        expect(statusTone('something-new')).toBe('neutral');
    });
});

describe('historical CSV export', () => {
    it('quotes cells and neutralises spreadsheet formulas', () => {
        const row = chargebackRow({ id: '=cmd', status: 'accepted', clientInfo: { email: 'a"b@example.test' } }, 0);
        const csv = toHistoricalCsv(HISTORICAL_KINDS.chargebacks, [row]);
        const [header, line] = csv.trim().split('\n');
        expect(header.startsWith('"id","status","party_id","shop_id"')).toBeTrue();
        expect(line.startsWith(`"'=cmd","accepted"`)).toBeTrue();
        expect(line).toContain('"a""b@example.test"');
    });
});

describe('formatPaymentTool', () => {
    it('reads Thrift bank cards', () => {
        expect(
            formatPaymentTool(
                'BankCard(token:trusted_test_token_497, payment_system:PaymentSystemRef(id:visa), bin:427640, last_digits:6395, category:)'
            )
        ).toBe('Bank card · visa · 427640 •••• 6395');
    });

    it('keeps plain values', () => {
        expect(formatPaymentTool('bank_card')).toBe('bank_card');
        expect(formatPaymentTool(undefined)).toBe('');
    });
});
