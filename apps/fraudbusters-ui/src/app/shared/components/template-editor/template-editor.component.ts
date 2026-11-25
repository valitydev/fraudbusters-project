import { Clipboard, ClipboardModule } from '@angular/cdk/clipboard';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

import {
    PAYMENT_CATCH_OUTCOMES,
    PAYMENT_FIELDS,
    PAYMENT_FRAUDO_SECTIONS,
    PAYMENT_OUTCOMES,
} from './payment-fraudo-reference';
import { PAYOUT_EXAMPLES, PAYOUT_FIELDS, PAYOUT_PRESETS, PAYOUT_SECTIONS } from './payout-fraudo-reference';
import { searchSnippets, SnippetGroup } from './snippet-search';
import { Template } from '../../../api/fb-management/swagger-codegen/model/template';
import { SharedPipesModule } from '../../pipes';
import { FraudoEditorComponent } from '../fraudo-editor/fraudo-editor.component';

@Component({
    selector: 'fb-template-editor',
    standalone: true,
    imports: [
        CommonModule,
        ClipboardModule,
        MatIconModule,
        MatButtonModule,
        ReactiveFormsModule,
        MatProgressBarModule,
        FraudoEditorComponent,
        SharedPipesModule,
    ],
    templateUrl: './template-editor.component.html',
    styleUrls: ['./template-editor.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TemplateEditorComponent {
    @Input() form: FormGroup<{ id: FormControl<string>; template: FormControl<string> }>;
    @Input() template?: Template;
    @Input() isEditableName = true;
    @Input() payout = false;
    @Input() busy = false;
    @Input() validationMessage = '';
    @Input() checkedRule: string | null = null;
    @Output() readonly validate = new EventEmitter<void>();
    @Output() readonly save = new EventEmitter<void>();
    snippetsOpen = false;
    snippetQuery = '';
    readonly historyFlags = ['mobile', 'recurrent'];

    private readonly paymentExamples = [
        {
            label: 'Lists and conditions',
            code: 'rule: (inBlackList("card_token") OR inGreyList("email")) AND amount() > 10000 -> decline;',
        },
        {
            label: 'History with grouping',
            code: 'rule: repeat_attempts: count("card_token", 60, minutes, "party_id", "shop_id") > 3 -> 3ds;',
        },
        {
            label: 'Fallback on evaluation error',
            code: 'rule: sumSuccess("card_token", 24, hours, "currency") > 10000 -> highRisk catch: notify;',
        },
    ];

    private readonly paymentPresets = [
        { label: 'Empty', code: '' },
        { label: 'Black list check', code: 'rule: inBlackList("card_token") -> decline;' },
        { label: 'Amount limit', code: 'rule: amount() > 10000 -> decline;' },
        { label: '3DS challenge', code: 'rule: amount() >= 5000 OR inGreyList("email") -> 3ds;' },
    ];

    private readonly payoutOutcomes = PAYMENT_OUTCOMES.filter((outcome) => outcome.code !== '3ds');
    private readonly payoutCatch = PAYMENT_CATCH_OUTCOMES.filter((outcome) => outcome !== '3ds');
    private searchCache?: { key: string; results: SnippetGroup[] };

    @ViewChild(FraudoEditorComponent) private editor?: FraudoEditorComponent;

    constructor(private clipboard: Clipboard, private snackBar: MatSnackBar) {}

    @ViewChild('idInput') set idInput(input: ElementRef<HTMLInputElement> | undefined) {
        input?.nativeElement.focus();
    }

    get examples() {
        return this.payout ? PAYOUT_EXAMPLES : this.paymentExamples;
    }

    get presets() {
        return this.payout ? PAYOUT_PRESETS : this.paymentPresets;
    }

    get hasCustomRules(): boolean {
        const value = this.form.controls.template.value;
        return !!value?.trim() && !this.presets.some((preset) => preset.code === value);
    }

    get referenceSections() {
        return this.payout ? PAYOUT_SECTIONS : PAYMENT_FRAUDO_SECTIONS;
    }

    get fields() {
        return this.payout ? PAYOUT_FIELDS : PAYMENT_FIELDS;
    }

    get outcomes() {
        return this.payout ? this.payoutOutcomes : PAYMENT_OUTCOMES;
    }

    get catchOutcomes() {
        return this.payout ? this.payoutCatch : PAYMENT_CATCH_OUTCOMES;
    }

    /** Snippets of every section matching the search query, for the current template type. */
    get snippetResults(): SnippetGroup[] {
        const key = `${this.payout}|${this.snippetQuery}`;
        if (this.searchCache?.key !== key) {
            this.searchCache = { key, results: searchSnippets(this.snippetCatalog(), this.snippetQuery) };
        }
        return this.searchCache.results;
    }

    get snippetResultCount(): number {
        return this.snippetResults.reduce((count, group) => count + group.items.length, 0);
    }

    /** Escape clears the search first; a second Escape closes the panel. */
    clearSnippetSearch(event: Event): void {
        if (this.snippetQuery) {
            event.stopPropagation();
            this.snippetQuery = '';
        }
    }

    copy(value: string, label: string): void {
        const copied = this.clipboard.copy(value);
        this.snackBar.open(copied ? label + ' copied' : 'Could not copy ' + label.toLowerCase(), 'OK', {
            duration: 2500,
        });
    }

    formatRules(): void {
        const control = this.form.controls.template;
        // Keep strings and line comments intact while formatting rule separators.
        const formatted = control.value
            .trim()
            .replace(/"(?:[^"\r\n]|"")*"|#[^\r\n]*|;[ \t]*(?=rule:)|[ \t]*->[ \t]*/g, (token) =>
                token.startsWith(';') ? ';\n' : token.trim() === '->' ? ' -> ' : token
            );
        if (formatted !== control.value) {
            this.editor?.replace(formatted);
        }
    }

    validateFromKeyboard(event: Event, busy: boolean | null): void {
        event.preventDefault();
        if (!busy && this.form.controls.template.value?.trim()) {
            this.validate.emit();
        }
    }

    private snippetCatalog(): SnippetGroup[] {
        const quoted = (field: string) => `"${field}"`;
        return [
            ...this.referenceSections.map((section) => ({
                title: section.title,
                items: section.snippets.map(({ code, description }) => ({ label: code, insert: code, description })),
            })),
            {
                title: 'Transaction fields',
                items: [
                    ...this.fields.map((field) => ({ label: field, insert: quoted(field), description: 'Field' })),
                    ...(this.payout ? [] : this.historyFlags).map((field) => ({
                        label: field,
                        insert: quoted(field),
                        description: 'Payment history flag',
                    })),
                ],
            },
            {
                title: 'Outcomes & error fallback',
                items: [
                    ...this.outcomes.map(({ code, description }) => ({ label: code, insert: code, description })),
                    ...this.catchOutcomes.map((code) => ({
                        label: `catch: ${code}`,
                        insert: code,
                        description: 'Supported after catch:',
                    })),
                ],
            },
            {
                title: 'Complete rule examples',
                items: this.examples.map(({ label, code }) => ({ label: code, insert: code, description: label })),
            },
        ];
    }
}
