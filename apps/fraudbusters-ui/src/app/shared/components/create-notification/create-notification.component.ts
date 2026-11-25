import { HttpErrorResponse } from '@angular/common/http';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { NotificationService } from './services/notification.service';
import { Notification } from '../../../api/fb-management/swagger-codegen/model/notification';

interface Option {
    value: string;
    label: string;
}

const PERIODS: Option[] = [
    { value: '1d', label: '1 day' },
    { value: '7d', label: '7 days' },
    { value: '30d', label: '30 days' },
];

const FREQUENCIES: Option[] = [
    { value: '1m', label: 'Every minute' },
    { value: '10m', label: 'Every 10 minutes' },
    { value: '60m', label: 'Every hour' },
];

/** Notification form with its page header, used by the New and Edit notification routes. */
@Component({
    selector: 'fb-create-notification',
    templateUrl: './create-notification.component.html',
    styleUrls: ['../reference-form/reference-form.component.scss', './create-notification.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [NotificationService],
})
export class CreateNotificationComponent implements OnChanges {
    @Input() notification?: Notification;
    @Input() editing = false;
    @Input() loading = false;
    @Input() loadFailed = false;
    @Output() retry = new EventEmitter<void>();

    form = this.notificationService.form;
    notificationsTemplates$ = this.notificationService.notificationsTemplates$;
    notificationsChannels$ = this.notificationService.notificationsChannels$;

    periods = PERIODS;
    frequencies = FREQUENCIES;

    saving = false;
    error = '';

    constructor(
        private router: Router,
        private notificationService: NotificationService,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnChanges({ notification }: SimpleChanges) {
        if (notification && this.notification) {
            const { id, name, templateId, subject, channel, period, frequency } = this.notification;
            this.form.setValue({ id, name, type: templateId, subject, channel, period, frequency });
            // Keep a stored value selectable even when it is not one of the presets.
            this.periods = withValue(PERIODS, period);
            this.frequencies = withValue(FREQUENCIES, frequency);
        }
    }

    saveNotification() {
        if (this.saving) {
            return;
        }
        const value = this.form.getRawValue();
        this.saving = true;
        this.error = '';
        this.notificationService
            .save({
                id: value.id,
                channel: value.channel,
                frequency: value.frequency,
                period: value.period,
                subject: value.subject,
                templateId: value.type,
                name: value.name,
                createdAt: new Date(Date.now()),
                status: 'ACTIVE',
            })
            .pipe(
                finalize(() => {
                    this.saving = false;
                    this.cdr.markForCheck();
                })
            )
            .subscribe({
                next: () => {
                    this.snackBar.open(this.editing ? 'Notification saved' : 'Notification created', 'OK', {
                        duration: 3000,
                    });
                    if (!this.editing) {
                        this.back();
                    }
                },
                error: ({ status }: HttpErrorResponse) => {
                    this.error = `Could not save the notification${status ? ` (${status})` : ''}. Try again.`;
                },
            });
    }

    back() {
        void this.router.navigate(['/notifications/periodical']);
    }
}

function withValue(options: Option[], value?: string): Option[] {
    return !value || options.some((option) => option.value === value) ? options : [...options, { value, label: value }];
}
