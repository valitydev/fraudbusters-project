import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { TEST_RESULTS_KIND, testResultRow } from './test-result-details';
import { CheckedDataSet } from '../../../api/fb-management/swagger-codegen/model/checkedDataSet';
import { CheckedDataSetRow } from '../../../api/fb-management/swagger-codegen/model/checkedDataSetRow';
import { HistoricalRow } from '../historical-table/historical-row';

/** Results of a data set test, in the table of Historical data: a click anywhere on a row opens it. */
@Component({
    selector: 'fb-testing-data-data-set-list',
    styleUrls: ['testing-data-set-list.component.scss'],
    templateUrl: 'testing-data-set-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class TestingDataSetListComponent {
    readonly kind = TEST_RESULTS_KIND;
    rows: HistoricalRow<CheckedDataSetRow>[] = [];

    @Input() set dataSet(dataSet: CheckedDataSet | null | undefined) {
        this.rows = (dataSet?.rows ?? []).map(testResultRow);
    }
}
