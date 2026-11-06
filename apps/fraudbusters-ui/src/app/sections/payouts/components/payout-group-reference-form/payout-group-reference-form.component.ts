import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, forkJoin } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { PayoutGroupReference } from '../../../../api/fb-management/swagger-codegen/model/payoutGroupReference';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

/** Creates payout group references row by row; without a party the group applies to every payout. */
@Component({
    templateUrl: 'payout-group-reference-form.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutGroupReferenceFormComponent {
    inProgress$ = new BehaviorSubject<boolean>(false);
    groupIds$ = this.payoutsService.findGroups().pipe(map((response) => response.result.map((group) => group.groupId)));
    forms = new FormArray<FormGroup>([this.createItem()]);

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    addItem() {
        this.forms.push(this.createItem());
    }

    removeItem(index: number) {
        this.forms.removeAt(index);
    }

    createReference() {
        let references: PayoutGroupReference[];
        try {
            references = this.forms
                .getRawValue()
                .map((row: { groupId: string; partyId: string; walletId: string }) => ({
                    groupId: row.groupId.trim(),
                    ...toPayoutScope(row.partyId, row.walletId),
                }));
        } catch (e) {
            this.feedback.failed((e as Error).message);
            return;
        }
        this.inProgress$.next(true);
        forkJoin(references.map((reference) => this.payoutsService.saveGroupReferences(reference.groupId, [reference])))
            .pipe(finalize(() => this.inProgress$.next(false)))
            .subscribe({
                next: () => {
                    this.feedback.done('Group references created');
                    void this.router.navigateByUrl(
                        this.route.snapshot.queryParamMap.get('returnUrl') ?? '/payouts/groups/references'
                    );
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    private createItem(): FormGroup {
        return this.fb.group({
            groupId: ['', Validators.required],
            partyId: [''],
            walletId: [''],
        });
    }
}
