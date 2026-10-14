import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { distinctUntilChanged, shareReplay } from 'rxjs/operators';

import { Template } from '../../../api/fb-management/swagger-codegen/model/template';
import { PaymentTemplatesService } from '../../../api/payments/templates';
import { ConfigService } from '../../../config';
import { SortOrder } from '../../../shared/constants/sort-order';
import { FetchResult, PartialFetcher } from '../../../shared/utils/partial-fetcher';

export interface FetchTemplatesParams {
    searchValue?: string;
    sortOrder?: SortOrder;
    pageSize?: number;
}

@Injectable()
export class FetchPaymentTemplatesService extends PartialFetcher<Template, FetchTemplatesParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));
    private pageSize = this.configService.pageSize;

    constructor(private paymentTemplatesService: PaymentTemplatesService, private configService: ConfigService) {
        super();
    }

    protected fetch(params: FetchTemplatesParams, lastId?: string): Observable<FetchResult<Template>> {
        const { searchValue, sortOrder, pageSize } = params;
        return this.paymentTemplatesService.findTemplates({
            size: pageSize ? pageSize : this.pageSize,
            sortOrder: sortOrder || SortOrder.Asc,
            ...(searchValue ? { searchValue } : {}),
            ...(lastId ? { lastId } : {}),
        });
    }
}
