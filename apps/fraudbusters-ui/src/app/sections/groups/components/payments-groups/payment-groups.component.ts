import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { LAYOUT_GAP_M } from '../../../../tokens';
import { FetchGroupsService } from '../../services/fetch-groups.service';
import { RemoveGroupService } from '../../services/remove-group.service';

@Component({
    templateUrl: './payment-groups.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchGroupsService, RemoveGroupService],
})
export class PaymentGroupsComponent {
    groups$ = this.fetchGroupsService.searchResult$;
    inProgress$ = this.fetchGroupsService.inProgress$;
    hasMore$ = this.fetchGroupsService.hasMore$;

    constructor(
        private router: Router,
        private fetchGroupsService: FetchGroupsService,
        private removeGroupService: RemoveGroupService,
        private snackBar: MatSnackBar,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.removeGroupService.removed$.subscribe((id) => {
            this.snackBar.open(`Group ${id} has been deleted`, 'OK', {
                duration: 1500,
            });
            this.fetchGroupsService.search({});
        });
    }

    createGroup(): void {
        this.router.navigate(['/group/new']);
    }

    editGroup(id: string) {
        this.router.navigate([`/group/${id}`]);
    }

    removeGroup(groupId: string) {
        this.removeGroupService.remove({ id: groupId });
    }

    search(searchValue: string) {
        this.fetchGroupsService.search({ searchValue });
    }

    fetchMore() {
        this.fetchGroupsService.fetchMore();
    }
}
