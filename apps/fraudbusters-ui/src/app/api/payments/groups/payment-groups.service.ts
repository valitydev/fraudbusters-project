import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { ConfigService } from '../../../config';
import { Group } from '../../../sections/groups/components/payments-groups/model/group';
import { GroupsResponse } from '../../fb-management/swagger-codegen/model/groupsResponse';
import { IdResponse } from '../../fb-management/swagger-codegen/model/idResponse';

@Injectable()
export class PaymentGroupsService {
    private readonly fbPaymentReferenceEndpoint = `${this.configService.fbManagementEndpoint}/payments-groups`;

    constructor(private http: HttpClient, private configService: ConfigService) {}

    filter(filterId: string): Observable<GroupsResponse> {
        return this.http.get<GroupsResponse>(`${this.fbPaymentReferenceEndpoint}/filter`, {
            params: filterId ? { searchValue: filterId } : {},
        });
    }

    getGroupById(id: string): Observable<Group> {
        return this.http.get<Group>(`${this.fbPaymentReferenceEndpoint}/${id}`);
    }

    delete(id: string): Observable<string> {
        return this.http
            .delete(`${this.fbPaymentReferenceEndpoint}/${id}`)
            .pipe(map((response: IdResponse) => response.id));
    }

    save(group: Group): Observable<string> {
        return this.http
            .post(`${this.fbPaymentReferenceEndpoint}`, group)
            .pipe(map((response: IdResponse) => response.id));
    }
}
