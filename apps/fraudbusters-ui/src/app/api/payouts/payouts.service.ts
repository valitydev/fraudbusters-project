import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { ConfigService } from '../../config';
import { HttpRequestModel } from '../../shared/model/http-request-model';
import { filterParameters } from '../../shared/utils/filter-params';
import { CreateTemplateResponse } from '../fb-management/swagger-codegen/model/createTemplateResponse';
import { Group } from '../fb-management/swagger-codegen/model/group';
import { GroupsResponse } from '../fb-management/swagger-codegen/model/groupsResponse';
import { IdResponse } from '../fb-management/swagger-codegen/model/idResponse';
import { ListResponse } from '../fb-management/swagger-codegen/model/listResponse';
import { ListType } from '../fb-management/swagger-codegen/model/listType';
import { PayoutGroupReference } from '../fb-management/swagger-codegen/model/payoutGroupReference';
import { PayoutGroupReferencesResponse } from '../fb-management/swagger-codegen/model/payoutGroupReferencesResponse';
import { PayoutListRecord } from '../fb-management/swagger-codegen/model/payoutListRecord';
import { PayoutListRecordsResponse } from '../fb-management/swagger-codegen/model/payoutListRecordsResponse';
import { PayoutReference } from '../fb-management/swagger-codegen/model/payoutReference';
import { PayoutReferencesResponse } from '../fb-management/swagger-codegen/model/payoutReferencesResponse';
import { PayoutRowListRequest } from '../fb-management/swagger-codegen/model/payoutRowListRequest';
import { Template } from '../fb-management/swagger-codegen/model/template';
import { TemplatesResponse } from '../fb-management/swagger-codegen/model/templatesResponse';
import { ValidateTemplatesResponse } from '../fb-management/swagger-codegen/model/validateTemplatesResponse';

/** Payout configuration in fraudbusters-management; payout templates, groups and lists are kept apart from payments. */
@Injectable()
export class PayoutsService {
    private readonly endpoint = this.configService.fbManagementEndpoint;

    constructor(private http: HttpClient, private configService: ConfigService) {}

    findTemplates(params: { searchValue?: string; lastId?: string; size?: number }): Observable<TemplatesResponse> {
        return this.http.get<TemplatesResponse>(`${this.endpoint}/payouts-templates/filter`, {
            params: filterParameters(params),
        });
    }

    getTemplateNames(prefix?: string): Observable<string[]> {
        return this.http
            .get<ListResponse>(`${this.endpoint}/payouts-templates/names`, {
                params: filterParameters({ regexpName: `${prefix ?? ''}%` }),
            })
            .pipe(map((response) => response.result));
    }

    saveTemplate(template: Template): Observable<CreateTemplateResponse> {
        return this.http.post<CreateTemplateResponse>(
            `${this.endpoint}/payouts-templates`,
            template,
            new HttpRequestModel()
        );
    }

    validateTemplate(template: Template): Observable<ValidateTemplatesResponse> {
        return this.http.post<ValidateTemplatesResponse>(
            `${this.endpoint}/payouts-templates/validate`,
            template,
            new HttpRequestModel()
        );
    }

    removeTemplate(id: string): Observable<string> {
        return this.http
            .delete<IdResponse>(`${this.endpoint}/payouts-templates/${encodeURIComponent(id)}`)
            .pipe(map((response) => response.id));
    }

    findReferences(params: {
        searchValue?: string;
        isGlobal?: boolean;
        size?: number;
    }): Observable<PayoutReferencesResponse> {
        return this.http.get<PayoutReferencesResponse>(`${this.endpoint}/payouts-references/filter`, {
            params: filterParameters(params),
        });
    }

    saveReferences(references: PayoutReference[]): Observable<string[]> {
        return this.http
            .post<ListResponse>(`${this.endpoint}/payouts-references`, references, new HttpRequestModel())
            .pipe(map((response) => response.result));
    }

    removeReference(reference: PayoutReference): Observable<string> {
        return this.http
            .delete<IdResponse>(`${this.endpoint}/payouts-references`, {
                params: filterParameters({
                    partyId: reference.partyId,
                    walletId: reference.walletId,
                    isGlobal: !!reference.isGlobal,
                }),
            })
            .pipe(map((response) => response.id));
    }

    findGroups(searchValue?: string): Observable<GroupsResponse> {
        return this.http.get<GroupsResponse>(`${this.endpoint}/payouts-groups/filter`, {
            params: filterParameters({ searchValue }),
        });
    }

    getGroup(groupId: string): Observable<Group> {
        return this.http.get<Group>(`${this.endpoint}/payouts-groups/${encodeURIComponent(groupId)}`);
    }

    saveGroup(group: Group): Observable<string> {
        return this.http
            .post<IdResponse>(`${this.endpoint}/payouts-groups`, group, new HttpRequestModel())
            .pipe(map((response) => response.id));
    }

    removeGroup(groupId: string): Observable<string> {
        return this.http
            .delete<IdResponse>(`${this.endpoint}/payouts-groups/${encodeURIComponent(groupId)}`)
            .pipe(map((response) => response.id));
    }

    findGroupReferences(params: { searchValue?: string; size?: number }): Observable<PayoutGroupReferencesResponse> {
        return this.http.get<PayoutGroupReferencesResponse>(`${this.endpoint}/payouts-groups/references/filter`, {
            params: filterParameters(params),
        });
    }

    saveGroupReferences(groupId: string, references: PayoutGroupReference[]): Observable<string[]> {
        return this.http
            .post<ListResponse>(
                `${this.endpoint}/payouts-groups/${encodeURIComponent(groupId)}/references`,
                references,
                new HttpRequestModel()
            )
            .pipe(map((response) => response.result));
    }

    removeGroupReference(reference: PayoutGroupReference): Observable<string> {
        return this.http
            .delete<IdResponse>(`${this.endpoint}/payouts-groups/${encodeURIComponent(reference.groupId)}/references`, {
                params: filterParameters({ partyId: reference.partyId, walletId: reference.walletId }),
            })
            .pipe(map((response) => response.id));
    }

    findListRows(params: {
        listType: ListType;
        listNames?: string[];
        searchValue?: string;
        size?: number;
    }): Observable<PayoutListRecordsResponse> {
        const listNames = params.listNames?.length ? params.listNames : undefined;
        return this.http.get<PayoutListRecordsResponse>(`${this.endpoint}/payouts-lists/filter`, {
            params: filterParameters({ ...params, listNames }),
        });
    }

    getAvailableListNames(): Observable<string[]> {
        return this.http
            .get<ListResponse>(`${this.endpoint}/payouts-lists/availableListNames`)
            .pipe(map((response) => response.result));
    }

    getCurrentListNames(listType: ListType): Observable<string[]> {
        return this.http
            .get<ListResponse>(`${this.endpoint}/payouts-lists/currentListNames`, {
                params: filterParameters({ listType }),
            })
            .pipe(map((response) => response.result));
    }

    saveListRows(request: PayoutRowListRequest): Observable<string[]> {
        return this.http
            .post<ListResponse>(`${this.endpoint}/payouts-lists`, request, new HttpRequestModel())
            .pipe(map((response) => response.result));
    }

    removeListRow(listType: ListType, record: PayoutListRecord): Observable<string> {
        return this.http
            .delete<IdResponse>(`${this.endpoint}/payouts-lists`, {
                params: filterParameters({ listType, ...record }),
            })
            .pipe(map((response) => response.id));
    }
}
