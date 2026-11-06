import { describePayoutScope, toPayoutScope } from './payout-scope';

describe('payout scope', () => {
    it('describes global, party and wallet scopes', () => {
        expect(describePayoutScope({ isGlobal: true, partyId: 'ignored' })).toBe('All payouts');
        expect(describePayoutScope({})).toBe('All payouts');
        expect(describePayoutScope({ partyId: 'party-1' })).toBe('Party party-1');
        expect(describePayoutScope({ partyId: 'party-1', walletId: 'wallet-1' })).toBe(
            'Party party-1, wallet wallet-1'
        );
    });

    it('sends empty inputs as absent values', () => {
        expect(toPayoutScope(' ', '')).toEqual({ partyId: undefined, walletId: undefined });
        expect(toPayoutScope(' party-1 ', ' wallet-1 ')).toEqual({ partyId: 'party-1', walletId: 'wallet-1' });
    });

    it('rejects a wallet without a party', () => {
        expect(() => toPayoutScope('', 'wallet-1')).toThrowError('A wallet needs a party');
    });
});
