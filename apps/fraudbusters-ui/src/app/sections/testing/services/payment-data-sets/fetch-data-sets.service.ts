import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { shareReplay, tap } from 'rxjs/operators';

import { DataSet } from '../../../../api/fb-management/swagger-codegen/model/dataSet';
import { DataSetService } from '../../../../api/payments/data-set';
import { ConfigService } from '../../../../config';
import { SortOrder } from '../../../../shared/constants/sort-order';
import { booleanDelay } from '../../../../shared/operators';
import { FetchResultContinuation } from '../../../../shared/utils/partial-fetcher/fetch-result-continuation';
import { PartialFetcherContinuation } from '../../../../shared/utils/partial-fetcher/partial-fetcher-continuation';

export interface FetchDataSetsParams {
    sortOrder?: SortOrder;
    size?: number;
    searchValue?: string;
    sortBy?: string;
    sortFieldValue?: string;
    from?: string;
    to?: string;
}

@Injectable()
export class FetchDataSetsService extends PartialFetcherContinuation<DataSet, FetchDataSetsParams> {
    readonly failed$ = new BehaviorSubject(false);
    inProgress$ = this.doAction$.pipe(booleanDelay(), shareReplay(1));
    private pageSize = this.configService.pageSize;

    constructor(
        private dataSetService: DataSetService,
        protected configService: ConfigService,
        public datepipe: DatePipe
    ) {
        super();
    }

    protected fetch(params: FetchDataSetsParams, lastId?: string): Observable<FetchResultContinuation<DataSet>> {
        const { sortOrder, sortBy, searchValue, size, from, to } = params;
        this.failed$.next(false);
        return this.dataSetService
            .filterDataSets({
                sortOrder: sortOrder || SortOrder.Desc,
                size: size ? size : this.pageSize,
                dataSetName: searchValue ? searchValue : null,
                ...(lastId ? { lastId } : {}),
                ...(sortBy ? { sortBy } : {}),
                ...(from ? { from } : {}),
                ...(to ? { to } : {}),
            })
            .pipe(tap({ error: () => this.failed$.next(true) }));
    }
}
