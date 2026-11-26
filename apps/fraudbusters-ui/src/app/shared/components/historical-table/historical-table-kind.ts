/** Columns the historical table can show between the ID and the event time. */
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

/** What the historical table shows: Historical data tabs and data set test results use it. */
export interface HistoricalTableKind {
    title: string;
    operation: 'payment' | 'payout';
    columns: HistoricalColumn[];
    /** Rule engine results call the status "Result status". */
    statusLabel: string;
    /** The template column links to this editor. */
    templateLink?: (id: string) => string[];
}
