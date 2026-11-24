import { ChangeDetectionStrategy, Component } from '@angular/core';

import { BaseAnalyticsService } from './services/base-analytics.service';

@Component({
    templateUrl: 'base-analytics.component.html',
    styleUrls: ['./base-analytics.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BaseAnalyticsComponent {
    readonly colors = ['#a49bb7', '#26866c', '#586eaa', '#be4658'];
    readonly state$ = this.analytics.state$;

    constructor(private analytics: BaseAnalyticsService) {}

    retry(): void {
        this.analytics.retry();
    }
}
