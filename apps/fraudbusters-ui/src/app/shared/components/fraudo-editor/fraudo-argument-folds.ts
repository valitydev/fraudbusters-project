export interface ArgumentFold {
    from: number;
    to: number;
    hiddenCount: number;
}

// Strings use doubled quotes in Fraudo. Skip their contents and line comments when scanning delimiters.
const TOKEN = /"(?:[^"\r\n]|"")*(?:"|(?=\r?$))|#[^\r\n]*|[A-Za-z_]\w*|[^\s]/gm;
const FUNCTION_NAME = /^[A-Za-z_]\w*$/;
const LOGICAL = /^(and|or|not)$/i;
const MIN_ARGUMENT_COUNT = 8;
const MIN_ARGUMENT_LENGTH = 120;

interface Parentheses {
    from: number;
    isCall: boolean;
    commas: number;
    foldFrom?: number;
}

export function findArgumentFolds(source: string): ArgumentFold[] {
    if (source.length < MIN_ARGUMENT_LENGTH) {
        return [];
    }

    const stack: Parentheses[] = [];
    const candidates: ArgumentFold[] = [];
    let previous = '';
    for (const match of source.matchAll(TOKEN)) {
        const token = match[0];
        if (token.startsWith('#')) {
            continue;
        }
        const offset = match.index;
        const current = stack[stack.length - 1];
        if (token === '(') {
            stack.push({
                from: offset + 1,
                isCall: FUNCTION_NAME.test(previous) && !LOGICAL.test(previous),
                commas: 0,
            });
        } else if (token === ',' && current) {
            current.commas++;
            if (current.commas === 2) {
                current.foldFrom = offset + 1;
            }
        } else if (token === ')' && current) {
            stack.pop();
            const count = current.commas + 1;
            if (current.isCall && count >= MIN_ARGUMENT_COUNT && offset - current.from >= MIN_ARGUMENT_LENGTH) {
                candidates.push({ from: current.foldFrom, to: offset, hiddenCount: count - 2 });
            }
        }
        previous = token;
    }

    // A nested fold is shown only if its arguments are outside an already folded range.
    const folds: ArgumentFold[] = [];
    let end = 0;
    for (const fold of candidates.sort((a, b) => a.from - b.from)) {
        if (fold.from >= end) {
            folds.push(fold);
            end = fold.to;
        }
    }
    return folds;
}

export interface ArgumentPreviewPart {
    from: number;
    text: string;
    fold?: ArgumentFold;
}

export function previewArguments(source: string, folds: readonly ArgumentFold[]): ArgumentPreviewPart[] {
    let offset = 0;
    const parts: ArgumentPreviewPart[] = [];
    for (const fold of folds) {
        parts.push(
            { from: offset, text: source.slice(offset, fold.from) },
            { from: fold.from, text: `… ${fold.hiddenCount} more`, fold }
        );
        offset = fold.to;
    }
    parts.push({ from: offset, text: source.slice(offset) });
    return parts;
}

export function previewLineNumbers(parts: readonly ArgumentPreviewPart[], source: string): number[] {
    const numbers = [1];
    let line = 1;
    for (const part of parts) {
        if (part.fold) {
            line += source.slice(part.fold.from, part.fold.to).split('\n').length - 1;
            continue;
        }
        for (const _newline of part.text.matchAll(/\n/g)) {
            numbers.push(++line);
        }
    }
    return numbers;
}
