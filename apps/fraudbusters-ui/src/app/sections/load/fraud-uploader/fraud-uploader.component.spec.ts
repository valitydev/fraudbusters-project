import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';

import { FraudUploaderComponent } from './fraud-uploader.component';
import { PaymentLoadDataService } from '../../../api/payments/load-data';
import { ErrorHandlerService } from '../../../shared/services/utils/error-handler.service';
import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../tokens';

describe('FraudUploaderComponent', () => {
    let component: FraudUploaderComponent;
    let fixture: ComponentFixture<FraudUploaderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CommonModule, RouterTestingModule],
            declarations: [FraudUploaderComponent],
            providers: [
                {
                    provide: PaymentLoadDataService,
                    useValue: jasmine.createSpyObj('PaymentLoadDataService', ['postFile']),
                },
                {
                    provide: ErrorHandlerService,
                    useValue: jasmine.createSpyObj('ErrorHandlerService', ['handleStringError']),
                },
                { provide: MatSnackBar, useValue: jasmine.createSpyObj('MatSnackBar', ['open']) },
                { provide: LAYOUT_GAP_L, useValue: '24px' },
                { provide: LAYOUT_GAP_M, useValue: '16px' },
            ],
            schemas: [NO_ERRORS_SCHEMA],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FraudUploaderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
