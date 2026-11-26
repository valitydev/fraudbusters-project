import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { startWith } from 'rxjs/operators';

import { GroupReference } from '../../../../api/fb-management/swagger-codegen/model/groupReference';
import { GroupTabCounts } from '../../../../shared/components/groups-table/group-tab-counts';
import { OperationType } from '../../../../shared/constants/operation-type';
import { SortOrder } from '../../../../shared/constants/sort-order';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';
import { FetchReferencesService } from '../../services/fetch-references/fetch-references.service';
import { RemoveReferenceService } from '../../services/remove-reference/remove-reference.service';

@Component({
    templateUrl: 'group-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchReferencesService, RemoveReferenceService],
    standalone: false,
})
export class GroupReferencesComponent {
    references$ = this.fetchReferencesService.fetchResultChanges$.pipe(
        startWith({ result: [], hasMore: false, count: 0 })
    );
    inProgress$ = this.fetchReferencesService.inProgress$;
    readonly sortOptions = [
        { value: SortOrder.Asc, label: 'Group ID (A–Z)' },
        { value: SortOrder.Desc, label: 'Group ID (Z–A)' },
    ];
    sort = SortOrder.Asc;
    private searchValue = '';

    constructor(
        route: ActivatedRoute,
        private fetchReferencesService: FetchReferencesService,
        private removeReferenceService: RemoveReferenceService,
        private snackBar: MatSnackBar,
        private tabCounts: GroupTabCounts
    ) {
        const destroyRef = inject(DestroyRef);
        catalogSearchQuery(route)
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe((query) => {
                this.searchValue = query;
                this.search();
            });
        this.removeReferenceService.removed$.pipe(takeUntilDestroyed(destroyRef)).subscribe((id) => {
            this.snackBar.open(`Reference ${id} has been deleted`, 'OK', {
                duration: 1500,
            });
            this.search();
            this.tabCounts.refresh();
        });
    }

    setSort(order: SortOrder): void {
        this.sort = order;
        this.search();
    }

    fetchMore(sortFieldValue: string): void {
        this.fetchReferencesService.fetchMore({ ...this.filters(), sortFieldValue });
    }

    removeReference(reference: GroupReference): void {
        this.removeReferenceService.removeReference({ reference });
    }

    private search(): void {
        this.fetchReferencesService.search(this.filters());
    }

    private filters() {
        return { type: OperationType.Payment, searchValue: this.searchValue, sortOrder: this.sort, isGlobal: false };
    }
}
