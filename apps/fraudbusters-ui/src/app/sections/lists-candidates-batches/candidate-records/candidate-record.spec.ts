import { candidateRecordRow, matchesCandidate } from './candidate-record';
import { parseBatchFields } from '../../lists/components/candidates/candidate-batch';

describe('candidate records', () => {
    const record = {
        id: '1',
        listType: 'black' as const,
        listName: 'EMAIL',
        partyId: 'p-1',
        shopId: 's-1',
        value: 'a@example.test',
    };

    it('accepts a complete record', () => {
        expect(candidateRecordRow(record).issue).toBe('');
    });

    it('flags records that cannot be added to a list', () => {
        expect(candidateRecordRow({ ...record, value: '  ' }).issue).toBe('Value is empty');
        expect(candidateRecordRow({ ...record, listType: 'naming' }).issue).toBe('Unknown list type');
        expect(candidateRecordRow({ ...record, listName: '' }).issue).toBe('List name is empty');
        expect(candidateRecordRow({ ...record, id: undefined }).issue).toBe('No record ID');
    });

    it('matches search text on value, list name, party and shop', () => {
        const row = candidateRecordRow(record);
        expect(matchesCandidate(row, 'EXAMPLE')).toBeTrue();
        expect(matchesCandidate(row, 's-1')).toBeTrue();
        expect(matchesCandidate(row, '')).toBeTrue();
        expect(matchesCandidate(row, 'other')).toBeFalse();
    });

    it('parses quoted batch fields', () => {
        expect(parseBatchFields('"EMAIL","FINGERPRINT", "IP"')).toEqual(['EMAIL', 'FINGERPRINT', 'IP']);
        expect(parseBatchFields(undefined)).toEqual([]);
        expect(parseBatchFields('NULL')).toEqual([]);
        expect(parseBatchFields('"null","EMAIL"')).toEqual(['EMAIL']);
    });
});
