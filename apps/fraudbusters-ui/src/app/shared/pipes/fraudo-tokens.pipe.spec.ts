import { tokenizeFraudo } from './fraudo-tokens.pipe';

const kinds = (source: string) =>
    tokenizeFraudo(source)
        .filter((token) => token.text.trim())
        .map((token) => `${token.text}:${token.kind}`);

describe('tokenizeFraudo', () => {
    it('keeps the source text intact', () => {
        const source = 'rule: inBlackList("card_token") -> decline;';
        expect(
            tokenizeFraudo(source)
                .map((token) => token.text)
                .join('')
        ).toBe(source);
    });

    it('marks keywords, functions, strings and the decline result', () => {
        expect(kinds('rule: inBlackList("card_token") -> decline;')).toEqual([
            'rule::keyword',
            'inBlackList:function',
            '(:punctuation',
            '"card_token":string',
            '):punctuation',
            '->:keyword',
            'decline:result-decline',
            ';:punctuation',
        ]);
    });

    it('marks rule names, logical operators, numbers and catch results', () => {
        expect(kinds('rule: big_amount: amount() >= 10.5 AND count("email", 1440) > 3 -> 3ds catch: accept;')).toEqual([
            'rule::keyword',
            'big_amount:name',
            '::punctuation',
            'amount:function',
            '(:punctuation',
            '):punctuation',
            '>=:operator',
            '10.5:number',
            'AND:keyword',
            'count:function',
            '(:punctuation',
            '"email":string',
            ',:punctuation',
            '1440:number',
            '):punctuation',
            '>:operator',
            '3:number',
            '->:keyword',
            '3ds:result-review',
            'catch::keyword',
            'accept:result-accept',
            ';:punctuation',
        ]);
    });
    it('highlights outcomes after Fraudo comments', () => {
        const source = 'rule: amount() > 0 -> # outcome follows\naccept catch: # fallback follows\n3ds;';
        const outcomes = tokenizeFraudo(source).filter(({ kind }) => kind.startsWith('result-'));
        expect(outcomes).toEqual([
            { text: 'accept', kind: 'result-accept' },
            { text: '3ds', kind: 'result-review' },
        ]);
    });
});
