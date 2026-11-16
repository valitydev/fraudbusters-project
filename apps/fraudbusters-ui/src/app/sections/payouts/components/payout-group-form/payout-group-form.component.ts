import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../../tokens';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

/** Creates or edits a payout group, as the payment group page does. */
@Component({
    templateUrl: 'payout-group-form.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutGroupFormComponent implements OnInit {
    inProgress$ = new BehaviorSubject<boolean>(false);
    templateNames$ = this.payoutsService.getTemplateNames('');
    editing = false;
    form = this.fb.group({ id: ['', Validators.required] });
    forms = new FormArray<FormGroup>([this.createItem('', 1)]);

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder,
        private cdr: ChangeDetectorRef,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string,
        @Inject(LAYOUT_GAP_L) public layoutGapL: string
    ) {}

    ngOnInit() {
        const groupId = this.route.snapshot.paramMap.get('id');
        if (!groupId) {
            return;
        }
        this.editing = true;
        this.payoutsService.getGroup(groupId).subscribe({
            next: (group) => {
                this.form.setValue({ id: group.groupId });
                this.form.controls.id.disable();
                this.forms.clear();
                group.priorityTemplates.forEach((template) =>
                    this.forms.push(this.createItem(template.id, template.priority))
                );
                this.cdr.markForCheck();
            },
            error: (error: HttpErrorResponse) => this.feedback.failed(error),
        });
    }

    addItem() {
        this.forms.push(this.createItem('', this.forms.length + 1));
    }

    removeItem(index: number) {
        this.forms.removeAt(index);
    }

    save() {
        const groupId = this.form.getRawValue().id.trim();
        this.inProgress$.next(true);
        this.payoutsService
            .saveGroup({
                groupId,
                priorityTemplates: this.forms.getRawValue().map((row: { id: string; priority: number }) => ({
                    id: row.id.trim(),
                    priority: Number(row.priority),
                })),
            })
            .pipe(finalize(() => this.inProgress$.next(false)))
            .subscribe({
                next: () => {
                    this.feedback.done(`Group ${groupId} saved`);
                    void this.router.navigateByUrl(
                        this.route.snapshot.queryParamMap.get('returnUrl') ?? '/payouts/groups'
                    );
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    private createItem(id: string, priority: number): FormGroup {
        return this.fb.group({
            id: [id, Validators.required],
            priority: [priority, [Validators.required, Validators.min(0)]],
        });
    }
}
