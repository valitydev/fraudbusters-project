import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { FraudResultSummary } from '../../../../../../api/fb-management/swagger-codegen/model/fraudResultSummary';

@Component({
    selector: 'fb-fraud-summary-templates-list',
    templateUrl: 'fraud-summary-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FraudSummaryListComponent {
    @Input()
    summaryRows: FraudResultSummary[];
}
