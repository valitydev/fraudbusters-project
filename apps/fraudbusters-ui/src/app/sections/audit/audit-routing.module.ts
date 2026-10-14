import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuditComponent } from './audit.component';
import { AuthGuard, Roles } from '../../auth';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: AuditComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
            },
        ]),
    ],
    exports: [RouterModule],
})
export class AuditRoutingModule {}
