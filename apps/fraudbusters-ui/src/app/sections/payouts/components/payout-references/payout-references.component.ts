import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { debounceTime, map, startWith, switchMap } from 'rxjs/operators';

import { PayoutReference } from '../../../../api/fb-management/swagger-codegen/model/payoutReference';
import { PayoutsService } from '../../../../api/payouts';
import { describePayoutScope, toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutReferencesComponent {
    readonly columns = ['scope', 'templateId', 'actions'];

    search$ = new BehaviorSubject<string>('');
    references$ = this.search$.pipe(
        switchMap((searchValue) => this.payoutsService.findReferences({ searchValue, size: 100 })),
        map((response) => response.result)
    );

    form = this.fb.group({
        isGlobal: [false],
        partyId: [''],
        walletId: [''],
        templateId: ['', Validators.required],
    });
    templateNames$ = this.form.controls.templateId.valueChanges.pipe(
        startWith(''),
        debounceTime(300),
        switchMap((prefix) => this.payoutsService.getTemplateNames(prefix ?? ''))
    );

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder
    ) {}

    describeScope(scope: { partyId?: string; walletId?: string; isGlobal?: boolean }): string {
        return describePayoutScope(scope);
    }

    search(searchValue: string) {
        this.search$.next(searchValue.trim());
    }

    save() {
        const { isGlobal, partyId, walletId, templateId } = this.form.getRawValue();
        let reference: PayoutReference;
        try {
            reference = isGlobal
                ? { templateId: templateId.trim(), isGlobal: true }
                : { templateId: templateId.trim(), isGlobal: false, ...toPayoutScope(partyId, walletId) };
        } catch (e) {
            this.feedback.failed((e as Error).message);
            return;
        }
        if (!reference.isGlobal && !reference.partyId) {
            this.feedback.failed('Set a party or make the reference global');
            return;
        }
        this.payoutsService.saveReferences([reference]).subscribe({
            next: () => {
                this.feedback.done('Reference saved');
                this.form.reset({ isGlobal: false, partyId: '', walletId: '', templateId: '' });
                this.search$.next(this.search$.value);
            },
            error: (error: HttpErrorResponse) => this.feedback.failed(error),
        });
    }

    remove(reference: PayoutReference) {
        this.feedback
            .confirm(`Remove the reference of ${describePayoutScope(reference)} to ${reference.templateId}?`)
            .pipe(switchMap(() => this.payoutsService.removeReference(reference)))
            .subscribe({
                next: () => {
                    this.feedback.done('Reference removed');
                    this.search$.next(this.search$.value);
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
