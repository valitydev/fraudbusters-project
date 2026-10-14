import { UploadStatus } from '../constants/upload-status';

export interface UploadFile {
    file: File;
    status: UploadStatus;
    errorMessage?: string;
}

export interface ExtendedFile extends File {
    progress?: number;
    status?: UploadStatus;
}
