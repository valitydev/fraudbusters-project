import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../../../../../tokens';

@Component({
    selector: 'fb-candidate-header',
    templateUrl: 'candidate-header.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CandidateHeaderComponent {
    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
