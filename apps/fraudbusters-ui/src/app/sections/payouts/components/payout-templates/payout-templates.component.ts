import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { Template } from '../../../../api/fb-management/swagger-codegen/model/template';
import { PayoutsService } from '../../../../api/payouts';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-templates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutTemplatesComponent {
    readonly columns = ['id', 'template', 'actions'];

    search$ = new BehaviorSubject<string>('');
    templates$ = this.search$.pipe(
        switchMap((searchValue) => this.payoutsService.findTemplates({ searchValue, size: 100 })),
        map((response) => response.result)
    );

    editing = false;
    errors: string[] = [];
    form = this.fb.group({
        id: ['', Validators.required],
        template: ['', Validators.required],
    });

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder,
        private cdr: ChangeDetectorRef
    ) {}

    search(searchValue: string) {
        this.search$.next(searchValue.trim());
    }

    create() {
        this.open({ id: '', template: '' }, false);
    }

    edit(template: Template) {
        this.open(template, true);
    }

    cancel() {
        this.editing = false;
        this.errors = [];
    }

    validate() {
        this.payoutsService.validateTemplate(this.value()).subscribe({
            next: (response) => {
                this.errors = response.validateResults.flatMap((result) => result.errors ?? []);
                if (this.errors.length === 0) {
                    this.feedback.done('Template is valid for payouts');
                }
                this.cdr.markForCheck();
            },
            error: (error: HttpErrorResponse) => this.feedback.failed(error),
        });
    }

    save() {
        this.payoutsService.saveTemplate(this.value()).subscribe({
            next: (response) => {
                this.errors = response.errors ?? [];
                if (this.errors.length === 0) {
                    this.feedback.done(`Template ${response.id} saved`);
                    this.editing = false;
                    this.search$.next(this.search$.value);
                }
                this.cdr.markForCheck();
            },
            error: (error: HttpErrorResponse) => this.feedback.failed(error),
        });
    }

    remove(template: Template) {
        this.feedback
            .confirm(`Remove payout template ${template.id}?`)
            .pipe(switchMap(() => this.payoutsService.removeTemplate(template.id)))
            .subscribe({
                next: () => {
                    this.feedback.done(`Template ${template.id} removed`);
                    this.search$.next(this.search$.value);
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    private open(template: Template, existing: boolean) {
        this.form.reset({ id: template.id, template: template.template });
        if (existing) {
            this.form.controls.id.disable();
        } else {
            this.form.controls.id.enable();
        }
        this.errors = [];
        this.editing = true;
    }

    private value(): Template {
        const { id, template } = this.form.getRawValue();
        return { id: id.trim(), template };
    }
}
