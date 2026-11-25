/** Wall-clock parts of a moment in either UTC or the browser's time zone. */
export interface DateTimeParts {
    year: number;
    /** 0-based, like Date. */
    month: number;
    day: number;
    hours: number;
    minutes: number;
    seconds: number;
}

export function toParts(date: Date, utc: boolean): DateTimeParts {
    return utc
        ? {
              year: date.getUTCFullYear(),
              month: date.getUTCMonth(),
              day: date.getUTCDate(),
              hours: date.getUTCHours(),
              minutes: date.getUTCMinutes(),
              seconds: date.getUTCSeconds(),
          }
        : {
              year: date.getFullYear(),
              month: date.getMonth(),
              day: date.getDate(),
              hours: date.getHours(),
              minutes: date.getMinutes(),
              seconds: date.getSeconds(),
          };
}

export function fromParts({ year, month, day, hours, minutes, seconds }: DateTimeParts, utc: boolean): Date {
    return utc
        ? new Date(Date.UTC(year, month, day, hours, minutes, seconds))
        : new Date(year, month, day, hours, minutes, seconds);
}

/** A sortable number for the calendar day only: 20261125. */
export function dayKey({ year, month, day }: Pick<DateTimeParts, 'year' | 'month' | 'day'>): number {
    return year * 10000 + month * 100 + day;
}

export function pad2(value: number): string {
    return String(value).padStart(2, '0');
}

/** "25/11/2026, 17:11:16" or "25/11/2026" without the time. */
export function formatTrigger(date: Date | null, utc: boolean, withTime: boolean): string {
    if (!date || Number.isNaN(date.getTime())) {
        return '';
    }
    const p = toParts(date, utc);
    const day = `${pad2(p.day)}/${pad2(p.month + 1)}/${p.year}`;
    return withTime ? `${day}, ${pad2(p.hours)}:${pad2(p.minutes)}:${pad2(p.seconds)}` : day;
}

/** "7d 00h 00m" between two moments; negative spans count as zero. */
export function formatWindow(from: Date, to: Date): string {
    const minutes = Math.floor(Math.max(0, to.getTime() - from.getTime()) / 60_000);
    const days = Math.floor(minutes / (60 * 24));
    return `${days}d ${pad2(Math.floor((minutes % (60 * 24)) / 60))}h ${pad2(minutes % 60)}m`;
}

/** "UTC" or the browser offset, e.g. "UTC+03:00", for the moment shown. */
export function zoneLabel(date: Date, utc: boolean): string {
    if (utc) {
        return 'UTC';
    }
    const offset = -date.getTimezoneOffset();
    const sign = offset < 0 ? '-' : '+';
    const abs = Math.abs(offset);
    return `UTC${sign}${pad2(Math.floor(abs / 60))}:${pad2(abs % 60)}`;
}

export interface CalendarCell {
    year: number;
    month: number;
    day: number;
    key: number;
    inMonth: boolean;
}

/** Six Monday-first weeks around a month, with the neighbouring months' days greyed out. */
export function calendarCells(year: number, month: number): CalendarCell[] {
    const first = new Date(Date.UTC(year, month, 1));
    const lead = (first.getUTCDay() + 6) % 7;
    const cells: CalendarCell[] = [];
    for (let index = 0; index < 42; index++) {
        const date = new Date(Date.UTC(year, month, 1 + index - lead));
        const cell = { year: date.getUTCFullYear(), month: date.getUTCMonth(), day: date.getUTCDate() };
        cells.push({ ...cell, key: dayKey(cell), inMonth: cell.month === month });
    }
    return cells;
}
