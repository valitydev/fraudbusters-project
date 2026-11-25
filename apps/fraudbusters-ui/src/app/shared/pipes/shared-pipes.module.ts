import { NgModule } from '@angular/core';

import { FraudoTokensPipe } from './fraudo-tokens.pipe';
import { ArraySortPipe } from './sort-by-field.pipe';
import { TemplateToRulesPipe } from './template-to-rules.pipe';

@NgModule({
    declarations: [TemplateToRulesPipe, ArraySortPipe, FraudoTokensPipe],
    exports: [TemplateToRulesPipe, ArraySortPipe, FraudoTokensPipe],
})
export class SharedPipesModule {}
