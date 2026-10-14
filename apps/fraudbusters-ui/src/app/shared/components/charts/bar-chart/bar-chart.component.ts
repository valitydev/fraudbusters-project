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
})
export class BarChartComponent implements OnChanges {
    @Input()
    series?: ApexAxisChartSeries;

    @Input()
    colors?: string[];

    @Input()
    height?: number;

    config = cloneDeep(DEFAULT_CONFIG);

    ngOnChanges(changes: SimpleChanges) {
        if (changes.height && changes.height.currentValue !== changes.height.previousValue) {
            this.config.chart.height = this.height;
        }
    }
}
