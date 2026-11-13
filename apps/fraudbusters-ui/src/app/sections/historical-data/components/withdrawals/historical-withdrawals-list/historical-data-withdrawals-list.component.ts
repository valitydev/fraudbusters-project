import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { Withdrawal } from '../../../../../api/fb-management/swagger-codegen/model/withdrawal';
import { LAYOUT_GAP_M } from '../../../../../tokens';

@Component({
    selector: 'fb-historical-data-withdrawals-list',
    templateUrl: 'historical-data-withdrawals-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataWithdrawalsListComponent {
    @Input()
    withdrawals: Withdrawal[];

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
