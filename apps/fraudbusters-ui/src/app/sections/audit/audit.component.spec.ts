import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { defer, of, Subject } from 'rxjs';

import { AuditComponent } from './audit.component';
import { AuditModule } from './audit.module';
import { AuditRemoteService } from '../../api/audit';

describe(AuditComponent.name, () => {
    it('shows filters and a loader before either the filter options or logs arrive', () => {
        const commands = new Subject<string[]>();
        const retryCommands = new Subject<string[]>();
        const objects = new Subject<string[]>();
        const logs = new Subject<{ count: number; result: { initiator: string }[] }>();
        const findLogs = jasmine.createSpy('findLogs').and.returnValue(logs);
        const getCommandTypes = jasmine.createSpy('getCommandTypes').and.returnValues(commands, retryCommands);
        TestBed.configureTestingModule({
            imports: [AuditModule],
            providers: [
                provideRouter([]),
                { provide: ActivatedRoute, useValue: { queryParams: of({ userId: 'audit-user' }) } },
                {
                    provide: AuditRemoteService,
                    useValue: {
                        getCommandTypes: () => defer(getCommandTypes),
                        getObjectTypes: () => objects,
                        findLogs,
                    },
                },
            ],
        });
        spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
        const fixture = TestBed.createComponent(AuditComponent);
        const page: HTMLElement = fixture.nativeElement;
        fixture.detectChanges();

        expect(page.querySelector<HTMLInputElement>('#audit-user').value).toBe('audit-user');
        expect(page.querySelector('thead')).toBeNull();
        expect(page.querySelector('mat-progress-bar')).not.toBeNull();
        expect(page.querySelector('.table-state')).toBeNull();
        expect(findLogs).not.toHaveBeenCalled();

        commands.error(new Error('Filter options unavailable'));
        fixture.detectChanges();
        expect(page.querySelector('mat-progress-bar')).toBeNull();
        page.querySelector<HTMLButtonElement>('[role="alert"] button').click();
        fixture.detectChanges();
        expect(page.querySelector('mat-progress-bar')).not.toBeNull();

        retryCommands.next(['CREATE']);
        objects.next(['TEMPLATE']);
        fixture.detectChanges();

        expect(findLogs).toHaveBeenCalledOnceWith(
            jasmine.objectContaining({
                searchValue: 'audit-user%',
                commandTypes: ['CREATE'],
                objectTypes: ['TEMPLATE'],
            })
        );
        expect(page.querySelector('mat-progress-bar')).not.toBeNull();

        logs.next({ count: 1, result: [{ initiator: 'audit-user' }] });
        logs.complete();
        fixture.detectChanges();

        expect(page.querySelector('thead')).not.toBeNull();
        expect(page.querySelector('tbody').textContent).toContain('audit-user');
        expect(page.querySelector('mat-progress-bar')).toBeNull();
    });
});
