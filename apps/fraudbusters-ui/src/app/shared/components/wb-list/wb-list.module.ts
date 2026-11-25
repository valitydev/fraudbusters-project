import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { PaymentListsService } from '../../../api/payments/lists/payment-lists.service';
import { ConfirmActionDialogModule } from '../confirm-action-dialog';
import { FetchWbListService } from './services/fetch-wb-list.service';
import { RemoveWbListComponentService } from './services/remove-wb-list.service';
import { WbListComponent } from './wb-list.component';
import { CatalogPanelComponent } from '../catalog-panel/catalog-panel.component';
import { ListRecordsTableComponent } from '../list-records-table/list-records-table.component';

@NgModule({
    declarations: [WbListComponent],
    exports: [WbListComponent],
    imports: [
        CommonModule,
        MatDialogModule,
        MatSelectModule,
        MatSnackBarModule,
        ConfirmActionDialogModule,
        CatalogPanelComponent,
        ListRecordsTableComponent,
    ],
    providers: [PaymentListsService, FetchWbListService, RemoveWbListComponentService],
})
export class WbListModule {}
