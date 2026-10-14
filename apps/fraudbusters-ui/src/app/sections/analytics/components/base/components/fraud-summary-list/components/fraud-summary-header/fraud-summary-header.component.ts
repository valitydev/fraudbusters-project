import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../../../../../tokens';

@Component({
    selector: 'fb-fraud-summary-header',
    templateUrl: 'fraud-summary-header.component.html',
    styleUrls: ['fraud-summary-header.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FraudSummaryHeaderComponent {
    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
