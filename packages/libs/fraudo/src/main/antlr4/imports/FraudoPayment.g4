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
 ;
