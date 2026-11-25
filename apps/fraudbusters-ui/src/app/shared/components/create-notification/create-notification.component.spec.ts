import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ChangeDetectorRef, SimpleChange } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { CreateNotificationComponent } from './create-notification.component';
import { NotificationService } from './services/notification.service';
import { NotificationsService } from '../../../api/payments/notifications';
import { ConfigService } from '../../../config';

describe('CreateNotificationComponent contract', () => {
    let component: CreateNotificationComponent;
    let http: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                NotificationsService,
                { provide: ConfigService, useValue: { fbManagementEndpoint: '/api', pageSize: 20 } },
            ],
        });
        http = TestBed.inject(HttpTestingController);
        const service = new NotificationService(
            new FormBuilder(),
            TestBed.inject(NotificationsService),
            TestBed.inject(ConfigService)
        );
        component = new CreateNotificationComponent(
            jasmine.createSpyObj<Router>('Router', ['navigate']),
            service,
            jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']),
            jasmine.createSpyObj<ChangeDetectorRef>('ChangeDetectorRef', ['markForCheck'])
        );
    });

    afterEach(() => http.verify());

    it('creates a notification with a numeric template ID in the HTTP payload', () => {
        component.form.patchValue({
            name: 'Daily fraud report',
            type: 42,
            channel: 'test-channel',
            period: '1d',
            frequency: '60m',
            subject: 'Report',
        });

        component.saveNotification();

        const request = http.expectOne('/api/notifications');
        expect(request.request.method).toBe('POST');
        expect(request.request.body).toEqual(
            jasmine.objectContaining({ templateId: 42, channel: 'test-channel', status: 'ACTIVE' })
        );
        request.flush({ id: 7 });
    });

    it('preserves notification and template IDs when editing an existing notification', () => {
        component.notification = {
            id: 7,
            templateId: 42,
            name: 'Existing report',
            channel: 'test-channel',
            period: '7d',
            frequency: '10m',
            subject: 'Original subject',
        };
        component.editing = true;
        component.ngOnChanges({ notification: new SimpleChange(undefined, component.notification, true) });
        component.form.patchValue({ subject: 'Updated subject' });

        component.saveNotification();

        const request = http.expectOne('/api/notifications');
        expect(request.request.body).toEqual(
            jasmine.objectContaining({ id: 7, templateId: 42, subject: 'Updated subject', period: '7d' })
        );
        request.flush({ id: 7 });
    });

    it('keeps the form usable after a failed save', () => {
        component.form.patchValue({
            name: 'Daily fraud report',
            type: 42,
            channel: 'test-channel',
            period: '1d',
            frequency: '60m',
            subject: 'Report',
        });

        component.saveNotification();
        http.expectOne('/api/notifications').flush('fail', { status: 500, statusText: 'Server Error' });
        expect(component.saving).toBeFalse();
        expect(component.error).toContain('500');

        component.saveNotification();
        http.expectOne('/api/notifications').flush({ id: 7 });
        expect(component.error).toBe('');
    });

    it('requires every field the notificator stores', () => {
        expect(component.form.invalid).toBeTrue();
        component.form.patchValue({ name: 'Report', type: 42, channel: 'mail', period: '1d', frequency: '1m' });
        expect(component.form.invalid).toBeTrue();
        component.form.patchValue({ subject: 'Report' });
        expect(component.form.valid).toBeTrue();
    });
});
