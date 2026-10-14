import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { distinctUntilChanged, shareReplay } from 'rxjs/operators';

import { PaymentReference } from '../../../api/fb-management/swagger-codegen/model/paymentReference';
import { PaymentReferencesService } from '../../../api/payments/references';
import { ConfigService } from '../../../config';
import { SortOrder } from '../../../shared/constants/sort-order';
import { FetchResult, PartialFetcher } from '../../../shared/utils/partial-fetcher';

export interface FetchReferencesParams {
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
export class FetchReferencesService extends PartialFetcher<PaymentReference, FetchReferencesParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));
    private pageSize = this.configService.pageSize;

    constructor(private paymentReferencesService: PaymentReferencesService, private configService: ConfigService) {
        super();
    }

    protected fetch(params: FetchReferencesParams, lastId?: string): Observable<FetchResult<PaymentReference>> {
        const { searchValue, sortOrder, sortFieldValue, sortBy, id, isDefault, isGlobal, name, size } = params;
        return this.paymentReferencesService.findReferences({
            isDefault: isDefault || false,
            searchValue: searchValue || '',
            sortFieldValue: sortFieldValue || '',
            sortOrder: sortOrder || SortOrder.Asc,
            size: size ? size : this.pageSize,
            isGlobal,
            ...(lastId ? { lastId } : {}),
            ...(sortBy ? { sortBy } : {}),
            ...(id ? { id } : {}),
            ...(name ? { name } : {}),
        });
    }
}
