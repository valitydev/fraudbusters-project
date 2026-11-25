import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { EMPTY, merge, Subject } from 'rxjs';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';

import { PaymentTemplatesService } from '../../../../../api';
import { progress } from '../../../../operators';
import { CreateTemplateData } from '../../types/create-template-data';

@Injectable()
export class TemplateService {
    static defaultParams = {
        id: '',
        template: '',
    };

    private save$ = new Subject<CreateTemplateData>();
    private validate$ = new Subject<CreateTemplateData>();

    // A failed request is reported here so the save and validate streams stay alive for the next attempt.
    readonly errors$ = new Subject<HttpErrorResponse>();

    saved$ = this.save$.pipe(
        switchMap(({ template }) => this.paymentTemplatesService.saveTemplate(template).pipe(this.reportError())),
        shareReplay(1)
    );

    validated$ = this.validate$.pipe(
        switchMap(({ template }) => this.paymentTemplatesService.validateTemplate(template).pipe(this.reportError())),
        shareReplay(1)
    );

    inProgress$ = progress(merge(this.save$, this.validate$), merge(this.saved$, this.validated$, this.errors$));

    constructor(private fb: FormBuilder, private paymentTemplatesService: PaymentTemplatesService) {}

    form = this.fb.group(TemplateService.defaultParams);

    saveTemplate(data: CreateTemplateData) {
        this.save$.next(data);
    }

    validateTemplate(data: CreateTemplateData) {
        this.validate$.next(data);
    }

    private reportError<T>() {
        return catchError<T, typeof EMPTY>((error: HttpErrorResponse) => {
            this.errors$.next(error);
            return EMPTY;
        });
    }
}
