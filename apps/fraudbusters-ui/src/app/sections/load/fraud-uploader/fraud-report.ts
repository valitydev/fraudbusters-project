/** A row of a fraud report CSV as fraudbusters-management reads it (CsvFraudPaymentParser). */
export interface FraudReportRow {
    line: number;
    id: string;
    fraudType: string;
    comment: string;
    eventTime: string;
    /** Why the backend would reject the file because of this row; empty when the row is fine. */
    issue: string;
}

export const FRAUD_REPORT_COLUMNS = ['id', 'fraud_type', 'comment', 'event_time'] as const;

const EVENT_TIME = /^\d{4}-\d{2}-\d{2}( \d{2}:\d{2}:\d{2})?$/;

export function fraudReportRow(record: Record<string, string>, line: number): FraudReportRow {
    const row = {
        line,
        id: record.id?.trim() ?? '',
        fraudType: record.fraud_type?.trim() ?? '',
        comment: record.comment?.trim() ?? '',
        eventTime: record.event_time?.trim() ?? '',
    };
    const issue = !row.id
        ? 'Payment ID is empty'
        : !row.fraudType
        ? 'Fraud type is empty'
        : row.eventTime && !EVENT_TIME.test(row.eventTime)
        ? 'Event time must be yyyy-MM-dd or yyyy-MM-dd HH:mm:ss'
        : '';
    return { ...row, issue };
}

/** Columns the backend reads by name; a file without them fails as a whole. */
export function missingColumns(header: readonly string[]): string[] {
    return FRAUD_REPORT_COLUMNS.filter((column) => !header.includes(column));
}
