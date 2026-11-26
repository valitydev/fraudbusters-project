import { ChangeDetectionStrategy, Component, DestroyRef, Input, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map, startWith } from 'rxjs/operators';

import { FetchWbListService } from './services/fetch-wb-list.service';
import { ListsEventsService } from './services/lists-events.service';
import { RemoveWbListComponentService } from './services/remove-wb-list.service';
import { WbListRecord } from '../../../api/fb-management/swagger-codegen/model/wbListRecord';
import { ListType } from '../../constants/list-type';
import { SortOrder } from '../../constants/sort-order';
import { catalogSearchQuery } from '../../utils/catalog-collection';
import { CatalogMetadataService } from '../catalog-panel/catalog-metadata.service';
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
    standalone: false,
})
export class WbListComponent implements OnInit {
    @Input() listType: ListType;

    readonly sortOptions = [
        { value: SortOrder.Desc, label: 'Inserted at (newest)' },
        { value: SortOrder.Asc, label: 'Inserted at (oldest)' },
    ];

    readonly page$: Observable<{ rows: ListRecordRow<WbListRecord>[]; count: number }>;

    readonly inProgress$: Observable<boolean>;
    listNames$: Observable<string[]>;
    sort = SortOrder.Desc;
    listName = '';

    private searchValue = '';
    constructor(
        private route: ActivatedRoute,
        private fetchWbListService: FetchWbListService,
        private removeWbListComponentService: RemoveWbListComponentService,
        private snackBar: MatSnackBar,
        private destroyRef: DestroyRef,
        private listsEvents: ListsEventsService,
        private metadata: CatalogMetadataService
    ) {
        this.page$ = this.fetchWbListService.fetchResultChanges$.pipe(
            map(({ result, count }) => ({ rows: result.map(paymentListRow), count: count ?? result.length })),
            startWith({ rows: [], count: 0 })
        );
        this.inProgress$ = this.fetchWbListService.inProgress$;
    }

    get label(): string {
        return (LIST_LABELS[this.listType] ?? this.listType) + ' list';
    }

    get isGrey(): boolean {
        return this.listType === ListType.Grey;
    }

    ngOnInit(): void {
        this.listNames$ = this.metadata.listNames(this.listType);
        catalogSearchQuery(this.route)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((query) => {
                this.searchValue = query;
                this.search();
            });
        this.removeWbListComponentService.removed$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
            this.snackBar.open('Record removed', 'OK', { duration: 1500 });
            this.listsEvents.changed(this.listType);
        });
        this.listsEvents.changed$
            .pipe(
                filter((type) => type === this.listType),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe(() => this.search());
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
        this.fetchWbListService.search({
            listType: this.listType,
            sortOrder: this.sort,
            ...(this.listName ? { listNames: [this.listName] } : {}),
            ...(this.searchValue ? { searchValue: this.searchValue } : {}),
        });
    }
}
