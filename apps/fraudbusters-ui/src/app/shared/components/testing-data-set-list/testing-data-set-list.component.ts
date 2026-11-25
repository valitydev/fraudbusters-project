import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { CheckedDataSet } from '../../../api/fb-management/swagger-codegen/model/checkedDataSet';

@Component({
    selector: 'fb-testing-data-data-set-list',
    styleUrls: ['testing-data-set-list.component.scss'],
    templateUrl: 'testing-data-set-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestingDataSetListComponent {
    @Input()
    dataSet: CheckedDataSet;

    expanded: number | null = null;
}
