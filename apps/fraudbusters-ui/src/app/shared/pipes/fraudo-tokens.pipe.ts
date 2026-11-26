import { Pipe, PipeTransform } from '@angular/core';

import { FRAUDO_RESULTS } from './fraudo-grammar.generated';

export type FraudoTokenKind =
    | 'keyword'
    | 'operator'
    | 'string'
    | 'number'
    | 'function'
    | 'name'
    | 'result-decline'
    | 'result-accept'
    | 'result-review'
    | 'punctuation'
    | 'plain';

export interface FraudoToken {
    text: string;
    kind: FraudoTokenKind;
    /** Set by `checkFraudo` when the token breaks the rule, e.g. `Unclosed "("`. */
    problem?: string;
}

// Lexical rules follow packages/libs/fraudo (FraudoCommon.g4): STRING, RULE_BLOCK,
// CATCH_ERROR, RETURN, AND/OR/NOT, comparison operators, numbers, identifiers.
// A string without its closing quote runs to the end of the line, so the rest of the
// line is not read as code.
const TOKEN =
    /("(?:[^"\r\n]|"")*"?)|(#[^\r\n]*)|(rule:|catch:)|(->)|(3ds\b)|(-?\d+(?:\.\d+)?)|([A-Za-z_]\w*)|(>=|<=|!=|[<>=])|([(),;:])|(\s+)|(.)/g;
const LOGICAL = new Set(['AND', 'and', 'OR', 'or', 'NOT', 'not']);
const BOOLEAN = new Set(['TRUE', 'true', 'FALSE', 'false']);
const DECLINE_RESULTS = new Set(['decline', 'declineAndNotify']);
const ACCEPT_RESULTS = new Set(['accept', 'trust', 'acceptAndNotify']);

function resultKind(text: string): FraudoTokenKind {
    if (DECLINE_RESULTS.has(text)) {
        return 'result-decline';
    }
    if (ACCEPT_RESULTS.has(text)) {
        return 'result-accept';
    }
    // An unknown word after "->" must not look like a valid result.
    return FRAUDO_RESULTS.has(text) ? 'result-review' : 'plain';
}

export function tokenizeFraudo(source: string): FraudoToken[] {
    const tokens: FraudoToken[] = [];
    let previous = '';
    for (const match of source.matchAll(TOKEN)) {
        const [text, string, comment, block, arrow, threeDs, num, word, operator, punctuation, space] = match;
        const rest = source.slice(match.index + text.length).trimStart();
        let kind: FraudoTokenKind = 'plain';
        if (string) {
            kind = 'string';
        } else if (block || arrow) {
            kind = 'keyword';
        } else if (threeDs || ((previous === '->' || previous === 'catch:') && word)) {
            kind = resultKind(text);
        } else if (num) {
            kind = 'number';
        } else if (word) {
            if (LOGICAL.has(word)) {
                kind = 'keyword';
            } else if (BOOLEAN.has(word)) {
                kind = 'number';
            } else if (rest.startsWith('(')) {
                kind = 'function';
            } else if (previous === 'rule:' && rest.startsWith(':')) {
                kind = 'name';
            }
        } else if (operator) {
            kind = 'operator';
        } else if (punctuation) {
            kind = 'punctuation';
        }
        tokens.push({ text, kind });
        if (!space && !comment) {
            previous = text;
        }
    }
    return tokens;
}

@Pipe({
    name: 'fraudoTokens',
    standalone: false,
})
export class FraudoTokensPipe implements PipeTransform {
    transform(rule: string): FraudoToken[] {
        return tokenizeFraudo(rule ?? '');
    }
}
