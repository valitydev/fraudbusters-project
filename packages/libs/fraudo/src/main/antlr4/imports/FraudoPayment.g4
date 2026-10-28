grammar FraudoPayment;

import FraudoCommon;

count
 : 'count' LPAREN STRING time_window (group_by)? RPAREN
 ;

sum
 : 'sum' LPAREN STRING time_window (group_by)? RPAREN
 ;

unique
 : 'unique' LPAREN STRING DELIMITER STRING time_window (group_by)? RPAREN
 ;

count_success
 : 'countSuccess' LPAREN STRING time_window (group_by)? RPAREN
 ;

count_pending
 : 'countPending' LPAREN STRING time_window (group_by)? RPAREN
 ;

count_error
 : 'countError' LPAREN STRING time_window (DELIMITER STRING)? (group_by)? RPAREN
 ;

count_chargeback
 : 'countChargeback' LPAREN STRING time_window (group_by)? RPAREN
 ;

count_refund
 : 'countRefund' LPAREN STRING time_window (group_by)? RPAREN
 ;

sum_success
 : 'sumSuccess' LPAREN STRING time_window (group_by)? RPAREN
 ;

sum_error
 : 'sumError' LPAREN STRING time_window (DELIMITER STRING)? (group_by)? RPAREN
 ;

sum_chargeback
 : 'sumChargeback' LPAREN STRING time_window (group_by)? RPAREN
 ;

sum_refund
 : 'sumRefund' LPAREN STRING time_window (group_by)? RPAREN
 ;

is_mobile
 : 'isMobile' LPAREN RPAREN
 ;

is_recurrent
 : 'isRecurrent' LPAREN RPAREN
 ;

is_trusted
 : 'isTrusted' LPAREN RPAREN                                                        #isTrusted
 | 'isTrusted' LPAREN STRING RPAREN                                                 #isTrustedTemplateName
 | 'isTrusted' LPAREN (payment_conditions | withdrawal_conditions) RPAREN           #isTrustedConditionsSingleList
 | 'isTrusted' LPAREN payment_conditions DELIMITER withdrawal_conditions RPAREN     #isTrustedPaymentsAndWithdrawalConditions
 ;

payment_conditions
 : 'paymentsConditions' LPAREN conditions_list RPAREN
 ;

withdrawal_conditions
 : 'withdrawalsConditions' LPAREN conditions_list RPAREN
 ;

conditions_list
 : trusted_token_condition (DELIMITER trusted_token_condition | WS)*
 ;

trusted_token_condition
 : 'condition' LPAREN
        STRING DELIMITER            //transactions_currency
        INTEGER DELIMITER           //transactions_years_offset
        INTEGER DELIMITER           //transactions_count
        INTEGER                     //transactions_sum
    RPAREN
 | 'condition' LPAREN
        STRING DELIMITER            //transactions_currency
        INTEGER DELIMITER           //transactions_years_offset
        INTEGER                     //transactions_count
    RPAREN
 ;
