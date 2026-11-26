import { ChangeDetectionStrategy, Component, signal, Input, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { TemplateService } from './services/template/template.service';
import { Template } from '../../../api/fb-management/swagger-codegen/model/template';
import { ErrorHandlerService } from '../../services/utils/error-handler.service';

@Component({
    selector: 'fb-create-template',
    templateUrl: './create-template.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [TemplateService],
    standalone: false,
})
export class CreateTemplateComponent implements OnInit {
    @Input() template: Template;
    @Input() isEditableName = true;

    form = this.templateService.form;

    saved$ = this.templateService.saved$;
    validated$ = this.templateService.validated$;
    inProgress$ = this.templateService.inProgress$;

    readonly validationMessage = signal('');
    checkedRule: string | null = null;

    constructor(
        private router: Router,
        private templateService: TemplateService,
        private errorHandlerService: ErrorHandlerService,
        private snackBar: MatSnackBar
    ) {}

    ngOnInit() {
        if (this.template) {
            this.form.setValue({ id: this.isEditableName ? '' : this.template.id, template: this.template.template });
            if (this.isEditableName === false) {
                this.form.get('id').disable();
            }
        }
        this.templateService.errors$.subscribe((error) => this.errorHandlerService.handleError(error, this.snackBar));
        this.saved$.subscribe((template) => {
            if (!this.isEditableName) {
                this.snackBar.open(`Saved success: ${template.id}`, 'OK', {
                    duration: 3000,
                });
            } else {
                this.snackBar.open(`Template has been created`, 'OK', {
                    duration: 3000,
                });
                this.navigateToEdit(template.id);
            }
        });
        this.validated$.subscribe((response) => {
            this.validationMessage.set(
                response.validateResults?.flatMap((result) => result.errors ?? []).join('\n') || 'No errors'
            );
        });
    }

    saveTemplate() {
        this.templateService.saveTemplate({
            template: { id: this.form.getRawValue().id, template: this.form.getRawValue().template },
        });
    }

    validateTemplate() {
        this.validationMessage.set('');
        this.checkedRule = this.form.controls.template.value;
        this.templateService.validateTemplate({
            template: { id: this.form.getRawValue().id, template: this.form.getRawValue().template },
        });
    }

    navigateToEdit(id) {
        this.router.navigate([`../template/${id}`]);
    }

    back() {
        this.router.navigate([`../templates`]);
    }
}
