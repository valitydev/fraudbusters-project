import { Pipe, PipeTransform } from '@angular/core';

import { ThemePaletteEnum } from '../../../../../../../styles/utils/theme-palette-enum';

@Pipe({
    name: 'refundStatusToColor',
})
export class RefundStatusToColorPipe implements PipeTransform {
    transform(status: string): ThemePaletteEnum {
        switch (status) {
            case 'pending':
                return ThemePaletteEnum.Pending;
            case 'succeeded':
                return ThemePaletteEnum.Success;
            default:
                return ThemePaletteEnum.Failed;
        }
    }
}
