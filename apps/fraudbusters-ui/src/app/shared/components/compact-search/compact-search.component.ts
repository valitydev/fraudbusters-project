import { ChangeDetectorRef, Component, ElementRef, Input, ViewChild, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'fb-compact-search',
    standalone: true,
    imports: [MatIconModule],
    templateUrl: './compact-search.component.html',
    styleUrls: ['./compact-search.component.scss'],
    providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => CompactSearchComponent), multi: true }],
})
export class CompactSearchComponent implements ControlValueAccessor {
    @Input() label = 'Search';
    @Input() placeholder = '';

    readonly fieldId = 'fb-compact-search-' + CompactSearchComponent.nextId++;
    value = '';
    open = false;
    disabled = false;

    private static nextId = 0;
    @ViewChild('trigger', { static: true }) private trigger!: ElementRef<HTMLButtonElement>;
    @ViewChild('input', { static: true }) private input!: ElementRef<HTMLInputElement>;

    constructor(private cdr: ChangeDetectorRef) {}

    onTouched: () => void = () => {};

    writeValue(value: string | null): void {
        this.value = value ?? '';
        this.open ||= Boolean(this.value);
    }

    registerOnChange(callback: (value: string) => void): void {
        this.onChange = callback;
    }

    registerOnTouched(callback: () => void): void {
        this.onTouched = callback;
    }

    setDisabledState(disabled: boolean): void {
        this.disabled = disabled;
    }

    toggle(): void {
        if (this.open && !this.value) {
            this.close();
            return;
        }
        this.open = true;
        this.cdr.detectChanges();
        this.input.nativeElement.focus();
    }

    setQuery(value: string): void {
        if (value !== this.value) {
            this.value = value;
            this.onChange(value);
        }
    }

    clear(): void {
        this.setQuery('');
        this.input.nativeElement.focus();
    }

    dismiss(event: KeyboardEvent): void {
        if (this.open) {
            event.preventDefault();
            event.stopPropagation();
            this.setQuery('');
            this.close();
        }
    }

    private onChange: (value: string) => void = () => {};

    private close(): void {
        this.open = false;
        this.trigger.nativeElement.focus();
    }
}
