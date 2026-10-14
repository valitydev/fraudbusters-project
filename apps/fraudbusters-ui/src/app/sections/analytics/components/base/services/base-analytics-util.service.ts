import { Injectable } from '@angular/core';

@Injectable()
export class BaseAnalyticsUtilService {
    todayFromTime(time): Date {
        let diffDay = 1;
        switch (time) {
            case '1 day': {
                diffDay = 1;
                break;
            }
            case '1 week': {
                diffDay = 7;
                break;
            }
            case 'last month': {
                return this.getDaysInCurrentMonth();
            }
            case 'last year': {
                return this.getDaysYear();
            }
            default: {
                diffDay = 1;
                break;
            }
        }
        const now = new Date();
        now.setDate(now.getDate() - diffDay);
        return now;
    }

    getSplitUnitFromTime(time): string {
        switch (time) {
            case '1 day': {
                return 'hour';
            }
            case 'last year': {
                return 'month';
            }
            default: {
                return 'day';
            }
        }
    }

    private getDaysInCurrentMonth() {
        return new Date(new Date().getFullYear(), new Date().getMonth(), 0);
    }

    private getDaysYear() {
        return new Date(new Date().getFullYear(), 0, 0);
    }
}
