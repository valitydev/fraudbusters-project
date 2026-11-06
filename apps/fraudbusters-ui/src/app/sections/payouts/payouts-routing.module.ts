import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { PayoutGroupReferencesComponent } from './components/payout-group-references/payout-group-references.component';
import { PayoutGroupsComponent } from './components/payout-groups/payout-groups.component';
import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutReferencesComponent } from './components/payout-references/payout-references.component';
import { PayoutTemplatesComponent } from './components/payout-templates/payout-templates.component';
import { PayoutsComponent } from './payouts.component';

const OFFICER = { canActivate: [AuthGuard], data: { roles: [Roles.FraudOfficer] } };

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: PayoutsComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
                children: [
                    { path: 'templates', component: PayoutTemplatesComponent, ...OFFICER },
                    { path: 'references', component: PayoutReferencesComponent, ...OFFICER },
                    { path: 'groups', component: PayoutGroupsComponent, ...OFFICER },
                    { path: 'group-references', component: PayoutGroupReferencesComponent, ...OFFICER },
                    {
                        path: 'lists',
                        component: PayoutListsComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer, Roles.FraudMonitoring] },
                    },
                    { path: '', redirectTo: 'templates', pathMatch: 'full' },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class PayoutsRoutingModule {}
