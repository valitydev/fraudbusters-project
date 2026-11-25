import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { PayoutsService } from '../../../../api/payouts';
import {
    GroupEditorComponent,
    GroupEditorConfig,
} from '../../../../shared/components/group-editor/group-editor.component';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    standalone: true,
    imports: [GroupEditorComponent],
    template: '<fb-group-editor [config]="config" (saved)="onSaved($event)"></fb-group-editor>',
})
export class PayoutGroupFormComponent {
    readonly config: GroupEditorConfig = {
        backUrl: '/payouts/groups',
        loadGroup: (id) => this.payouts.getGroup(id),
        loadTemplateNames: (prefix) => this.payouts.getTemplateNames(prefix),
        save: (group) => this.payouts.saveGroup(group),
    };

    constructor(
        private readonly route: ActivatedRoute,
        private readonly router: Router,
        private readonly payouts: PayoutsService,
        private readonly feedback: PayoutsFeedbackService
    ) {}

    onSaved({ id }: { id: string }): void {
        this.feedback.done(`Group ${id} saved`);
        void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') || this.config.backUrl);
    }
}
