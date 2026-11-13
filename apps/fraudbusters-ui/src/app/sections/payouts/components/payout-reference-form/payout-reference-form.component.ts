import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { PayoutReference } from '../../../../api/fb-management/swagger-codegen/model/payoutReference';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

interface ReferenceRow {
    templateId: string;
    partyId: string;
    walletId: string;
    isGlobal: boolean;
}

/** Creates payout references row by row, as the payment reference page does. */
@Component({
    templateUrl: 'payout-reference-form.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutReferenceFormComponent {
    inProgress$ = new BehaviorSubject<boolean>(false);
    templateNames$ = this.payoutsService.getTemplateNames('');
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
        let references: PayoutReference[];
        try {
            references = (this.forms.getRawValue() as ReferenceRow[]).map((row) => this.toReference(row));
        } catch (e) {
            this.feedback.failed((e as Error).message);
            return;
        }
        this.inProgress$.next(true);
        this.payoutsService
            .saveReferences(references)
            .pipe(finalize(() => this.inProgress$.next(false)))
            .subscribe({
                next: () => {
                    this.feedback.done('References created');
                    void this.router.navigateByUrl(
                        this.route.snapshot.queryParamMap.get('returnUrl') ?? '/payouts/templates/references'
                    );
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    private toReference(row: ReferenceRow): PayoutReference {
        const templateId = row.templateId.trim();
        if (row.isGlobal) {
            return { templateId, isGlobal: true };
        }
        const scope = toPayoutScope(row.partyId, row.walletId);
        if (!scope.partyId) {
            throw new Error('Set a party or make the reference global');
        }
        return { templateId, isGlobal: false, ...scope };
    }

    private createItem(): FormGroup {
        return this.fb.group({
            templateId: ['', Validators.required],
            partyId: [''],
            walletId: [''],
            isGlobal: [false],
        });
    }
}
