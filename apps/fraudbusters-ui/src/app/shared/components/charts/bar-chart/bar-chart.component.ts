import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
// Apex options contain formatter callbacks, which structuredClone cannot clone.
// eslint-disable-next-line you-dont-need-lodash-underscore/clone-deep
import cloneDeep from 'lodash-es/cloneDeep';
import { ApexAxisChartSeries } from 'ng-apexcharts';

import { DEFAULT_CONFIG } from './default-config';

@Component({
    selector: 'fb-bar-chart',
    templateUrl: 'bar-chart.component.html',
    styleUrls: ['bar-chart.component.scss'],
    standalone: false,
})
export class BarChartComponent implements OnChanges {
    @Input()
    series?: ApexAxisChartSeries;

    @Input()
    colors?: string[];

    @Input()
    height?: number;

    @Input()
    categorical = false;

    config = cloneDeep(DEFAULT_CONFIG);

    ngOnChanges(changes: SimpleChanges) {
        if (changes.categorical || (this.categorical && changes.series)) {
            const points = this.series?.[0]?.data.length ?? 0;
            this.config.xaxis = {
                ...this.config.xaxis,
                type: this.categorical ? 'category' : 'datetime',
                tickAmount: this.categorical && points > 8 ? 7 : undefined,
            };
        }
        if (changes.height && changes.height.currentValue !== changes.height.previousValue) {
            this.config.chart.height = this.height;
        }
    }
}
