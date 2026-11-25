import {
    ChangeDetectionStrategy,
    Component,
    DestroyRef,
    EventEmitter,
    signal,
    Input,
    OnInit,
    Output,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar } from '@angular/material/snack-bar';

import { CheckedDataSet } from '../../../../../api/fb-management/swagger-codegen/model/checkedDataSet';
import { checkValidateResponse } from '../../../../services/utils/check-validation-response';
import { ErrorHandlerService } from '../../../../services/utils/error-handler.service';
import { TestingDataSetService } from '../../services/data-set/testing-data-set.service';

@Component({
    selector: 'fb-data-set-template',
    templateUrl: './data-set-template.component.html',
    styleUrls: ['data-set-template.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [TestingDataSetService],
})
export class DataSetTemplateComponent implements OnInit {
    @Input() dataSet: CheckedDataSet;

    readonly validationMessage = signal('');
    checkedRule: string | null = null;

    form = this.testingDataSetService.form;

    @Output()
    readonly valueChanges = new EventEmitter<string>();

    tested$ = this.testingDataSetService.tested$;

    validated$ = this.testingDataSetService.validated$;
    inProgress$ = this.testingDataSetService.inProgress$;

    constructor(
        private testingDataSetService: TestingDataSetService,
        private errorHandlerService: ErrorHandlerService,
        private snackBar: MatSnackBar,
        private destroyRef: DestroyRef
    ) {}

    get timestamp(): Date | null {
        const value = this.form.get('checkingTimestamp')?.value as Date | string | null;
        const date = value ? new Date(value) : null;
        return date && !Number.isNaN(date.getTime()) ? date : null;
    }

    ngOnInit() {
        if (this.dataSet) {
            this.form.setValue({
                template: this.dataSet.template ?? '',
                party: this.dataSet.merchantInfo?.partyId ?? '',
                shop: this.dataSet.merchantInfo?.shopId ?? '',
                checkingTimestamp: this.dataSet.checkingTimestamp,
            });
        }
        this.testingDataSetService.errors$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((error) => this.errorHandlerService.handleError(error, this.snackBar));
        this.tested$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((checkedSetId) => {
            this.valueChanges.emit(checkedSetId);
            this.snackBar.open('Test completed', 'OK', { duration: 3000 });
        });
        this.validated$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((response) => {
            this.validationMessage.set(
                response.validateResults?.flatMap((result) => result.errors ?? []).join('\n') ?? ''
            );
            this.snackBar.open(checkValidateResponse(response), 'OK', { duration: 3000 });
        });
    }

    testTemplate() {
        this.testingDataSetService.testTemplate(
            this.form.get('template').value,
            this.dataSet,
            this.form.get('checkingTimestamp') ? (this.form.get('checkingTimestamp').value as Date) : new Date(),
            this.form.get('party').value,
            this.form.get('shop').value
        );
    }

    validateTemplate() {
        const source = this.form.controls.template.value;
        this.validationMessage.set('');
        this.checkedRule = source;
        this.testingDataSetService.validateTemplate(source);
    }
}
