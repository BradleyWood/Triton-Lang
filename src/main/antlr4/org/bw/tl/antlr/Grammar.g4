grammar Grammar;

file
    :
    ;




fqn
    :   IDENTIFIER
    |   fqn DOT IDENTIFIER
    ;


// tokens

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]*;

WS  :  [ \t\r\n\u000C]+ -> skip;

COMMENT :   '/*' .*? '*/' -> channel(HIDDEN);

LINE_COMMENT :   '//' ~[\r\n]* -> channel(HIDDEN);

// arithmetic

PLUS    : '+';
MINUS   : '-';
MULT    : '*';
DIV     : '/';
MOD     : '%';
POW     : '**';

ASSIGN  : '=';
PLUS_EQ : '+=';
MINUS_EQ: '-=';
MULT_EQ : '*=';
DIV_EQ  : '/=';
MOD_EQ  : '%=';
POW_EQ  : '**=';

// logical

EQUALS  : '==';
NOT_EQ  : '!=';
AND     : '&&';
OR      : '||';
NOT     : '!';
GT      : '>';
GTE     : '>=';
LT      : '<';
LTE     : '<=';

COLON   : ':';
DOT     : '.';
