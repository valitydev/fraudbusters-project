import { ChangeDetectionStrategy, Component, Inject, Input, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { Filter } from './model/filter';
import { FetchWbListService } from './services/fetch-wb-list.service';
import { RemoveWbListComponentService } from './services/remove-wb-list.service';
import { PaymentListsService } from '../../../api/payments/lists';
import { LAYOUT_GAP_M } from '../../../tokens';
import { ListType } from '../../constants/list-type';
import { ErrorHandlerService } from '../../services/utils/error-handler.service';

@Component({
    selector: 'fb-wb-list',
    templateUrl: './wb-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WbListComponent implements OnInit {
    @Input() listType: ListType;

    rows$ = this.fetchWbListService.searchResult$;
    listNames$: Observable<string[]>;
    inProgress$ = this.fetchWbListService.inProgress$;
    hasMore$ = this.fetchWbListService.hasMore$;

    constructor(
        private router: Router,
        private fetchWbListService: FetchWbListService,
        private removeWbListComponentService: RemoveWbListComponentService,
        private snackBar: MatSnackBar,
        private errorHandlerService: ErrorHandlerService,
        private listService: PaymentListsService,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.removeWbListComponentService.removed$.subscribe((id) => {
            this.snackBar.open(`WbList ${id} has been deleted`, 'OK', {
                duration: 1500,
            });
            this.fetchWbListService.search({ listType: this.listType });
        });
    }

    createRow() {
        this.router.navigate([`/list/${this.listType}/new`]);
    }

    removeRow(rowId: string) {
        this.removeWbListComponentService.remove({ rowId });
    }

    deleteByFileWithList(file: File): void {
        this.removeWbListComponentService.deleteByFileWithListRaws(this.listType, file);
    }

    search(filter?: Filter) {
        this.fetchWbListService.search({
            searchValue: filter.searchValue,
            listType: this.listType,
            listNames: filter.typeChanges,
        });
    }

    fetchMore() {
        this.fetchWbListService.fetchMore();
    }

    ngOnInit(): void {
        this.listNames$ = this.listService.getNames(this.listType);
    }
}
