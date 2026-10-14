import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ChannelComponent } from './channel.component';
import { AuthGuard, Roles } from '../../auth';
import { CreateChannelComponent } from './components/create-channel/create-channel.component';
import { EditChannelComponent } from './components/edit-channel/edit-channel.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: ChannelComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'new',
                        component: CreateChannelComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: ':id',
                        component: EditChannelComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class ChannelRoutingModule {}
