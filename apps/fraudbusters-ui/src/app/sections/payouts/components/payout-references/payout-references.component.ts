import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, map, switchMap } from 'rxjs/operators';

import { PayoutReference } from '../../../../api/fb-management/swagger-codegen/model/payoutReference';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { PAYOUT_PAGE_SIZE, PayoutPageFetcher } from '../../services/payout-page-fetcher';
import { describePayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutReferencesComponent {
    searchForm = this.fb.group({ searchQuery: [''] });
    readonly fetcher = new PayoutPageFetcher<PayoutReference, string>((searchValue, lastId) =>
        this.payoutsService.findReferences({ searchValue, lastId, size: PAYOUT_PAGE_SIZE })
    );
    references$ = this.fetcher.searchResult$;
    hasMore$ = this.fetcher.hasMore$;
    inProgress$ = this.fetcher.inProgress$;

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private router: Router,
        private fb: FormBuilder,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.searchForm.controls.searchQuery.valueChanges
            .pipe(
                debounceTime(300),
                map((value) => (value ?? '').trim())
            )
            .subscribe((searchValue) => this.fetcher.search(searchValue));
        this.fetcher.search('');
    }

    fetchMore() {
        this.fetcher.fetchMore();
    }

    createReference() {
        void this.router.navigate(['/payouts/reference/new'], { queryParams: { returnUrl: this.router.url } });
    }

    goToTemplate(id: string) {
        void this.router.navigate([`/payouts/template/${id}`], { queryParams: { returnUrl: this.router.url } });
    }

    removeReference(reference: PayoutReference) {
        this.feedback
            .confirm(`Remove the reference of ${describePayoutScope(reference)} to ${reference.templateId}?`)
            .pipe(switchMap(() => this.payoutsService.removeReference(reference)))
            .subscribe({
                next: () => this.fetcher.refresh(),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
