import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { FraudResultSummary } from '../../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';
import { LAYOUT_GAP_M } from '../../../../../../tokens';

@Component({
    selector: 'fb-fraud-summary-templates-list',
    templateUrl: 'fraud-summary-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FraudSummaryListComponent {
    @Input()
    summaryRows: FraudResultSummary[];

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
