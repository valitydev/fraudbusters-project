import { Injectable } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs';

import { Channel } from '../../../../api/fb-management/swagger-codegen/model/channel';
import { ChannelType } from '../../../../api/fb-management/swagger-codegen/model/channelType';
import { NotificationsService } from '../../../../api/payments/notifications';

@Injectable()
export class ChannelService {
    form = this.fb.group({
        name: ['', Validators.required],
        type: [ChannelType.Mail as ChannelType, Validators.required],
        destination: ['', Validators.required],
    });

    constructor(private fb: FormBuilder, private notificationsService: NotificationsService) {}

    save(channel: Channel): Observable<Channel> {
        return this.notificationsService.saveChannel(channel);
    }
}
