import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { Template } from '../../../../../api/fb-management/swagger-codegen/model/template';

@Component({
    // Attribute selector keeps the native table row/head element for table semantics.
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'tr[fb-template-item]',
    templateUrl: 'template-item.component.html',
    styleUrls: ['template-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class TemplateItemComponent {
    @Input()
    set template(template: Template) {
        this.row = template;
        const source = template.template?.trim() ?? '';
        // Match quoted strings and comments before semicolons or rule declarations.
        const tokens = source.matchAll(/"(?:[^"\r\n]|"")*"|#[^\r\n]*|(\brule:)|(;)/g);
        const starts: number[] = [];
        let firstEnd = source.length;
        for (const match of tokens) {
            if (match[1]) {
                starts.push(match.index);
            } else if (match[2] && starts.length === 1 && firstEnd === source.length) {
                firstEnd = match.index + 1;
            }
        }
        this.preview = source
            .slice(starts[0] ?? 0, Math.min(firstEnd, starts[1] ?? source.length))
            .replace(/\r?\n/g, ' ')
            .trim();
        this.additionalRules = Math.max(0, starts.length - 1);
    }

    get template(): Template {
        return this.row;
    }

    @Input() showAuthor = false;
    @Input() expanded = false;
    @Input() detailsId: string;

    @Output() readonly toggleDetail = new EventEmitter<void>();
    @Output() readonly editItem = new EventEmitter<string>();
    @Output() readonly deleteItem = new EventEmitter<string>();
    @Output() readonly copyItem = new EventEmitter<string>();

    preview = '';
    additionalRules = 0;

    private row: Template;
}
