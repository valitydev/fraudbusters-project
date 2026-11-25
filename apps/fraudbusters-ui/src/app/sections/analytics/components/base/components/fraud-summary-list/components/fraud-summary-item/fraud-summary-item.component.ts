import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { FraudResultSummary } from '../../../../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';
import { statusPillClass } from '../../../../../../../../shared/utils/status-tone';

@Component({
    selector: 'fb-fraud-summary-item',
    templateUrl: 'fraud-summary-item.component.html',
    styleUrls: ['fraud-summary-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FraudSummaryItemComponent {
    @Input()
    summary: FraudResultSummary;

    @Input()
    currency: string;

    get ruleIndex(): string | undefined {
        return /^(?:Rule #)?(\d+)$/.exec(this.summary.checkedRule ?? '')?.[1];
    }

    pill(status: string): string {
        return statusPillClass(status);
    }

    metricValue(value: number): number | null {
        return Number.isFinite(value) && value >= 0 ? value : null;
    }
}
