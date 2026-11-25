import { Injectable } from '@angular/core';

import { Series } from '../../../sections/analytics/model/chart-data';
import { RiskScoreOffsetCountRatio } from '../../fb-management/swagger-codegen/model/riskScoreOffsetCountRatio';
import { SplitRiskScoreCountRatioResponse } from '../../fb-management/swagger-codegen/model/splitRiskScoreCountRatioResponse';

@Injectable()
export class RiskSeriesMapperService {
    mapSeries(response: SplitRiskScoreCountRatioResponse) {
        return response.offsetCountRatios.map(
            (value) =>
                ({
                    name: value.score,
                    color: this.getSeriesColor(value),
                    data: value.offsetCountRatio.map((offsetCountRatioValue) => ({
                        x: new Date(offsetCountRatioValue.offset),
                        y: offsetCountRatioValue.countRatio >= 0 ? offsetCountRatioValue.countRatio : null,
                    })),
                } as Series)
        );
    }

    private getSeriesColor(value: RiskScoreOffsetCountRatio) {
        switch (value.score) {
            case 'low':
                return '#10b981';
            case 'fatal':
                return '#ef4444';
            case 'high':
                return '#94a3b8';
            default:
                return '#6338f5';
        }
    }
}
