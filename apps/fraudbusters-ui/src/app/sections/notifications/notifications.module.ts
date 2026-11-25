import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterModule } from '@angular/router';

import { ChannelsComponent } from './components/channels/channels.component';
import { PeriodicalNotificationComponent } from './components/periodical/periodical-notification.component';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { NotificationsComponent } from './notifications.component';
import { NotificationsService } from '../../api/payments/notifications/notifications.service';
import { CatalogPanelComponent } from '../../shared/components/catalog-panel/catalog-panel.component';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        NotificationsRoutingModule,
        MatTabsModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        MatDialogModule,
        MatSnackBarModule,
        ConfirmActionDialogModule,
        CatalogPanelComponent,
    ],
    declarations: [NotificationsComponent, PeriodicalNotificationComponent, ChannelsComponent],
    providers: [NotificationsService],
})
export class NotificationsModule {}
