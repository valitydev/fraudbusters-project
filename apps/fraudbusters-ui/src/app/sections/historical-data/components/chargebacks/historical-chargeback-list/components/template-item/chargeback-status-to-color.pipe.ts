import { Pipe, PipeTransform } from '@angular/core';

import { ThemePaletteEnum } from '../../../../../../../styles/utils/theme-palette-enum';

@Pipe({
    name: 'chargebackStatusToColor',
})
export class ChargebackStatusToColorPipe implements PipeTransform {
    transform(status: string): ThemePaletteEnum {
        switch (status) {
            case 'cancelled':
                return ThemePaletteEnum.Cancel;
            case 'accepted':
                return ThemePaletteEnum.Success;
            default:
                return ThemePaletteEnum.Failed;
        }
    }
}
