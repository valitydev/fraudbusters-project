import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule, FlexModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import {
    NgxMatDatetimePickerModule,
    NgxMatNativeDateModule,
    NgxMatTimepickerModule,
} from '@angular-material-components/datetime-picker';

import { AddGreyRowListComponent } from './add-grey-row-list.component';
import { PaymentListsService } from '../../../../../api/payments/lists';
import { CsvUtilsService } from '../../../../services/utils/csv-utils.service';
import { ErrorHandlerService } from '../../../../services/utils/error-handler.service';
import { HeadlineModule } from '../../../headline';
import { AddGreyRowListService } from '../../services/add-grey-row-list.service';
import { WbListService } from '../../wb-list.service';

@NgModule({
    declarations: [AddGreyRowListComponent],
    imports: [
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatSelectModule,
        MatFormFieldModule,
        MatInputModule,
        MatToolbarModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatAutocompleteModule,
        NgxMatTimepickerModule,
        NgxMatNativeDateModule,
        NgxMatDatetimePickerModule,
        ReactiveFormsModule,
        FlexLayoutModule,
        FormsModule,
        FlexModule,
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatSnackBarModule,
        MatProgressBarModule,
        HeadlineModule,
    ],
    exports: [AddGreyRowListComponent],
    providers: [PaymentListsService, ErrorHandlerService, AddGreyRowListService, WbListService, CsvUtilsService],
})
export class AddGreyRowListModule {}
