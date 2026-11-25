/**
 * Which string arguments of each Fraudo function name a field, by the position of the
 * string among the call's own string arguments (numbers and time units do not count).
 * Taken from the grammar (packages/libs/fraudo) and the visitors that resolve these
 * strings with a FieldResolver; fraudo-field-arguments.spec.ts fails when the grammar
 * gains a function that is missing here.
 */
type FieldArgument = (stringIndex: number) => boolean;

const EVERY: FieldArgument = () => true;
const FIRST: FieldArgument = (index) => index === 0;
// "*Error" functions: the string after the time window is an error code, then group fields.
const EXCEPT_ERROR_CODE: FieldArgument = (index) => index !== 1;
// inList: the first string is the list name.
const EXCEPT_LIST_NAME: FieldArgument = (index) => index > 0;
const NONE: FieldArgument = () => false;

export const FRAUDO_FIELD_ARGUMENTS: Readonly<Record<string, FieldArgument>> = {
    amount: NONE,
    cardCategory: NONE,
    count: EVERY,
    countChargeback: EVERY,
    countError: EXCEPT_ERROR_CODE,
    countPayouts: EVERY,
    countPayoutsError: EXCEPT_ERROR_CODE,
    countPayoutsSuccess: EVERY,
    countPending: EVERY,
    countRefund: EVERY,
    countSuccess: EVERY,
    countryBy: FIRST,
    currency: NONE,
    // in(field, "value", ...): the values are not fields.
    in: FIRST,
    inBlackList: EVERY,
    inGreyList: EVERY,
    inList: EXCEPT_LIST_NAME,
    inWhiteList: EVERY,
    isMobile: NONE,
    isRecurrent: NONE,
    // isTrusted("template"): a template name.
    isTrusted: NONE,
    // like(field, "pattern").
    like: FIRST,
    payerType: NONE,
    paymentSystem: NONE,
    rand: NONE,
    sum: EVERY,
    sumChargeback: EVERY,
    sumError: EXCEPT_ERROR_CODE,
    sumPayouts: EVERY,
    sumPayoutsError: EXCEPT_ERROR_CODE,
    sumPayoutsSuccess: EVERY,
    sumRefund: EVERY,
    sumSuccess: EVERY,
    tokenProvider: NONE,
    unique: EVERY,
    uniquePayouts: EVERY,
    userId: NONE,
};
