import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
    NgxMatDatetimePickerModule,
    NgxMatNativeDateModule,
    NgxMatTimepickerModule,
} from '@angular-material-components/datetime-picker';

import { PaymentListsService } from '../../../api/payments/lists/payment-lists.service';
import { ErrorHandlerService } from '../../services/utils/error-handler.service';
import { SearchFieldService } from '../../services/utils/search-field.service';
import { EmptySearchResultModule } from '../empty-search-result';
import { ShowMorePanelModule } from '../show-more-panel';
import { AddRowListModule } from './components/add-row-list/add-row-list.module';
import { RemoveRowListDialogComponent } from './components/remove-row-list/remove-row-list-dialog.component';
import { WbListSearchComponent } from './components/wb-list-search/wb-list-search.component';
import { WbRowsListModule } from './components/wb-rows-list';
import { FetchWbListService } from './services/fetch-wb-list.service';
import { RemoveWbListComponentService } from './services/remove-wb-list.service';
import { WbListComponent } from './wb-list.component';

@NgModule({
    declarations: [WbListComponent, RemoveRowListDialogComponent, WbListSearchComponent],
    exports: [WbListComponent, RemoveRowListDialogComponent],
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatSelectModule,
        MatFormFieldModule,
        MatInputModule,
        MatToolbarModule,
        MatPaginatorModule,
        MatSortModule,
        MatSnackBarModule,
        MatDialogModule,
        MatTooltipModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatAutocompleteModule,
        NgxMatTimepickerModule,
        NgxMatNativeDateModule,
        NgxMatDatetimePickerModule,
        ReactiveFormsModule,
        FlexLayoutModule,
        FormsModule,
        ShowMorePanelModule,
        EmptySearchResultModule,
        MatProgressSpinnerModule,
        WbRowsListModule,
        AddRowListModule,
    ],
    providers: [
        SearchFieldService,
        PaymentListsService,
        ErrorHandlerService,
        FetchWbListService,
        RemoveWbListComponentService,
    ],
})
export class WbListModule {}
