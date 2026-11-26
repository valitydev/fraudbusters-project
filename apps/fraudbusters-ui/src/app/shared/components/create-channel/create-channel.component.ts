import { HttpErrorResponse } from '@angular/common/http';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { ChannelService } from './services/channel.service';
import { Channel } from '../../../api/fb-management/swagger-codegen/model/channel';
import { ChannelType } from '../../../api/fb-management/swagger-codegen/model/channelType';

/** Channel form with its page header, used by the New and Edit channel routes. */
@Component({
    selector: 'fb-create-channel',
    templateUrl: './create-channel.component.html',
    styleUrls: ['../form-page/form-page.scss', '../create-notification/create-notification.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [ChannelService],
    standalone: false,
})
export class CreateChannelComponent implements OnChanges {
    @Input() channel?: Channel;
    @Input() editing = false;
    @Input() loading = false;
    @Input() loadFailed = false;
    @Output() retry = new EventEmitter<void>();

    form = this.channelService.form;
    channelTypes = Object.values(ChannelType);

    saving = false;
    error = '';

    constructor(
        private router: Router,
        private channelService: ChannelService,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnChanges({ channel }: SimpleChanges) {
        if (channel && this.channel) {
            const { name, type, destination } = this.channel;
            this.form.setValue({ name, type, destination });
            // The channel name is its ID and cannot be changed after creation.
            this.form.controls.name.disable();
        }
    }

    saveChannel() {
        if (this.saving) {
            return;
        }
        const { name, type, destination } = this.form.getRawValue();
        this.saving = true;
        this.error = '';
        this.channelService
            .save({ name, type, destination, createdAt: new Date(Date.now()) })
            .pipe(
                finalize(() => {
                    this.saving = false;
                    this.cdr.markForCheck();
                })
            )
            .subscribe({
                next: () => {
                    this.snackBar.open(this.editing ? 'Channel saved' : 'Channel created', 'OK', { duration: 3000 });
                    if (!this.editing) {
                        this.back();
                    }
                },
                error: ({ status }: HttpErrorResponse) => {
                    this.error = `Could not save the channel${status ? ` (${status})` : ''}. Try again.`;
                },
            });
    }

    back() {
        void this.router.navigate(['/notifications/channels']);
    }
}
