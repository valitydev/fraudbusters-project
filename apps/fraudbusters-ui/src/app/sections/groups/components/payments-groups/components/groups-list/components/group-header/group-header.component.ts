import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../../../../../tokens';

@Component({
    selector: 'fb-group-header',
    templateUrl: 'group-header.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupHeaderComponent {
    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
