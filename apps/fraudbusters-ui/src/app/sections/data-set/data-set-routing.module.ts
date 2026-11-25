import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { DataSetComponent } from './data-set.component';
import { AuthGuard, Roles } from '../../auth';
import { TestingDataSetComponent } from './testing/testing-data-set.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: DataSetComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: ':id/testing',
                        component: TestingDataSetComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class DataSetRoutingModule {}
