import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { ThemePaletteEnum } from '../../../styles/utils/theme-palette-enum';
import { StatusTone, tonePillClass } from '../../utils/status-tone';

const PALETTE_TONES: Record<ThemePaletteEnum, StatusTone> = {
    [ThemePaletteEnum.Success]: 'success',
    [ThemePaletteEnum.Failed]: 'danger',
    [ThemePaletteEnum.Warn]: 'danger',
    [ThemePaletteEnum.Pending]: 'warning',
    [ThemePaletteEnum.Blue]: 'info',
    [ThemePaletteEnum.Primary]: 'info',
    [ThemePaletteEnum.Cancel]: 'neutral',
    [ThemePaletteEnum.Unknown]: 'neutral',
};

/** A status badge in the shared pill style; the colour comes from the status pipes' palette. */
@Component({
    selector: 'fb-tag',
    templateUrl: 'tag.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TagComponent {
    @Input() title: string;
    @Input() color: ThemePaletteEnum = ThemePaletteEnum.Primary;

    get pill(): string {
        return tonePillClass(PALETTE_TONES[this.color] ?? 'neutral');
    }
}
