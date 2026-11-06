import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { PayoutsService } from './payouts.service';
import { ConfigService } from '../../config';
import { ListType } from '../fb-management/swagger-codegen/model/listType';

describe('PayoutsService', () => {
    const endpoint = 'http://management';
    let service: PayoutsService;
    let http: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [PayoutsService, { provide: ConfigService, useValue: { fbManagementEndpoint: endpoint } }],
        });
        service = TestBed.inject(PayoutsService);
        http = TestBed.inject(HttpTestingController);
    });

    afterEach(() => http.verify());

    it('removes a reference by its scope', () => {
        service.removeReference({ templateId: 't', partyId: 'party-1', walletId: 'wallet-1' }).subscribe();
        const request = http.expectOne((r) => r.url === `${endpoint}/payouts-references`);
        expect(request.request.method).toBe('DELETE');
        expect(request.request.params.get('partyId')).toBe('party-1');
        expect(request.request.params.get('walletId')).toBe('wallet-1');
        expect(request.request.params.get('isGlobal')).toBe('false');
        request.flush({ id: 'key' });
    });

    it('removes a global reference without party and wallet', () => {
        service.removeReference({ templateId: 't', isGlobal: true }).subscribe();
        const request = http.expectOne((r) => r.url === `${endpoint}/payouts-references`);
        expect(request.request.params.has('partyId')).toBeFalse();
        expect(request.request.params.get('isGlobal')).toBe('true');
        request.flush({ id: 'GLOBAL' });
    });

    it('removes a list record by type, list, value and scope', () => {
        service
            .removeListRow(ListType.Grey, { listName: 'CARD_TOKEN', value: 'token-1', partyId: 'party-1' })
            .subscribe();
        const request = http.expectOne((r) => r.url === `${endpoint}/payouts-lists`);
        expect(request.request.method).toBe('DELETE');
        expect(request.request.params.get('listType')).toBe('grey');
        expect(request.request.params.get('listName')).toBe('CARD_TOKEN');
        expect(request.request.params.get('value')).toBe('token-1');
        expect(request.request.params.get('partyId')).toBe('party-1');
        expect(request.request.params.has('walletId')).toBeFalse();
        request.flush({ id: 'message' });
    });

    it('searches template names by prefix and group references of a group', () => {
        let names: string[];
        service.getTemplateNames('payout').subscribe((result) => (names = result));
        const namesRequest = http.expectOne((r) => r.url === `${endpoint}/payouts-templates/names`);
        expect(namesRequest.request.params.get('regexpName')).toBe('payout%');
        namesRequest.flush({ result: ['payout-a'] });
        expect(names).toEqual(['payout-a']);

        service.removeGroupReference({ groupId: 'group/1', partyId: 'party-1' }).subscribe();
        const groupRequest = http.expectOne((r) => r.url === `${endpoint}/payouts-groups/group%2F1/references`);
        expect(groupRequest.request.method).toBe('DELETE');
        expect(groupRequest.request.params.get('partyId')).toBe('party-1');
        groupRequest.flush({ id: 'key' });
    });

    it('searches payout history with only the filled filters', () => {
        service
            .filterWithdrawals({
                from: '2034-01-01 00:00:00',
                to: '2034-01-02 00:00:00',
                sortOrder: 'DESC',
                walletId: 'wallet-1',
                status: undefined,
            })
            .subscribe();
        const request = http.expectOne((r) => r.url === `${endpoint}/payouts-historical-data/withdrawals`);
        expect(request.request.method).toBe('GET');
        expect(request.request.params.get('walletId')).toBe('wallet-1');
        expect(request.request.params.get('from')).toBe('2034-01-01 00:00:00');
        expect(request.request.params.has('status')).toBeFalse();
        request.flush({ result: [] });
    });
});
