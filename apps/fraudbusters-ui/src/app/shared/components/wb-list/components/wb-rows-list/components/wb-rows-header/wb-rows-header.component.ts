import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../../../../tokens';

@Component({
    selector: 'fb-wb-rows-header',
    templateUrl: 'wb-rows-header.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WbRowsHeaderComponent {
    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
