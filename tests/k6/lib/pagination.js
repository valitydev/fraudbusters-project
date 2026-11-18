import { expectEqual, expectTrue, fail } from './assert.js';

export function readAllPages(fetchPage, cursorAfter, size, maxPages = 100) {
  const pages = [];
  let cursor;
  for (let index = 0; index < maxPages; index += 1) {
    const page = fetchPage(cursor);
    pages.push(page);
    if (page.items.length < size || page.items.length === 0) return pages;
    cursor = cursorAfter(page.items[page.items.length - 1], page);
    if (cursor === undefined) return pages;
  }
  return fail(`Pagination did not finish after ${maxPages} pages`);
}

export function verifyPages(pages, { size, expectedKeys, keyOf, sortKeyOf = keyOf, order, expectedCount, what }) {
  const keys = pages.flatMap((page) => page.items.map(keyOf));
  const unique = new Set(keys);
  expectEqual(unique.size, keys.length, `${what}: PG-2 no duplicates across pages`);
  expectEqual([...unique].sort(), [...expectedKeys].sort(), `${what}: PG-1 union of pages`);
  pages.slice(0, -1).forEach((page, index) => expectEqual(page.items.length, size,
    `${what}: PG-4 size of page ${index + 1}`));
  if (expectedKeys.length % size === 0) {
    expectEqual(pages[pages.length - 1].items.length, 0, `${what}: PG-5 page after an exactly full page`);
  }
  if (order) {
    const sortKeys = pages.flatMap((page) => page.items.map(sortKeyOf));
    for (let index = 1; index < sortKeys.length; index += 1) {
      const ascending = sortKeys[index - 1] <= sortKeys[index];
      expectTrue(order === 'ASC' ? ascending : sortKeys[index - 1] >= sortKeys[index],
        `${what}: PG-3 ${order} order broken between ${sortKeys[index - 1]} and ${sortKeys[index]}`);
    }
  }
  if (expectedCount !== undefined) {
    pages.forEach((page, index) => expectEqual(page.count, expectedCount, `${what}: PG-7 count on page ${index + 1}`));
  }
}

export function expectPaginated(search, { size, cursorAfter, ...expectations }) {
  const pages = readAllPages((cursor) => {
    const body = search({ size, ...cursor });
    return { items: body.result || [], count: body.count, continuationId: body.continuationId };
  }, cursorAfter, size);
  verifyPages(pages, { size, ...expectations });
  return pages;
}

export const byLastId = (last) => ({ lastId: last.id });
export const byContinuation = (last, page) => (page.continuationId
  ? { continuationId: page.continuationId }
  : undefined);
