export interface SearchHistoricalParams {
    continuationId?: string;
    size?: number;
    sortBy?: string;
    sortOrder: string;
    from?: string;
    to?: string;
    paymentId?: string;
    cardToken?: string;
    shopId?: string;
    partyId?: string;
    status?: string;
    fingerprint?: string;
    customerId?: string;
    userId?: string;
    email?: string;
    terminal?: string;
    maskedPan?: string;
    template?: string;
    rule?: string;
}
