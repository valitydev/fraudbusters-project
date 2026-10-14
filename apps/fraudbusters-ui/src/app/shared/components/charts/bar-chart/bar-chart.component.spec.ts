import { BarChartComponent } from './bar-chart.component';
import { DEFAULT_CONFIG } from './default-config';

describe('BarChartComponent', () => {
    it('creates an independent config containing formatter functions', () => {
        const component = new BarChartComponent();

        expect(component.config).toEqual(DEFAULT_CONFIG);
        expect(component.config).not.toBe(DEFAULT_CONFIG);
    });

    it('zooms by selection without reacting to the mouse wheel', () => {
        const component = new BarChartComponent();

        expect(component.config.chart.zoom).toEqual(
            jasmine.objectContaining({
                enabled: true,
                type: 'x',
                allowMouseWheelZoom: false,
            })
        );
    });
});
