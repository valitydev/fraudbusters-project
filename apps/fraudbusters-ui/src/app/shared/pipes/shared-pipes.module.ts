import { NgModule } from '@angular/core';

import { FraudoTokensPipe } from './fraudo-tokens.pipe';
import { RuleLabelPipe, UnnamedRuleIndexPipe } from './rule-label.pipe';
import { ArraySortPipe } from './sort-by-field.pipe';
import { TemplateToRulesPipe } from './template-to-rules.pipe';

@NgModule({
    declarations: [TemplateToRulesPipe, ArraySortPipe, FraudoTokensPipe, RuleLabelPipe, UnnamedRuleIndexPipe],
    exports: [TemplateToRulesPipe, ArraySortPipe, FraudoTokensPipe, RuleLabelPipe, UnnamedRuleIndexPipe],
})
export class SharedPipesModule {}
