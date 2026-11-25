/** One insertable item of the syntax panel, whatever section it comes from. */
export interface SnippetItem {
    /** Text shown in the result row. */
    label: string;
    /** Text inserted at the cursor. */
    insert: string;
    description: string;
}

export interface SnippetGroup {
    title: string;
    items: SnippetItem[];
}

/**
 * Keeps the items whose section title, label or description contain every word of the
 * query, ignoring case. Sections without matches are dropped.
 */
export function searchSnippets(groups: SnippetGroup[], query: string): SnippetGroup[] {
    const words = query.toLowerCase().split(/\s+/).filter(Boolean);
    if (!words.length) {
        return groups;
    }
    return groups
        .map(({ title, items }) => ({
            title,
            items: items.filter((item) => {
                const text = `${title} ${item.label} ${item.description}`.toLowerCase();
                return words.every((word) => text.includes(word));
            }),
        }))
        .filter((group) => group.items.length);
}
