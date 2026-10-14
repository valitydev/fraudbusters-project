import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AddRowBlackListComponent } from './add-row-black-list/add-row-black-list.component';
import { BlackListComponent } from './black-list.component';
import { AuthGuard, Roles } from '../../../../auth';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: BlackListComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
                children: [
                    {
                        path: 'new',
                        component: AddRowBlackListComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class BlackListRoutingModule {}
