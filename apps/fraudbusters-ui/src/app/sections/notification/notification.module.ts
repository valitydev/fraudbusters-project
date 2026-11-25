import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { CreateNotificationComponent } from './components/create-notification/create-notification.component';
import { EditNotificationComponent } from './components/edit-notification/edit-notification.component';
import { NotificationRoutingModule } from './notification-routing.module';
import { NotificationComponent } from './notification.component';
import { NotificationService } from './services/notification.service';
import { NotificationsService } from '../../api/payments/notifications';
import { CreateNotificationModule } from '../../shared/components/create-notification/create-notification.module';

@NgModule({
    declarations: [NotificationComponent, EditNotificationComponent, CreateNotificationComponent],
    imports: [CommonModule, NotificationRoutingModule, CreateNotificationModule],
    providers: [NotificationsService, NotificationService],
})
export class NotificationModule {}
