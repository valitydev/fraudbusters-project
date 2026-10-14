import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { FraudUploaderComponent } from './fraud-uploader.component';
import { AuthGuard, Roles } from '../../../auth';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: FraudUploaderComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
            },
        ]),
    ],
    exports: [RouterModule],
})
export class FraudUploaderRoutingModule {}
