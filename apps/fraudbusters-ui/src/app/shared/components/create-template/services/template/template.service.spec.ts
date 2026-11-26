import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import { TemplateService } from './template.service';
import { PaymentTemplatesService } from '../../../../../api';
import { ConfigService } from '../../../../../config';

describe('TemplateService', () => {
    let service: TemplateService;
    let http: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [
                PaymentTemplatesService,
                { provide: ConfigService, useValue: { fbManagementEndpoint: '/api' } },
                provideHttpClient(withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ],
        });
        http = TestBed.inject(HttpTestingController);
        service = new TemplateService(new FormBuilder(), TestBed.inject(PaymentTemplatesService));
    });

    afterEach(() => http.verify());

    it('keeps saving after a failed request and clears the busy state', () => {
        const saved: string[] = [];
        const errors: number[] = [];
        const busy: boolean[] = [];
        service.saved$.subscribe((response) => saved.push(response.id));
        service.errors$.subscribe((error) => errors.push(error.status));
        service.inProgress$.subscribe((value) => busy.push(value));
        const template = { id: 'rule', template: 'rule: amount() > 0 -> decline;' };

        service.saveTemplate({ template });
        http.expectOne({ method: 'POST' }).flush('fail', { status: 500, statusText: 'Server Error' });
        service.saveTemplate({ template });
        http.expectOne({ method: 'POST' }).flush({ id: 'rule' });

        expect(errors).toEqual([500]);
        expect(saved).toEqual(['rule']);
        expect(busy).toEqual([false, true, false, true, false]);
    });
});
