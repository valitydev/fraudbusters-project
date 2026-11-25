import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { DataSetTemplateComponent } from './data-set-template.component';
import { DateTimeRangeComponent } from '../../../datetime-range/datetime-range.component';
import { FraudoEditorComponent } from '../../../fraudo-editor/fraudo-editor.component';

@NgModule({
    declarations: [DataSetTemplateComponent],
    exports: [DataSetTemplateComponent],
    imports: [
        DateTimeRangeComponent,
        CommonModule,
        MatButtonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatProgressBarModule,
        FraudoEditorComponent,
    ],
})
export class DataSetTemplateModule {}
