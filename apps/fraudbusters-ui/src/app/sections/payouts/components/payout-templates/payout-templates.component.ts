import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, map, switchMap } from 'rxjs/operators';

import { Template } from '../../../../api/fb-management/swagger-codegen/model/template';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../tokens';
import { PAYOUT_PAGE_SIZE, PayoutPageFetcher } from '../../services/payout-page-fetcher';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-templates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutTemplatesComponent {
    searchForm = this.fb.group({ searchQuery: [''] });
    readonly fetcher = new PayoutPageFetcher<Template, string>((searchValue, lastId) =>
        this.payoutsService.findTemplates({ searchValue, lastId, size: PAYOUT_PAGE_SIZE })
    );
    templates$ = this.fetcher.searchResult$;
    hasMore$ = this.fetcher.hasMore$;
    inProgress$ = this.fetcher.inProgress$;

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private router: Router,
        private fb: FormBuilder,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string,
        @Inject(LAYOUT_GAP_S) public layoutGapS: string
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
                next: () => this.fetcher.refresh(),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
