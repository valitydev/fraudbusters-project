import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AnalyticsComponent } from './analytics.component';
import { AuthGuard, Roles } from '../../auth';
import { BaseAnalyticsComponent } from './components/base/base-analytics.component';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: '',
                component: AnalyticsComponent,
                canActivate: [AuthGuard],
                data: { roles: [Roles.FraudOfficer] },
                children: [
                    {
                        path: 'base',
                        component: BaseAnalyticsComponent,
                        canActivate: [AuthGuard],
                        data: { roles: [Roles.FraudOfficer] },
                    },
                    {
                        path: '',
                        redirectTo: 'base',
                        pathMatch: 'full',
                    },
                ],
            },
        ]),
    ],
    exports: [RouterModule],
})
export class AnalyticsRoutingModule {}
