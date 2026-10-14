import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { CreateNotificationComponent } from './components/create-notification/create-notification.component';
import { EditNotificationComponent } from './components/edit-notification/edit-notification.component';
import { NotificationComponent } from './notification.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: NotificationComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'new',
                        component: CreateNotificationComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: ':id',
                        component: EditNotificationComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class NotificationRoutingModule {}
