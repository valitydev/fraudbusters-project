import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    templateUrl: 'analytics.component.html',
    styles: ['.analytics-title { margin-bottom: 16px; }'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnalyticsComponent {}
