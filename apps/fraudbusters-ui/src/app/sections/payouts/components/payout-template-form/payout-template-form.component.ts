import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { Template } from '../../../../api/fb-management/swagger-codegen/model/template';
import { PayoutsService } from '../../../../api/payouts';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

/** Creates, edits or copies a payout template, as the payment template page does. */
@Component({
    templateUrl: 'payout-template-form.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
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

    template?: Template;
    loading = false;
    loadFailed = false;

    get validationMessage(): string {
        return this.errors.length ? this.errors.join('\n') : 'No errors';
    }

    checkedRule: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        this.editing = !!id && this.route.snapshot.data.mode === 'edit';
        this.title = this.editing ? 'Edit template' : 'New template';
        if (!id) {
            return;
        }
        this.loading = true;
        this.payoutsService
            .findTemplates({ searchValue: id, size: 100 })
            .pipe(map((response) => response.result.find((template) => template.id === id)))
            .subscribe({
                next: (template) => {
                    this.loading = false;
                    this.loadFailed = !template;
                    if (template) {
                        this.template = template;
                        this.form.setValue({ id: this.editing ? template.id : '', template: template.template });
                        if (this.editing) {
                            this.form.controls.id.disable();
                        }
                    }
                    this.cdr.markForCheck();
                },
                error: (error: HttpErrorResponse) => {
                    this.loading = false;
                    this.loadFailed = true;
                    this.feedback.failed(error);
                    this.cdr.markForCheck();
                },
            });
    }

    back() {
        void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') ?? '/payouts/templates');
    }

    validateTemplate() {
        this.inProgress$.next(true);
        const rule = this.form.controls.template.value;
        this.checkedRule = null;
        this.payoutsService
            .validateTemplate(this.value())
            .pipe(finalize(() => this.inProgress$.next(false)))
            .subscribe({
                next: (response) => {
                    this.errors = response.validateResults.flatMap((result) => result.errors ?? []);
                    this.checkedRule = rule;
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
                        this.checkedRule = this.form.controls.template.value;
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
