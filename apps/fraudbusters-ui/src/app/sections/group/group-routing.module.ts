import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { CreateGroupComponent } from './create-group/create-group.component';
import { EditGroupComponent } from './edit-group/edit-group.component';
import { GroupComponent } from './group.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: GroupComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'new',
                        component: CreateGroupComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: ':id',
                        component: EditGroupComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class GroupRoutingModule {}
