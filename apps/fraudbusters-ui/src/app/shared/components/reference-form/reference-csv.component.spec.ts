import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { ReferenceCsvComponent } from './reference-csv.component';

describe('ReferenceCsvComponent', () => {
    it('updates the preview when file reading finishes outside Angular', async () => {
        TestBed.configureTestingModule({
            imports: [ReferenceCsvComponent],
            providers: [provideNoopAnimations()],
        });
        const fixture = TestBed.createComponent(ReferenceCsvComponent);
        fixture.componentRef.setInput('scope', 'wallet');
        fixture.autoDetectChanges();

        const file = new File([], 'references.csv');
        let finishReading: (contents: string) => void;
        spyOn(file, 'text').and.returnValue(
            new Promise<string>((resolve) => {
                finishReading = resolve;
            })
        );

        const { ngZone } = fixture;
        const input = { files: [file], value: file.name } as unknown as HTMLInputElement;
        const loading = ngZone.run(() => fixture.componentInstance.panel.loadFiles(input));
        expect(fixture.nativeElement.textContent).toContain('Reading CSV files');

        ngZone.runOutsideAngular(() =>
            finishReading(
                'template_id,party_id,wallet_id,is_global\n' +
                    'fixture-template,fixture-party,fixture-wallet,false\n' +
                    'global-fixture,another-party,another-wallet,true'
            )
        );
        await loading;

        expect(fixture.nativeElement.textContent).toContain('fixture-wallet');
        expect(fixture.nativeElement.textContent).toContain('1 invalid row skipped');
        expect(fixture.nativeElement.textContent).not.toContain('global-fixture');
        const addButton = Array.from<HTMLButtonElement>(fixture.nativeElement.querySelectorAll('button')).find(
            (button) => button.textContent.includes('Add 1 to queue')
        );
        expect(addButton?.disabled).toBeFalse();
    });

    it('reads group references and keeps global rows where the party is optional', async () => {
        TestBed.configureTestingModule({
            imports: [ReferenceCsvComponent],
            providers: [provideNoopAnimations()],
        });
        const fixture = TestBed.createComponent(ReferenceCsvComponent);
        fixture.componentRef.setInput('scope', 'wallet');
        fixture.componentRef.setInput('target', 'group');
        fixture.componentRef.setInput('partyOptional', true);
        fixture.autoDetectChanges();

        const file = new File([], 'group-references.csv');
        spyOn(file, 'text').and.resolveTo(
            'group_id,party_id,wallet_id,is_global\n' +
                'fixture-group,fixture-party,fixture-wallet,false\n' +
                'global-group,,,true\n' +
                'orphan-wallet-group,,lonely-wallet,false'
        );
        const input = { files: [file], value: file.name } as unknown as HTMLInputElement;
        await fixture.ngZone.run(() => fixture.componentInstance.panel.loadFiles(input));

        expect(fixture.componentInstance.panel.rows).toEqual([
            { targetId: 'fixture-group', partyId: 'fixture-party', scopeId: 'fixture-wallet' },
            { targetId: 'global-group', partyId: undefined, scopeId: undefined },
        ]);
        expect(fixture.nativeElement.textContent).toContain('1 invalid row skipped');
    });
});
