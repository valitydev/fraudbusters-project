import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    templateUrl: 'analytics.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class AnalyticsComponent {}
