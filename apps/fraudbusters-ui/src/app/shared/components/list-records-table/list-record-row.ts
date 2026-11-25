import { CountInfo } from '../../../api/fb-management/swagger-codegen/model/countInfo';
import { PayoutListRow } from '../../../api/fb-management/swagger-codegen/model/payoutListRow';
import { WbListRecord } from '../../../api/fb-management/swagger-codegen/model/wbListRecord';

/** Grey list limit: how many operations may match within the time window. */
export interface ListRecordWindow {
    count?: number;
    start?: Date;
    end?: Date;
}

export type ListRecordWindowStatus = 'active' | 'scheduled' | 'expired';

/** One row of a black/white/grey list, independent of payments or payouts. */
export interface ListRecordRow<T> {
    id?: string;
    listName?: string;
    partyId?: string;
    /** Shop ID for payments, wallet ID for payouts. */
    scopeId?: string;
    value?: string;
    insertTime?: Date;
    createdByUser?: string;
    window?: ListRecordWindow;
    source: T;
}

function toDate(value?: string | Date): Date | undefined {
    if (!value) {
        return undefined;
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? undefined : date;
}

/**
 * Payment list rows carry the grey limit as a JSON string (`rowInfo`) written by fb-management:
 * `{ count, startCountTime, timeToLive }`; the end of the window is `timeToLive`.
 */
export function parseRowInfo(rowInfo?: string): ListRecordWindow | undefined {
    if (!rowInfo) {
        return undefined;
    }
    try {
        const info = JSON.parse(rowInfo) as {
            count?: number;
            startCountTime?: string;
            start_count_time?: string;
            timeToLive?: string;
            time_to_live?: string;
            endCountTime?: string;
        };
        const window: ListRecordWindow = {
            count: typeof info.count === 'number' ? info.count : undefined,
            start: toDate(info.startCountTime ?? info.start_count_time),
            end: toDate(info.timeToLive ?? info.time_to_live ?? info.endCountTime),
        };
        return window.count === undefined && !window.start && !window.end ? undefined : window;
    } catch {
        return undefined;
    }
}

export function fromCountInfo(countInfo?: CountInfo): ListRecordWindow | undefined {
    if (!countInfo) {
        return undefined;
    }
    return { count: countInfo.count, start: toDate(countInfo.startCountTime), end: toDate(countInfo.endCountTime) };
}

export function windowStatus(window: ListRecordWindow | undefined, now = new Date()): ListRecordWindowStatus {
    if (window?.start && window.start > now) {
        return 'scheduled';
    }
    if (window?.end && window.end < now) {
        return 'expired';
    }
    return 'active';
}

export function paymentListRow(record: WbListRecord): ListRecordRow<WbListRecord> {
    return {
        id: record.id,
        listName: record.listName,
        partyId: record.partyId,
        scopeId: record.shopId,
        value: record.value,
        insertTime: record.insertTime,
        createdByUser: record.createdByUser,
        window: parseRowInfo(record.rowInfo),
        source: record,
    };
}

export function payoutListRow(row: PayoutListRow): ListRecordRow<PayoutListRow> {
    return {
        id: row.id,
        listName: row.listRecord?.listName,
        partyId: row.listRecord?.partyId,
        scopeId: row.listRecord?.walletId,
        value: row.listRecord?.value,
        insertTime: row.insertTime,
        createdByUser: row.createdByUser,
        window: fromCountInfo(row.countInfo),
        source: row,
    };
}

const NAME_PILLS = ['fb-pill-accent', 'fb-pill-info', 'fb-pill-warning', 'fb-pill-success', 'fb-pill-danger'];

/** A stable pill colour per list name, so the same name looks the same in every table. */
export function listNamePill(name?: string): string {
    let hash = 0;
    for (const char of name ?? '') {
        hash = (hash * 31 + char.charCodeAt(0)) >>> 0;
    }
    return NAME_PILLS[hash % NAME_PILLS.length];
}
