import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { Refund } from '../../../../../../../api/fb-management/swagger-codegen/model/refund';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../../../../tokens';

@Component({
    selector: 'fb-historical-data-refunds-item',
    templateUrl: 'historical-data-refunds-item.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataRefundsItemComponent {
    @Input()
    refund: Refund;

    constructor(@Inject(LAYOUT_GAP_S) public layoutGapS: string, @Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
