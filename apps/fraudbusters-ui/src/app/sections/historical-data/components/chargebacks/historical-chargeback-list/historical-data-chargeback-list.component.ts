import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { Chargeback } from '../../../../../api/fb-management/swagger-codegen/model/chargeback';
import { LAYOUT_GAP_M } from '../../../../../tokens';

@Component({
    selector: 'fb-historical-data-chargeback-list',
    templateUrl: 'historical-data-chargeback-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalDataChargebackListComponent {
    @Input()
    chargebacks: Chargeback[];

    @Input()
    selectedChargebacks: Chargeback[];

    @Output()
    changed: EventEmitter<Chargeback[]> = new EventEmitter<Chargeback[]>();

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}

    onChange($event) {
        if (this.selectedChargebacks.includes($event)) {
            this.selectedChargebacks.splice(
                this.selectedChargebacks.findIndex((value) => Object.is(value, $event)),
                1
            );
        } else {
            this.selectedChargebacks.push($event);
        }
        this.changed.emit(this.selectedChargebacks);
    }

    onChangeAll($event) {
        if ($event.checked) {
            this.selectedChargebacks = Array.from(this.chargebacks);
        } else {
            this.selectedChargebacks = new Array<Chargeback>();
        }
        this.changed.emit(this.selectedChargebacks);
    }
}
