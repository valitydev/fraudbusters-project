import { Component, Input } from '@angular/core';
import { Observable } from 'rxjs';

@Component({
    selector: 'fb-info-card',
    templateUrl: 'fb-info-card.component.html',
    styleUrls: ['fb-info-card.component.scss'],
})
export class FbInfoCardComponent {
    @Input() headerText: string;
    @Input() value = 0.0;
    @Input() units = '';
    @Input() type: string;
    @Input() inProgress: Observable<boolean>;
    @Input() formatNumber = false;

    numberFormat(num) {
        return num === 'NaN' || !num ? 0.0 : num;
    }
}
