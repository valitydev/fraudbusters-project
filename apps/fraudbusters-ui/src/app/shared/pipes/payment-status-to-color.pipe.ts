import { Pipe, PipeTransform } from '@angular/core';

import { ThemePaletteEnum } from '../../styles/utils/theme-palette-enum';

@Pipe({
    name: 'paymentStatusToColor',
})
export class PaymentStatusToColorPipe implements PipeTransform {
    transform(status: string): ThemePaletteEnum {
        switch (status) {
            case 'pending':
                return ThemePaletteEnum.Pending;
            case 'processed':
                return ThemePaletteEnum.Blue;
            case 'captured':
                return ThemePaletteEnum.Success;
            case 'cancelled':
                return ThemePaletteEnum.Cancel;
            case 'failed':
                return ThemePaletteEnum.Failed;
            default:
                return ThemePaletteEnum.Unknown;
        }
    }
}
