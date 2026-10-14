import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../tokens';

@Component({
    templateUrl: './create-group.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [CreateGroupComponent],
})
export class CreateGroupComponent {
    constructor(@Inject(LAYOUT_GAP_L) public layoutGapL: string, @Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
