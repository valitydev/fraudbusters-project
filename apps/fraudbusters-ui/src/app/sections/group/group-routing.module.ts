import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { GroupComponent } from './group.component';
import { AuthGuard, Roles } from '../../auth';
import { PaymentGroupFormComponent } from './payment-group-form/payment-group-form.component';

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
                        component: PaymentGroupFormComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: ':id',
                        component: PaymentGroupFormComponent,
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
