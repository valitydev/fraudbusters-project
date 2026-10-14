import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { SearchReferenceParams } from './search-reference-params';
import { ConfigService } from '../../../config';
import { HttpRequestModel } from '../../../shared/model/http-request-model';
import { filterParameters } from '../../../shared/utils/filter-params';
import { IdResponse } from '../../fb-management/swagger-codegen/model/idResponse';
import { ListResponse } from '../../fb-management/swagger-codegen/model/listResponse';
import { PaymentReference } from '../../fb-management/swagger-codegen/model/paymentReference';
import { ReferencesResponse } from '../../fb-management/swagger-codegen/model/referencesResponse';

@Injectable()
export class PaymentReferencesService {
    private readonly fbPaymentReferenceEndpoint = `${this.configService.fbManagementEndpoint}/payments-references`;

    constructor(private http: HttpClient, private configService: ConfigService) {}

    findReferences(params?: SearchReferenceParams): Observable<ReferencesResponse> {
        return this.http.get<ReferencesResponse>(`${this.fbPaymentReferenceEndpoint}/filter`, {
            params: filterParameters(params),
        });
    }

    deleteReference(reference: PaymentReference): Observable<string> {
        return this.http
            .delete(`${this.fbPaymentReferenceEndpoint}/${reference.id}`)
            .pipe(map((response: IdResponse) => response.id));
    }

    saveReferences(references: PaymentReference[]): Observable<string[]> {
        return this.http
            .post(`${this.fbPaymentReferenceEndpoint}`, references, new HttpRequestModel())
            .pipe(map((response: ListResponse) => response.result));
    }
}
