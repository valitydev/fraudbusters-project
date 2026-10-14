import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { EmulationTemplateComponent } from './emulation-template.component';
import { AuthGuard, Roles } from '../../../../../auth';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: EmulationTemplateComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring, Roles.FraudSupport] },
            },
        ]),
    ],
    exports: [RouterModule],
})
export class EmulationTemplateRoutingModule {}
