import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { PayoutGroupReferencesComponent } from './components/payout-group-references/payout-group-references.component';
import { PayoutGroupsComponent } from './components/payout-groups/payout-groups.component';
import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutReferencesComponent } from './components/payout-references/payout-references.component';
import { PayoutTemplatesComponent } from './components/payout-templates/payout-templates.component';
import { PayoutSectionComponent } from './payout-section.component';
import { ListType } from '../../api/fb-management/swagger-codegen/model/listType';

const OFFICER = { canActivate: [AuthGuard], data: { roles: [Roles.FraudOfficer] } };
const LIST_ROLES = [Roles.FraudOfficer, Roles.FraudMonitoring];

const listTab = (path: string, listType: ListType) => ({
    path,
    component: PayoutListsComponent,
    canActivate: [AuthGuard],
    data: { roles: LIST_ROLES, listType },
});

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: 'templates',
                component: PayoutSectionComponent,
                canActivate: [AuthGuard],
                data: {
                    roles: [Roles.FraudOfficer],
                    title: 'Payout templates',
                    links: [
                        { path: 'templates', name: 'Templates' },
                        { path: 'references', name: 'References' },
                    ],
                },
                children: [
                    { path: 'templates', component: PayoutTemplatesComponent, ...OFFICER },
                    { path: 'references', component: PayoutReferencesComponent, ...OFFICER },
                    { path: '', redirectTo: 'templates', pathMatch: 'full' },
                ],
            },
            {
                path: 'groups',
                component: PayoutSectionComponent,
                canActivate: [AuthGuard],
                data: {
                    roles: [Roles.FraudOfficer],
                    title: 'Payout groups',
                    links: [
                        { path: 'list', name: 'Groups' },
                        { path: 'references', name: 'References' },
                    ],
                },
                children: [
                    { path: 'list', component: PayoutGroupsComponent, ...OFFICER },
                    { path: 'references', component: PayoutGroupReferencesComponent, ...OFFICER },
                    { path: '', redirectTo: 'list', pathMatch: 'full' },
                ],
            },
            {
                path: 'lists',
                component: PayoutSectionComponent,
                canActivate: [AuthGuard],
                data: {
                    roles: LIST_ROLES,
                    title: 'Payout lists',
                    links: [
                        { path: 'black', name: 'Black' },
                        { path: 'white', name: 'White' },
                        { path: 'grey', name: 'Grey' },
                        { path: 'named', name: 'Named' },
                    ],
                },
                children: [
                    listTab('black', ListType.Black),
                    listTab('white', ListType.White),
                    listTab('grey', ListType.Grey),
                    listTab('named', ListType.Naming),
                    { path: '', redirectTo: 'black', pathMatch: 'full' },
                ],
            },
            { path: '', redirectTo: 'templates', pathMatch: 'full' },
        ]),
    ],
    exports: [RouterModule],
})
export class PayoutsRoutingModule {}
