import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map, switchMap } from 'rxjs/operators';

import { PayoutsService } from '../../../../api/payouts';
import { ConfigService } from '../../../../config';
import { GroupListRow, groupListRow } from '../../../../shared/components/groups-table/group-list-row';
import { GroupTabCounts } from '../../../../shared/components/groups-table/group-tab-counts';
import { CATALOG_SORT_OPTIONS, compareCatalogRows } from '../../../../shared/utils/catalog';
import { CatalogCollection } from '../../../../shared/utils/catalog-collection';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-groups.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class PayoutGroupsComponent {
    readonly sortOptions = CATALOG_SORT_OPTIONS;
    readonly catalog: CatalogCollection<GroupListRow>;

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private router: Router,
        route: ActivatedRoute,
        config: ConfigService,
        private tabCounts: GroupTabCounts
    ) {
        this.catalog = new CatalogCollection<GroupListRow>(
            route,
            config.pageSize,
            (query) => this.payoutsService.findGroups(query).pipe(map((response) => response.result.map(groupListRow))),
            (a, b, sort) => compareCatalogRows(a.groupId, b.groupId, a.lastUpdate, b.lastUpdate, sort)
        );
    }

    editGroup(groupId: string) {
        void this.router.navigate([`/payouts/group/${groupId}`], { queryParams: { returnUrl: this.router.url } });
    }

    duplicateGroup(id: string): void {
        void this.router.navigate(['/payouts/group/new'], {
            queryParams: { copyFrom: id, returnUrl: this.router.url },
        });
    }

    removeGroup(groupId: string) {
        this.feedback
            .confirm(`Remove group ${groupId}?`)
            .pipe(switchMap(() => this.payoutsService.removeGroup(groupId)))
            .subscribe({
                next: () => {
                    this.catalog.refresh();
                    this.tabCounts.refresh();
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
