/** How a payout reference, group reference or list record is scoped, as fraudbusters applies it. */
export const describePayoutScope = (scope: { partyId?: string; walletId?: string; isGlobal?: boolean }): string => {
    if (scope.isGlobal || !scope.partyId) {
        return 'All payouts';
    }
    return scope.walletId ? `Party ${scope.partyId}, wallet ${scope.walletId}` : `Party ${scope.partyId}`;
};

/** Empty inputs are sent as absent values; a wallet without a party is rejected before sending. */
export const toPayoutScope = (partyId?: string, walletId?: string): { partyId?: string; walletId?: string } => {
    const party = partyId?.trim() || undefined;
    const wallet = walletId?.trim() || undefined;
    if (wallet && !party) {
        throw new Error('A wallet needs a party');
    }
    return { partyId: party, walletId: party ? wallet : undefined };
};
