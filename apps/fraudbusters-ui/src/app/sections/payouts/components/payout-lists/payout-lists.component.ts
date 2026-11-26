import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, EventEmitter, Output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { ListType } from '../../../../api/fb-management/swagger-codegen/model/listType';
import { PayoutListRow } from '../../../../api/fb-management/swagger-codegen/model/payoutListRow';
import { PayoutsService } from '../../../../api/payouts';
import { ListRecordRow, payoutListRow } from '../../../../shared/components/list-records-table/list-record-row';
import { SortOrder } from '../../../../shared/constants/sort-order';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';
import { PAYOUT_PAGE_SIZE, PayoutPageFetcher } from '../../services/payout-page-fetcher';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

interface ListFilter {
    listNames: string[];
    searchValue: string;
    sortOrder: SortOrder;
}

@Component({
    templateUrl: 'payout-lists.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class PayoutListsComponent {
    readonly listType = this.route.snapshot.data.listType as ListType;
    readonly isGrey = this.listType === ListType.Grey;
    readonly listNames = new FormControl<string[]>([], { nonNullable: true });
    readonly sort = new BehaviorSubject(SortOrder.Desc);
    readonly sortOptions = [
        { value: SortOrder.Desc, label: 'Inserted at (newest)' },
        { value: SortOrder.Asc, label: 'Inserted at (oldest)' },
    ];
    readonly listNames$: Observable<string[]>;
    readonly failed$ = new BehaviorSubject(false);
    readonly page$: Observable<{ rows: ListRecordRow<PayoutListRow>[]; count: number }>;
    readonly inProgress$: Observable<boolean>;
    @Output() readonly removed = new EventEmitter<void>();

    private readonly reloadNames = new BehaviorSubject<void>(undefined);
    private readonly fetcher: PayoutPageFetcher<PayoutListRow, ListFilter>;

    constructor(
        private route: ActivatedRoute,
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private destroyRef: DestroyRef
    ) {
        this.listNames$ = this.reloadNames.pipe(
            switchMap(() =>
                this.payoutsService.getCurrentListNames(this.listType).pipe(catchError(() => of([] as string[])))
            ),
            shareReplay({ bufferSize: 1, refCount: true })
        );
        this.fetcher = new PayoutPageFetcher<PayoutListRow, ListFilter>((filter, lastId) => {
            this.failed$.next(false);
            return this.payoutsService
                .findListRows({ listType: this.listType, ...filter, lastId, size: PAYOUT_PAGE_SIZE })
                .pipe(
                    catchError(() => {
                        this.failed$.next(true);
                        return of({ result: [], count: 0 });
                    })
                );
        });
        this.page$ = this.fetcher.fetchResultChanges$.pipe(
            map(({ result, count }) => ({ rows: result.map(payoutListRow), count: count ?? result.length })),
            startWith({ rows: [], count: 0 })
        );
        this.inProgress$ = this.fetcher.inProgress$;

        const query$ = catalogSearchQuery(this.route);
        combineLatest([this.listNames.valueChanges.pipe(startWith(this.listNames.value)), query$, this.sort])
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(([listNames, searchValue, sortOrder]) =>
                this.fetcher.search({ listNames, searchValue, sortOrder })
            );
    }

    setSort(order: string): void {
        this.sort.next(this.sortOptions.find((option) => String(option.value) === order)?.value ?? SortOrder.Desc);
    }

    fetchMore(): void {
        this.fetcher.fetchMore();
    }

    retry(): void {
        this.fetcher.refresh();
    }

    removeRow(row: PayoutListRow): void {
        this.feedback
            .confirm(`Remove ${row.listRecord.value} from ${row.listRecord.listName}?`)
            .pipe(
                switchMap(() => this.payoutsService.removeListRow(row.id)),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe({
                next: () => {
                    this.reloadNames.next();
                    this.fetcher.refresh();
                    this.removed.emit();
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
