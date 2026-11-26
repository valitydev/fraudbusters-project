import { testResultRow } from './test-result-details';
import { CheckedDataSetRow } from '../../../api/fb-management/swagger-codegen/model/checkedDataSetRow';
import { Payment } from '../../../api/fb-management/swagger-codegen/model/payment';

describe('testResultRow', () => {
    const row: CheckedDataSetRow = {
        id: '17',
        checkedTemplate: 'black-list-template',
        resultStatus: 'decline',
        ruleChecked: 'black',
        notificationRule: ['mail'],
        payment: {
            id: 'invoice_id_1.1',
            status: Payment.StatusEnum.Captured,
            amount: 10000,
            currency: 'RUB',
            merchantInfo: { partyId: 'party-wb-id', shopId: 'shop-wb-id' },
            clientInfo: { email: 'test@mail.ru', ip: '123.123.123.123' },
            cardToken: 'wb_test_token_133',
            bin: '427640',
            lastDigits: '6395',
        },
    };

    it('fills the shared table columns from the test result', () => {
        const result = testResultRow(row, 0);
        expect(result).toEqual(
            jasmine.objectContaining({
                key: '17#0',
                id: 'invoice_id_1.1',
                status: 'decline',
                rule: 'black',
                amount: 10000,
                currency: 'RUB',
                partyId: 'party-wb-id',
                scopeId: 'shop-wb-id',
                email: 'test@mail.ru',
            })
        );
        expect(result.source).toBe(row);
    });

    it('keeps the payment details of Historical data without repeating the amount column', () => {
        const labels = testResultRow(row, 0).details.map((detail) => detail.label);
        expect(labels).toContain('Card token');
        expect(labels).toContain('Notification rules');
        expect(labels).not.toContain('Amount');
    });

    it('handles a row without a payment', () => {
        expect(testResultRow({ resultStatus: 'accept' }, 3).details).toEqual([]);
    });
});
