import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { Router } from '@angular/router';

import { FetchCandidatesService } from './services/fetch-candidates.service';
import { ConfigService } from '../../../../config';
import { SortOrder } from '../../../../shared/constants/sort-order';
import { LAYOUT_GAP_M } from '../../../../tokens';

@Component({
    templateUrl: './candidates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchCandidatesService],
})
export class CandidatesComponent {
    candidates$ = this.fetchCandidatesService.searchResult$;
    inProgress$ = this.fetchCandidatesService.inProgress$;
    hasMore$ = this.fetchCandidatesService.hasMore$;

    private size = this.configService.pageSize;

    constructor(
        private router: Router,
        private fetchCandidatesService: FetchCandidatesService,
        private configService: ConfigService,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    search(searchValue: string) {
        this.fetchCandidatesService.search({ sortOrder: SortOrder.Desc, searchValue, size: this.size });
    }

    goToApproveCandidates(id: string) {
        this.router.navigate([`candidates/${id}`]);
    }

    fetchMore(event) {
        this.fetchCandidatesService.fetchMore(this.initParams(event));
    }

    private initParams(event) {
        return {
            searchValue: event.searchValue,
            sortOrder: SortOrder.Desc,
            size: this.size,
        };
    }
}
