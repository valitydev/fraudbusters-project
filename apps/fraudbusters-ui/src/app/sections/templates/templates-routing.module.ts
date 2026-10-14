import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { DefaultPaymentReferencesComponent } from './components/payment-dafeult-references/default-payment-references.component';
import { PaymentReferencesComponent } from './components/payment-references/payment-references.component';
import { PaymentTemplatesComponent } from './components/payment-templates/payment-templates.component';
import { TemplatesComponent } from './templates.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: TemplatesComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'templates',
                        component: PaymentTemplatesComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: 'references',
                        component: PaymentReferencesComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: 'default-references',
                        component: DefaultPaymentReferencesComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: '',
                        redirectTo: 'templates',
                        pathMatch: 'full',
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class TemplatesRoutingModule {}
