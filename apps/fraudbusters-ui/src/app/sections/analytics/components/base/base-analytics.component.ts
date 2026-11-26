import { ChangeDetectionStrategy, Component, DOCUMENT, inject, signal } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { createAnalyticsReport } from './analytics-report';
import { AnalyticsData, BaseAnalyticsService } from './services/base-analytics.service';
import { FraudResultSummary } from '../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';

@Component({
    templateUrl: 'base-analytics.component.html',
    styleUrls: ['./base-analytics.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class BaseAnalyticsComponent {
    readonly state$ = this.analytics.state$;
    readonly statuses = ['All', 'decline', 'three_ds', 'accept', 'trust'];
    readonly exporting = signal(false);
    readonly search = new FormControl('', { nonNullable: true });
    status = 'All';
    /** The blocked metrics count payments, not rule decisions (see AnalyticsQuery in fraudbusters-management). */
    readonly blockedDefinition =
        'Payments that failed in processing with no_route_found:risk_score_is_too_high. ' +
        'Rule decisions without a processed payment are not counted here.';
    selectedRule: { template?: string; checkedRule?: string } | null = null;

    private readonly document = inject(DOCUMENT);
    private readonly snackBar = inject(MatSnackBar);

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

    async exportReport(data?: AnalyticsData): Promise<void> {
        if (!data || this.exporting()) {
            return;
        }
        this.exporting.set(true);
        try {
            const { fromTime, toTime } = data.parameters;
            const blob = await createAnalyticsReport(data, this.visibleRows(data.summaries));
            const url = URL.createObjectURL(blob);
            const link = this.document.createElement('a');
            link.href = url;
            link.download = `analytics-${fromTime.slice(0, 10)}-${toTime.slice(0, 10)}.xlsx`;
            link.click();
            setTimeout(() => URL.revokeObjectURL(url), 0);
        } catch {
            this.snackBar.open('Could not export the analytics report.', 'Dismiss', { duration: 4000 });
        } finally {
            this.exporting.set(false);
        }
    }
}
