import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable } from 'rxjs';
import { shareReplay, switchMap } from 'rxjs/operators';

import { CheckedDataSet } from '../../../api/fb-management/swagger-codegen/model/checkedDataSet';
import { DataSetService } from '../../../api/payments/data-set';
import { TestingDataSetService } from '../../../shared/components/testing-data-set-list/services/data-set/testing-data-set.service';

@Component({
    selector: 'fb-testing-data-set',
    templateUrl: './testing-data-set.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [TestingDataSetService],
})
export class TestingDataSetComponent {
    readonly dataSet$: Observable<CheckedDataSet>;
    private readonly refresh = new BehaviorSubject<void>(undefined);

    constructor(private route: ActivatedRoute, private router: Router, private dataSetService: DataSetService) {
        this.dataSet$ = combineLatest([this.route.paramMap, this.refresh]).pipe(
            switchMap(([params]) => this.dataSetService.getCheckedById(params.get('id'))),
            shareReplay({ bufferSize: 1, refCount: true })
        );
    }

    updateSet(): void {
        this.refresh.next();
    }

    back(): void {
        void this.router.navigate(['/testing/data-sets']);
    }
}
