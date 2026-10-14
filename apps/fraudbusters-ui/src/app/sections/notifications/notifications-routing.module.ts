import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { ChannelsComponent } from './components/channels/channels.component';
import { PeriodicalNotificationComponent } from './components/periodical/periodical-notification.component';
import { NotificationsComponent } from './notifications.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: NotificationsComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'periodical',
                        component: PeriodicalNotificationComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: 'channels',
                        component: ChannelsComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: '',
                        redirectTo: 'periodical',
                        pathMatch: 'full',
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class NotificationsRoutingModule {}
