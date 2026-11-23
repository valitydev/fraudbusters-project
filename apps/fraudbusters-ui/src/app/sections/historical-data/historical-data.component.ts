import { Component, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs/operators';

import { hasActiveFragments } from '../../shared/utils/has-active-fragments';
import { LAYOUT_GAP_S } from '../../tokens';

export interface HistoricalDataLink {
    path: string;
    name: string;
    otherActiveUrlFragments: string[];
}

@Component({
    templateUrl: 'historical-data.component.html',
    styleUrls: ['historical-data.component.scss'],
})
export class HistoricalDataComponent {
    title$ = this.route.data.pipe(map((data) => data.title as string));
    links$ = this.route.data.pipe(map((data) => data.links as HistoricalDataLink[]));

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        @Inject(LAYOUT_GAP_S) public layoutGapS: string
    ) {}

    hasActiveFragments(fragments: string[]): boolean {
        const ulrFragments = this.router.url.split('/');
        return hasActiveFragments(fragments, ulrFragments);
    }
}
