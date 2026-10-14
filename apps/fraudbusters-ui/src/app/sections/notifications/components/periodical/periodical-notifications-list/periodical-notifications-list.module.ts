import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { NotificationHeaderComponent } from './components/notification-header/notification-header.component';
import { NotificationItemComponent } from './components/notification-item/notification-item.component';
import { NotificationStatusToColorPipe } from './components/notification-item/notification-status-to-color.pipe';
import { PeriodicalNotificationsListComponent } from './periodical-notifications-list.component';
import { ListHeaderModule } from '../../../../../shared/components/list-header';
import { TagModule } from '../../../../../shared/components/tag';
import { SharedPipesModule } from '../../../../../shared/pipes';

@NgModule({
    imports: [
        CommonModule,
        FlexModule,
        MatCardModule,
        MatExpansionModule,
        MatDividerModule,
        MatIconModule,
        MatMenuModule,
        SharedPipesModule,
        MatButtonModule,
        ListHeaderModule,
        TagModule,
    ],
    declarations: [
        PeriodicalNotificationsListComponent,
        NotificationHeaderComponent,
        NotificationItemComponent,
        NotificationStatusToColorPipe,
    ],
    exports: [PeriodicalNotificationsListComponent],
})
export class PeriodicalNotificationsListModule {}
