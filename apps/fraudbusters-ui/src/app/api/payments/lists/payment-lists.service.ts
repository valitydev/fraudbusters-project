import { HttpClient, HttpEvent, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import { SearchListsParams } from './search-lists-params';
import { ConfigService } from '../../../config';
import { ListType } from '../../../shared/constants/list-type';
import { HttpRequestModel } from '../../../shared/model/http-request-model';
import { SearchParams } from '../../../shared/model/search-params';
import { filterParameters } from '../../../shared/utils/filter-params';
import { Chargeback } from '../../fb-management/swagger-codegen/model/chargeback';
import { IdResponse } from '../../fb-management/swagger-codegen/model/idResponse';
import { ListResponse } from '../../fb-management/swagger-codegen/model/listResponse';
import { PaymentCountInfo } from '../../fb-management/swagger-codegen/model/paymentCountInfo';
import { WbListCandidatesBatchesResponse } from '../../fb-management/swagger-codegen/model/wbListCandidatesBatchesResponse';
import { WbListCandidatesResponse } from '../../fb-management/swagger-codegen/model/wbListCandidatesResponse';
import { WbListRecordsResponse } from '../../fb-management/swagger-codegen/model/wbListRecordsResponse';

@Injectable()
export class PaymentListsService {
    private readonly fbPaymentReferenceEndpoint = `${this.configService.fbManagementEndpoint}/payments-lists`;

    constructor(private http: HttpClient, private configService: ConfigService) {}

    deleteListRow(id: string): Observable<string> {
        return this.http
            .delete(`${this.fbPaymentReferenceEndpoint}/${id}`)
            .pipe(map((response: IdResponse) => response.id));
    }

    findListRows(params: SearchListsParams): Observable<WbListRecordsResponse> {
        return this.http.get<WbListRecordsResponse>(`${this.fbPaymentReferenceEndpoint}/filter`, {
            params: filterParameters(params),
        });
    }

    getNames(listTypeSearch: ListType): Observable<string[]> {
        return this.http
            .get<ListResponse>(`${this.fbPaymentReferenceEndpoint}/currentListNames`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                params: { listType: listTypeSearch },
            })
            .pipe(map((response: ListResponse) => response.result));
    }

    getAvailableListNames(): Observable<string[]> {
        return this.http
            .get<ListResponse>(`${this.fbPaymentReferenceEndpoint}/availableListNames`, {
                headers: {
                    'Content-Type': 'application/json',
                },
            })
            .pipe(map((response: ListResponse) => response.result));
    }

    saveListsRows(listType: ListType, records: PaymentCountInfo[]): Observable<string[]> {
        return this.http
            .post(`${this.fbPaymentReferenceEndpoint}`, { listType, records }, new HttpRequestModel())
            .pipe(map((response: ListResponse) => response.result));
    }

    saveListsRowsFromFile(listType: ListType, file: File): Observable<HttpEvent<File>> {
        const formData: FormData = new FormData();
        formData.append('file', file, file.name);
        formData.append('listType', listType);
        return this.http
            .post<File>(`${this.fbPaymentReferenceEndpoint}/csv`, formData, {
                reportProgress: true,
                observe: 'events',
            })
            .pipe(filter((r) => r instanceof HttpResponse && r.status === 200));
    }

    deleteListsRowsByFile(listType: ListType, file: File): Observable<HttpEvent<File>> {
        const formData: FormData = new FormData();
        formData.append('file', file, file.name);
        formData.append('listType', listType);
        return this.http
            .post<File>(`${this.fbPaymentReferenceEndpoint}/remove/csv`, formData, {
                reportProgress: true,
                observe: 'events',
            })
            .pipe(filter((r) => r instanceof HttpResponse && r.status === 200));
    }

    findCandidatesBatch(params: SearchParams): Observable<WbListCandidatesBatchesResponse> {
        return this.http.get<WbListCandidatesBatchesResponse>(`${this.fbPaymentReferenceEndpoint}/candidates-batches`, {
            params: filterParameters(params),
        });
    }

    findCandidates(params: SearchParams): Observable<WbListCandidatesResponse> {
        return this.http.get<WbListCandidatesResponse>(`${this.fbPaymentReferenceEndpoint}/candidates`, {
            params: filterParameters(params),
        });
    }

    approveCandidates(ids: string[]): Observable<string> {
        return this.http
            .post<void>(`${this.fbPaymentReferenceEndpoint}/candidates/approved`, {
                ids,
            })
            .pipe(map(() => 'OK'));
    }

    createCandidatesByChargebacks(chargebacks: Chargeback[]): Observable<string> {
        return this.http
            .post<void>(`${this.fbPaymentReferenceEndpoint}/candidates`, {
                records: chargebacks,
            })
            .pipe(map(() => 'OK'));
    }
}
