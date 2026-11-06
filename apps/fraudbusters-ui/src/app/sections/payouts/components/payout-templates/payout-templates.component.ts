import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { debounceTime, finalize, map, switchMap } from 'rxjs/operators';

import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../tokens';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-templates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutTemplatesComponent {
    search$ = new BehaviorSubject<string>('');
    inProgress$ = new BehaviorSubject<boolean>(false);
    searchForm = this.fb.group({ searchQuery: [''] });
    templates$ = this.search$.pipe(
        switchMap((searchValue) => {
            this.inProgress$.next(true);
            return this.payoutsService
                .findTemplates({ searchValue, size: 100 })
                .pipe(finalize(() => this.inProgress$.next(false)));
        }),
        map((response) => response.result)
    );

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private router: Router,
        private fb: FormBuilder,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string,
        @Inject(LAYOUT_GAP_S) public layoutGapS: string
    ) {
        this.searchForm.controls.searchQuery.valueChanges
            .pipe(debounceTime(300))
            .subscribe((value) => this.search$.next((value ?? '').trim()));
    }

    createTemplate() {
        void this.router.navigate(['/payouts/template/new'], { queryParams: { returnUrl: this.router.url } });
    }

    editTemplate(id: string) {
        void this.router.navigate([`/payouts/template/${id}`], { queryParams: { returnUrl: this.router.url } });
    }

    copyTemplate(id: string) {
        void this.router.navigate([`/payouts/template/new/${id}`], { queryParams: { returnUrl: this.router.url } });
    }

    removeTemplate(id: string) {
        this.feedback
            .confirm(`Remove template ${id}?`)
            .pipe(switchMap(() => this.payoutsService.removeTemplate(id)))
            .subscribe({
                next: () => this.search$.next(this.search$.value),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
