import { Pipe, PipeTransform } from '@angular/core';

import { Withdrawal } from '../../../../../../../api/fb-management/swagger-codegen/model/withdrawal';
import { ThemePaletteEnum } from '../../../../../../../styles/utils/theme-palette-enum';

@Pipe({
    name: 'withdrawalStatusToColor',
})
export class WithdrawalStatusToColorPipe implements PipeTransform {
    transform(status: Withdrawal.StatusEnum): ThemePaletteEnum {
        switch (status) {
            case Withdrawal.StatusEnum.Pending:
                return ThemePaletteEnum.Pending;
            case Withdrawal.StatusEnum.Succeeded:
                return ThemePaletteEnum.Success;
            default:
                return ThemePaletteEnum.Failed;
        }
    }
}
