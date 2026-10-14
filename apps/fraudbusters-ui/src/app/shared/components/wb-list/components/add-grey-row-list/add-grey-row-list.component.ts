import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, Inject, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { PaymentCountInfo } from '../../../../../api/fb-management/swagger-codegen/model/paymentCountInfo';
import { PaymentListsService } from '../../../../../api/payments/lists/payment-lists.service';
import { LAYOUT_GAP_M } from '../../../../../tokens';
import { ErrorHandlerService } from '../../../../services/utils/error-handler.service';
import { AddGreyRowListService } from '../../services/add-grey-row-list.service';

@Component({
    selector: 'fb-add-grey-row-list',
    templateUrl: './add-grey-row-list.component.html',
    styleUrls: ['./add-grey-row-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddGreyRowListComponent implements OnInit {
    paymentRecords: PaymentCountInfo[] = [];

    filteredOptions: Observable<string[]>;
    forms = this.addGreyRowListService.forms;
    inProgress$ = this.addGreyRowListService.inProgress$;

    constructor(
        private listService: PaymentListsService,
        private errorHandlerService: ErrorHandlerService,
        private addGreyRowListService: AddGreyRowListService,
        private snackBar: MatSnackBar,
        private location: Location,
        private router: Router,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.addGreyRowListService.created$.subscribe((results) => {
            this.snackBar.open(`Saved success: ${results.length} rows`, 'OK', {
                duration: 3000,
            });
            this.router.navigate(['/lists/grey']);
        });
    }

    ngOnInit(): void {
        this.filteredOptions = this.listService.getAvailableListNames();
    }

    addItem() {
        this.addGreyRowListService.addItem();
    }

    removeItem(index: number) {
        this.addGreyRowListService.removeItem(index);
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    prepareFilesList(files: Array<any>): void {
        this.addGreyRowListService.prepareFilesList(files);
    }

    save(): void {
        this.addGreyRowListService.create();
    }
}
