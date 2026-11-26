import { HistoricalKind } from './historical-kinds';
import { formatAmount, HistoricalRow } from '../../../shared/components/historical-table/historical-row';
import { HistoricalColumn } from '../../../shared/components/historical-table/historical-table-kind';

function cell(value: unknown): string {
    const text = value === undefined || value === null ? '' : String(value);
    // Quote every cell; a leading formula character is neutralised so spreadsheets show it as text.
    const safe = /^[=+\-@\t\r]/.test(text) ? `'${text}` : text;
    return `"${safe.replace(/"/g, '""')}"`;
}

const COLUMN_VALUE: Record<HistoricalColumn, (row: HistoricalRow) => unknown> = {
    status: (row) => row.status,
    amount: (row) => formatAmount(row.amount, row.currency),
    party: (row) => row.partyId,
    scope: (row) => row.scopeId,
    email: (row) => row.email,
    user: (row) => row.userId,
    method: (row) => row.method,
    category: (row) => row.category,
    code: (row) => row.code,
    fraudType: (row) => row.fraudType,
    template: (row) => row.template,
    rule: (row) => row.rule,
};

const COLUMN_HEADER: Record<HistoricalColumn, string> = {
    status: 'status',
    amount: 'amount',
    party: 'party_id',
    scope: 'scope_id',
    email: 'email',
    user: 'user_id',
    method: 'method',
    category: 'category',
    code: 'code',
    fraudType: 'fraud_type',
    template: 'template',
    rule: 'rule',
};

/** The loaded rows of a tab as CSV: table columns, then every detail any row has. */
export function toHistoricalCsv(kind: HistoricalKind, rows: readonly HistoricalRow[]): string {
    const detailLabels = [...new Set(rows.flatMap((row) => row.details.map((item) => item.label)))];
    const scopeHeader = kind.operation === 'payout' ? 'wallet_id' : 'shop_id';
    const header = [
        'id',
        ...kind.columns.map((column) => (column === 'scope' ? scopeHeader : COLUMN_HEADER[column])),
        'event_time',
        ...detailLabels.map((label) => label.toLowerCase().replace(/\W+/g, '_')),
    ];
    const lines = rows.map((row) => {
        const details = new Map(row.details.map((item) => [item.label, item.value]));
        return [
            row.id,
            ...kind.columns.map((column) => COLUMN_VALUE[column](row)),
            row.eventTime ? new Date(row.eventTime).toISOString() : '',
            ...detailLabels.map((label) => details.get(label)),
        ]
            .map(cell)
            .join(',');
    });
    return [header.map(cell).join(','), ...lines].join('\n') + '\n';
}
