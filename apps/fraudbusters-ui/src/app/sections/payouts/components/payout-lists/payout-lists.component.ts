import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { debounceTime, finalize, map, switchMap } from 'rxjs/operators';

import { ListType } from '../../../../api/fb-management/swagger-codegen/model/listType';
import { PayoutListRow } from '../../../../api/fb-management/swagger-codegen/model/payoutListRow';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
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
    filter$ = new BehaviorSubject<ListFilter>({ listNames: [], searchValue: '' });
    inProgress$ = new BehaviorSubject<boolean>(false);
    searchForm = this.fb.group({ types: [[] as string[]], searchQuery: [''] });
    listNames$ = this.payoutsService.getCurrentListNames(this.listType);
    rows$ = this.filter$.pipe(
        switchMap((filter) => {
            this.inProgress$.next(true);
            return this.payoutsService
                .findListRows({ listType: this.listType, ...filter, size: 100 })
                .pipe(finalize(() => this.inProgress$.next(false)));
        }),
        map((response) => response.result)
    );

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
                this.filter$.next({ listNames: types ?? [], searchValue: (searchQuery ?? '').trim() })
            );
    }

    createRow() {
        void this.router.navigate([`/payouts/list/new/${this.listType}`], {
            queryParams: { returnUrl: this.router.url },
        });
    }

    removeRow(row: PayoutListRow) {
        this.feedback
            .confirm(`Remove ${row.listRecord.value} from ${row.listRecord.listName}?`)
            .pipe(switchMap(() => this.payoutsService.removeListRow(this.listType, row.listRecord)))
            .subscribe({
                next: () => this.filter$.next(this.filter$.value),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
