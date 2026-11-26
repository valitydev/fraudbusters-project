import type { Cell, Row, SheetData } from 'write-excel-file/universal';

import type { AnalyticsData } from './services/base-analytics.service';
import type { FraudResultSummary } from '../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';
import { ruleLabel } from '../../../../shared/pipes/rule-label.pipe';

const SHEET_NAMES = ['Rule breakdown', 'Overview'];
const AMOUNT_FORMAT = '#,##0.00';
const PERCENT_FORMAT = '0.00%';

function header(...labels: string[]): Row {
    return labels.map((value) => ({ value, fontWeight: 'bold' }));
}

function numberCell(value: number | null | undefined, format?: string): Cell {
    return typeof value === 'number' && Number.isFinite(value) ? { value, format } : null;
}

function breakdownMetric(value: number, format?: string): Cell {
    return value >= 0 ? numberCell(value, format) : null;
}

export function analyticsReportSheets(data: AnalyticsData, rows: FraudResultSummary[]): SheetData[] {
    const { fromTime, toTime, currency, merchantId, shopId, splitUnit } = data.parameters;
    const breakdown: SheetData = [
        header('Status', 'Template', 'Checked rule', 'Sum', 'Count', 'Ratio'),
        ...rows.map(
            ({ status, template, checkedRule, summary }): Row => [
                { value: status?.toLowerCase() ?? '' },
                { value: template ?? '' },
                { value: ruleLabel(checkedRule) },
                breakdownMetric(summary.sum, AMOUNT_FORMAT),
                breakdownMetric(summary.count),
                breakdownMetric(summary.ratio, PERCENT_FORMAT),
            ]
        ),
    ];

    const overview: SheetData = [
        header('Report parameters', 'Value'),
        [{ value: 'From (UTC)' }, { value: fromTime }],
        [{ value: 'To (UTC)' }, { value: toTime }],
        [{ value: 'Currency' }, { value: currency }],
        [{ value: 'Merchant ID' }, { value: merchantId ?? '' }],
        [{ value: 'Shop ID' }, { value: shopId ?? '' }],
        [{ value: 'Granularity' }, { value: splitUnit ?? '' }],
        [],
        header('Key metrics', 'Value'),
        [{ value: 'Attempted payments' }, numberCell(data.attempted)],
        [{ value: 'Change vs. previous period' }, numberCell(data.attemptedChange, PERCENT_FORMAT)],
        [{ value: 'Blocked payments' }, numberCell(data.blocked)],
        [{ value: 'Blocked share' }, numberCell(data.ratio, PERCENT_FORMAT)],
        [{ value: 'Blocked amount' }, numberCell(data.blockedAmount, AMOUNT_FORMAT)],
        [{ value: 'Decline decisions' }, numberCell(data.declineCount)],
        [],
        header('Top declined rules'),
        header('Template', 'Checked rule', 'Decisions'),
        ...data.topDeclines.map(
            ({ template, checkedRule, summary }): Row => [
                { value: template ?? '' },
                { value: ruleLabel(checkedRule) },
                breakdownMetric(summary.count),
            ]
        ),
        [],
        header('Risk distribution'),
        header('Period (UTC)', ...data.chart.series.map((series) => series.name ?? '')),
    ];

    const riskRows = new Map<string | number, Row>();
    data.chart.series.forEach((series, index) => {
        for (const { x, y } of series.data) {
            const period = x instanceof Date ? x.toISOString() : x;
            let row = riskRows.get(period);
            if (!row) {
                row = [{ value: period }, ...Array<Cell>(data.chart.series.length).fill(null)];
                riskRows.set(period, row);
            }
            row[index + 1] = numberCell(typeof y === 'number' ? y / 100 : null, PERCENT_FORMAT);
        }
    });
    return [breakdown, overview.concat(Array.from(riskRows.values()))];
}

export async function createAnalyticsReport(data: AnalyticsData, rows: FraudResultSummary[]): Promise<Blob> {
    const { default: writeXlsxFile } = await import('write-excel-file/universal');
    return writeXlsxFile(analyticsReportSheets(data, rows), {
        sheets: SHEET_NAMES,
        columns: [
            [{ width: 18 }, { width: 36 }, { width: 46 }, { width: 16 }, { width: 14 }, { width: 14 }],
            [{ width: 34 }, { width: 44 }, { width: 16 }, { width: 16 }, { width: 16 }],
        ],
    });
}
