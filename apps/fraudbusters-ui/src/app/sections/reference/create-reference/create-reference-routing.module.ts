import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CreatePaymentReferenceComponent } from './components';
import { CreateReferenceComponent } from './create-reference.component';
import { AuthGuard, Roles } from '../../../auth';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: CreateReferenceComponent,
                children: [
                    {
                        path: 'payment',
                        component: CreatePaymentReferenceComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class CreateReferenceRoutingModule {}
