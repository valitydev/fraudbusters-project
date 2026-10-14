import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { Chargeback } from '../../../../../../../api/fb-management/swagger-codegen/model/chargeback';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../../../../tokens';

@Component({
    selector: 'fb-historical-data-chargeback-item',
    templateUrl: 'historical-data-chargeback-item.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataChargebackItemComponent {
    @Input()
    chargeback: Chargeback;

    @Output()
    changed: EventEmitter<Chargeback> = new EventEmitter<Chargeback>();

    @Input()
    enabled = false;

    constructor(@Inject(LAYOUT_GAP_S) public layoutGapS: string, @Inject(LAYOUT_GAP_M) public layoutGapM: string) {}

    emmitChange() {
        this.enabled = !this.enabled;
        this.changed.emit(this.chargeback);
    }
}
