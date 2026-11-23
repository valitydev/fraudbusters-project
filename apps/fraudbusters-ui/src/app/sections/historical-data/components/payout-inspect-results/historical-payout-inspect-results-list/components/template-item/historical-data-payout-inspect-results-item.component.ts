import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { PayoutInspectResult } from '../../../../../../../api/fb-management/swagger-codegen/model/payoutInspectResult';
import { LAYOUT_GAP_M } from '../../../../../../../tokens';

@Component({
    selector: 'fb-historical-data-payout-inspect-results-item',
    templateUrl: 'historical-data-payout-inspect-results-item.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataPayoutInspectResultsItemComponent {
    @Input()
    inspectResult: PayoutInspectResult;

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
