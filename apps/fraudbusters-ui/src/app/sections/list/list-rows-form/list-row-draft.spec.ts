import { listRowKey, toListRow, toPaymentCountInfo, toPayoutCountInfo } from './list-row-draft';
import { ListType } from '../../../api/fb-management/swagger-codegen/model/listType';

describe('toListRow', () => {
    const base = { listName: ' EMAIL ', partyId: ' party-1 ', value: ' user@example.test ' };

    it('trims black and white rows and drops an empty shop', () => {
        expect(toListRow(ListType.Black, { ...base, scopeId: '  ' })).toEqual({
            listType: ListType.Black,
            listName: 'EMAIL',
            partyId: 'party-1',
            scopeId: undefined,
            value: 'user@example.test',
        });
    });

    it('requires list name, party and value', () => {
        expect(toListRow(ListType.White, { ...base, value: ' ' })).toBe('List name, Party ID and Value are required.');
    });

    it('needs a whole positive count and a valid window for grey rows', () => {
        const window = { start: '2026-11-25T10:00:00Z', end: '2026-11-26T10:00:00Z' };
        expect(toListRow(ListType.Grey, { ...base, ...window, count: '0' })).toContain('Count');
        expect(toListRow(ListType.Grey, { ...base, ...window, count: '1.5' })).toContain('Count');
        expect(toListRow(ListType.Grey, { ...base, count: 3 })).toContain('start and an end');
        expect(toListRow(ListType.Grey, { ...base, count: 3, start: window.end, end: window.start })).toContain(
            'after the start'
        );
        expect(toListRow(ListType.Grey, { ...base, ...window, count: '3' })).toEqual(
            jasmine.objectContaining({ count: 3, start: '2026-11-25T10:00:00.000Z', end: '2026-11-26T10:00:00.000Z' })
        );
    });
});

describe('toPaymentCountInfo', () => {
    it('sends the count window only for grey rows', () => {
        const black = toListRow(ListType.Black, { listName: 'IP', partyId: 'p', value: '203.0.113.7' });
        const grey = toListRow(ListType.Grey, {
            listName: 'IP',
            partyId: 'p',
            value: '203.0.113.7',
            count: 2,
            start: '2026-11-25T10:00:00Z',
            end: '2026-11-25T12:00:00Z',
        });
        if (typeof black === 'string' || typeof grey === 'string') {
            fail('rows should be valid');
            return;
        }
        expect(toPaymentCountInfo(black).countInfo).toBeUndefined();
        expect(toPaymentCountInfo(grey).countInfo).toEqual(
            jasmine.objectContaining({ count: 2, startCountTime: '2026-11-25T10:00:00.000Z' as unknown as Date })
        );
        expect(listRowKey(black)).not.toBe(listRowKey(grey));
    });
});

describe('payout list drafts', () => {
    const draft = { listName: 'EMAIL', value: 'user@example.test' };

    it('accepts an unscoped named row and requires a party for a wallet', () => {
        const global = toListRow(ListType.Naming, draft, true) as Exclude<ReturnType<typeof toListRow>, string>;
        expect(toPayoutCountInfo(global).listRecord.partyId).toBeUndefined();
        expect(toListRow(ListType.White, { ...draft, scopeId: 'wallet' }, true)).toContain('Party ID');
    });

    it('sends Wallet ID and supports a grey row without a start time', () => {
        const row = toListRow(
            ListType.Grey,
            {
                ...draft,
                partyId: 'party',
                scopeId: 'wallet',
                count: '1',
                end: '2026-11-26T10:00:00Z',
            },
            true
        ) as Exclude<ReturnType<typeof toListRow>, string>;
        expect(toPayoutCountInfo(row)).toEqual({
            listRecord: { ...draft, partyId: 'party', walletId: 'wallet' },
            countInfo: {
                count: 1,
                startCountTime: undefined,
                endCountTime: '2026-11-26T10:00:00.000Z' as unknown as Date,
            },
        });
    });
});
