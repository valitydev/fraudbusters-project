import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';

import { WithdrawalInspectResult } from '../../../../../api/fb-management/swagger-codegen/model/withdrawalInspectResult';
import { LAYOUT_GAP_M } from '../../../../../tokens';

@Component({
    selector: 'fb-historical-data-withdrawal-inspect-results-list',
    templateUrl: 'historical-data-withdrawal-inspect-results-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataWithdrawalInspectResultsListComponent {
    @Input()
    inspectResults: WithdrawalInspectResult[];

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
