import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map, shareReplay, switchMap } from 'rxjs/operators';

import { PaymentGroupsService } from '../../../api/payments/groups';
import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../tokens';

@Component({
    templateUrl: './edit-group.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditGroupComponent {
    group$ = this.route.params.pipe(
        map((params) => params.id),
        switchMap((id) => {
            return this.paymentGroupsService.filter(id);
        }),
        map((response) => response.result),
        map((res) => res[0]),
        shareReplay(1)
    );

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private paymentGroupsService: PaymentGroupsService,
        @Inject(LAYOUT_GAP_L) public layoutGapL: string,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}
}
