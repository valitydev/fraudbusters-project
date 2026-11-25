import { WbListRecord } from '../../../api/fb-management/swagger-codegen/model/wbListRecord';

export type CandidateListType = 'black' | 'white' | 'grey';

export interface CandidateRecordRow {
    id: string;
    listType?: CandidateListType;
    listName: string;
    partyId: string;
    shopId: string;
    value: string;
    /** Why the record cannot be added to a list as it is; empty when it can. */
    issue: string;
}

export const CANDIDATE_LIST_LABELS: Record<CandidateListType, string> = {
    black: 'Black',
    white: 'White',
    grey: 'Grey',
};

export function candidateRecordRow(record: WbListRecord): CandidateRecordRow {
    const listType = record.listType in CANDIDATE_LIST_LABELS ? (record.listType as CandidateListType) : undefined;
    const row = {
        id: record.id ?? '',
        listType,
        listName: record.listName?.trim() ?? '',
        partyId: record.partyId?.trim() ?? '',
        shopId: record.shopId?.trim() ?? '',
        value: record.value?.trim() ?? '',
    };
    return { ...row, issue: candidateIssue(row) };
}

function candidateIssue(row: Omit<CandidateRecordRow, 'issue'>): string {
    if (!row.id) {
        return 'No record ID';
    }
    if (!row.value) {
        return 'Value is empty';
    }
    if (!row.listType) {
        return 'Unknown list type';
    }
    return row.listName ? '' : 'List name is empty';
}

/** Case-insensitive match on value, list name, party and shop. */
export function matchesCandidate(row: CandidateRecordRow, term: string): boolean {
    const needle = term.trim().toLowerCase();
    return (
        !needle ||
        [row.value, row.listName, row.partyId, row.shopId].some((field) => field.toLowerCase().includes(needle))
    );
}
