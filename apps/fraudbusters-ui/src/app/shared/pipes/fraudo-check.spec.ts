import { checkFraudo, FraudoDialect, fraudoProblemsMessage } from './fraudo-check';
import { tokenizeFraudo } from './fraudo-tokens.pipe';

const check = (source: string, dialect: FraudoDialect = 'payment') => {
    const tokens = tokenizeFraudo(source);
    const { pairs } = checkFraudo(tokens, dialect);
    return { tokens, pairs, message: fraudoProblemsMessage(tokens) };
};

const problems = (source: string, dialect: FraudoDialect = 'payment') =>
    check(source, dialect)
        .tokens.filter((token) => token.problem)
        .map((token) => `${token.text.trim()}: ${token.problem}`);

describe('checkFraudo', () => {
    it('accepts valid rules', () => {
        const source = [
            '# Block risky countries',
            'rule: risky: in(countryBy("bin"), "RUS", "BLR") and amount() > 10 -> decline catch: highRisk;',
            'rule: count("card_token", 1440, "party_id") >= 3 -> 3ds;',
        ].join('\n');
        expect(problems(source)).toEqual([]);
        expect(check(source).message).toBe('');
    });

    describe('brackets', () => {
        it('marks an unclosed bracket and reports its line', () => {
            const source = 'rule: amount() > 10 -> accept;\nrule: in(countryBy("bin"), "RUS" -> decline;';
            expect(problems(source)).toEqual(['(: Unclosed "("']);
            expect(check(source).message).toBe('Unclosed "(" on line 2');
        });

        it('marks a closing bracket without an opening one', () => {
            expect(problems('rule: amount()) > 10 -> decline;')).toEqual(['): Unexpected ")"']);
        });

        it('checks each rule on its own', () => {
            // The missing ")" of the first rule is not closed by the stray ")" of the second one.
            const source = 'rule: in(amount() -> accept;\nrule: amount()) -> decline;';
            expect(problems(source)).toEqual(['(: Unclosed "("', '): Unexpected ")"']);
            expect(check(source).message).toBe('2 problems, first: Unclosed "(" on line 1');
        });

        it('ignores brackets inside strings and comments', () => {
            expect(problems('# note (\nrule: isTrusted(")(") -> accept;')).toEqual([]);
        });

        it('pairs brackets both ways for the cursor highlight', () => {
            const { tokens, pairs } = check('rule: in(amount()) -> accept;');
            const open = tokens.findIndex((token) => token.text === '(');
            const close = tokens.map((token) => token.text).lastIndexOf(')');
            expect(pairs.get(open)).toBe(close);
            expect(pairs.get(close)).toBe(open);
        });
    });

    describe('results', () => {
        it('marks an unknown result instead of showing it as a valid one', () => {
            const { tokens } = check('rule: amount() > 10 -> declin;');
            const result = tokens.find((token) => token.text === 'declin');
            expect(result.kind).toBe('plain');
            expect(result.problem).toBe('Unknown result "declin"');
        });

        it('allows only catch results after catch:', () => {
            expect(problems('rule: amount() > 10 -> accept catch: decline;')).toEqual([
                'decline: "decline" is not allowed after "catch:"',
            ]);
        });

        it('marks an arrow without a result', () => {
            expect(problems('rule: amount() > 10 -> ;')).toEqual(['->: Missing result after "->"']);
        });

        it('marks a rule without a result', () => {
            expect(problems('rule: amount() > 10;')).toEqual([';: Rule has no "->" result']);
        });
    });

    it('marks functions missing from the grammar', () => {
        expect(problems('rule: cuont("card_token", 60) > 2 -> decline;')).toEqual(['cuont: Unknown function "cuont"']);
    });

    it('keeps logical operators before a bracket out of the function check', () => {
        expect(problems('rule: NOT (amount() > 10) -> accept;')).toEqual([]);
    });

    it('runs an unclosed string to the end of the line', () => {
        const { tokens } = check('rule: inBlackList("card_token) -> decline;\nrule: amount() > 1 -> accept;');
        const string = tokens.find((token) => token.kind === 'string');
        expect(string.text).toBe('"card_token) -> decline;');
        expect(string.problem).toBe('Unclosed string');
        // The rest of the first line is inside the string, so its bracket and ";" are missing.
        expect(problems('rule: inBlackList("card_token) -> decline;')).toEqual([
            '(: Unclosed "("',
            '"card_token) -> decline;: Unclosed string',
        ]);
    });

    it('treats a doubled quote as an escaped quote, not as the end of the string', () => {
        expect(problems('rule: like("email", "a""b") -> accept;')).toEqual([]);
        expect(problems('rule: like("email", "a"") -> accept;')[0]).toContain('Unclosed');
    });

    describe('semicolons', () => {
        it('marks the end of a rule followed by another rule', () => {
            expect(problems('rule: amount() > 10 -> decline\nrule: amount() > 1 -> accept;')).toEqual([
                'decline: Missing ";"',
            ]);
        });

        it('marks the last rule without a semicolon', () => {
            expect(problems('rule: amount() > 10 -> decline')).toEqual(['decline: Missing ";"']);
        });
    });
    describe('fields', () => {
        it('accepts known fields in fields, group by and lists', () => {
            const source = [
                'rule: count("card_token", 1440, "party_id", "shop_id") > 3',
                '  and unique("email", "fingerprint", 60, hours) > 2',
                '  and inBlackList("email", "ip") and inList("vip", "card_token")',
                '  and like("email", ".*@test.com") and in(countryBy("country_bank"), "RUS", "anything")',
                '  and countError("card_token", 1440, "insufficient_funds", "party_id") > 1',
                '  and isTrusted("trusted_template") -> decline;',
            ].join('\n');
            expect(problems(source)).toEqual([]);
        });

        it('marks a mistyped field and group by field', () => {
            expect(problems('rule: count("card_tokn", 1440, "party") > 3 -> decline;')).toEqual([
                '"card_tokn": Unknown field "card_tokn"',
                '"party": Unknown field "party"',
            ]);
        });

        it('checks the fields of the call they belong to', () => {
            // "RUS" is a value of in(), "bin_country" is the field of countryBy().
            expect(problems('rule: in(countryBy("bin_country"), "RUS") -> decline;')).toEqual([
                '"bin_country": Unknown field "bin_country"',
            ]);
        });

        it('skips error codes, list names and patterns', () => {
            expect(
                problems(
                    'rule: countError("card_token", 60, "any_code") > 1 and inList("any_list", "email") and like("ip", "10.*") -> decline;'
                )
            ).toEqual([]);
        });

        it('uses the fields of the rule type', () => {
            expect(problems('rule: countPayouts("wallet_id", 60) > 1 -> decline;', 'payout')).toEqual([]);
            expect(problems('rule: count("wallet_id", 60) > 1 -> decline;')).toEqual([
                '"wallet_id": Field "wallet_id" is not available in payment rules',
            ]);
            expect(problems('rule: countPayouts("card_token", 60, "shop_id") > 1 -> decline;', 'payout')).toEqual([
                '"shop_id": Field "shop_id" is not available in payout rules',
            ]);
        });
    });
    it('applies the static payout restrictions of the server', () => {
        expect(problems('rule: isTrusted() and isMobile() -> 3ds;', 'payout')).toEqual([
            'isTrusted: "isTrusted" is not supported for payouts',
            'isMobile: "isMobile" is not supported for payouts',
            '3ds: "3ds" is not supported for payouts',
        ]);
        expect(problems('rule: isTrusted() and isMobile() -> 3ds;')).toEqual([]);
    });
});
