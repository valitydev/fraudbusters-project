grammar FraudoRules;

import Fraudo, FraudoPaymentFunctions, FraudoPayout;

// Entry point shared by all operation types.
parse
 : fraud_rule* EOF
 ;

relationalExpression
    : unaryExpression (LT | LE | GT | GE | EQ | NEQ) unaryExpression
    | in
    | in_white_list
    | in_black_list
    | in_grey_list
    | in_list
    | like
    | is_mobile
    | is_recurrent
    | is_trusted
    ;

integerExpression
    : count
    | count_payouts
    | count_payouts_success
    | count_payouts_error
    | unique_payouts
    | count_pending
    | count_success
    | count_error
    | count_chargeback
    | count_refund
    | unique
    | rand
    | INTEGER
    ;

floatExpression
    : sum
    | sum_payouts
    | sum_payouts_success
    | sum_payouts_error
    | sum_success
    | sum_error
    | sum_chargeback
    | sum_refund
    | amount
    | DECIMAL
    ;
