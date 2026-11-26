import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { FraudResultSummary } from '../../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';

@Component({
    selector: 'fb-fraud-summary-templates-list',
    templateUrl: 'fraud-summary-list.component.html',
    styleUrls: ['./fraud-summary-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class FraudSummaryListComponent {
    @Input()
    set summaryRows(rows: FraudResultSummary[]) {
        this.rows = rows;
        this.totalSum = this.total('sum');
        this.totalCount = this.total('count');
        this.totalRatio = this.total('ratio');
    }

    get summaryRows(): FraudResultSummary[] {
        return this.rows;
    }

    totalSum: number | null;
    totalCount: number | null;
    totalRatio: number | null;

    @Input()
    currency: string;

    private rows: FraudResultSummary[] = [];

    private total(field: 'sum' | 'count' | 'ratio'): number | null {
        const values = this.rows.map((row) => row.summary[field]);
        return values.every((value) => Number.isFinite(value) && value >= 0)
            ? values.reduce((total, value) => total + value, 0)
            : null;
    }
}
