import { TextFieldModule } from '@angular/cdk/text-field';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    ElementRef,
    inject,
    Input,
    ViewChild,
    forwardRef,
    DOCUMENT,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';

import {
    ArgumentFold,
    ArgumentPreviewPart,
    findArgumentFolds,
    previewArguments,
    previewLineNumbers,
} from './fraudo-argument-folds';
import { fraudoValidationLines } from './fraudo-validation-lines';
import { SharedPipesModule } from '../../pipes';
import { checkFraudo, FraudoDialect, fraudoProblemsMessage } from '../../pipes/fraudo-check';
import { FraudoToken, tokenizeFraudo } from '../../pipes/fraudo-tokens.pipe';

/**
 * Plain textarea with fraudo syntax highlighting: the highlighted copy of the text is
 * drawn under a transparent textarea, so typing, selection and undo stay native.
 */
@Component({
    selector: 'fb-fraudo-editor',
    imports: [TextFieldModule, MatButtonModule, SharedPipesModule],
    templateUrl: './fraudo-editor.component.html',
    styleUrls: ['./fraudo-editor.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => FraudoEditorComponent), multi: true }],
})
export class FraudoEditorComponent implements ControlValueAccessor, AfterViewInit {
    @Input() showLineNumbers = false;
    @Input() autoGrow = false;
    @Input() fixedHeight = false;
    @Input() placeholder = '';
    @Input() label = 'Template rules';
    @Input() minHeight = 400;

    /** Payment and payout rules accept different fields. */
    @Input() set dialect(dialect: FraudoDialect) {
        this.currentDialect = dialect ?? 'payment';
        this.setValue(this.value);
    }

    @Input() set validationMessage(message: string) {
        this.errorLines = fraudoValidationLines(message ?? '');
        if (!this.errorLines.length) {
            return;
        }
        this.folded = false;
        const line = this.errorLines[0];
        requestAnimationFrame(() => {
            const textarea = this.textarea?.nativeElement;
            if (this.errorLines[0] !== line || !textarea?.isConnected) {
                return;
            }
            const lineHeight = parseFloat(getComputedStyle(textarea).lineHeight);
            textarea.scrollTop = Math.max(0, (line - 1) * lineHeight - textarea.clientHeight / 2);
            this.syncScroll(textarea);
        });
    }

    value = '';
    disabled = false;
    cursorLine = 1;
    cursorColumn = 1;
    lineNumbers = [1];
    foldedLineNumbers = [1];
    folded = false;
    errorLines: number[] = [];
    folds: ArgumentFold[] = [];
    /** Highlighted source; tokens that break the rule carry a `problem`. */
    tokens: FraudoToken[] = [];
    problemMessage = '';
    /** Token indexes of the bracket next to the cursor and its pair. */
    activePair: number[] = [];

    previewParts: ArgumentPreviewPart[] = [];

    private currentDialect: FraudoDialect = 'payment';
    private bracketPairs = new Map<number, number>();
    /** Start offset of each token in the textarea text, where line breaks are single "\n". */
    private tokenStarts: number[] = [];

    @ViewChild('editorViewport', { static: true }) private editorViewport!: ElementRef<HTMLElement>;
    @ViewChild('textarea', { static: true }) private textarea!: ElementRef<HTMLTextAreaElement>;
    @ViewChild('foldPreview') private foldPreview?: ElementRef<HTMLPreElement>;
    @ViewChild('gutter') private gutter?: ElementRef<HTMLElement>;
    @ViewChild('highlight', { static: true }) private highlight!: ElementRef<HTMLElement>;

    private initializedSource = false;
    private readonly document = inject(DOCUMENT);

    constructor(private cdr: ChangeDetectorRef, private destroyRef: DestroyRef) {}

    ngAfterViewInit(): void {
        this.syncTextareaValue();
        if (!this.fixedHeight) {
            return;
        }
        const textarea = this.textarea.nativeElement;
        const observer = new ResizeObserver(() => this.syncScroll(textarea));
        observer.observe(textarea);
        this.destroyRef.onDestroy(() => observer.disconnect());
    }

    onTouched: () => void = () => {};

    writeValue(value: string | null): void {
        this.setValue(value ?? '');
        // Assigning textarea.value after every native input discards its undo history.
        this.syncTextareaValue();
        if (!this.initializedSource && this.value) {
            this.initializedSource = true;
            this.folded = this.folds.length > 0;
        }
        this.cdr.markForCheck();
    }

    registerOnChange(callback: (value: string) => void): void {
        this.onChange = callback;
    }

    registerOnTouched(callback: () => void): void {
        this.onTouched = callback;
    }

    setDisabledState(disabled: boolean): void {
        this.disabled = disabled;
        this.cdr.markForCheck();
    }

    update(value: string): void {
        this.setValue(value);
        this.onChange(value);
    }

    toggleArguments(): void {
        if (this.folded) {
            this.openSource();
        } else {
            this.folded = true;
            this.cdr.detectChanges();
            this.syncGutter(this.foldPreview.nativeElement);
        }
    }

    openFold(event: MouseEvent, fold: ArgumentFold): void {
        event.stopPropagation();
        const whitespace = this.value.slice(fold.from, fold.to).match(/^\s*/)[0].length;
        this.openSource(fold.from + whitespace, event.detail ? event.clientY : undefined);
    }

    editPreview(event: MouseEvent): void {
        const caret = this.previewCaret(event);
        const node = caret?.startContainer;
        const element = node instanceof Element ? node : node?.parentElement;
        const segment = element?.closest<HTMLElement>('[data-source-start]');
        if (!segment || !caret) {
            this.openSource();
            return;
        }
        const prefix = this.document.createRange();
        prefix.setStart(segment, 0);
        prefix.setEnd(caret.startContainer, caret.startOffset);
        this.openSource(Number(segment.dataset.sourceStart) + prefix.toString().length, event.clientY);
    }

    editPreviewFromKeyboard(event: Event): void {
        if (event.target === event.currentTarget) {
            event.preventDefault();
            this.openSource();
        }
    }

    insert(text: string): void {
        this.insertText(text, false);
    }

    replace(text: string): void {
        this.insertText(text, true);
    }

    useHistory(event: KeyboardEvent): void {
        if (this.disabled || event.altKey || (!event.metaKey && !event.ctrlKey)) {
            return;
        }
        const key = event.key.toLowerCase();
        if (key !== 'z' && key !== 'y') {
            return;
        }
        event.preventDefault();
        this.document.execCommand(key === 'y' || event.shiftKey ? 'redo' : 'undo');
    }

    updateCursor(): void {
        const textarea = this.textarea.nativeElement;
        const lines = textarea.value.slice(0, textarea.selectionStart).split('\n');
        this.cursorLine = lines.length;
        this.cursorColumn = lines[lines.length - 1].length + 1;
        this.activePair = this.pairAt(textarea.selectionStart, textarea.selectionEnd);
    }

    syncScroll(textarea: HTMLTextAreaElement): void {
        const highlight = this.highlight.nativeElement;
        if (this.fixedHeight && textarea.clientHeight) {
            // Match the textarea viewport, excluding its native scrollbars.
            highlight.style.width = `${textarea.clientWidth}px`;
            highlight.style.height = `${textarea.clientHeight}px`;
        }
        highlight.scrollTop = textarea.scrollTop;
        highlight.scrollLeft = textarea.scrollLeft;
        this.syncGutter(textarea);
        this.editorViewport.nativeElement.style.setProperty('--source-scroll-top', `${textarea.scrollTop}px`);
    }

    syncGutter(viewport: HTMLElement): void {
        const gutter = this.gutter?.nativeElement;
        if (!gutter || !viewport.clientHeight) {
            return;
        }
        if (this.fixedHeight) {
            gutter.style.height = `${viewport.clientHeight}px`;
        }
        gutter.scrollTop = viewport.scrollTop;
    }

    private syncTextareaValue(): void {
        const textarea = this.textarea?.nativeElement;
        const text = this.value.replace(/\r\n?/g, '\n');
        if (textarea && textarea.value !== text) {
            textarea.value = text;
        }
    }

    private insertText(text: string, replace: boolean): void {
        if (this.disabled) {
            return;
        }
        if (this.folded) {
            this.openSource();
        }
        const textarea = this.textarea.nativeElement;
        textarea.focus({ preventScroll: true });
        if (replace) {
            textarea.select();
        }
        // insertText integrates snippet insertion and formatting with the native undo stack.
        if (!this.document.execCommand('insertText', false, text)) {
            textarea.setRangeText(text, textarea.selectionStart, textarea.selectionEnd, 'end');
            this.update(textarea.value);
            this.updateCursor();
        }
        this.cdr.markForCheck();
    }

    private previewCaret(event: MouseEvent): Range | null {
        const document = this.document as Document & {
            caretPositionFromPoint?(x: number, y: number): { offsetNode: Node; offset: number } | null;
        };
        const position = document.caretPositionFromPoint?.(event.clientX, event.clientY);
        if (position) {
            const caret = document.createRange();
            caret.setStart(position.offsetNode, position.offset);
            caret.collapse(true);
            return caret;
        }
        return document.caretRangeFromPoint?.(event.clientX, event.clientY) ?? null;
    }

    private openSource(offset?: number, clickY?: number): void {
        const preview = this.foldPreview?.nativeElement;
        const viewportTop = preview?.getBoundingClientRect().top;
        const scrollLeft = preview?.scrollLeft;
        this.folded = false;
        if (this.disabled) {
            return;
        }

        this.cdr.detectChanges();
        const textarea = this.textarea.nativeElement;
        textarea.focus({ preventScroll: true });
        if (offset !== undefined) {
            const before = this.value.slice(0, offset).replace(/\r\n?/g, '\n');
            textarea.setSelectionRange(before.length, before.length);
            const style = getComputedStyle(textarea);
            const lineStart = before.lastIndexOf('\n') + 1;
            const line = before.slice(lineStart);
            const lineIndex = before.split('\n').length - 1;
            const targetY =
                clickY === undefined || viewportTop === undefined ? textarea.clientHeight / 2 : clickY - viewportTop;
            const lineHeight = parseFloat(style.lineHeight);
            textarea.scrollTop = lineIndex * lineHeight + parseFloat(style.paddingTop) + lineHeight / 2 - targetY;
            textarea.scrollLeft = scrollLeft ?? textarea.scrollLeft;
            if (this.showLineNumbers) {
                this.revealColumn(textarea, line, style);
            }
        }
        this.updateCursor();
        this.syncScroll(textarea);
    }

    private revealColumn(textarea: HTMLTextAreaElement, line: string, style: CSSStyleDeclaration): void {
        const context = this.document.createElement('canvas').getContext('2d');
        if (!context) {
            return;
        }
        context.font = `${style.fontSize} ${style.fontFamily}`;
        const tabWidth = Number(style.tabSize) || 4;
        let column = 0;
        const rendered = line.replace(/[^\t]+|\t/g, (part) => {
            const text = part === '\t' ? ' '.repeat(tabWidth - (column % tabWidth)) : part;
            column += text.length;
            return text;
        });
        const x = context.measureText(rendered).width + parseFloat(style.paddingLeft);
        if (x < textarea.scrollLeft || x >= textarea.scrollLeft + textarea.clientWidth - 12) {
            textarea.scrollLeft = x - textarea.clientWidth / 2;
        }
    }

    private setValue(value: string): void {
        this.value = value;
        this.tokens = tokenizeFraudo(value);
        this.bracketPairs = checkFraudo(this.tokens, this.currentDialect).pairs;
        this.problemMessage = fraudoProblemsMessage(this.tokens);
        let offset = 0;
        this.tokenStarts = this.tokens.map((token) => {
            const start = offset;
            offset += token.text.replace(/\r\n?/g, '\n').length;
            return start;
        });
        this.activePair = [];
        this.folds = findArgumentFolds(value);
        this.previewParts = previewArguments(value, this.folds);
        this.folded = this.folded && this.folds.length > 0;
        if (this.showLineNumbers) {
            this.lineNumbers = Array.from({ length: Math.max(7, value.split('\n').length) }, (_, index) => index + 1);
            this.foldedLineNumbers = previewLineNumbers(this.previewParts, value);
        }
    }

    /** Like code editors: the bracket right before the caret wins, then the one right after it. */
    private pairAt(start: number, end: number): number[] {
        if (start !== end) {
            return [];
        }
        for (const offset of [start - 1, start]) {
            const index = this.tokenAt(offset);
            const pair = index === -1 ? undefined : this.bracketPairs.get(index);
            if (pair !== undefined) {
                return [index, pair];
            }
        }
        return [];
    }

    private tokenAt(offset: number): number {
        if (offset < 0) {
            return -1;
        }
        let low = 0;
        let high = this.tokenStarts.length - 1;
        while (low <= high) {
            const middle = (low + high) >> 1;
            if (this.tokenStarts[middle] <= offset) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        // Brackets are single-character tokens, so only an exact start is a bracket.
        return high >= 0 && this.tokenStarts[high] === offset ? high : -1;
    }

    private onChange: (value: string) => void = () => {};
}
