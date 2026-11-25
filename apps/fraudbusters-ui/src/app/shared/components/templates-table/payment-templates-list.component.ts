import { Clipboard } from '@angular/cdk/clipboard';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { Template } from '../../../api/fb-management/swagger-codegen/model/template';

@Component({
    selector: 'fb-payment-templates-list',
    templateUrl: 'payment-templates-list.component.html',
    styleUrls: ['payment-templates-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentTemplatesListComponent {
    @Input() label = 'Payment templates';
    rows: Template[] = [];
    showAuthor = false;

    @Output()
    editItem = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();

    @Output()
    copyItem = new EventEmitter<string>();

    expandedId?: string;

    constructor(private clipboard: Clipboard, private snackBar: MatSnackBar) {}

    @Input()
    set templates(templates: Template[]) {
        this.rows = templates;
        this.showAuthor = templates.some((template) => Boolean(template.modifiedByUser?.trim()));
        if (!templates.some((template) => template.id === this.expandedId)) {
            this.expandedId = undefined;
        }
    }

    copyRule(source: string): void {
        const copied = this.clipboard.copy(source);
        this.snackBar.open(copied ? 'Rule copied' : 'Could not copy rule', 'OK', { duration: 2500 });
    }

    byId(_index: number, template: Template): string {
        return template.id;
    }

    toggle(id: string): void {
        this.expandedId = this.expandedId === id ? undefined : id;
    }
}
