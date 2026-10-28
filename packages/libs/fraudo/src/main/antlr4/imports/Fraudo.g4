grammar Fraudo;

expression
    : booleanAndExpression ( OR booleanAndExpression )*
    ;

booleanAndExpression
    : equalityExpression ( AND equalityExpression )*
    ;

equalityExpression
    : relationalExpression
    | stringExpression (EQ | NEQ) stringExpression
    | NOT expression
    | LPAREN expression RPAREN
    ;

stringExpression
    : country_by
    | currency
    | payment_system
    | card_category
    | user_id
    | STRING
    ;

unaryExpression
    : integerExpression
    | floatExpression
    ;

in
 : 'in' LPAREN (stringExpression) DELIMITER string_list RPAREN
 ;

fraud_rule
 : RULE_BLOCK (IDENTIFIER ':')? expression RETURN result (CATCH_ERROR catch_result)? SCOL
 ;

comparator
 : GT | GE | LT | LE | EQ
 ;

binary
 : AND | OR
 ;

bool
 : TRUE | FALSE
 ;

amount
 : 'amount' LPAREN RPAREN
 ;

currency
 : 'currency' LPAREN RPAREN
 ;

user_id
 : 'userId' LPAREN RPAREN
 ;

payment_system
 : 'paymentSystem' LPAREN RPAREN
 ;

card_category
 : 'cardCategory' LPAREN RPAREN
 ;

payer_type
 : 'payerType' LPAREN RPAREN
 ;

token_provider
 : 'tokenProvider' LPAREN RPAREN
 ;

rand
 : 'rand' LPAREN INTEGER RPAREN
 ;

in_white_list
 : 'inWhiteList' LPAREN string_list RPAREN
 ;

in_black_list
 : 'inBlackList' LPAREN string_list RPAREN
 ;

in_grey_list
 : 'inGreyList' LPAREN string_list RPAREN
 ;

in_list
 : 'inList' LPAREN STRING DELIMITER string_list RPAREN
 ;

like
 : 'like' LPAREN STRING DELIMITER STRING RPAREN
 ;

country_by
 : 'countryBy' LPAREN STRING RPAREN
 ;

result
 : 'trust'
 | 'accept'
 | '3ds'
 | 'highRisk'
 | 'decline'
 | 'notify'
 | 'declineAndNotify'
 | 'acceptAndNotify'
 ;

catch_result
 : 'trust'
 | 'accept'
 | '3ds'
 | 'highRisk'
 | 'notify'
 ;

string_list
 : STRING (DELIMITER STRING | WS)*
 ;

time_window
 : DELIMITER INTEGER DELIMITER INTEGER DELIMITER time_unit
 | DELIMITER INTEGER DELIMITER time_unit
 | DELIMITER INTEGER DELIMITER INTEGER
 | DELIMITER INTEGER
 ;

time_unit
 : 'minutes'
 | 'hours'
 | 'days'
 | 'calendar_months'
 | 'calendar_days'
 ;

group_by
 : DELIMITER string_list
 ;

STRING
 : '"' (~["\r\n] | '""')* '"'
 ;

DELIMITER : ',' ;

COMMENT
 : '#' ~[\r\n]* -> skip
 ;

RETURN     : '->' ;
CATCH_ERROR: 'catch:' ;
RULE_BLOCK : 'rule:' ;
AND        : 'AND' | 'and';
OR         : 'OR' | 'or' ;
NOT        : 'NOT' | 'not';
TRUE       : 'TRUE' | 'true';
FALSE      : 'FALSE' | 'false';
GT         : '>' ;
GE         : '>=' ;
LT         : '<' ;
LE         : '<=' ;
EQ         : '=' ;
NEQ        : '!=' ;
LPAREN     : '(' ;
RPAREN     : ')' ;
DECIMAL    : '-'? ('0'..'9')+ '.' ('0'..'9')+;
INTEGER    : '-'? ('0'..'9')+;
IDENTIFIER : [a-zA-Z_] [a-zA-Z_0-9]* ;
WS         : [ \u000C\t\n]+ -> skip;
SCOL       : ';';
BOOLEAN    : TRUE | FALSE;
