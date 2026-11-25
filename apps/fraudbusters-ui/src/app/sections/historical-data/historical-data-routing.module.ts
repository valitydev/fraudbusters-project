import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { HistoricalDataComponent } from './historical-data.component';
import { AuthGuard, Roles } from '../../auth';
import { HistoricalKindKey } from './records/historical-kinds';
import { HistoricalRecordsComponent } from './records/historical-records.component';

const tab = (path: string, kind: HistoricalKindKey) => ({
    path,
    component: HistoricalRecordsComponent,
    canActivate: [AuthGuard],
    data: { roles: [Roles.FraudOfficer], kind },
});

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: 'payments',
                component: HistoricalDataComponent,
                canActivate: [AuthGuard],
                data: {
                    roles: [Roles.FraudOfficer],
                    title: 'Historical data',
                    subtitle: 'Search rule evaluations, payments, refunds, chargebacks and fraud reports.',
                    links: [
                        { path: 'inspect-results', name: 'Inspect results', dot: '#6338f5' },
                        { path: 'payments', name: 'Payments', dot: '#10b981' },
                        { path: 'refunds', name: 'Refunds', dot: '#0ea5e9' },
                        { path: 'chargebacks', name: 'Chargebacks', dot: '#f59e0b' },
                        { path: 'fraud-payments', name: 'Fraud payments', dot: '#f43f5e' },
                    ],
                },
                children: [
                    tab('inspect-results', 'inspect-results'),
                    tab('payments', 'payments'),
                    tab('refunds', 'refunds'),
                    tab('chargebacks', 'chargebacks'),
                    tab('fraud-payments', 'fraud-payments'),
                    { path: '', redirectTo: 'inspect-results', pathMatch: 'full' },
                ],
            },
            {
                path: 'payouts',
                component: HistoricalDataComponent,
                canActivate: [AuthGuard],
                data: {
                    roles: [Roles.FraudOfficer],
                    title: 'Historical data',
                    subtitle: 'Search payouts and their rule evaluations.',
                    links: [
                        { path: 'payouts', name: 'Payouts', dot: '#10b981' },
                        { path: 'inspect-results', name: 'Inspect results', dot: '#6338f5' },
                    ],
                },
                children: [
                    tab('payouts', 'payouts'),
                    tab('inspect-results', 'payout-inspect-results'),
                    // Older links named the payouts tab after withdrawals.
                    { path: 'withdrawals', redirectTo: 'payouts', pathMatch: 'full' },
                    { path: '', redirectTo: 'payouts', pathMatch: 'full' },
                ],
            },
            { path: '', redirectTo: 'payments', pathMatch: 'full' },
        ]),
    ],
    exports: [RouterModule],
})
export class HistoricalDataRoutingModule {}
