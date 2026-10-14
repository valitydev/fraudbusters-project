import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { EmulationTemplateComponent } from './components/emulation/template/emulation-template.component';
import { PaymentDataSetsComponent } from './components/payment-data-sets/payment-data-sets.component';
import { TestingComponent } from './testing.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: TestingComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'data-sets',
                        component: PaymentDataSetsComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: 'emulation',
                        component: EmulationTemplateComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class TestingRoutingModule {}
