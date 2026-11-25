import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { CreateTemplateComponent } from './create-template.component';
import { TemplateEditorComponent } from '../template-editor/template-editor.component';

@NgModule({
    declarations: [CreateTemplateComponent],
    exports: [CreateTemplateComponent],
    imports: [CommonModule, TemplateEditorComponent],
})
export class CreateTemplateModule {}
