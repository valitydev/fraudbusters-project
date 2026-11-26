import { CheckedDataSetRow } from '../../../api/fb-management/swagger-codegen/model/checkedDataSetRow';
import { HistoricalRow, inspectResultRow } from '../historical-table/historical-row';
import { HistoricalTableKind } from '../historical-table/historical-table-kind';

/** Test results read like Inspect results of Historical data, with the amount as a column. */
export const TEST_RESULTS_KIND: HistoricalTableKind = {
    title: 'Test results',
    operation: 'payment',
    columns: ['status', 'rule', 'amount', 'party', 'scope', 'email'],
    statusLabel: 'Result status',
};

/** A tested payment is an inspect result: the same row, chips and details as in Historical data. */
export function testResultRow(row: CheckedDataSetRow, index: number): HistoricalRow<CheckedDataSetRow> {
    const result = inspectResultRow(
        {
            payment: row.payment,
            checkedTemplate: row.checkedTemplate,
            resultStatus: row.resultStatus,
            ruleChecked: row.ruleChecked,
            notificationsRule: row.notificationRule,
        },
        index
    );
    return {
        ...result,
        key: `${row.id ?? result.id}#${index}`,
        // The amount has its own column here.
        details: result.details.filter((detail) => detail.label !== 'Amount'),
        source: row,
    };
}
