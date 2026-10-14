import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { RiskSeriesMapperService } from './riskSeriesMapper.service';
import { SearchBaseAnalyticsParams } from './searchBaseAnalyticsParams';
import { ConfigService } from '../../../config';
import { ChartData } from '../../../sections/analytics/model/chart-data';
import { filterParameters } from '../../../shared/utils/filter-params';
import { CountResponse } from '../../fb-management/swagger-codegen/model/countResponse';
import { FraudResultListSummaryResponse } from '../../fb-management/swagger-codegen/model/fraudResultListSummaryResponse';
import { FraudResultSummary } from '../../fb-management/swagger-codegen/model/fraudResultSummary';
import { ListResponse } from '../../fb-management/swagger-codegen/model/listResponse';
import { RatioResponse } from '../../fb-management/swagger-codegen/model/ratioResponse';
import { SplitRiskScoreCountRatioResponse } from '../../fb-management/swagger-codegen/model/splitRiskScoreCountRatioResponse';
import { SumResponse } from '../../fb-management/swagger-codegen/model/sumResponse';

@Injectable()
export class AnalyticsService {
    private readonly fbAnalyticsUrl = `${this.configService.fbManagementEndpoint}/analytics`;

    constructor(
        private http: HttpClient,
        private configService: ConfigService,
        private riskSeriesMapperService: RiskSeriesMapperService
    ) {}

    getCurrencies(): Observable<string[]> {
        return this.http
            .get<ListResponse>(`${this.fbAnalyticsUrl}/currencies`)
            .pipe(map((response: ListResponse) => response.result));
    }

    getAttemptedPaymentsCount(paramsRes: SearchBaseAnalyticsParams): Observable<number> {
        return this.http
            .get(`${this.fbAnalyticsUrl}/fraud-payments/count`, {
                params: filterParameters(paramsRes),
            })
            .pipe(map((response: CountResponse) => response.count));
    }

    getBlockedPaymentsCount(paramsRes: SearchBaseAnalyticsParams): Observable<number> {
        return this.http
            .get(`${this.fbAnalyticsUrl}/fraud-payments/blocked/count`, {
                params: filterParameters(paramsRes),
            })
            .pipe(map((response: CountResponse) => response.count));
    }

    getBlockedPaymentsRatio(paramsRes: SearchBaseAnalyticsParams): Observable<number> {
        return this.http
            .get(`${this.fbAnalyticsUrl}/fraud-payments/blocked/count/ratio`, {
                params: filterParameters(paramsRes),
            })
            .pipe(map((response: RatioResponse) => response.ratio));
    }

    getBlockedPaymentsSum(paramsRes: SearchBaseAnalyticsParams): Observable<number> {
        return this.http
            .get(`${this.fbAnalyticsUrl}/fraud-payments/blocked/sum`, {
                params: filterParameters(paramsRes),
            })
            .pipe(map((response: SumResponse) => response.sum));
    }

    getSplitCountRatio(paramsRes: SearchBaseAnalyticsParams): Observable<ChartData> {
        return this.http
            .get(`${this.fbAnalyticsUrl}/fraud-payments/scores/split-count/ratio`, {
                params: filterParameters(paramsRes),
            })
            .pipe(
                map((response: SplitRiskScoreCountRatioResponse) => {
                    return {
                        series: this.riskSeriesMapperService.mapSeries(response),
                    } as ChartData;
                })
            );
    }

    getSummary(paramsRes: SearchBaseAnalyticsParams): Observable<FraudResultSummary[]> {
        return this.http
            .get(`${this.fbAnalyticsUrl}/fraud-payments/results/summary`, {
                params: filterParameters(paramsRes),
            })
            .pipe(map((response: FraudResultListSummaryResponse) => response.result));
    }
}
