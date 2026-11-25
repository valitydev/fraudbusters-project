import { Injectable } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

import { Channel } from '../../../../api/fb-management/swagger-codegen/model/channel';
import { Notification } from '../../../../api/fb-management/swagger-codegen/model/notification';
import { NotificationTemplate } from '../../../../api/fb-management/swagger-codegen/model/notificationTemplate';
import { NotificationsService } from '../../../../api/payments/notifications';
import { ConfigService } from '../../../../config';

@Injectable()
export class NotificationService {
    // Every notification column is NOT NULL in the notificator schema.
    form = this.fb.group({
        id: [undefined as number | undefined],
        name: ['', Validators.required],
        type: [null as number | null, Validators.required],
        subject: ['', Validators.required],
        channel: ['', Validators.required],
        period: ['', Validators.required],
        frequency: ['', Validators.required],
    });

    notificationsTemplates$: Observable<Array<NotificationTemplate>> =
        this.notificationsService.getNotificationsTemplates();
    notificationsChannels$: Observable<Array<Channel>> = this.notificationsService.getNotificationsChannels({
        searchValue: '',
        size: this.configService.pageSize,
    });

    constructor(
        private fb: FormBuilder,
        private notificationsService: NotificationsService,
        private configService: ConfigService
    ) {}

    save(notification: Notification): Observable<Notification> {
        return this.notificationsService.save(notification);
    }
}
