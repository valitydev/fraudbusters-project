import { findArgumentFolds, previewArguments, previewLineNumbers } from './fraudo-argument-folds';

const COUNTRIES = Array.from({ length: 25 }, (_, index) => `"country-${index}"`);

describe('Fraudo argument folding', () => {
    it('keeps the leading nested call and shows how many arguments are hidden', () => {
        const source = `rule: block: in(countryBy("country_bank"), ${COUNTRIES.join(', ')}) -> decline;`;
        const folds = findArgumentFolds(source);
        expect(folds.length).toBe(1);
        const [prefix, marker, suffix] = previewArguments(source, folds);
        expect(prefix.text).toBe('rule: block: in(countryBy("country_bank"), "country-0",');
        expect(marker.text).toBe('… 24 more');
        expect(source.slice(marker.from, suffix.from).trimStart()).toMatch(/^"country-1",/);
        expect(suffix.text).toBe(') -> decline;');
    });

    it('ignores commas and parentheses inside strings and comments', () => {
        const source = `rule: in(name("a, ) and ""b"""), # commas: ,,,,,, (\n${COUNTRIES.join(', ')}) -> decline;`;
        expect(findArgumentFolds(source).map((fold) => fold.hiddenCount)).toEqual([24]);
    });

    it('keeps short and unfinished argument lists expanded', () => {
        const long = `in("name", ${COUNTRIES.join(', ')}`;
        expect(findArgumentFolds('rule: in(countryBy("country_bank"), "AFG") -> decline;')).toEqual([]);
        expect(findArgumentFolds(long)).toEqual([]);
    });

    it('omits folds hidden inside an outer folded range', () => {
        const nested = `in("name", ${COUNTRIES.join(', ')})`;
        const source = `in("first", "second", ${nested}, ${COUNTRIES.join(', ')})`;
        expect(findArgumentFolds(source).map((fold) => fold.hiddenCount)).toEqual([26]);
    });

    it('keeps source line numbers after a multiline fold', () => {
        const source = `first\nin("leading", ${COUNTRIES.join(',\n')})\nlast`;
        const parts = previewArguments(source, findArgumentFolds(source));
        expect(previewLineNumbers(parts, source)).toEqual([1, 2, COUNTRIES.length + 2]);
    });
});
