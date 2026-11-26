import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { DataSet } from '../../../../../api/fb-management/swagger-codegen/model/dataSet';

@Component({
    selector: 'fb-payment-data-sets-list',
    styleUrls: ['payment-data-sets-list.component.scss'],
    templateUrl: 'payment-data-sets-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class PaymentDataSetsListComponent {
    @Input()
    dataSets: DataSet[];

    @Output()
    goToDataSet = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();
}
