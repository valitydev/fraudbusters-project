import { TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { ConfirmActionDialogComponent, ConfirmActionDialogData } from './confirm-action-dialog.component';
import { ConfirmActionDialogModule } from './confirm-action-dialog.module';

describe('ConfirmActionDialogComponent', () => {
    const render = (data: ConfirmActionDialogData) => {
        const close = jasmine.createSpy('close');
        TestBed.configureTestingModule({
            imports: [ConfirmActionDialogModule],
            providers: [
                { provide: MAT_DIALOG_DATA, useValue: data },
                { provide: MatDialogRef, useValue: { close } },
            ],
        });
        const fixture = TestBed.createComponent(ConfirmActionDialogComponent);
        fixture.detectChanges();
        const element: HTMLElement = fixture.nativeElement;
        return { element, close, confirm: element.querySelector<HTMLButtonElement>('.fb-dialog-confirm') };
    };

    it('shows a red labelled action for destructive confirmations', () => {
        const { element, confirm } = render({ title: 'Remove group g1?', confirmLabel: 'Remove', destructive: true });
        expect(element.querySelector('.fb-dialog-title').textContent.trim()).toBe('Remove group g1?');
        expect(confirm.textContent.trim()).toBe('Remove');
        expect(confirm.classList).toContain('fb-dialog-danger');
    });

    it('falls back to a neutral Confirm action', () => {
        const { confirm } = render({ title: 'Archive notification n1?' });
        expect(confirm.textContent.trim()).toBe('Confirm');
        expect(confirm.classList).not.toContain('fb-dialog-danger');
    });

    it('closes with the chosen result', () => {
        const { element, close, confirm } = render({ title: 'Remove row 1?' });
        element.querySelector<HTMLButtonElement>('.fb-dialog-cancel').click();
        expect(close).toHaveBeenCalledWith('cancel');
        confirm.click();
        expect(close).toHaveBeenCalledWith('confirm');
    });
});
