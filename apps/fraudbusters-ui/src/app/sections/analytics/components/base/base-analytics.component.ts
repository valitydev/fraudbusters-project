import { DOCUMENT } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Papa } from 'ngx-papaparse';

import { AnalyticsData, BaseAnalyticsService } from './services/base-analytics.service';
import { FraudResultSummary } from '../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';

@Component({
    templateUrl: 'base-analytics.component.html',
    styleUrls: ['./base-analytics.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BaseAnalyticsComponent {
    readonly state$ = this.analytics.state$;
    readonly statuses = ['All', 'decline', 'three_ds', 'accept'];
    readonly search = new FormControl('', { nonNullable: true });
    status = 'All';
    /** The blocked metrics count payments, not rule decisions (see AnalyticsQuery in fraudbusters-management). */
    readonly blockedDefinition =
        'Payments that failed in processing with no_route_found:risk_score_is_too_high. ' +
        'Rule decisions without a processed payment are not counted here.';
    selectedRule: { template?: string; checkedRule?: string } | null = null;

    private readonly document = inject(DOCUMENT);
    private readonly papa = inject(Papa);

    constructor(private analytics: BaseAnalyticsService) {}

    retry(): void {
        this.analytics.retry();
    }

    toggleRule(row: FraudResultSummary): void {
        this.selectedRule = this.isSelected(row) ? null : { template: row.template, checkedRule: row.checkedRule };
        if (this.selectedRule && this.status !== 'decline') {
            this.status = 'All';
        }
    }

    isSelected(row: FraudResultSummary): boolean {
        return (
            !!this.selectedRule &&
            this.selectedRule.template === row.template &&
            this.selectedRule.checkedRule === row.checkedRule
        );
    }

    setStatus(status: string): void {
        this.status = status;
        if (status !== 'All' && status !== 'decline') {
            this.selectedRule = null;
        }
    }

    resetBreakdown(): void {
        this.status = 'All';
        this.selectedRule = null;
        this.search.reset();
    }

    visibleRows(rows: FraudResultSummary[]): FraudResultSummary[] {
        const query = this.search.value.trim().toLowerCase();
        return rows.filter(
            (row) =>
                (this.status === 'All' || row.status?.toLowerCase() === this.status) &&
                (!this.selectedRule || (this.isSelected(row) && row.status?.toLowerCase() === 'decline')) &&
                `${row.template ?? ''} ${row.checkedRule ?? ''}`.toLowerCase().includes(query)
        );
    }

    exportReport(data?: AnalyticsData): void {
        if (!data) {
            return;
        }
        const { fromTime, toTime, currency, merchantId, shopId, splitUnit } = data.parameters;
        const safeCell = (value: string | number) =>
            typeof value === 'string' && /^[\t\r\n ]*[=+\-@]|^[\t\r\n]/.test(value) ? "'" + value : value;
        const rows = [
            ['From (UTC)', fromTime],
            ['To (UTC)', toTime],
            ['Currency', currency],
            ['Merchant ID', merchantId ?? ''],
            ['Shop ID', shopId ?? ''],
            ['Granularity', splitUnit],
            [],
            ['Attempted payments', data.attempted],
            ['Change vs. previous period (%)', data.attemptedChange === null ? '' : data.attemptedChange * 100],
            ['Blocked payments', data.blocked],
            ['Blocked share (%)', data.ratio === null ? '' : data.ratio * 100],
            ['Blocked amount', data.blockedAmount],
            [],
            ['Status', 'Template', 'Checked rule', 'Sum', 'Currency', 'Count', 'Ratio (%)'],
            ...this.visibleRows(data.summaries).map(({ status, template, checkedRule, summary }) => [
                status?.toLowerCase() ?? '',
                template ?? '',
                checkedRule ?? '',
                summary.sum >= 0 ? summary.sum : '',
                currency,
                summary.count >= 0 ? summary.count : '',
                summary.ratio >= 0 && Number.isFinite(summary.ratio) ? summary.ratio * 100 : '',
            ]),
        ];
        const csv = this.papa.unparse(rows.map((row) => row.map(safeCell)));
        const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
        const link = this.document.createElement('a');
        link.href = url;
        link.download = `analytics-${fromTime.slice(0, 10)}-${toTime.slice(0, 10)}.csv`;
        link.click();
        setTimeout(() => URL.revokeObjectURL(url), 0);
    }
}
