import { NgModule } from '@angular/core';

import { FormatNumberPipe } from './format-number.pipe';
import { InspectResultToColorPipe } from './inspect-result-to-color.pipe';
import { PaymentStatusToColorPipe } from './payment-status-to-color.pipe';
import { ArraySortPipe } from './sort-by-field.pipe';
import { TemplateToRulesPipe } from './template-to-rules.pipe';

@NgModule({
    declarations: [
        TemplateToRulesPipe,
        PaymentStatusToColorPipe,
        ArraySortPipe,
        FormatNumberPipe,
        InspectResultToColorPipe,
    ],
    exports: [TemplateToRulesPipe, PaymentStatusToColorPipe, ArraySortPipe, FormatNumberPipe, InspectResultToColorPipe],
})
export class SharedPipesModule {}
