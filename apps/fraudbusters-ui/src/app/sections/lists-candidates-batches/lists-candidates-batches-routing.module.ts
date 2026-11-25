import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { CandidateRecordsComponent } from './candidate-records/candidate-records.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: ':id',
                component: CandidateRecordsComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
            },
            { path: '', redirectTo: '/lists/candidates', pathMatch: 'full' },
        ]),
    ],
    exports: [RouterModule],
})
export class ListsCandidatesBatchesRoutingModule {}
