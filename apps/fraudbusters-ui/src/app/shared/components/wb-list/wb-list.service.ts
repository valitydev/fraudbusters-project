import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PaymentCountInfo } from '../../../api/fb-management/swagger-codegen/model/paymentCountInfo';
import { PaymentListRecord } from '../../../api/fb-management/swagger-codegen/model/paymentListRecord';
import { PaymentListsService } from '../../../api/payments/lists/payment-lists.service';
import { ListType } from '../../constants/list-type';
import { SortOrder } from '../../constants/sort-order';
import { HttpSearchResponse } from '../../model/http-search-response';

@Injectable()
export class WbListService {
    constructor(private paymentListsService: PaymentListsService) {}

    findLists(
        listNamesValue: string[],
        listTypeValue: ListType,
        sizeValue?: number,
        nameRegexp?: string,
        lastInListName?: string,
        sortOrder?: SortOrder,
        sortField?: string
    ): Observable<HttpSearchResponse<PaymentListRecord>> {
        return this.paymentListsService.findListRows({
            searchValue: nameRegexp,
            lastId: lastInListName,
            size: sizeValue,
            sortOrder: sortOrder ? sortOrder : SortOrder.Asc,
            sortFieldValue: sortField,
            listNames: listNamesValue,
            listType: listTypeValue,
        });
    }

    deleteListRow(id: string): Observable<string> {
        return this.paymentListsService.deleteListRow(id);
    }

    getNames(listType: ListType): Observable<string[]> {
        return this.paymentListsService.getNames(listType);
    }

    getAvailableListNames(): Observable<string[]> {
        return this.paymentListsService.getAvailableListNames();
    }

    saveListRow(listType: ListType, rows: PaymentCountInfo[]): Observable<string[]> {
        return this.paymentListsService.saveListsRows(listType, rows);
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    saveListRowsFromFile(listType: ListType, file: File): Observable<any> {
        return this.paymentListsService.saveListsRowsFromFile(listType, file);
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    deleteListRowsByFile(listType: ListType, file: File): Observable<any> {
        return this.paymentListsService.deleteListsRowsByFile(listType, file);
    }
}
