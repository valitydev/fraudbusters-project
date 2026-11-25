import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { EMPTY, merge, Subject } from 'rxjs';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';

import { PaymentTemplatesService } from '../../../../../api';
import { ApplyRuleOnHistoricalDataSetRequest } from '../../../../../api/fb-management/swagger-codegen/model/applyRuleOnHistoricalDataSetRequest';
import { CheckedDataSet } from '../../../../../api/fb-management/swagger-codegen/model/checkedDataSet';
import { DataSetService } from '../../../../../api/payments/data-set';
import { progress } from '../../../../operators';

@Injectable()
export class TestingDataSetService {
    static defaultParams = {
        party: '',
        shop: '',
        template: '',
        checkingTimestamp: null,
    };

    form = this.fb.group(TestingDataSetService.defaultParams);
    test$ = new Subject<ApplyRuleOnHistoricalDataSetRequest>();
    validate$ = new Subject<string>();
    // A failed request is reported here so the test and validate streams stay alive for the next attempt.
    readonly errors$ = new Subject<HttpErrorResponse>();
    tested$ = this.test$.pipe(
        switchMap((value) => this.dataSetService.applyRuleOnHistoricalDataSet(value).pipe(this.reportError())),
        shareReplay(1)
    );

    validated$ = this.validate$.pipe(
        switchMap((value) =>
            this.paymentTemplatesService
                .validateTemplate({ id: 'validate_checking_template', template: value })
                .pipe(this.reportError())
        ),
        shareReplay(1)
    );

    inProgress$ = progress(merge(this.test$, this.validate$), merge(this.tested$, this.validated$, this.errors$));

    constructor(
        private fb: FormBuilder,
        private dataSetService: DataSetService,
        private paymentTemplatesService: PaymentTemplatesService
    ) {}

    testTemplate(
        templateValue: string,
        data: CheckedDataSet,
        ruleSetTimestamp?: Date,
        partyId?: string,
        shopId?: string
    ) {
        this.test$.next({
            dataSetId: +data.testDataSetId,
            template: templateValue,
            records: (data.rows ?? []).map((value) => value.payment),
            reference: {
                partyId,
                shopId,
            },
            ruleSetTimestamp,
        });
    }

    validateTemplate(templateValue: string) {
        this.validate$.next(templateValue);
    }

    private reportError<T>() {
        return catchError<T, typeof EMPTY>((error: HttpErrorResponse) => {
            this.errors$.next(error);
            return EMPTY;
        });
    }
}
