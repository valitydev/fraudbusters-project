import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { Payout } from '../../../../../api/fb-management/swagger-codegen/model/payout';
import { LAYOUT_GAP_M } from '../../../../../tokens';

@Component({
    selector: 'fb-historical-data-payouts-list',
    templateUrl: 'historical-data-payouts-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataPayoutsListComponent {
    @Input()
    payouts: Payout[];

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
