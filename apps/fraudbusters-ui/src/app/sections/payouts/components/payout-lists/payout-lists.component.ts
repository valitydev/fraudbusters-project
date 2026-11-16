import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, switchMap } from 'rxjs/operators';

import { ListType } from '../../../../api/fb-management/swagger-codegen/model/listType';
import { PayoutListRow } from '../../../../api/fb-management/swagger-codegen/model/payoutListRow';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { PAYOUT_PAGE_SIZE, PayoutPageFetcher } from '../../services/payout-page-fetcher';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

interface ListFilter {
    listNames: string[];
    searchValue: string;
}

/** One payout list; the tab fixes the list type, as each payment list has its own tab. */
@Component({
    templateUrl: 'payout-lists.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutListsComponent {
    listType = this.route.snapshot.data.listType as ListType;
    isGrey = this.listType === ListType.Grey;
    searchForm = this.fb.group({ types: [[] as string[]], searchQuery: [''] });
    listNames$ = this.payoutsService.getCurrentListNames(this.listType);
    readonly fetcher = new PayoutPageFetcher<PayoutListRow, ListFilter>((filter, lastId) =>
        this.payoutsService.findListRows({ listType: this.listType, ...filter, lastId, size: PAYOUT_PAGE_SIZE })
    );
    rows$ = this.fetcher.searchResult$;
    hasMore$ = this.fetcher.hasMore$;
    inProgress$ = this.fetcher.inProgress$;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.searchForm.valueChanges
            .pipe(debounceTime(300))
            .subscribe(({ types, searchQuery }) =>
                this.fetcher.search({ listNames: types ?? [], searchValue: (searchQuery ?? '').trim() })
            );
        this.fetcher.search({ listNames: [], searchValue: '' });
    }

    fetchMore() {
        this.fetcher.fetchMore();
    }

    createRow() {
        void this.router.navigate([`/payouts/list/new/${this.listType}`], {
            queryParams: { returnUrl: this.router.url },
        });
    }

    removeRow(row: PayoutListRow) {
        this.feedback
            .confirm(`Remove ${row.listRecord.value} from ${row.listRecord.listName}?`)
            .pipe(switchMap(() => this.payoutsService.removeListRow(row.id)))
            .subscribe({
                next: () => this.fetcher.refresh(),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
