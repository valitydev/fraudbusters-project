import { Clipboard } from '@angular/cdk/clipboard';
import { MatSnackBar } from '@angular/material/snack-bar';

import { CopyToClipboardService } from './copy-to-clipboard.service';

describe('CopyToClipboardService', () => {
    const create = (copied: boolean) => {
        const clipboard = jasmine.createSpyObj<Clipboard>('Clipboard', { copy: copied });
        const snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);
        return { service: new CopyToClipboardService(clipboard, snackBar), clipboard, snackBar };
    };

    it('copies the value and confirms it', () => {
        const { service, clipboard, snackBar } = create(true);
        service.copy('tmpl-1', 'Template ID');
        expect(clipboard.copy).toHaveBeenCalledWith('tmpl-1');
        expect(snackBar.open).toHaveBeenCalledWith('Template ID copied', 'OK', { duration: 2500 });
    });

    it('names the label inside the sentence when copying fails, keeping abbreviations', () => {
        const { service, snackBar } = create(false);
        service.copy('x', 'Template ID');
        service.copy('x', 'ID');
        expect(snackBar.open.calls.allArgs().map(([message]) => message)).toEqual([
            'Could not copy template ID',
            'Could not copy ID',
        ]);
    });
});
