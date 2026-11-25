import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';

import { EmulationModule } from './components/emulation';
import { PaymentDataSetsListModule } from './components/payment-data-sets/payment-data-sets-list';
import { PaymentDataSetsComponent } from './components/payment-data-sets/payment-data-sets.component';
import { FetchDataSetsService } from './services/payment-data-sets/fetch-data-sets.service';
import { RemoveDataSetsService } from './services/payment-data-sets/remove-data-sets.service';
import { TestingRoutingModule } from './testing-routing.module';
import { TestingComponent } from './testing.component';
import { DataSetService } from '../../api/payments/data-set';
import { CatalogPanelComponent } from '../../shared/components/catalog-panel/catalog-panel.component';
import { DateTimeRangeComponent } from '../../shared/components/datetime-range/datetime-range.component';
import { TestingDataSetListModule } from '../../shared/components/testing-data-set-list';
import { SearchFieldService } from '../../shared/services/utils/search-field.service';

@NgModule({
    declarations: [TestingComponent, PaymentDataSetsComponent],
    imports: [
        MatButtonModule,
        CommonModule,
        MatProgressBarModule,
        MatSelectModule,
        MatTabsModule,
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        MatSnackBarModule,
        MatFormFieldModule,
        MatInputModule,
        MatNativeDateModule,
        DateTimeRangeComponent,
        CatalogPanelComponent,
        TestingRoutingModule,
        EmulationModule,
        TestingDataSetListModule,
        PaymentDataSetsListModule,
    ],
    providers: [DatePipe, SearchFieldService, DataSetService, FetchDataSetsService, RemoveDataSetsService],
})
export class TestingModule {}
