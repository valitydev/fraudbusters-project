import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    templateUrl: 'analytics.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnalyticsComponent {}
