import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { BlackListComponent } from './components/black-list/black-list.component';
import { CandidatesComponent } from './components/candidates/candidates.component';
import { GreyListComponent } from './components/grey-list/grey-list.component';
import { WhiteListComponent } from './components/white-list/white-list.component';
import { ListsComponent } from './lists.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: ListsComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'white',
                        component: WhiteListComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: 'black',
                        component: BlackListComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: 'grey',
                        component: GreyListComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: 'candidates',
                        component: CandidatesComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: '',
                        redirectTo: 'black',
                        pathMatch: 'full',
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class ListsRoutingModule {}
