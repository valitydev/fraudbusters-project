import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { shareReplay, distinctUntilChanged } from 'rxjs/operators';

import { PaymentReference } from '../../../api/fb-management/swagger-codegen/model/paymentReference';
import { PaymentDefaultReferencesService } from '../../../api/payments/default-references';
import { ConfigService } from '../../../config';
import { SortOrder } from '../../../shared/constants/sort-order';
import { FetchResult, PartialFetcher } from '../../../shared/utils/partial-fetcher';

export interface FetchDefaultReferencesParams {
    isGlobal: boolean;
    searchValue?: string;
    sortOrder?: SortOrder;
    size?: number;
    isDefault?: boolean;
    id?: string;
    name?: string;
    sortBy?: string;
    sortFieldValue?: string;
}

@Injectable()
export class FetchDefaultReferencesService extends PartialFetcher<PaymentReference, FetchDefaultReferencesParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));
    private pageSize = this.configService.pageSize;

    constructor(
        private paymentDefaultReferencesService: PaymentDefaultReferencesService,
        private configService: ConfigService
    ) {
        super();
    }

    protected fetch(params: FetchDefaultReferencesParams, lastId?: string): Observable<FetchResult<PaymentReference>> {
        const { searchValue, sortOrder, sortFieldValue, sortBy, id, name, size } = params;
        return this.paymentDefaultReferencesService.filter({
            searchValue: searchValue || '',
            sortFieldValue: sortFieldValue || '',
            sortOrder: sortOrder || SortOrder.Asc,
            size: size ? size : this.pageSize,
            ...(lastId ? { lastId } : {}),
            ...(sortBy ? { sortBy } : {}),
            ...(id ? { id } : {}),
            ...(name ? { name } : {}),
        });
    }
}
