import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AnalyticsRoutingModule } from './analytics-routing.module';
import { AnalyticsComponent } from './analytics.component';
import { BaseAnalyticsComponent } from './components/base/base-analytics.component';
import { AnalyticsService } from '../../api/payments/analytics';
import { RiskSeriesMapperService } from '../../api/payments/analytics/riskSeriesMapper.service';
import { BarChartModule } from '../../shared/components/charts';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { SharedPipesModule } from '../../shared/pipes';
import { ArraySortPipe } from '../../shared/pipes/sort-by-field.pipe';
import { FraudSummaryListModule } from './components/base/components/fraud-summary-list';
import { BaseAnalyticsSearchComponent } from './components/base/components/search/base-analytics-search.component';
import { BaseAnalyticsUtilService } from './components/base/services/base-analytics-util.service';
import { BaseAnalyticsService } from './components/base/services/base-analytics.service';
import { DateTimeRangeComponent } from '../../shared/components/datetime-range/datetime-range.component';

@NgModule({
    imports: [
        DateTimeRangeComponent,
        AnalyticsRoutingModule,
        CommonModule,
        MatButtonModule,
        MatIconModule,
        ConfirmActionDialogModule,
        SharedPipesModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatNativeDateModule,
        MatSelectModule,
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
