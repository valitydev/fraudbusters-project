import { Injectable } from '@angular/core';

const DAY_MS = 24 * 60 * 60 * 1000;

export const ANALYTICS_PERIODS = [
    { key: 'today', label: 'Today' },
    { key: 'yesterday', label: 'Yesterday' },
    { key: '7-days', label: '7 days' },
    { key: '30-days', label: '30 days' },
    { key: 'this-month', label: 'This month' },
    { key: 'this-year', label: 'This year' },
    { key: 'custom', label: 'Custom range' },
] as const;

export const DEFAULT_ANALYTICS_PERIOD = '7-days';

export interface AnalyticsRange {
    from: Date;
    to: Date;
    splitUnit: 'hour' | 'day' | 'month';
}

@Injectable()
export class BaseAnalyticsUtilService {
    resolveRange(period: string, fromDate = '', toDate = '', now = new Date()): AnalyticsRange | null {
        const year = now.getUTCFullYear();
        const month = now.getUTCMonth();
        const day = now.getUTCDate();
        switch (period) {
            case 'today':
                return { from: this.utcDay(year, month, day), to: now, splitUnit: 'hour' };
            case 'yesterday':
                return {
                    from: this.utcDay(year, month, day - 1),
                    to: new Date(this.utcDay(year, month, day).getTime() - 1),
                    splitUnit: 'hour',
                };
            case '30-days':
                return { from: this.utcDay(year, month, day - 29), to: now, splitUnit: 'day' };
            case 'this-month':
                return { from: this.utcDay(year, month, 1), to: now, splitUnit: 'day' };
            case 'this-year':
                return { from: this.utcDay(year, 0, 1), to: now, splitUnit: 'month' };
            case 'custom':
                return this.customRange(fromDate, toDate, now);
            // Older Analytics links keep their original rolling or calendar interval.
            case '1 day':
                return { from: new Date(now.getTime() - DAY_MS), to: now, splitUnit: 'hour' };
            case '1 week':
                return { from: new Date(now.getTime() - 7 * DAY_MS), to: now, splitUnit: 'day' };
            case 'last month':
                return { from: new Date(now.getFullYear(), now.getMonth(), 0), to: now, splitUnit: 'day' };
            case 'last year':
                return { from: new Date(now.getFullYear(), 0, 0), to: now, splitUnit: 'month' };
            default:
                return { from: this.utcDay(year, month, day - 6), to: now, splitUnit: 'day' };
        }
    }

    dateInputValue(date: Date): string {
        return date.toISOString().slice(0, 10);
    }

    private customRange(fromDate: string, toDate: string, now: Date): AnalyticsRange | null {
        const from = this.parseDateInput(fromDate);
        const lastDay = this.parseDateInput(toDate);
        if (!from || !lastDay || from > lastDay || toDate > this.dateInputValue(now)) {
            return null;
        }
        const days = (lastDay.getTime() - from.getTime()) / DAY_MS + 1;
        const to = new Date(Math.min(lastDay.getTime() + DAY_MS - 1, now.getTime()));
        return { from, to, splitUnit: days === 1 ? 'hour' : days <= 92 ? 'day' : 'month' };
    }

    private parseDateInput(value: string): Date | null {
        if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
            return null;
        }
        const date = new Date(value + 'T00:00:00Z');
        return Number.isFinite(date.getTime()) && this.dateInputValue(date) === value ? date : null;
    }

    private utcDay(year: number, month: number, day: number): Date {
        return new Date(Date.UTC(year, month, day));
    }
}
