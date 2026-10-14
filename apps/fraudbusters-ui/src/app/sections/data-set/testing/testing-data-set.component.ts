import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map, shareReplay, switchMap } from 'rxjs/operators';

import { DataSetService } from '../../../api/payments/data-set';
import { TestingDataSetService } from '../../../shared/components/testing-data-set-list/services/data-set/testing-data-set.service';
import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../tokens';

@Component({
    selector: 'fb-testing-data-set',
    templateUrl: './testing-data-set.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [TestingDataSetService],
})
export class TestingDataSetComponent {
    dataSet$ = this.route.params.pipe(
        map((params) => params.id),
        switchMap((id) => {
            return this.dataSetService.getCheckedById(id);
        }),
        shareReplay(1)
    );

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private dataSetService: DataSetService,
        @Inject(LAYOUT_GAP_L) public layoutGapL: string,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    updateSet(): void {
        const currentUrl = this.router.url;
        this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
            this.router.navigate([currentUrl]);
        });
    }

    back() {
        this.router.navigate([`../testing/data-sets`]);
    }
}
