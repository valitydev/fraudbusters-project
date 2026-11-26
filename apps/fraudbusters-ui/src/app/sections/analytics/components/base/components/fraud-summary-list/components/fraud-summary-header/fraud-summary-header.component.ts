import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'fb-fraud-summary-header',
    templateUrl: 'fraud-summary-header.component.html',
    styleUrls: ['fraud-summary-header.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class FraudSummaryHeaderComponent {}
