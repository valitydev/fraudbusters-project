import { WbListCandidateBatch } from '../../../../api/fb-management/swagger-codegen/model/wbListCandidateBatch';

export interface CandidateBatchRow {
    id: string;
    source: string;
    size: number;
    /** List names the batch has candidates for, e.g. EMAIL, IP. */
    fields: string[];
    createdAt?: Date;
}

/** The backend sends fields as a quoted, comma-separated string: `"EMAIL","IP"`. */
export function parseBatchFields(fields?: string): string[] {
    return (fields ?? '')
        .split(',')
        .map((field) => field.trim().replace(/^"|"$/g, '').trim())
        .filter((field) => field.length > 0 && field.toUpperCase() !== 'NULL');
}

export function candidateBatchRow(batch: WbListCandidateBatch): CandidateBatchRow {
    return {
        id: batch.id ?? '',
        source: batch.source || 'Unknown source',
        size: batch.size ?? 0,
        fields: parseBatchFields(batch.fields),
        createdAt: batch.createdAt,
    };
}
