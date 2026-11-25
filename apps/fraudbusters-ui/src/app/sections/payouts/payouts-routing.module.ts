import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { PayoutGroupFormComponent } from './components/payout-group-form/payout-group-form.component';
import { PayoutGroupReferenceFormComponent } from './components/payout-group-reference-form/payout-group-reference-form.component';
import { PayoutGroupReferencesComponent } from './components/payout-group-references/payout-group-references.component';
import { PayoutGroupsComponent } from './components/payout-groups/payout-groups.component';
import { ListRowsFormComponent } from '../list/list-rows-form/list-rows-form.component';
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
                    title: 'Templates',
                    subtitle: 'Manage payout rules and bind templates to parties and wallets.',
                    links: [
                        {
                            path: 'templates',
                            name: 'Templates',
                            createUrl: '/payouts/template/new',
                            createLabel: 'Create template',
                        },
                        {
                            path: 'references',
                            name: 'References',
                            createUrl: '/payouts/reference/new',
                            createLabel: 'Create reference',
                        },
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
                    title: 'Groups',
                    subtitle: 'Set the order of payout rule templates and manage their bindings.',
                    links: [
                        { path: 'list', name: 'Groups', createUrl: '/payouts/group/new', createLabel: 'Create group' },
                        {
                            path: 'references',
                            name: 'References',
                            createUrl: '/payouts/group-reference/new',
                            createLabel: 'Create reference',
                        },
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
                    title: 'Lists',
                    subtitle: 'Block, allow and limit entities in payouts.',
                    links: [
                        {
                            path: 'black',
                            name: 'Black',
                            listType: ListType.Black,
                            createUrl: '/payouts/list/new/black',
                            createLabel: 'Create record',
                        },
                        {
                            path: 'white',
                            name: 'White',
                            listType: ListType.White,
                            createUrl: '/payouts/list/new/white',
                            createLabel: 'Create record',
                        },
                        {
                            path: 'grey',
                            name: 'Grey',
                            listType: ListType.Grey,
                            createUrl: '/payouts/list/new/grey',
                            createLabel: 'Create record',
                        },
                        {
                            path: 'named',
                            name: 'Named',
                            listType: ListType.Naming,
                            createUrl: '/payouts/list/new/naming',
                            createLabel: 'Create record',
                        },
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
                component: ListRowsFormComponent,
                canActivate: [AuthGuard],
                data: { roles: LIST_ROLES, payout: true },
            },
            { path: '', redirectTo: 'templates', pathMatch: 'full' },
        ]),
    ],
    exports: [RouterModule],
})
export class PayoutsRoutingModule {}
