import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { UploadStatus } from './constants/upload-status';
import { UploadFile, ExtendedFile } from './model/upload-file';
import { PaymentLoadDataService } from '../../../api/payments/load-data';
import { ErrorHandlerService } from '../../../shared/services/utils/error-handler.service';
import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../tokens';

@Component({
    templateUrl: './fraud-uploader.component.html',
    styleUrls: ['./fraud-uploader.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FraudUploaderComponent {
    files: ExtendedFile[] = [];
    uploadFiles = new Map<string, UploadFile>();

    constructor(
        private router: Router,
        private fraudUploadService: PaymentLoadDataService,
        private errorHandlerService: ErrorHandlerService,
        private snackBar: MatSnackBar,
        @Inject(LAYOUT_GAP_L) public layoutGapL: string,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    uploadFileToActivity(): void {
        if (this.files.length === 0) {
            this.errorHandlerService.handleStringError('File for upload is not find!', this.snackBar);
        }
        this.files
            .filter((file) => !this.uploadFiles.has(file.name))
            .forEach((item) => {
                this.startProgress(this.files.indexOf(item), this.calcProgressCoef(item.size));
                this.fraudUploadService.postFile(item).subscribe(
                    () => {
                        this.finishProgress(this.files.indexOf(item), UploadStatus.Success);
                        this.uploadFiles.set(item.name, { file: item, status: UploadStatus.Success });
                    },
                    (error: HttpErrorResponse) => {
                        this.finishProgress(this.files.indexOf(item), UploadStatus.Error);
                        this.uploadFiles.set(item.name, {
                            file: item,
                            status: UploadStatus.Error,
                            errorMessage: error.message,
                        });
                    }
                );
            });
    }

    onFileDropped($event): void {
        this.prepareFilesList($event);
    }

    fileBrowseHandler(files): void {
        this.prepareFilesList(files);
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    prepareFilesList(files: Array<any>): void {
        for (const item of files) {
            if (item.type !== 'text/csv') {
                this.errorHandlerService.handleStringError('File only csv format!', this.snackBar);
            } else {
                const extendedFile = item as ExtendedFile;
                extendedFile.progress = 0;
                this.files.push(extendedFile);
            }
        }
    }

    startProgress(index: number, loadCoef: number): void {
        setTimeout(() => {
            const progressInterval = setInterval(() => {
                if (this.files[index].progress >= 100) {
                    clearInterval(progressInterval);
                } else {
                    this.files[index].progress += 5 * loadCoef;
                }
            }, 300);
        }, 300);
    }

    finishProgress(index: number, status: UploadStatus): void {
        this.files[index].progress = 100;
        this.files[index].status = status;
    }

    deleteFile(index: number): void {
        this.uploadFiles.delete(this.files[index].name);
        this.files.splice(index, 1);
    }

    formatBytes(bytes: number, decimals?: number): string {
        if (bytes === 0) {
            return '0 Bytes';
        }
        const k = 1024;
        const dm = decimals <= 0 ? 0 : decimals || 2;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    }

    calcProgressCoef(bytes): number {
        if (bytes === 0) {
            return 20;
        }
        const k = 1024;
        return k / bytes;
    }

    isLoadError(name: string): boolean {
        return !!this.uploadFiles.get(name) && this.uploadFiles.get(name).status === UploadStatus.Error;
    }

    isLoadSuccess(name: string): boolean {
        return !!this.uploadFiles.get(name) && this.uploadFiles.get(name).status === UploadStatus.Success;
    }

    back() {
        this.router.navigate([`/historical-data/fraud-payments`]);
    }
}
