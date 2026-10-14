import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { ConfigService } from '../../../config';
import { SearchParams } from '../../../shared/model/search-params';
import { filterParameters } from '../../../shared/utils/filter-params';
import { IdResponse } from '../../fb-management/swagger-codegen/model/idResponse';
import { PaymentReference } from '../../fb-management/swagger-codegen/model/paymentReference';
import { ReferencesResponse } from '../../fb-management/swagger-codegen/model/referencesResponse';

@Injectable()
export class PaymentDefaultReferencesService {
    private readonly fbPaymentReferenceEndpoint = `${this.configService.fbManagementEndpoint}/payments-references/default`;

    constructor(private http: HttpClient, private configService: ConfigService) {}

    delete(reference: PaymentReference): Observable<string> {
        return this.http
            .delete(`${this.fbPaymentReferenceEndpoint}/${reference.id}`)
            .pipe(map((response: IdResponse) => response.id));
    }

    save(reference: PaymentReference): Observable<string> {
        return this.http
            .post(`${this.fbPaymentReferenceEndpoint}`, reference)
            .pipe(map((response: IdResponse) => response.id));
    }

    filter(params?: SearchParams): Observable<ReferencesResponse> {
        return this.http.get<ReferencesResponse>(`${this.fbPaymentReferenceEndpoint}/filter`, {
            params: filterParameters(params),
        });
    }
}
