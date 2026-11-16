import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';

export interface PayoutSectionLink {
    path: string;
    name: string;
}

/** A payout section with its own tabs, laid out like the payment section of the same name. */
@Component({
    templateUrl: 'payout-section.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutSectionComponent {
    title$ = this.route.data.pipe(map((data) => data.title as string));
    links$ = this.route.data.pipe(map((data) => data.links as PayoutSectionLink[]));

    constructor(private route: ActivatedRoute) {}
}
