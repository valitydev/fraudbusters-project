import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { WbListRecord } from '../../../../../../../../api/fb-management/swagger-codegen/model/wbListRecord';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../../../../../tokens';

@Component({
    selector: 'fb-candidates-batches-table-item',
    templateUrl: 'candidates-batches-table-item.component.html',
    styleUrls: ['candidates-batches-table-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CandidatesBatchesTableItemComponent {
    @Input()
    candidate: WbListRecord;

    @Output()
    approve: EventEmitter<WbListRecord> = new EventEmitter<WbListRecord>();

    @Input()
    enabled = false;

    constructor(@Inject(LAYOUT_GAP_S) public layoutGapS: string, @Inject(LAYOUT_GAP_M) public layoutGapM: string) {}

    emmitChange() {
        this.enabled = !this.enabled;
        this.approve.emit(this.candidate);
    }
}
