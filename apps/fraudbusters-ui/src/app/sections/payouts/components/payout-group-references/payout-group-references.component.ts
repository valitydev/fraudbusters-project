import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { PayoutGroupReference } from '../../../../api/fb-management/swagger-codegen/model/payoutGroupReference';
import { PayoutsService } from '../../../../api/payouts';
import { describePayoutScope, toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-group-references.component.html',
    styleUrls: ['../../payouts-section.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutGroupReferencesComponent {
    readonly columns = ['scope', 'groupId', 'actions'];

    search$ = new BehaviorSubject<string>('');
    references$ = this.search$.pipe(
        switchMap((searchValue) => this.payoutsService.findGroupReferences({ searchValue, size: 100 })),
        map((response) => response.result)
    );
    groups$ = this.payoutsService.findGroups().pipe(map((response) => response.result.map((group) => group.groupId)));

    form = this.fb.group({
        groupId: ['', Validators.required],
        partyId: [''],
        walletId: [''],
    });

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
        const { groupId, partyId, walletId } = this.form.getRawValue();
        let scope: { partyId?: string; walletId?: string };
        try {
            scope = toPayoutScope(partyId, walletId);
        } catch (e) {
            this.feedback.failed((e as Error).message);
            return;
        }
        this.payoutsService.saveGroupReferences(groupId, [{ groupId, ...scope }]).subscribe({
            next: () => {
                this.feedback.done('Group reference saved');
                this.form.reset({ groupId: '', partyId: '', walletId: '' });
                this.search$.next(this.search$.value);
            },
            error: (error: HttpErrorResponse) => this.feedback.failed(error),
        });
    }

    remove(reference: PayoutGroupReference) {
        this.feedback
            .confirm(`Remove the reference of ${describePayoutScope(reference)} to group ${reference.groupId}?`)
            .pipe(switchMap(() => this.payoutsService.removeGroupReference(reference)))
            .subscribe({
                next: () => {
                    this.feedback.done('Group reference removed');
                    this.search$.next(this.search$.value);
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
