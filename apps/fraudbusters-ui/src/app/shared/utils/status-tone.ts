export type StatusTone = 'success' | 'danger' | 'info' | 'warning' | 'neutral';

const STATUS_TONES: Record<string, StatusTone> = {
    accept: 'success',
    accept_and_notify: 'success',
    captured: 'success',
    processed: 'success',
    succeeded: 'success',
    success: 'success',
    accepted: 'success',
    active: 'success',
    created: 'info',
    create: 'success',
    valid: 'success',
    decline: 'danger',
    decline_and_notify: 'danger',
    failed: 'danger',
    rejected: 'danger',
    remove: 'danger',
    delete: 'danger',
    three_ds: 'info',
    notify: 'info',
    normal: 'info',
    update: 'info',
    scheduled: 'info',
    pending: 'warning',
    high_risk: 'warning',
    cancelled: 'neutral',
    expired: 'neutral',
    archive: 'neutral',
    unknown: 'neutral',
};

const TONE_CLASSES: Record<StatusTone, string> = {
    success: 'fb-pill-success',
    danger: 'fb-pill-danger',
    info: 'fb-pill-info',
    warning: 'fb-pill-warning',
    neutral: '',
};

/** One colour per status meaning across the app: green passes, red blocks, blue checks, amber waits. */
export function statusTone(status?: string): StatusTone {
    return STATUS_TONES[(status ?? '').trim().toLowerCase()] ?? 'neutral';
}

/** The fb-pill modifier for a status; neutral statuses use the plain pill. */
export function statusPillClass(status?: string): string {
    return TONE_CLASSES[statusTone(status)];
}

export function tonePillClass(tone: StatusTone): string {
    return TONE_CLASSES[tone];
}
