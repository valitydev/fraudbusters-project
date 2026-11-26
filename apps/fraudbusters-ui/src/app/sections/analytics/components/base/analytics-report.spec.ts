import type { Row } from 'write-excel-file/universal';

import { analyticsReportSheets, createAnalyticsReport } from './analytics-report';
import { AnalyticsData } from './services/base-analytics.service';
import { FraudResultSummary } from '../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';

describe('Analytics report', () => {
    const decline: FraudResultSummary = {
        status: 'DECLINE',
        template: '=rule-template',
        checkedRule: '0',
        summary: { sum: 200.25, count: 2, ratio: 0.125 },
    };
    const accept: FraudResultSummary = { status: 'ACCEPT', summary: { sum: -1, count: 14, ratio: 0.875 } };
    const data: AnalyticsData = {
        parameters: { fromTime: '2026-11-01 00:00:00', toTime: '2026-11-01 23:59:59', currency: 'RUB' },
        currency: 'RUB',
        attempted: 16,
        attemptedChange: -0.2,
        blocked: 1,
        blockedAmount: 100,
        ratio: 0.0625,
        declineCount: 2,
        topDeclines: [decline],
        summaries: [accept, decline],
        chart: {
            currency: 'RUB',
            series: [
                {
                    name: 'trusted',
                    data: [
                        { x: '00:00', y: 12.5 },
                        { x: '01:00', y: null },
                    ],
                },
                { name: 'low', data: [{ x: '01:00', y: 0 }] },
            ],
        },
    };

    it('separates the visible rule rows from the metrics, ranking and risk distribution', () => {
        const [breakdown, overview] = analyticsReportSheets(data, [decline]);
        const values = (row: Row) =>
            row.map((cell) => (cell && typeof cell === 'object' && 'value' in cell ? cell.value : undefined));

        expect(breakdown.map(values)).toEqual([
            ['Status', 'Template', 'Checked rule', 'Sum', 'Count', 'Ratio'],
            ['decline', '=rule-template', 'Rule #0', 200.25, 2, 0.125],
        ]);
        expect(breakdown[1][5]).toEqual(jasmine.objectContaining({ format: '0.00%' }));
        const metrics = overview.map(values);
        expect(metrics).toContain(['Change vs. previous period', -0.2]);
        expect(metrics).toContain(['=rule-template', 'Rule #0', 2]);
        expect(metrics).toContain(['00:00', 0.125, undefined]);
        expect(metrics).toContain(['01:00', undefined, 0]);
    });

    it('writes an XLSX workbook even when the breakdown filter returns no rows', async () => {
        const report = await createAnalyticsReport(data, []);

        expect(report.type).toBe('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
        expect(Array.from(new Uint8Array(await report.slice(0, 4).arrayBuffer()))).toEqual([80, 75, 3, 4]);
    });
});
