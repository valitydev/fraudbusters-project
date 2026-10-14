import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CreateDefaultPaymentReferenceComponent } from './components';
import { CreateDefaultReferenceComponent } from './create-default-reference.component';
import { AuthGuard, Roles } from '../../../auth';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: CreateDefaultReferenceComponent,
                children: [
                    {
                        path: 'payment',
                        component: CreateDefaultPaymentReferenceComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class CreateDefaultReferenceRoutingModule {}
