import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { PayoutGroupFormComponent } from './components/payout-group-form/payout-group-form.component';
import { PayoutGroupReferenceFormComponent } from './components/payout-group-reference-form/payout-group-reference-form.component';
import { PayoutGroupReferencesComponent } from './components/payout-group-references/payout-group-references.component';
import { PayoutGroupsComponent } from './components/payout-groups/payout-groups.component';
import { PayoutListRowsFormComponent } from './components/payout-list-rows-form/payout-list-rows-form.component';
import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutReferenceFormComponent } from './components/payout-reference-form/payout-reference-form.component';
import { PayoutReferencesComponent } from './components/payout-references/payout-references.component';
import { PayoutTemplateFormComponent } from './components/payout-template-form/payout-template-form.component';
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
            { path: 'template/new', component: PayoutTemplateFormComponent, ...OFFICER },
            { path: 'template/new/:id', component: PayoutTemplateFormComponent, ...OFFICER },
            {
                path: 'template/:id',
                component: PayoutTemplateFormComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer], mode: 'edit' },
            },
            { path: 'reference/new', component: PayoutReferenceFormComponent, ...OFFICER },
            { path: 'group/new', component: PayoutGroupFormComponent, ...OFFICER },
            { path: 'group/:id', component: PayoutGroupFormComponent, ...OFFICER },
            { path: 'group-reference/new', component: PayoutGroupReferenceFormComponent, ...OFFICER },
            {
                path: 'list/new/:listType',
                component: PayoutListRowsFormComponent,
                canActivate: [AuthGuard],
                data: { roles: LIST_ROLES },
            },
            { path: '', redirectTo: 'templates', pathMatch: 'full' },
        ]),
    ],
    exports: [RouterModule],
})
export class PayoutsRoutingModule {}
