import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import { GroupListRow, groupListRow } from '../../../../shared/components/groups-table/group-list-row';
import { GroupTabCounts } from '../../../../shared/components/groups-table/group-tab-counts';
import { CATALOG_SORT_OPTIONS, CatalogSort, compareCatalogRows } from '../../../../shared/utils/catalog';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';
import { FetchGroupsService } from '../../services/fetch-groups.service';
import { RemoveGroupService } from '../../services/remove-group.service';

@Component({
    templateUrl: './payment-groups.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchGroupsService, RemoveGroupService],
    standalone: false,
})
export class PaymentGroupsComponent {
    readonly sortOptions = CATALOG_SORT_OPTIONS.map((option) => ({
        ...option,
        label: option.label.replace(/^ID/, 'Group ID'),
    }));
    readonly sort$ = new BehaviorSubject<CatalogSort>('updated-desc');
    readonly inProgress$ = this.fetchGroupsService.inProgress$;
    readonly view$ = combineLatest([
        this.fetchGroupsService.searchResult$.pipe(
            map((groups) => groups.map(groupListRow)),
            startWith([] as GroupListRow[])
        ),
        this.sort$,
    ]).pipe(
        map(([groups, sort]) => ({
            groups: [...groups].sort((a, b) => this.compare(a, b, sort)),
            sort,
        }))
    );

    private searchValue = '';

    constructor(
        private router: Router,
        route: ActivatedRoute,
        private fetchGroupsService: FetchGroupsService,
        private removeGroupService: RemoveGroupService,
        private snackBar: MatSnackBar,
        private tabCounts: GroupTabCounts
    ) {
        const destroyRef = inject(DestroyRef);
        catalogSearchQuery(route)
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe((query) => {
                this.searchValue = query;
                this.fetchGroupsService.search({ searchValue: query });
            });
        this.removeGroupService.removed$.pipe(takeUntilDestroyed(destroyRef)).subscribe((id) => {
            this.snackBar.open(`Group ${id} has been deleted`, 'OK', {
                duration: 1500,
            });
            this.fetchGroupsService.search({ searchValue: this.searchValue });
            this.tabCounts.refresh();
        });
    }

    editGroup(id: string): void {
        void this.router.navigate(['/group', id], { queryParams: { returnUrl: this.router.url } });
    }

    duplicateGroup(id: string): void {
        void this.router.navigate(['/group/new'], { queryParams: { copyFrom: id, returnUrl: this.router.url } });
    }

    removeGroup(groupId: string): void {
        this.removeGroupService.remove({ id: groupId });
    }

    setSort(sort: CatalogSort): void {
        this.sort$.next(sort);
    }

    private compare(a: GroupListRow, b: GroupListRow, sort: CatalogSort): number {
        return compareCatalogRows(a.groupId, b.groupId, a.lastUpdate, b.lastUpdate, sort);
    }
}
