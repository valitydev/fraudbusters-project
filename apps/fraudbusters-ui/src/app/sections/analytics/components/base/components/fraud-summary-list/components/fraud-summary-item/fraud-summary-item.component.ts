import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { FraudResultSummary } from '../../../../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../../../../../tokens';

@Component({
    selector: 'fb-fraud-summary-item',
    templateUrl: 'fraud-summary-item.component.html',
    styleUrls: ['fraud-summary-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FraudSummaryItemComponent {
    @Input()
    summary: FraudResultSummary;

    constructor(@Inject(LAYOUT_GAP_S) public layoutGapS: string, @Inject(LAYOUT_GAP_M) public layoutGapM: string) {}

    numberFormat(num) {
        return num === 'Infinity' ? 100 : num;
    }
}
