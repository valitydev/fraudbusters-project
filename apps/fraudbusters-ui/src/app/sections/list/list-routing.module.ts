import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ListRowsFormComponent } from './list-rows-form/list-rows-form.component';
import { AuthGuard, Roles } from '../../auth';

const ROUTES: Routes = [
    {
        // One page for black, white and grey rows; the list can also be switched on the page.
        path: ':listType/new',
        component: ListRowsFormComponent,
        canActivate: [AuthGuard],
        data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
    },
];

@NgModule({
    imports: [RouterModule.forChild(ROUTES)],
    exports: [RouterModule],
})
export class ListRoutingModule {}
