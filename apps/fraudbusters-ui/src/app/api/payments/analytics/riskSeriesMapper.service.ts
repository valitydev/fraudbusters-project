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
                    data: value.offsetCountRatio.map((offsetCountRatioValue) => ({
                        x: new Date(offsetCountRatioValue.offset),
                        y: offsetCountRatioValue.countRatio,
                        fillColor: this.getFillColor(value),
                    })),
                } as Series)
        );
    }

    private getFillColor(value: RiskScoreOffsetCountRatio) {
        switch (value.score) {
            case 'low':
                return '#1ab152';
            case 'fatal':
                return '#cf1c1d';
            default:
                return '#c4c4c4';
        }
    }
}
