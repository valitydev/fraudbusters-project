import { FormGroupDirective, NgControl, NgForm } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { Subject } from 'rxjs';

export class InputBase {
    errorState = false;
    stateChanges = new Subject<void>();

    // These properties should be provided by the component using this base
    ngControl: NgControl | null = null;
    _parentFormGroup: FormGroupDirective | null = null;
    _parentForm: NgForm | null = null;

    constructor(public _defaultErrorStateMatcher: ErrorStateMatcher) {}

    updateErrorState() {
        const oldState = this.errorState;
        const parent = this._parentFormGroup || this._parentForm;
        const matcher = this._defaultErrorStateMatcher;
        const control = this.ngControl ? this.ngControl.control : null;
        const newState = matcher.isErrorState(control, parent);

        if (newState !== oldState) {
            this.errorState = newState;
            this.stateChanges.next(undefined);
        }
    }
}

export const INPUT_MIX_IN_BASE = InputBase;
