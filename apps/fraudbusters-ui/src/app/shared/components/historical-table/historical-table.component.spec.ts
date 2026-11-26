import { ComponentFixture, TestBed } from '@angular/core/testing';

import { payoutInspectResultRow, payoutRow } from './historical-row';
import { HistoricalTableComponent } from './historical-table.component';
import { CopyToClipboardService } from '../../services/copy-to-clipboard.service';

describe(HistoricalTableComponent.name, () => {
    const partyId = 'party-' + 'a'.repeat(40);
    const shopId = 'shop-' + 'b'.repeat(40);
    const fingerprint = 'Browser signature ' + 'c'.repeat(600);
    let fixture: ComponentFixture<HistoricalTableComponent>;
    let copy: jasmine.Spy;
    let table: HTMLElement;

    beforeEach(() => {
        copy = jasmine.createSpy('copy');
        TestBed.configureTestingModule({
            providers: [{ provide: CopyToClipboardService, useValue: { copy } }],
        });
        fixture = TestBed.createComponent(HistoricalTableComponent);
        fixture.componentRef.setInput('kind', {
            title: 'Inspect results',
            operation: 'payment',
            columns: ['party', 'scope'],
            statusLabel: 'Result status',
        });
        fixture.componentRef.setInput('rows', [
            {
                key: 'record',
                id: 'record',
                partyId,
                scopeId: shopId,
                details: [{ label: 'Fingerprint', value: fingerprint }],
            },
        ]);
        table = fixture.nativeElement;
        fixture.detectChanges();
    });

    it('truncates party and shop previews and copies the full IDs without opening details', () => {
        const previews = table.querySelectorAll<HTMLElement>('.id-preview');
        expect(previews.length).toBe(2);
        previews.forEach((preview) => {
            expect(preview.scrollWidth).toBeGreaterThan(preview.clientWidth);
            expect(getComputedStyle(preview).textOverflow).toBe('ellipsis');
        });

        table.querySelector<HTMLButtonElement>('button[title="Copy Party ID"]').click();
        table.querySelector<HTMLButtonElement>('button[title="Copy Shop ID"]').click();
        fixture.detectChanges();

        expect(copy.calls.allArgs()).toEqual([
            [partyId, 'Party ID'],
            [shopId, 'Shop ID'],
        ]);
        expect(table.querySelector('.fb-catalog-details')).toBeNull();
    });

    it('truncates and copies wallet IDs from payouts and payout inspect results', () => {
        const walletId = 'wallet-' + 'd'.repeat(40);
        const payout = { partyId, walletId, eventTime: new Date('2026-01-01T00:00:00Z'), amount: 10, currency: 'RUB' };
        fixture.componentRef.setInput('kind', { ...fixture.componentInstance.kind, operation: 'payout' });
        fixture.componentRef.setInput('rows', [
            payoutRow({ ...payout, id: 'payout', status: 'succeeded' }, 0),
            payoutInspectResultRow({ ...payout, withdrawalId: 'payout', resultStatus: 'accept' }, 1),
        ]);
        fixture.detectChanges();

        expect(table.querySelector('thead').textContent).toContain('Wallet ID');
        const buttons = table.querySelectorAll<HTMLButtonElement>('button[title="Copy Wallet ID"]');
        expect(buttons.length).toBe(2);
        buttons.forEach((button) => {
            const preview = button.parentElement.querySelector<HTMLElement>('.id-preview');
            expect(preview.scrollWidth).toBeGreaterThan(preview.clientWidth);
            button.click();
        });
        expect(copy.calls.allArgs()).toEqual([
            [walletId, 'Wallet ID'],
            [walletId, 'Wallet ID'],
        ]);
    });

    it('displays payout amounts with the precision of each currency', () => {
        const eventTime = new Date(0);
        fixture.componentRef.setInput('kind', {
            ...fixture.componentInstance.kind,
            operation: 'payout',
            columns: ['amount'],
        });
        fixture.componentRef.setInput('rows', [
            payoutRow({ id: 'payout', amount: 12345, currency: 'RUB', eventTime, status: 'succeeded' }, 0),
            payoutInspectResultRow(
                { withdrawalId: 'inspection', amount: 12345, currency: 'JPY', eventTime, resultStatus: 'accept' },
                1
            ),
        ]);
        fixture.detectChanges();

        const amounts = Array.from(table.querySelectorAll('.amount'), (cell) => cell.textContent.trim());
        expect(amounts).toEqual(['123.45 RUB', '12,345 JPY']);
    });

    it('wraps long details inside their grid column', () => {
        table.querySelector<HTMLButtonElement>('button[aria-expanded]').click();
        fixture.detectChanges();

        const value = table.querySelector<HTMLElement>('.fb-catalog-details dd');
        expect(value.textContent).toBe(fingerprint);
        expect(value.scrollWidth).toBeLessThanOrEqual(value.clientWidth);
    });
});
