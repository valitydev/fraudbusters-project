package dev.vality.fraudo.payout.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Set;

import static dev.vality.fraudo.FraudoRulesParser.*;

/** Grammar functions that read history, grouped by the history they read from. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryFunctions {

    public static final Set<Class<? extends ParserRuleContext>> PAYMENT_HISTORY_FUNCTIONS = Set.of(
            CountContext.class, Count_successContext.class, Count_pendingContext.class, Count_errorContext.class,
            Count_chargebackContext.class, Count_refundContext.class, SumContext.class, Sum_successContext.class,
            Sum_errorContext.class, Sum_chargebackContext.class, Sum_refundContext.class, UniqueContext.class);

    public static final Set<Class<? extends ParserRuleContext>> PAYOUT_HISTORY_FUNCTIONS = Set.of(
            Count_payoutsContext.class, Count_payouts_successContext.class, Count_payouts_errorContext.class,
            Sum_payoutsContext.class, Sum_payouts_successContext.class, Sum_payouts_errorContext.class,
            Unique_payoutsContext.class);

}
