import { searchSnippets, SnippetGroup } from './snippet-search';

const GROUPS: SnippetGroup[] = [
    {
        title: 'Lists & matching',
        items: [
            { label: 'inBlackList("card_token")', insert: 'inBlackList("card_token")', description: 'Black list' },
            { label: 'inGreyList("email")', insert: 'inGreyList("email")', description: 'Grey list by email' },
        ],
    },
    {
        title: 'Transaction fields',
        items: [{ label: 'card_token', insert: '"card_token"', description: 'Field' }],
    },
];

describe('searchSnippets', () => {
    it('returns every group for an empty query', () => {
        expect(searchSnippets(GROUPS, '  ')).toBe(GROUPS);
    });

    it('matches labels and descriptions ignoring case', () => {
        expect(searchSnippets(GROUPS, 'GREY').map((group) => group.items.map((item) => item.label))).toEqual([
            ['inGreyList("email")'],
        ]);
    });

    it('requires every word and lets the section title match', () => {
        const found = searchSnippets(GROUPS, 'card list');
        expect(found.map((group) => group.title)).toEqual(['Lists & matching']);
        expect(found[0].items.map((item) => item.label)).toEqual(['inBlackList("card_token")']);
        expect(searchSnippets(GROUPS, 'fields card').map((group) => group.title)).toEqual(['Transaction fields']);
    });

    it('drops groups without matches', () => {
        expect(searchSnippets(GROUPS, 'nothing-like-this')).toEqual([]);
    });
});
