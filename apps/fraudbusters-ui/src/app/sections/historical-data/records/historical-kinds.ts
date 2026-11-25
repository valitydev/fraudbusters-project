import { DatePipe } from '@angular/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import {
    chargebackRow,
    fraudPaymentRow,
    HistoricalRow,
    inspectResultRow,
    paymentRow,
    payoutInspectResultRow,
    payoutRow,
    refundRow,
} from './historical-row';
import { Payment } from '../../../api/fb-management/swagger-codegen/model/payment';
import { Withdrawal } from '../../../api/fb-management/swagger-codegen/model/withdrawal';
import { HistoricalDataService } from '../../../api/payments/historical-data';
import { PayoutsService } from '../../../api/payouts';
import { DateFormat } from '../../../shared/constants/date-format';
import { SortOrder } from '../../../shared/constants/sort-order';
import { SearchHistoricalParams } from '../search-historical-params';

export type HistoricalKindKey =
    | 'inspect-results'
    | 'payments'
    | 'refunds'
    | 'chargebacks'
    | 'fraud-payments'
    | 'payouts'
    | 'payout-inspect-results';

export type HistoricalColumn =
    | 'status'
    | 'amount'
    | 'party'
    | 'scope'
    | 'email'
    | 'user'
    | 'method'
    | 'category'
    | 'code'
    | 'fraudType'
    | 'template'
    | 'rule';

/** Filters under "Additional filters"; ID, Email, Party and Shop / Wallet are always shown. */
export type AdditionalFilter =
    | 'cardToken'
    | 'country'
    | 'status'
    | 'fingerprint'
    | 'customerId'
    | 'userId'
    | 'terminal'
    | 'maskedPan'
    | 'template'
    | 'rule';

export interface HistoricalFilters {
    from: string;
    to: string;
    id?: string;
    email?: string;
    partyId?: string;
    scopeId?: string;
    cardToken?: string;
    country?: string;
    status?: string;
    fingerprint?: string;
    customerId?: string;
    userId?: string;
    terminal?: string;
    maskedPan?: string;
    template?: string;
    rule?: string;
}

export interface HistoricalPage {
    rows: HistoricalRow[];
    continuationId?: string;
}

export interface HistoricalApis {
    payments: HistoricalDataService;
    payouts: PayoutsService;
    datePipe: DatePipe;
}

/** What a historical tab shows and how it asks the backend for it. */
export interface HistoricalKind {
    key: HistoricalKindKey;
    title: string;
    operation: 'payment' | 'payout';
    columns: HistoricalColumn[];
    /** Rule engine results call the status "Result status". */
    statusLabel: string;
    statuses: string[];
    additional: AdditionalFilter[];
    filterHint: string;
    /** Rows can be picked for a data set (payments) or sent to list candidates (chargebacks). */
    selection?: 'data-set' | 'candidates';
    /** The template column links to this editor. */
    templateLink?: (id: string) => string[];
    fetch(apis: HistoricalApis, query: HistoricalQuery): Observable<HistoricalPage>;
}

export interface HistoricalQuery {
    filters: HistoricalFilters;
    sortOrder: SortOrder;
    size: number;
    continuationId?: string;
}

/** fraudbusters-management reads the period as "yyyy-MM-dd HH:mm:ss". */
function period(apis: HistoricalApis, filters: HistoricalFilters) {
    return {
        from: apis.datePipe.transform(new Date(filters.from), DateFormat._yyyyMMDdHHMmSs),
        to: apis.datePipe.transform(new Date(filters.to), DateFormat._yyyyMMDdHHMmSs),
    };
}

function filled<T extends object>(params: T): Partial<T> {
    return Object.fromEntries(
        Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
    ) as Partial<T>;
}

function paymentParams(apis: HistoricalApis, query: HistoricalQuery, rules = false): SearchHistoricalParams {
    const { filters } = query;
    return filled({
        ...period(apis, filters),
        sortOrder: query.sortOrder,
        size: query.size,
        continuationId: query.continuationId,
        paymentId: filters.id,
        email: filters.email,
        partyId: filters.partyId,
        shopId: filters.scopeId,
        cardToken: filters.cardToken,
        providerCountry: filters.country,
        status: filters.status,
        fingerprint: filters.fingerprint,
        customerId: filters.customerId,
        userId: filters.userId,
        terminal: filters.terminal,
        maskedPan: filters.maskedPan,
        ...(rules ? { template: filters.template, rule: filters.rule } : {}),
    }) as SearchHistoricalParams;
}

function payoutParams(apis: HistoricalApis, query: HistoricalQuery) {
    const { filters } = query;
    return {
        ...period(apis, filters),
        sortOrder: query.sortOrder,
        ...filled({
            size: query.size,
            continuationId: query.continuationId,
            withdrawalId: filters.id,
            email: filters.email,
            partyId: filters.partyId,
            walletId: filters.scopeId,
            cardToken: filters.cardToken,
            status: filters.status,
            fingerprint: filters.fingerprint,
            customerId: filters.customerId,
            userId: filters.userId,
            maskedPan: filters.maskedPan,
        }),
    };
}

