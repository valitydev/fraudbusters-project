import { CountInfo } from '../../../api/fb-management/swagger-codegen/model/countInfo';
import { ListType } from '../../../api/fb-management/swagger-codegen/model/listType';
import { PaymentCountInfo } from '../../../api/fb-management/swagger-codegen/model/paymentCountInfo';
import { PayoutCountInfo } from '../../../api/fb-management/swagger-codegen/model/payoutCountInfo';
import { toPayoutScope } from '../../payouts/services/payout-scope';

/** A row queued on the New rows page; grey rows also carry their match limit and window. */
export interface ListRowDraft {
    listType: ListType;
    listName: string;
    partyId: string;
    scopeId?: string;
    value: string;
    count?: number;
    start?: string;
    end?: string;
}

export const MANUAL_LIST_TYPES = [ListType.Black, ListType.White, ListType.Grey] as const;

export const LIST_TYPE_LABELS: Record<string, string> = {
    [ListType.Black]: 'Black',
    [ListType.White]: 'White',
    [ListType.Grey]: 'Grey',
    [ListType.Naming]: 'Named',
};

export interface ListRowFields {
    listName?: string;
    partyId?: string;
    scopeId?: string;
    value?: string;
    count?: string | number;
    start?: string;
    end?: string;
}

function isoTime(value?: string): string | undefined {
    if (!value?.trim()) {
        return undefined;
    }
    const date = new Date(value.trim());
    return Number.isNaN(date.getTime()) ? undefined : date.toISOString();
}

/**
 * Checks required values, scope and grey list windows before queuing a row.
 * Payout rows can apply across all parties, and their start time is optional.
 */
export function toListRow(listType: ListType, fields: ListRowFields, payout = false): ListRowDraft | string {
    const listName = fields.listName?.trim() ?? '';
    const partyId = fields.partyId?.trim() ?? '';
    const value = fields.value?.trim() ?? '';
    if (!listName || !value || (!payout && !partyId)) {
        return payout ? 'List name and Value are required.' : 'List name, Party ID and Value are required.';
    }
    if (payout && fields.scopeId?.trim() && !partyId) {
        return 'A Wallet ID requires a Party ID.';
    }
    const row: ListRowDraft = { listType, listName, partyId, scopeId: fields.scopeId?.trim() || undefined, value };
    if (listType !== ListType.Grey) {
        return row;
    }
    const count = Number(fields.count);
    const start = isoTime(fields.start);
    const end = isoTime(fields.end);
    if (!Number.isInteger(count) || count < 1) {
        return 'Count must be a whole number of at least 1.';
    }
    if (!end || (!start && (!payout || fields.start?.trim()))) {
        return payout
            ? 'Enter a valid end time and, if provided, a valid start time.'
            : 'Grey rows need a start and an end time.';
    }
    if (start && new Date(end) <= new Date(start)) {
        return 'End time must be after the start time.';
    }
    return { ...row, count, start, end };
}

export function listRowKey(row: ListRowDraft): string {
    return JSON.stringify([row.listType, row.listName, row.partyId, row.scopeId, row.value]);
}

function countInfo(row: ListRowDraft): CountInfo | undefined {
    if (row.listType !== ListType.Grey) {
        return undefined;
    }
    return {
        count: row.count,
        startCountTime: row.start as unknown as Date,
        endCountTime: row.end as unknown as Date,
    };
}

export function toPaymentCountInfo(row: ListRowDraft): PaymentCountInfo {
    return {
        listRecord: { listName: row.listName, partyId: row.partyId, shopId: row.scopeId, value: row.value },
        countInfo: countInfo(row),
    };
}

export function toPayoutCountInfo(row: ListRowDraft): PayoutCountInfo {
    return {
        listRecord: {
            listName: row.listName,
            value: row.value,
            ...toPayoutScope(row.partyId, row.scopeId),
        },
        countInfo: countInfo(row),
    };
}
