import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
    // Attribute selector keeps the native table row/head element for table semantics.
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'tr[fb-template-header]',
    templateUrl: 'template-header.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class TemplateHeaderComponent {
    @Input() showAuthor = false;
}
