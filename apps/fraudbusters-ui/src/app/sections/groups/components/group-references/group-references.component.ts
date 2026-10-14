import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { GroupReference } from '../../../../api/fb-management/swagger-codegen/model/groupReference';
import { OperationType } from '../../../../shared/constants/operation-type';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { Action, ActionType } from '../../model/action';
import { FetchReferencesService } from '../../services/fetch-references/fetch-references.service';
import { RemoveReferenceService } from '../../services/remove-reference/remove-reference.service';

@Component({
    templateUrl: 'group-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchReferencesService, RemoveReferenceService],
})
export class GroupReferencesComponent {
    displayedColumns = ['groupId', 'partyId', 'shopId', 'lastUpdateDate', 'delete'];

    references$ = this.fetchReferencesService.searchResult$;
    inProgress$ = this.fetchReferencesService.inProgress$;
    hasMore$ = this.fetchReferencesService.hasMore$;

    constructor(
        private router: Router,
        private fetchReferencesService: FetchReferencesService,
        private removeReferenceService: RemoveReferenceService,
        private snackBar: MatSnackBar,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.removeReferenceService.removed$.subscribe((id) => {
            this.snackBar.open(`Reference ${id} has been deleted`, 'OK', {
                duration: 1500,
            });
            this.fetchReferencesService.search({ type: OperationType.Payment, isGlobal: false });
        });
    }

    action(action: Action) {
        switch (action.type) {
            case ActionType.CreateReference:
                this.router.navigate(['/create-group-reference/payment'], { fragment: OperationType.Payment });
                break;
            case ActionType.RemoveReference:
                this.removeReferenceService.removeReference({
                    reference: action.reference,
                });
                break;
            case ActionType.SortReferences:
                this.fetchReferencesService.search({
                    type: OperationType.Payment,
                    sortOrder: action.sortDirection,
                    isGlobal: false,
                });
                break;
            default:
                console.error('Wrong reference action.');
        }
    }

    createReference() {
        this.action({ type: ActionType.CreateReference });
    }

    search(searchValue: string) {
        this.fetchReferencesService.search({ type: OperationType.Payment, searchValue, isGlobal: false });
    }

    fetchMore(sortFieldValue: string) {
        this.fetchReferencesService.fetchMore({ type: OperationType.Payment, sortFieldValue, isGlobal: false });
    }

    removeReference(reference: GroupReference): void {
        this.action({ type: ActionType.RemoveReference, reference });
    }

    goToGroup(id: string) {
        this.router.navigate([`/group/${id}`]);
    }
}
