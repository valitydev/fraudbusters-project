package dev.vality.fraudo.payout.validator;

import java.util.Set;

/**
 * Field names a payout rule may use, grouped by where the value is read from.
 *
 * @param operation      fields of the inspected payout: in, like and list functions
 * @param country        fields that countryBy can turn into a country
 * @param paymentHistory fields stored in payment history: count, sum, unique and their variants
 * @param payoutHistory  fields stored in payout history: payout functions and the grey list
 */
public record PayoutRuleFields(Set<String> operation,
                               Set<String> country,
                               Set<String> paymentHistory,
                               Set<String> payoutHistory) {
}
