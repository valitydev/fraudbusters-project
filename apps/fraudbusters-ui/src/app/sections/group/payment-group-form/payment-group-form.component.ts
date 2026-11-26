import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { PaymentGroupsService } from '../../../api/payments/groups';
import { PaymentTemplatesService } from '../../../api/payments/templates';
import { GroupEditorConfig } from '../../../shared/components/group-editor/group-editor.component';

@Component({
    template: '<fb-group-editor [config]="config" (saved)="onSaved($event)"></fb-group-editor>',
    standalone: false,
})
export class PaymentGroupFormComponent {
    readonly config: GroupEditorConfig = {
        backUrl: '/groups/list',
        loadGroup: (id) => this.groups.getGroupById(id),
        loadTemplateNames: (prefix) => this.templates.getTemplatesName(prefix),
        save: (group) => this.groups.save(group),
    };

    constructor(
        private readonly router: Router,
        private readonly snackBar: MatSnackBar,
        private readonly groups: PaymentGroupsService,
        private readonly templates: PaymentTemplatesService
    ) {}

    onSaved({ id, editing }: { id: string; editing: boolean }): void {
        this.snackBar.open(editing ? `Group ${id} saved` : 'Group has been created', 'OK', { duration: 3000 });
        if (!editing) {
            void this.router.navigate(['/group', id], { queryParamsHandling: 'preserve' });
        }
    }
}
