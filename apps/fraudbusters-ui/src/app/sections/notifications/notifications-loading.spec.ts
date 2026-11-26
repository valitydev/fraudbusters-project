import { Type } from '@angular/core';
import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';

import { ChannelsComponent } from './components/channels/channels.component';
import { PeriodicalNotificationComponent } from './components/periodical/periodical-notification.component';
import { NotificationsModule } from './notifications.module';
import { NotificationsService } from '../../api/payments/notifications';
import { ConfigService } from '../../config';

describe('Notifications loading', () => {
    const tabs: Type<unknown>[] = [PeriodicalNotificationComponent, ChannelsComponent];
    const scenarios = tabs.flatMap((tab) => [true, false].map((hasRows) => ({ tab, hasRows })));
    for (const { tab, hasRows } of scenarios) {
        it(`shows the loader then ${hasRows ? 'the table' : 'an empty state'} for ${tab.name}`, fakeAsync(() => {
            const response = new Subject<{ result: { name: string }[]; count: number }>();
            TestBed.configureTestingModule({
                imports: [NotificationsModule],
                providers: [
                    provideRouter([]),
                    { provide: ActivatedRoute, useValue: { queryParamMap: of(convertToParamMap({})) } },
                    { provide: ConfigService, useValue: { pageSize: 10 } },
                    {
                        provide: NotificationsService,
                        useValue: { getNotifications: () => response, getChannels: () => response },
                    },
                ],
            });
            const fixture = TestBed.createComponent(tab);
            const page: HTMLElement = fixture.nativeElement;
            fixture.detectChanges();

            expect(page.querySelector('input[type="search"]')).not.toBeNull();
            expect(page.querySelector('thead')).toBeNull();
            expect(page.querySelector('mat-progress-bar')).not.toBeNull();
            expect(page.querySelector('.table-state')).toBeNull();

            tick(300);
            const result = hasRows ? [{ name: 'Example' }] : [];
            response.next({ result, count: result.length });
            fixture.detectChanges();

            expect(page.querySelector('mat-progress-bar')).toBeNull();
            if (hasRows) {
                expect(page.querySelector('thead')).not.toBeNull();
                expect(page.querySelector('tbody').textContent).toContain('Example');
            } else {
                expect(page.querySelector('thead')).toBeNull();
                expect(page.querySelector('.table-state').textContent).toContain('found');
            }
        }));
    }
});
