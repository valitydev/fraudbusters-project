import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { shareReplay, distinctUntilChanged } from 'rxjs/operators';

import { WbListRecord } from '../../../../api/fb-management/swagger-codegen/model/wbListRecord';
import { PaymentListsService } from '../../../../api/payments/lists';
import { ConfigService } from '../../../../config';
import { ListType } from '../../../constants/list-type';
import { SortOrder } from '../../../constants/sort-order';
import { FetchResult, PartialFetcher } from '../../../utils/partial-fetcher';

export interface FetchRowsParams {
    searchValue?: string;
    sortOrder?: SortOrder;
    pageSize?: number;
    listNames?: string[];
    listType: ListType;
}

@Injectable()
export class FetchWbListService extends PartialFetcher<WbListRecord, FetchRowsParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));
    private pageSize = this.configService.pageSize;

    constructor(private paymentListsService: PaymentListsService, private configService: ConfigService) {
        super();
    }

    protected fetch(
        params: FetchRowsParams,
        lastId?: string,
        lastRecord?: WbListRecord
    ): Observable<FetchResult<WbListRecord>> {
        const { searchValue, sortOrder, pageSize, listType, listNames } = params;
        return this.paymentListsService.findListRows({
            size: pageSize ? pageSize : this.pageSize,
            sortOrder: sortOrder || SortOrder.Asc,
            ...(searchValue ? { searchValue } : {}),
            ...(lastId ? { lastId } : {}),
            ...(lastRecord?.insertTime ? { sortFieldValue: String(lastRecord.insertTime) } : {}),
            listType,
            listNames,
        });
    }
}
