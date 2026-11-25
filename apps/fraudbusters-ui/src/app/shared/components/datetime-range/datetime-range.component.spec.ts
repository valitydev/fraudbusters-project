import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { calendarCells, formatTrigger, formatWindow, zoneLabel } from './datetime-parts';
import { DateTimeRange, DateTimeRangeComponent } from './datetime-range.component';

describe('date-time parts', () => {
    it('formats triggers and windows like the prototype', () => {
        const from = new Date(Date.UTC(2026, 10, 18, 17, 11, 16));
        const to = new Date(Date.UTC(2026, 10, 25, 17, 11, 16));
        expect(formatTrigger(from, true, true)).toBe('18/11/2026, 17:11:16');
        expect(formatTrigger(from, true, false)).toBe('18/11/2026');
        expect(formatWindow(from, to)).toBe('7d 00h 00m');
        expect(formatWindow(to, from)).toBe('0d 00h 00m');
        expect(zoneLabel(from, true)).toBe('UTC');
    });

    it('lays out six Monday-first weeks', () => {
        const cells = calendarCells(2026, 10);
        expect(cells.length).toBe(42);
        // November 2026 starts on a Sunday: six October days come first.
        expect(cells[0]).toEqual(jasmine.objectContaining({ month: 9, day: 26, inMonth: false }));
        expect(cells[6]).toEqual(jasmine.objectContaining({ month: 10, day: 1, inMonth: true }));
    });
});

describe('DateTimeRangeComponent', () => {
    const create = () => {
        TestBed.configureTestingModule({ imports: [DateTimeRangeComponent], providers: [provideNoopAnimations()] });
        const fixture = TestBed.createComponent(DateTimeRangeComponent);
        const component = fixture.componentInstance;
        component.utc = true;
        component.from = new Date(Date.UTC(2026, 10, 18, 10, 0, 0));
        component.to = new Date(Date.UTC(2026, 10, 25, 10, 0, 0));
        const emitted: DateTimeRange[] = [];
        component.rangeChange.subscribe((range) => emitted.push(range));
        return { component, emitted };
    };

    it('changes nothing until Apply', () => {
        const { component, emitted } = create();
        component.toggle('from');
        component.selectDay({ year: 2026, month: 10, day: 20, key: 0, inMonth: true });
        component.cancel();
        expect(emitted).toEqual([]);
        component.toggle('from');
        component.selectDay({ year: 2026, month: 10, day: 20, key: 0, inMonth: true });
        component.setTimeOfDay([0, 0, 0]);
        component.apply();
        expect(emitted[0].from.toISOString()).toBe('2026-11-20T00:00:00.000Z');
        expect(emitted[0].to.toISOString()).toBe('2026-11-25T10:00:00.000Z');
    });

    it('keeps From before To', () => {
        const { component, emitted } = create();
        component.toggle('to');
        component.selectDay({ year: 2026, month: 10, day: 10, key: 0, inMonth: true });
        expect(component.feedback).toContain('cannot be earlier');
        component.edit('from');
        component.selectDay({ year: 2026, month: 10, day: 28, key: 0, inMonth: true });
        expect(component.feedback).toContain('moved forward');
        component.apply();
        expect(emitted[0].from.getTime()).toBeLessThan(emitted[0].to.getTime());
    });

    it('wraps time steps around the clock', () => {
        const { component } = create();
        component.toggle('from');
        component.setTime('hours', 0);
        component.step('hours', -1);
        expect(component.value('hours')).toBe(23);
        component.setTime('minutes', 99);
        expect(component.value('minutes')).toBe(59);
    });
});