function page<T>(
    response: Observable<{ result?: T[]; continuationId?: string }>,
    toRow: (item: T, index: number) => HistoricalRow
): Observable<HistoricalPage> {
    return response.pipe(
        map(({ result, continuationId }) => ({
            rows: (result ?? []).map(toRow),
            continuationId: continuationId || undefined,
        }))
    );
}

/** fraudbusters result statuses (ResultStatus). */
const RESULT_STATUSES = [
    'accept',
    'accept_and_notify',
    'three_ds',
    'high_risk',
    'normal',
    'notify',
    'decline',
    'decline_and_notify',
];

const PAYMENT_FILTERS: AdditionalFilter[] = [
    'cardToken',
    'country',
    'status',
    'fingerprint',
    'customerId',
    'userId',
    'terminal',
    'maskedPan',
];

const paymentTemplate = (id: string) => ['/template', id];

export const HISTORICAL_KINDS: Record<HistoricalKindKey, HistoricalKind> = {
    'inspect-results': {
        key: 'inspect-results',
        title: 'Inspect results',
        operation: 'payment',
        columns: ['status', 'party', 'scope', 'email', 'user', 'template', 'rule'],
        statusLabel: 'Result status',
        statuses: RESULT_STATUSES,
        additional: [
            'cardToken',
            'country',
            'status',
            'fingerprint',
            'customerId',
            'userId',
            'terminal',
            'template',
            'rule',
        ],
        filterHint: 'Rule engine inspection filters',
        templateLink: paymentTemplate,
        fetch: (apis, query) =>
            page(apis.payments.filterInspectResults(paymentParams(apis, query, true)), inspectResultRow),
    },
    payments: {
        key: 'payments',
        title: 'Payments',
        operation: 'payment',
        columns: ['status', 'amount', 'party', 'scope', 'email', 'method'],
        statusLabel: 'Status',
        statuses: Object.values(Payment.StatusEnum),
        additional: PAYMENT_FILTERS,
        filterHint: 'Transaction filters',
        selection: 'data-set',
        fetch: (apis, query) => page(apis.payments.filterPayments(paymentParams(apis, query)), paymentRow),
    },
    refunds: {
        key: 'refunds',
        title: 'Refunds',
        operation: 'payment',
        columns: ['status', 'amount', 'party', 'scope', 'email', 'method'],
        statusLabel: 'Status',
        statuses: ['pending', 'succeeded', 'failed'],
        additional: PAYMENT_FILTERS,
        filterHint: 'Transaction filters',
        fetch: (apis, query) => page(apis.payments.filterRefunds(paymentParams(apis, query)), refundRow),
    },
    chargebacks: {
        key: 'chargebacks',
        title: 'Chargebacks',
        operation: 'payment',
        columns: ['status', 'party', 'scope', 'category', 'code', 'email', 'user'],
        statusLabel: 'Status',
        statuses: ['accepted', 'rejected', 'cancelled'],
        additional: PAYMENT_FILTERS,
        filterHint: 'Dispute filters',
        selection: 'candidates',
        fetch: (apis, query) => page(apis.payments.filterChargebacks(paymentParams(apis, query)), chargebackRow),
    },
    'fraud-payments': {
        key: 'fraud-payments',
        title: 'Fraud payments',
        operation: 'payment',
        columns: ['fraudType', 'status', 'amount', 'party', 'scope', 'email'],
        statusLabel: 'Status',
        statuses: Object.values(Payment.StatusEnum),
        additional: PAYMENT_FILTERS,
        filterHint: 'Fraud report filters',
        fetch: (apis, query) => page(apis.payments.filterFraudPayments(paymentParams(apis, query)), fraudPaymentRow),
    },
    payouts: {
        key: 'payouts',
        title: 'Payouts',
        operation: 'payout',
        columns: ['status', 'amount', 'party', 'scope', 'method', 'email'],
        statusLabel: 'Status',
        statuses: Object.values(Withdrawal.StatusEnum),
        additional: PAYMENT_FILTERS,
        filterHint: 'Payout filters',
        fetch: (apis, query) =>
            page(
                apis.payouts.filterWithdrawals({
                    ...payoutParams(apis, query),
                    ...filled({ terminal: query.filters.terminal, providerCountry: query.filters.country }),
                }),
                payoutRow
            ),
    },
    'payout-inspect-results': {
        key: 'payout-inspect-results',
        title: 'Inspect results',
        operation: 'payout',
        columns: ['status', 'amount', 'party', 'scope', 'email', 'template', 'rule'],
        statusLabel: 'Result status',
        statuses: RESULT_STATUSES,
        additional: ['cardToken', 'status', 'fingerprint', 'customerId', 'userId', 'maskedPan', 'template', 'rule'],
        filterHint: 'Rule engine inspection filters',
        templateLink: (id) => ['/payouts/template', id],
        fetch: (apis, query) =>
            page(
                apis.payouts.filterWithdrawalInspectResults({
                    ...payoutParams(apis, query),
                    ...filled({ template: query.filters.template, rule: query.filters.rule }),
                }),
                payoutInspectResultRow
            ),
    },
};
