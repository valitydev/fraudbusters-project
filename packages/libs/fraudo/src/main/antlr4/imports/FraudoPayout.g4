grammar FraudoPayout;

import Fraudo;

count_payouts
 : 'countPayouts' LPAREN STRING time_window (group_by)? RPAREN
 ;

count_payouts_success
 : 'countPayoutsSuccess' LPAREN STRING time_window (group_by)? RPAREN
 ;

count_payouts_error
 : 'countPayoutsError' LPAREN STRING time_window (DELIMITER STRING)? (group_by)? RPAREN
 ;

sum_payouts
 : 'sumPayouts' LPAREN STRING time_window (group_by)? RPAREN
 ;

sum_payouts_success
 : 'sumPayoutsSuccess' LPAREN STRING time_window (group_by)? RPAREN
 ;

sum_payouts_error
 : 'sumPayoutsError' LPAREN STRING time_window (DELIMITER STRING)? (group_by)? RPAREN
 ;

unique_payouts
 : 'uniquePayouts' LPAREN STRING DELIMITER STRING time_window (group_by)? RPAREN
 ;
