import { ChangeDetectionStrategy, Component, DestroyRef, Input, OnInit, Optional } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, merge, Observable, of, Subject } from 'rxjs';
import { catchError, distinctUntilChanged, filter, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { FetchWbListService } from './services/fetch-wb-list.service';
import { ListsEventsService } from './services/lists-events.service';
import { RemoveWbListComponentService } from './services/remove-wb-list.service';
import { WbListRecord } from '../../../api/fb-management/swagger-codegen/model/wbListRecord';
import { PaymentListsService } from '../../../api/payments/lists';
import { ListType } from '../../constants/list-type';
import { SortOrder } from '../../constants/sort-order';
import { ListRecordRow, paymentListRow } from '../list-records-table/list-record-row';

const LIST_LABELS: Record<string, string> = {
    [ListType.Black]: 'Black',
    [ListType.White]: 'White',
    [ListType.Grey]: 'Grey',
};

/** One payment list tab: filters, a catalog table of records and server-side paging. */
@Component({
    selector: 'fb-wb-list',
    templateUrl: './wb-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WbListComponent implements OnInit {
    @Input() listType: ListType;

    readonly sortOptions = [
        { value: SortOrder.Desc, label: 'Inserted at (newest)' },
        { value: SortOrder.Asc, label: 'Inserted at (oldest)' },
    ];

    readonly page$: Observable<{ rows: ListRecordRow<WbListRecord>[]; count: number }>;

    readonly inProgress$ = this.fetchWbListService.inProgress$;
    listNames$: Observable<string[]>;
    sort = SortOrder.Desc;
    listName = '';

    private searchValue = '';
    /** List names of this type; null until loaded. The backend matches only the names it is given. */
    private allNames: string[] | null = null;
    private readonly reloadNames$ = new BehaviorSubject<void>(undefined);
    /** Emits when the list has no names at all, so there is nothing to ask the backend for. */
    private readonly cleared$ = new Subject<void>();

    constructor(
        private route: ActivatedRoute,
        private fetchWbListService: FetchWbListService,
        private removeWbListComponentService: RemoveWbListComponentService,
        private listService: PaymentListsService,
        private snackBar: MatSnackBar,
        private destroyRef: DestroyRef,
        @Optional() private listsEvents?: ListsEventsService
    ) {
        const empty = { rows: [] as ListRecordRow<WbListRecord>[], count: 0 };
        this.page$ = merge(
            this.fetchWbListService.fetchResultChanges$.pipe(
                map(({ result, count }) => ({ rows: result.map(paymentListRow), count: count ?? result.length }))
            ),
            this.cleared$.pipe(map(() => empty))
        ).pipe(startWith(empty));
    }

    get label(): string {
        return (LIST_LABELS[this.listType] ?? this.listType) + ' list';
    }

    get isGrey(): boolean {
        return this.listType === ListType.Grey;
    }

    ngOnInit(): void {
        this.listNames$ = this.reloadNames$.pipe(
            switchMap(() => this.listService.getNames(this.listType).pipe(catchError(() => of([] as string[])))),
            shareReplay({ bufferSize: 1, refCount: true })
        );
        this.listNames$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((names) => {
            this.allNames = names;
            this.search();
        });
        this.route.queryParamMap
            .pipe(
                map((params) => (params.get('searchQuery') ?? '').trim()),
                distinctUntilChanged(),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe((query) => {
                this.searchValue = query;
                this.search();
            });
        this.removeWbListComponentService.removed$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
            this.snackBar.open('Record removed', 'OK', { duration: 1500 });
            this.reloadNames$.next();
            this.listsEvents?.changed(this.listType);
        });
        this.listsEvents?.changed$
            .pipe(
                filter((type) => type === this.listType),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe(() => this.reloadNames$.next());
    }

    setSort(order: SortOrder): void {
        this.sort = order;
        this.search();
    }

    setListName(name: string): void {
        this.listName = name;
        this.search();
    }

    fetchMore(): void {
        this.fetchWbListService.fetchMore();
    }

    removeRow(record: WbListRecord): void {
        this.removeWbListComponentService.remove({
            rowId: record.id,
            title: `Remove ${record.value ?? record.id} from the ${LIST_LABELS[this.listType] ?? ''} list?`,
        });
    }

    private search(): void {
        // "All lists" means every name of this type: an empty name filter matches nothing on the backend.
        const listNames = this.listName ? [this.listName] : this.allNames;
        if (!listNames?.length) {
            if (listNames) {
                this.cleared$.next();
            }
            return;
        }
        this.fetchWbListService.search({
            listType: this.listType,
            sortOrder: this.sort,
            listNames,
            ...(this.searchValue ? { searchValue: this.searchValue } : {}),
        });
    }
}
