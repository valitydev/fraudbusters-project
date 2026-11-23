import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { PayoutInspectResult } from '../../../../../api/fb-management/swagger-codegen/model/payoutInspectResult';
import { LAYOUT_GAP_M } from '../../../../../tokens';

@Component({
    selector: 'fb-historical-data-payout-inspect-results-list',
    templateUrl: 'historical-data-payout-inspect-results-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataPayoutInspectResultsListComponent {
    @Input()
    inspectResults: PayoutInspectResult[];

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
