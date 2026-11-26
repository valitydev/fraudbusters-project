import { Clipboard } from '@angular/cdk/clipboard';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/** "Template ID" → "template ID", "ID" stays "ID": the label inside a sentence. */
function inSentence(label: string): string {
    return /^[A-Z][a-z]/.test(label) ? label[0].toLowerCase() + label.slice(1) : label;
}

/** Copies a value and confirms it with a snack bar: "Template ID copied" or "Could not copy template ID". */
@Injectable({ providedIn: 'root' })
export class CopyToClipboardService {
    constructor(private clipboard: Clipboard, private snackBar: MatSnackBar) {}

    copy(value: string, label: string): void {
        const copied = this.clipboard.copy(value);
        this.snackBar.open(copied ? `${label} copied` : `Could not copy ${inSentence(label)}`, 'OK', {
            duration: 2500,
        });
    }
}
