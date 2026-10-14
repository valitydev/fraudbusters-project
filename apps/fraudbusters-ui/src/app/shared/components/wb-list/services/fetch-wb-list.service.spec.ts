import { of } from 'rxjs';

import { FetchWbListService } from './fetch-wb-list.service';
import { PaymentListsService } from '../../../../api/payments/lists';
import { ConfigService } from '../../../../config';
import { ListType } from '../../../constants/list-type';

describe('FetchWbListService', () => {
    it('uses the last record id and insert time as the next page cursor', () => {
        const paymentListsService = {
            findListRows: jasmine.createSpy().and.returnValue(of({ result: [], count: 0 })),
        } as unknown as PaymentListsService;
        const configService = { pageSize: 100 } as ConfigService;
        const service = new FetchWbListService(paymentListsService, configService);
        const lastRecord = {
            id: 'last-id',
            insertTime: new Date('2026-08-21T10:00:00'),
        };

        service['fetch']({ listType: ListType.Black, listNames: ['email'] }, lastRecord.id, lastRecord).subscribe();

        expect(paymentListsService.findListRows).toHaveBeenCalledWith(
            jasmine.objectContaining({
                lastId: lastRecord.id,
                sortFieldValue: String(lastRecord.insertTime),
            })
        );
    });
});
