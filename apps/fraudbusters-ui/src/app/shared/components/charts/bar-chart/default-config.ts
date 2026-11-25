import { ApexOptions } from 'ng-apexcharts';

import { DEFAULT_ANIMATION } from '../default-animation';
import { DEFAULT_LEGEND } from '../default-legend';
import { DEFAULT_STATES } from '../default-states';

const COLUMN_WIDTH = '30%';

type ZoomOptionsWithMouseWheelControl = NonNullable<NonNullable<ApexOptions['chart']>['zoom']> & {
    allowMouseWheelZoom: boolean;
};

export const DEFAULT_CONFIG: ApexOptions = {
    chart: {
        type: 'bar',
        stacked: true,
        height: 350,
        width: '100%',
        toolbar: {
            show: false,
        },
        zoom: {
            enabled: true,
            type: 'x',
            allowMouseWheelZoom: false,
        } as ZoomOptionsWithMouseWheelControl,
        animations: DEFAULT_ANIMATION,
    },
    dataLabels: {
        enabled: false,
    },
    legend: { ...DEFAULT_LEGEND, position: 'top', horizontalAlign: 'right' },
    grid: { borderColor: 'var(--fb-line)', strokeDashArray: 4 },
    fill: {
        opacity: 1,
    },
    plotOptions: {
        bar: {
            horizontal: false,
            columnWidth: COLUMN_WIDTH,
            borderRadius: 4,
        },
    },
    xaxis: {
        type: 'datetime',
        labels: {
            offsetY: -5,
            rotate: 0,
        },
        axisTicks: {
            show: false,
        },
        axisBorder: {
            show: false,
        },
        crosshairs: {
            show: false,
        },
    },
    yaxis: {
        min: 0,
        max: 100,
        tickAmount: 4,
        labels: {
            formatter: (value) => value + '%',
        },
    },
    states: DEFAULT_STATES,
};
