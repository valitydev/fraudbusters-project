import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AuthGuard, Roles } from '../../auth';
import { HistoricalChargebacksDataComponent } from './components/chargebacks/historical-chargebacks-data.component';
import { HistoricalFraudPaymentsDataComponent } from './components/fraud-payments/historical-fraud-payments-data.component';
import { HistoricalInspectResultsDataComponent } from './components/inspect-results/historical-inspect-results-data.component';
import { HistoricalPaymentsDataComponent } from './components/payments/historical-payments-data.component';
import { HistoricalPayoutInspectResultsDataComponent } from './components/payout-inspect-results/historical-payout-inspect-results-data.component';
import { HistoricalPayoutsDataComponent } from './components/payouts/historical-payouts-data.component';
import { HistoricalRefundsDataComponent } from './components/refunds/historical-refunds-data.component';
import { HistoricalDataComponent } from './historical-data.component';

const OFFICER = { canActivate: [AuthGuard], data: { roles: [Roles.FraudOfficer] } };

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: 'payments',
                component: HistoricalDataComponent,
                canActivate: [AuthGuard],
                data: {
                    roles: [Roles.FraudOfficer],
                    title: 'Payment historical data',
                    links: [
                        { path: 'inspect-results', name: 'Inspect results', otherActiveUrlFragments: [] },
                        { path: 'payments', name: 'Payments', otherActiveUrlFragments: [] },
                        { path: 'refunds', name: 'Refunds', otherActiveUrlFragments: [] },
                        { path: 'chargebacks', name: 'Chargebacks', otherActiveUrlFragments: [] },
                        { path: 'fraud-payments', name: 'Fraud payments', otherActiveUrlFragments: [] },
                    ],
                },
                children: [
                    { path: 'inspect-results', component: HistoricalInspectResultsDataComponent, ...OFFICER },
                    { path: 'payments', component: HistoricalPaymentsDataComponent, ...OFFICER },
                    { path: 'refunds', component: HistoricalRefundsDataComponent, ...OFFICER },
                    { path: 'fraud-payments', component: HistoricalFraudPaymentsDataComponent, ...OFFICER },
                    { path: 'chargebacks', component: HistoricalChargebacksDataComponent, ...OFFICER },
                    { path: '', redirectTo: 'inspect-results', pathMatch: 'full' },
                ],
            },
            {
                path: 'payouts',
                component: HistoricalDataComponent,
                canActivate: [AuthGuard],
                data: {
                    roles: [Roles.FraudOfficer],
                    title: 'Payout historical data',
                    links: [
                        { path: 'payouts', name: 'Payouts', otherActiveUrlFragments: [] },
                        { path: 'inspect-results', name: 'Inspect results', otherActiveUrlFragments: [] },
                    ],
                },
                children: [
                    { path: 'payouts', component: HistoricalPayoutsDataComponent, ...OFFICER },
                    { path: 'inspect-results', component: HistoricalPayoutInspectResultsDataComponent, ...OFFICER },
                    { path: '', redirectTo: 'payouts', pathMatch: 'full' },
                ],
            },
            { path: '', redirectTo: 'payments', pathMatch: 'full' },
        ]),
    ],
    exports: [RouterModule],
})
export class HistoricalDataRoutingModule {}
