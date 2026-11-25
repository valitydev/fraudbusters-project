/** Both Fraudo validators prefix positioned errors with "line N:", optionally followed by a column. */
export function fraudoValidationLines(message: string): number[] {
    const lines = Array.from(message.matchAll(/^\s*line\s+(\d+)\s*:/gim), (match) => Number(match[1]));
    return [...new Set(lines.filter((line) => Number.isSafeInteger(line) && line > 0))].sort((a, b) => a - b);
}
