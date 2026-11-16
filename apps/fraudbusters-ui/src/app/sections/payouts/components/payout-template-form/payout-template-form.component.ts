import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { Template } from '../../../../api/fb-management/swagger-codegen/model/template';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../../tokens';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

/** Creates, edits or copies a payout template, as the payment template page does. */
@Component({
    templateUrl: 'payout-template-form.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutTemplateFormComponent implements OnInit {
    inProgress$ = new BehaviorSubject<boolean>(false);
    editing = false;
    title = 'New template';
    errors: string[] = [];
    form = this.fb.group({
        id: ['', Validators.required],
        template: ['', Validators.required],
    });

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
        const id = this.route.snapshot.paramMap.get('id');
        this.editing = !!id && this.route.snapshot.data.mode === 'edit';
        this.title = this.editing ? `Template ${id}` : 'New template';
        if (!id) {
            return;
        }
        this.payoutsService
            .findTemplates({ searchValue: id, size: 100 })
            .pipe(map((response) => response.result.find((template) => template.id === id)))
            .subscribe((template) => {
                if (template) {
                    this.form.setValue({ id: this.editing ? template.id : '', template: template.template });
                    if (this.editing) {
                        this.form.controls.id.disable();
                    }
                    this.cdr.markForCheck();
                }
            });
    }

    back() {
        void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') ?? '/payouts/templates');
    }

    validateTemplate() {
        this.inProgress$.next(true);
        this.payoutsService
            .validateTemplate(this.value())
            .pipe(finalize(() => this.inProgress$.next(false)))
            .subscribe({
                next: (response) => {
                    this.errors = response.validateResults.flatMap((result) => result.errors ?? []);
                    if (!this.errors.length) {
                        this.feedback.done('Template is valid');
                    }
                    this.cdr.markForCheck();
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    saveTemplate() {
        this.inProgress$.next(true);
        this.payoutsService
            .saveTemplate(this.value())
            .pipe(finalize(() => this.inProgress$.next(false)))
            .subscribe({
                next: (response) => {
                    this.errors = response.errors ?? [];
                    if (this.errors.length) {
                        this.cdr.markForCheck();
                        return;
                    }
                    this.feedback.done(`Template ${response.id} saved`);
                    this.back();
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    private value(): Template {
        const { id, template } = this.form.getRawValue();
        return { id: id.trim(), template };
    }
}
