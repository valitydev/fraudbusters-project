import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AddRowGreyListComponent } from './add-row-grey-list/add-row-grey-list.component';
import { GreyListComponent } from './grey-list.component';
import { AuthGuard, Roles } from '../../../auth';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: GreyListComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
                children: [
                    {
                        path: 'new',
                        component: AddRowGreyListComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class GreyListRoutingModule {}
