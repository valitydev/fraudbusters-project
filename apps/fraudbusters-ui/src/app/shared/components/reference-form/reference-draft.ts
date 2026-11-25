import { Observable } from 'rxjs';

export type ReferenceScope = 'shop' | 'wallet';

/** What a reference binds to a party/scope: a rule template or a template group. */
export type ReferenceTarget = 'template' | 'group';

export interface ReferenceDraft {
    /** Template ID or group ID, depending on the form target. */
    targetId: string;
    /** Empty only on forms with an optional party: the reference then applies to every operation. */
    partyId?: string;
    scopeId?: string;
}

export type ReferenceFormConfig = {
    title: string;
    /** Rule templates by default. */
    target?: ReferenceTarget;
    scope: ReferenceScope;
    /** Party ID may stay empty (payout group references apply to all payouts without one). */
    partyOptional?: boolean;
    backUrl: string;
    loadTargetNames: (prefix: string) => Observable<string[]>;
    /** Runs once everything is saved, before going back (e.g. a confirmation message). */
    onSaved?: () => void;
} & (
    | { saveBatch: (rows: ReferenceDraft[]) => Observable<unknown> }
    | { saveRow: (row: ReferenceDraft) => Observable<unknown> }
);

export const TARGET_LABELS: Record<ReferenceTarget, { id: string; noun: string; plural: string }> = {
    template: { id: 'Template ID', noun: 'template', plural: 'rule templates' },
    group: { id: 'Group ID', noun: 'group', plural: 'template groups' },
};

/**
 * Trims a row and checks it is complete: a target is always required, a party unless the form
 * allows it to be empty, and a scope (shop/wallet) only together with a party.
 */
export function normalizeReference(
    target: string,
    party: string,
    scope = '',
    partyOptional = false
): ReferenceDraft | null {
    const targetId = target.trim();
    const partyId = party.trim() || undefined;
    const scopeId = scope.trim() || undefined;
    if (!targetId || (!partyId && !partyOptional) || (scopeId && !partyId)) {
        return null;
    }
    return { targetId, partyId, scopeId };
}

export function referenceKey({ targetId, partyId, scopeId }: ReferenceDraft): string {
    return JSON.stringify([targetId, partyId, scopeId]);
}

export function toPaymentReference({ targetId, partyId, scopeId }: ReferenceDraft) {
    return { templateId: targetId, partyId, shopId: scopeId };
}

export function toPaymentGroupReference({ targetId, partyId, scopeId }: ReferenceDraft) {
    return { groupId: targetId, partyId, shopId: scopeId };
}
