import { Pipe, PipeTransform } from '@angular/core';

import { ThemePaletteEnum } from '../../styles/utils/theme-palette-enum';

@Pipe({
    name: 'inspectResultToColor',
})
export class InspectResultToColorPipe implements PipeTransform {
    transform(status: string): ThemePaletteEnum {
        switch (status) {
            case 'three_ds':
                return ThemePaletteEnum.Blue;
            case 'accept':
                return ThemePaletteEnum.Success;
            case 'decline':
                return ThemePaletteEnum.Failed;
            default:
                return ThemePaletteEnum.Unknown;
        }
    }
}
