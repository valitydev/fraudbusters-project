import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, Inject, Input, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { PaymentCountInfo } from '../../../../../api/fb-management/swagger-codegen/model/paymentCountInfo';
import { PaymentListsService } from '../../../../../api/payments/lists/payment-lists.service';
import { LAYOUT_GAP_M } from '../../../../../tokens';
import { ListType } from '../../../../constants/list-type';
import { ErrorHandlerService } from '../../../../services/utils/error-handler.service';
import { AddRowListService } from '../../services/add-row-list.service';

@Component({
    selector: 'fb-add-row-list',
    templateUrl: './add-row-list.component.html',
    styleUrls: ['./add-row-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddRowListComponent implements OnInit {
    paymentRecords: PaymentCountInfo[] = [];

    @Input() listType: ListType;

    filteredOptions: Observable<string[]>;
    forms = this.addRowListService.forms;
    inProgress$ = this.addRowListService.inProgress$;

    constructor(
        private listService: PaymentListsService,
        private errorHandlerService: ErrorHandlerService,
        private addRowListService: AddRowListService,
        private snackBar: MatSnackBar,
        private location: Location,
        private router: Router,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.addRowListService.created$.subscribe((results) => {
            this.snackBar.open(`Saved success: ${results.length} rows`, 'OK', {
                duration: 3000,
            });
            this.router.navigate(['/lists/' + this.listType]);
        });
    }

    ngOnInit(): void {
        this.filteredOptions = this.listService.getAvailableListNames();
    }

    addItem() {
        this.addRowListService.addItem();
    }

    removeItem(index: number) {
        this.addRowListService.removeItem(index);
    }

    saveFileWithList(file: File): void {
        this.addRowListService.saveFileWithListRaws(this.listType, file);
    }

    save(): void {
        this.addRowListService.create(this.listType);
    }
}
