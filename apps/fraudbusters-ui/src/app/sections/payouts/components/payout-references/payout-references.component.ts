import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { debounceTime, finalize, map, switchMap } from 'rxjs/operators';

import { PayoutReference } from '../../../../api/fb-management/swagger-codegen/model/payoutReference';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { describePayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutReferencesComponent {
    search$ = new BehaviorSubject<string>('');
    inProgress$ = new BehaviorSubject<boolean>(false);
    searchForm = this.fb.group({ searchQuery: [''] });
    references$ = this.search$.pipe(
        switchMap((searchValue) => {
            this.inProgress$.next(true);
            return this.payoutsService
                .findReferences({ searchValue, size: 100 })
                .pipe(finalize(() => this.inProgress$.next(false)));
        }),
        map((response) => response.result)
    );

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private router: Router,
        private fb: FormBuilder,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.searchForm.controls.searchQuery.valueChanges
            .pipe(debounceTime(300))
            .subscribe((value) => this.search$.next((value ?? '').trim()));
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
                next: () => this.search$.next(this.search$.value),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
