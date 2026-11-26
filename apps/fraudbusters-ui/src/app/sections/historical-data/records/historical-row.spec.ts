import { toHistoricalCsv } from './historical-csv';
import { HISTORICAL_KINDS } from './historical-kinds';
import {
    chargebackRow,
    formatAmount,
    formatPaymentTool,
    inspectResultRow,
    maskedCard,
    payoutRow,
    statusTone,
} from '../../../shared/components/historical-table/historical-row';

describe('historical rows', () => {
    it('formats disputed and inspected amounts in major units in the details and CSV', () => {
        const amount = 12345;
        const currency = 'RUB';
        const expected = '123.45 RUB';
        const chargeback = chargebackRow({ amount, currency }, 0);
        const inspected = inspectResultRow({ payment: { amount, currency } }, 0);
        const payout = payoutRow({ id: 'payout', amount, currency, eventTime: new Date(0), status: 'succeeded' }, 0);

        expect(chargeback.details.find((item) => item.label === 'Disputed amount')?.value).toBe(expected);
        expect(inspected.details.find((item) => item.label === 'Amount')?.value).toBe(expected);
        expect(toHistoricalCsv(HISTORICAL_KINDS.chargebacks, [chargeback])).toContain(`"${expected}"`);
        expect(toHistoricalCsv(HISTORICAL_KINDS.payouts, [payout])).toContain(`"${expected}"`);
    });

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

describe('formatAmount', () => {
    it('uses the minor-unit precision of the currency', () => {
        expect(formatAmount(12345, 'RUB')).toBe('123.45 RUB');
        expect(formatAmount(12345, 'JPY')).toBe('12,345 JPY');
        expect(formatAmount(12345, 'KWD')).toBe('12.345 KWD');
        expect(formatAmount(1)).toBe('0.01');
        expect(formatAmount(0, 'RUB')).toBe('0.00 RUB');
        expect(formatAmount(undefined, 'RUB')).toBe('');
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
