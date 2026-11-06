import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { debounceTime, finalize, map, switchMap } from 'rxjs/operators';

import { PayoutGroupReference } from '../../../../api/fb-management/swagger-codegen/model/payoutGroupReference';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { describePayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-group-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutGroupReferencesComponent {
    search$ = new BehaviorSubject<string>('');
    inProgress$ = new BehaviorSubject<boolean>(false);
    searchForm = this.fb.group({ searchQuery: [''] });
    references$ = this.search$.pipe(
        switchMap((searchValue) => {
            this.inProgress$.next(true);
            return this.payoutsService
                .findGroupReferences({ searchValue, size: 100 })
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
        void this.router.navigate(['/payouts/group-reference/new'], {
            queryParams: { returnUrl: this.router.url },
        });
    }

    goToGroup(groupId: string) {
        void this.router.navigate([`/payouts/group/${groupId}`], { queryParams: { returnUrl: this.router.url } });
    }

    removeReference(reference: PayoutGroupReference) {
        this.feedback
            .confirm(`Remove the reference of ${describePayoutScope(reference)} to group ${reference.groupId}?`)
            .pipe(switchMap(() => this.payoutsService.removeGroupReference(reference)))
            .subscribe({
                next: () => this.search$.next(this.search$.value),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
