import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { Group } from '../../../../api/fb-management/swagger-codegen/model/group';
import { PayoutsService } from '../../../../api/payouts';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-groups.component.html',
    styleUrls: ['../../payouts-section.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutGroupsComponent {
    readonly columns = ['groupId', 'templates', 'actions'];

    search$ = new BehaviorSubject<string>('');
    groups$ = this.search$.pipe(
        switchMap((searchValue) => this.payoutsService.findGroups(searchValue)),
        map((response) => response.result)
    );

    editing = false;
    form = this.fb.group({
        groupId: ['', Validators.required],
        templates: this.fb.array<FormGroup>([]),
    });

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder,
        private cdr: ChangeDetectorRef
    ) {}

    get templates(): FormArray<FormGroup> {
        return this.form.controls.templates;
    }

    search(searchValue: string) {
        this.search$.next(searchValue.trim());
    }

    templatesOf(group: Group): string {
        return (group.priorityTemplates ?? []).map((template) => `${template.priority}: ${template.id}`).join(', ');
    }

    create() {
        this.open({ groupId: '', priorityTemplates: [{ id: '', priority: 1 }] }, false);
    }

    edit(group: Group) {
        this.open(group, true);
    }

    addTemplate() {
        this.templates.push(this.templateRow('', this.templates.length + 1));
    }

    removeTemplate(index: number) {
        this.templates.removeAt(index);
    }

    cancel() {
        this.editing = false;
    }

    save() {
        const { groupId, templates } = this.form.getRawValue();
        const group: Group = {
            groupId: groupId.trim(),
            priorityTemplates: templates.map((template: { id: string; priority: number }) => ({
                id: template.id.trim(),
                priority: Number(template.priority),
            })),
        };
        this.payoutsService.saveGroup(group).subscribe({
            next: () => {
                this.feedback.done(`Group ${group.groupId} saved`);
                this.editing = false;
                this.search$.next(this.search$.value);
                this.cdr.markForCheck();
            },
            error: (error: HttpErrorResponse) => this.feedback.failed(error),
        });
    }

    remove(group: Group) {
        this.feedback
            .confirm(`Remove payout group ${group.groupId}?`)
            .pipe(switchMap(() => this.payoutsService.removeGroup(group.groupId)))
            .subscribe({
                next: () => {
                    this.feedback.done(`Group ${group.groupId} removed`);
                    this.search$.next(this.search$.value);
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    private open(group: Group, existing: boolean) {
        this.form.controls.groupId.setValue(group.groupId);
        if (existing) {
            this.form.controls.groupId.disable();
        } else {
            this.form.controls.groupId.enable();
        }
        this.templates.clear();
        (group.priorityTemplates ?? []).forEach((template) =>
            this.templates.push(this.templateRow(template.id, template.priority))
        );
        this.editing = true;
    }

    private templateRow(id: string, priority: number): FormGroup {
        return this.fb.group({
            id: [id, Validators.required],
            priority: [priority, [Validators.required, Validators.min(0)]],
        });
    }
}
