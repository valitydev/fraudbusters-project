import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NEVER } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Chargeback } from '../../../../api/fb-management/swagger-codegen/model/chargeback';
import { PaymentListsService } from '../../../../api/payments/lists';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { FetchHistoricalChargebacksService } from '../../services/fetch-historical-chargebacks.service';

@Component({
    templateUrl: 'historical-chargebacks-data.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [PaymentListsService],
})
export class HistoricalChargebacksDataComponent {
    chargebacks$ = this.fetchHistoricalChargebacksService.searchResult$;
    inProgress$ = this.fetchHistoricalChargebacksService.inProgress$;
    hasMore$ = this.fetchHistoricalChargebacksService.hasMore$;

    selectedChargebacks$: Array<Chargeback> = new Array<Chargeback>();

    constructor(
        private fetchHistoricalChargebacksService: FetchHistoricalChargebacksService,
        private paymentListsService: PaymentListsService,
        private snackBar: MatSnackBar,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    search(event) {
        this.fetchHistoricalChargebacksService.search(this.initParams(event));
    }

    fetchMore() {
        this.fetchHistoricalChargebacksService.fetchMore();
    }

    checkChargebacks($event) {
        this.selectedChargebacks$ = $event;
    }

    addToWbListCandidate() {
        this.paymentListsService
            .createCandidatesByChargebacks(this.selectedChargebacks$)
            .pipe(
                catchError((error: HttpErrorResponse) => {
                    this.snackBar.open(`${error.status}: ${error.message}`, 'ERROR', {
                        duration: 1500,
                    });
                    return NEVER;
                })
            )
            .subscribe((value) => {
                if (value === 'OK') {
                    this.snackBar.open(`List candidates send to review`, 'OK', {
                        duration: 1000,
                    });
                }
            });
    }

    private initParams(event) {
        return {
            paymentId: event.paymentId,
            cardToken: event.cardToken,
            shopId: event.shopId,
            partyId: event.partyId,
            status: event.status,
            fingerprint: event.fingerprint,
            email: event.email,
            terminal: event.terminal,
            maskedPan: event.maskedPan,
            from: event.from,
            to: event.to,
        };
    }
}
