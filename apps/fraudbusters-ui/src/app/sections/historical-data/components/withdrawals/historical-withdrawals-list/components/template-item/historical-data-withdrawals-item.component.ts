import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { Withdrawal } from '../../../../../../../api/fb-management/swagger-codegen/model/withdrawal';
import { LAYOUT_GAP_M } from '../../../../../../../tokens';

@Component({
    selector: 'fb-historical-data-withdrawals-item',
    templateUrl: 'historical-data-withdrawals-item.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataWithdrawalsItemComponent {
    @Input()
    withdrawal: Withdrawal;

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
