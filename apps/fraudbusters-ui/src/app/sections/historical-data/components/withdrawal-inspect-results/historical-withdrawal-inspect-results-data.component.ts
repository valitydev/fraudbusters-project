import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../tokens';
import { FetchHistoricalWithdrawalInspectResultsService } from '../../services/fetch-historical-withdrawal-inspect-results.service';

@Component({
    templateUrl: 'historical-withdrawal-inspect-results-data.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalWithdrawalInspectResultsDataComponent {
    inspectResults$ = this.fetchWithdrawalInspectResultsService.searchResult$;
    inProgress$ = this.fetchWithdrawalInspectResultsService.inProgress$;
    hasMore$ = this.fetchWithdrawalInspectResultsService.hasMore$;

    constructor(
        private fetchWithdrawalInspectResultsService: FetchHistoricalWithdrawalInspectResultsService,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    search(event) {
        this.fetchWithdrawalInspectResultsService.search({
            partyId: event.partyId,
            walletId: event.walletId,
            withdrawalId: event.paymentId,
            status: event.status,
            cardToken: event.cardToken,
            fingerprint: event.fingerprint,
            customerId: event.customerId,
            userId: event.userId,
            email: event.email,
            template: event.template,
            rule: event.rule,
            from: event.from,
            to: event.to,
        });
    }

    fetchMore() {
        this.fetchWithdrawalInspectResultsService.fetchMore();
    }
}
