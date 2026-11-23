import { Pipe, PipeTransform } from '@angular/core';

import { Payout } from '../../../../../../../api/fb-management/swagger-codegen/model/payout';
import { ThemePaletteEnum } from '../../../../../../../styles/utils/theme-palette-enum';

@Pipe({
    name: 'payoutStatusToColor',
})
export class PayoutStatusToColorPipe implements PipeTransform {
    transform(status: Payout.StatusEnum): ThemePaletteEnum {
        switch (status) {
            case Payout.StatusEnum.Pending:
                return ThemePaletteEnum.Pending;
            case Payout.StatusEnum.Succeeded:
                return ThemePaletteEnum.Success;
            default:
                return ThemePaletteEnum.Failed;
        }
    }
}
