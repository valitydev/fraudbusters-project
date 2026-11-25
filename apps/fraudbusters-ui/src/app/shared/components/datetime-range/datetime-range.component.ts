import { ConnectedPosition, OverlayModule } from '@angular/cdk/overlay';
import { NgClass, NgFor, NgIf } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    Output,
    QueryList,
    ViewChildren,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';

import {
    calendarCells,
    CalendarCell,
    DateTimeParts,
    dayKey,
    formatTrigger,
    formatWindow,
    fromParts,
    pad2,
    toParts,
    zoneLabel,
} from './datetime-parts';

export interface DateTimeRange {
    from: Date;
    to: Date;
}

export type DateTimeBoundary = 'from' | 'to';
type TimePart = 'hours' | 'minutes' | 'seconds';

const MONTHS = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
];

const HOUR = 3_600_000;

/**
 * Date-time range (or single date-time) picker: two triggers and a popover with a calendar that
 * highlights the range, time steppers and drums, quick times, and Apply / Cancel. Nothing changes
 * until Apply, so a half-edited range is never searched.
 */
@Component({
    selector: 'fb-datetime-range',
    standalone: true,
    imports: [NgClass, NgFor, NgIf, OverlayModule, MatIconModule, MatSelectModule],
    templateUrl: 'datetime-range.component.html',
    styleUrls: ['datetime-range.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DateTimeRangeComponent {
    @Input() from: Date | null = null;
    @Input() to: Date | null = null;
    /** One moment instead of a range: only the From trigger, no range band. */
    @Input() single = false;
    @HostBinding('class.date-only') get dateOnly(): boolean {
        return !this.withTime;
    }

    @Input() withTime = true;
    /** Show and edit the time in UTC instead of the browser's time zone. */
    @Input() utc = false;
    /** Days after it cannot be picked (e.g. today for analytics). */
    @Input() max: Date | null = null;
    @Input() fromLabel = 'From';
    @Input() toLabel = 'To';
    /** "Reset to 1 day" in the footer: the range that ends now. */
    @Input() resetDuration = 24 * HOUR;
    @Input() resetLabel = '1 day';
    @Input() disabled = false;

    @Output() readonly rangeChange = new EventEmitter<DateTimeRange>();

    @ViewChildren('drum') drums: QueryList<ElementRef<HTMLElement>>;

    readonly months = MONTHS;
    readonly weekdays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    readonly hours = Array.from({ length: 24 }, (_, index) => index);
    readonly sixty = Array.from({ length: 60 }, (_, index) => index);
    readonly timeParts: { part: TimePart; label: string; values: number[] }[] = [
        { part: 'hours', label: 'Hours', values: this.hours },
        { part: 'minutes', label: 'Minutes', values: this.sixty },
        { part: 'seconds', label: 'Seconds', values: this.sixty },
    ];

    readonly quickTimes = [
        { label: 'Start of day', time: [0, 0, 0] },
        { label: 'Midday', time: [12, 0, 0] },
        { label: 'End of day', time: [23, 59, 59] },
    ];

    readonly positions: ConnectedPosition[] = [
        { originX: 'start', originY: 'bottom', overlayX: 'start', overlayY: 'top', offsetY: 8 },
        { originX: 'end', originY: 'bottom', overlayX: 'end', overlayY: 'top', offsetY: 8 },
        { originX: 'start', originY: 'top', overlayX: 'start', overlayY: 'bottom', offsetY: -8 },
        { originX: 'end', originY: 'top', overlayX: 'end', overlayY: 'bottom', offsetY: -8 },
    ];

    open = false;
    editing: DateTimeBoundary = 'from';
    draftFrom: DateTimeParts;
    draftTo: DateTimeParts;
    viewYear = 0;
    viewMonth = 0;
    feedback = '';
    cells: CalendarCell[] = [];

    constructor(private cdr: ChangeDetectorRef) {}

    get fromText(): string {
        return formatTrigger(this.from, this.utc, this.withTime);
    }

    get toText(): string {
        return formatTrigger(this.to, this.utc, this.withTime);
    }

    get draftWindowText(): string {
        return formatWindow(fromParts(this.draftFrom, this.utc), fromParts(this.draftTo, this.utc));
    }

    get active(): DateTimeParts {
        return this.editing === 'from' ? this.draftFrom : this.draftTo;
    }

    get activeDate(): string {
        const { year, month, day } = this.active;
        return `${pad2(day)}/${pad2(month + 1)}/${year}`;
    }

    get zone(): string {
        return zoneLabel(fromParts(this.active, this.utc), this.utc);
    }

    get years(): number[] {
        const now = new Date().getFullYear();
        const first = Math.min(now - 5, this.viewYear);
        const last = Math.max(now + 1, this.viewYear);
        return Array.from({ length: last - first + 1 }, (_, index) => first + index);
    }

    get nowLabel(): string {
        const now = toParts(new Date(), this.utc);
        return `${pad2(now.hours)}:${pad2(now.minutes)}:${pad2(now.seconds)}`;
    }

    toggle(boundary: DateTimeBoundary): void {
        if (this.disabled) {
            return;
        }
        if (!this.open) {
            this.openAt(boundary);
        } else if (this.editing === boundary) {
            this.cancel();
        } else {
            this.edit(boundary);
        }
    }

    edit(boundary: DateTimeBoundary): void {
        this.editing = boundary;
        this.showMonthOf(this.active);
        this.scrollDrums();
    }

    shiftMonth(delta: number): void {
        const date = new Date(Date.UTC(this.viewYear, this.viewMonth + delta, 1));
        this.viewYear = date.getUTCFullYear();
        this.viewMonth = date.getUTCMonth();
        this.cells = calendarCells(this.viewYear, this.viewMonth);
    }

    setView(month: number | string, year: number | string): void {
        this.viewMonth = Number(month);
        this.viewYear = Number(year);
        this.cells = calendarCells(this.viewYear, this.viewMonth);
    }

    isEndpoint(cell: CalendarCell): boolean {
        return cell.key === dayKey(this.draftFrom) || (!this.single && cell.key === dayKey(this.draftTo));
    }

    inRange(cell: CalendarCell): boolean {
        return !this.single && cell.key > dayKey(this.draftFrom) && cell.key < dayKey(this.draftTo);
    }

    /** The band runs from the start day to the end day; its ends fade into the endpoint. */
    band(cell: CalendarCell): string {
        if (this.single || dayKey(this.draftFrom) === dayKey(this.draftTo)) {
            return '';
        }
        if (cell.key === dayKey(this.draftFrom)) {
            return 'band-start';
        }
        if (cell.key === dayKey(this.draftTo)) {
            return 'band-end';
        }
        return this.inRange(cell) ? 'band' : '';
    }

    isToday(cell: CalendarCell): boolean {
        return cell.key === dayKey(toParts(new Date(), this.utc));
    }

    isAfterMax(cell: CalendarCell): boolean {
        return Boolean(this.max) && cell.key > dayKey(toParts(this.max, this.utc));
    }

    selectDay(cell: CalendarCell): void {
        if (this.isAfterMax(cell)) {
            return;
        }
        this.change({ year: cell.year, month: cell.month, day: cell.day });
        this.viewYear = cell.year;
        this.viewMonth = cell.month;
        this.cells = calendarCells(this.viewYear, this.viewMonth);
    }

    value(part: TimePart): number {
        return this.active[part];
    }

    setTime(part: TimePart, raw: number | string): void {
        const limit = part === 'hours' ? 23 : 59;
        const value = Math.max(0, Math.min(limit, Math.trunc(Number(raw)) || 0));
        this.change({ [part]: value });
        this.scrollDrums();
    }

    step(part: TimePart, delta: number): void {
        const size = part === 'hours' ? 24 : 60;
        this.setTime(part, (this.active[part] + delta + size) % size);
    }

    setTimeOfDay([hours, minutes, seconds]: number[]): void {
        this.change({ hours, minutes, seconds });
        this.scrollDrums();
    }

    setNow(): void {
        this.change(toParts(new Date(), this.utc));
        this.showMonthOf(this.active);
        this.scrollDrums();
    }

    reset(): void {
        const to = new Date();
        const from = new Date(to.getTime() - this.resetDuration);
        this.draftFrom = this.normalize(toParts(from, this.utc));
        this.draftTo = this.normalize(toParts(to, this.utc));
        this.feedback = '';
        this.showMonthOf(this.active);
        this.scrollDrums();
    }

    apply(): void {
        const from = fromParts(this.draftFrom, this.utc);
        const to = fromParts(this.single ? this.draftFrom : this.draftTo, this.utc);
        this.open = false;
        this.rangeChange.emit({ from, to });
    }

    cancel(): void {
        this.open = false;
        this.cdr.markForCheck();
    }

    pad(value: number): string {
        return pad2(value);
    }

    trackCell(_: number, cell: CalendarCell): number {
        return cell.key;
    }

    private openAt(boundary: DateTimeBoundary): void {
        const now = new Date();
        const to = this.to ?? now;
        const from = this.from ?? new Date(to.getTime() - this.resetDuration);
        this.draftFrom = this.normalize(toParts(from, this.utc));
        this.draftTo = this.normalize(toParts(this.single ? from : to, this.utc));
        this.feedback = '';
        this.open = true;
        this.edit(this.single ? 'from' : boundary);
    }

    private showMonthOf(parts: DateTimeParts): void {
        this.viewYear = parts.year;
        this.viewMonth = parts.month;
        this.cells = calendarCells(this.viewYear, this.viewMonth);
    }

    private normalize(parts: DateTimeParts): DateTimeParts {
        return this.withTime ? parts : { ...parts, hours: 0, minutes: 0, seconds: 0 };
    }

    /** Changes the edited boundary and keeps From ≤ To, saying what moved and why. */
    private change(patch: Partial<DateTimeParts>): void {
        const before = { from: fromParts(this.draftFrom, this.utc), to: fromParts(this.draftTo, this.utc) };
        const next = this.normalize({ ...this.active, ...patch });
        if (this.editing === 'from') {
            this.draftFrom = next;
        } else {
            this.draftTo = next;
        }
        this.feedback = '';
        if (this.single) {
            this.draftTo = this.draftFrom;
            return;
        }
        const from = fromParts(this.draftFrom, this.utc);
        const to = fromParts(this.draftTo, this.utc);
        if (from <= to) {
            return;
        }
        if (this.editing === 'from') {
            const span = Math.max(this.withTime ? HOUR : 0, before.to.getTime() - before.from.getTime());
            this.draftTo = this.normalize(toParts(new Date(from.getTime() + span), this.utc));
            this.feedback = `${this.toLabel} moved forward to keep the range valid.`;
        } else {
            this.draftTo = { ...this.draftFrom };
            this.feedback = `${this.toLabel} cannot be earlier than ${this.fromLabel}: set to ${this.fromLabel}.`;
        }
    }

    /** Centres the selected hour, minute and second in their columns. */
    private scrollDrums(): void {
        setTimeout(() => {
            this.drums?.forEach(({ nativeElement: drum }) => {
                const selected = drum.querySelector<HTMLElement>('.selected');
                if (selected) {
                    drum.scrollTop = selected.offsetTop - drum.clientHeight / 2 + selected.offsetHeight / 2;
                }
            });
        });
    }
}
