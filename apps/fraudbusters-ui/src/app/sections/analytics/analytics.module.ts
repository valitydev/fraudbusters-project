import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
    NgxMatDatetimePickerModule,
    NgxMatNativeDateModule,
    NgxMatTimepickerModule,
} from '@angular-material-components/datetime-picker';

import { AnalyticsRoutingModule } from './analytics-routing.module';
import { AnalyticsComponent } from './analytics.component';
import { BaseAnalyticsComponent } from './components/base/base-analytics.component';
import { AnalyticsService } from '../../api/payments/analytics';
import { RiskSeriesMapperService } from '../../api/payments/analytics/riskSeriesMapper.service';
import { BarChartModule } from '../../shared/components/charts';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { EmptySearchResultModule } from '../../shared/components/empty-search-result';
import { FbInfoCardModule } from '../../shared/components/fb-info-card';
import { ShowMorePanelModule } from '../../shared/components/show-more-panel';
import { SharedPipesModule } from '../../shared/pipes';
import { ArraySortPipe } from '../../shared/pipes/sort-by-field.pipe';
import { FraudSummaryListModule } from './components/base/components/fraud-summary-list';
import { BaseAnalyticsSearchComponent } from './components/base/components/search/base-analytics-search.component';
import { BaseAnalyticsUtilService } from './components/base/services/base-analytics-util.service';
import { BaseAnalyticsService } from './components/base/services/base-analytics.service';

@NgModule({
    imports: [
        AnalyticsRoutingModule,
        MatTabsModule,
        CommonModule,
        MatCardModule,
        MatTableModule,
        MatSortModule,
        MatButtonModule,
        MatIconModule,
        MatToolbarModule,
        ConfirmActionDialogModule,
        MatMenuModule,
        EmptySearchResultModule,
        SharedPipesModule,
        FlexModule,
        MatSnackBarModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        MatProgressSpinnerModule,
        MatProgressBarModule,
        ShowMorePanelModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatTooltipModule,
        NgxMatTimepickerModule,
        NgxMatNativeDateModule,
        NgxMatDatetimePickerModule,
        MatSelectModule,
        FbInfoCardModule,
        BarChartModule,
        FraudSummaryListModule,
    ],
    declarations: [AnalyticsComponent, BaseAnalyticsComponent, BaseAnalyticsSearchComponent],
    providers: [
        AnalyticsService,
        RiskSeriesMapperService,
        BaseAnalyticsService,
        DatePipe,
        BaseAnalyticsUtilService,
        ArraySortPipe,
    ],
})
export class AnalyticsModule {}
