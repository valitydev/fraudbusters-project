import { NgModule } from '@angular/core';

import { AnalyticsService } from './analytics.service';
import { RiskSeriesMapperService } from './riskSeriesMapper.service';

@NgModule({
    providers: [AnalyticsService, RiskSeriesMapperService],
})
export class AnalyticsModule {}
