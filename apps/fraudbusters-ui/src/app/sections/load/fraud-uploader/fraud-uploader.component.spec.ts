import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { Papa } from 'ngx-papaparse';

import { fraudReportRow, missingColumns } from './fraud-report';
import { FraudUploaderComponent } from './fraud-uploader.component';
import { PaymentLoadDataService } from '../../../api/payments/load-data';

describe('fraud report rows', () => {
    it('accepts a row with an optional or well-formed event time', () => {
        expect(fraudReportRow({ id: 'p1', fraud_type: 'card_testing', comment: '', event_time: '' }, 2).issue).toBe('');
        expect(fraudReportRow({ id: 'p1', fraud_type: 'x', event_time: '2026-01-15 10:30:00' }, 2).issue).toBe('');
        expect(fraudReportRow({ id: 'p1', fraud_type: 'x', event_time: '2026-01-15' }, 2).issue).toBe('');
    });

    it('flags rows the backend would reject', () => {
        expect(fraudReportRow({ id: ' ', fraud_type: 'x' }, 2).issue).toBe('Payment ID is empty');
        expect(fraudReportRow({ id: 'p1', fraud_type: '' }, 2).issue).toBe('Fraud type is empty');
        expect(fraudReportRow({ id: 'p1', fraud_type: 'x', event_time: '15.01.2026' }, 2).issue).toContain(
            'Event time'
        );
    });

    it('lists the columns a file is missing', () => {
        expect(missingColumns(['id', 'fraud_type', 'comment', 'event_time'])).toEqual([]);
        expect(missingColumns(['id', 'comment'])).toEqual(['fraud_type', 'event_time']);
    });
});

describe('FraudUploaderComponent', () => {
    it('enables upload only for a file without invalid rows', async () => {
        TestBed.configureTestingModule({
            imports: [RouterTestingModule],
            declarations: [FraudUploaderComponent],
            providers: [
                Papa,
                {
                    provide: PaymentLoadDataService,
                    useValue: jasmine.createSpyObj('PaymentLoadDataService', ['postFile']),
                },
                { provide: MatSnackBar, useValue: jasmine.createSpyObj('MatSnackBar', ['open']) },
            ],
            schemas: [NO_ERRORS_SCHEMA],
        });
        const component = TestBed.createComponent(FraudUploaderComponent).componentInstance;
        const input = (text: string) =>
            ({
                files: [new File([text], 'fraud.csv', { type: 'text/csv' })],
                value: 'fraud.csv',
            } as unknown as HTMLInputElement);

        component.choose(input('id,fraud_type,comment,event_time\np1,card_testing,,2026-01-15\n'));
        await new Promise((resolve) => setTimeout(resolve, 50));
        expect(component.rows.length).toBe(1);
        expect(component.canUpload).toBeTrue();

        component.choose(input('id,fraud_type,comment,event_time\np1,,,\n'));
        await new Promise((resolve) => setTimeout(resolve, 50));
        expect(component.invalidRows.length).toBe(1);
        expect(component.canUpload).toBeFalse();
    });
});
