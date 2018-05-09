grammar Grammar;

file
    :
    imp* topLevelStatement* EOF
    ;

topLevelStatement
    : (
    functionDef
    ) semi?
    ;

statement
    :
    expression
    ;

expression
    :
    fqn
    ;

functionDef
    : (VOID_T | primitiveType) NL* IDENTIFIER NL* LPAREN RPAREN NL* block
    ;

type
    : primitiveType
    | fqn
    ;

primitiveType
    : INT_T
    | LONG_T
    | BYTE_T
    | FLOAT_T
    | DOUBLE_T
    ;

imp
    : IMP fqn semi?
    ;

block
    : LBR NL* (statement semi)* (statement semi?)? NL* RBR
    ;

ifStatement
    :
    IF expression block (ELSE statement)?
    ;

literal
    : number
    | bool
    | string
    | NULL
    ;

bool
    : TRUE | FALSE
    ;

number
    : INT | HEX | FLOAT | HEX_FLOAT
    ;

string
    : StringLiteral
    ;

fqn
    :   IDENTIFIER
    |   fqn DOT IDENTIFIER
    ;


// tokens


StringLiteral
    :   '"' StringCharacters? '"'
    ;
fragment
StringCharacters
    :   StringCharacter+
    ;
fragment
StringCharacter
    :   ~["\\]
    |   EscapeSequence
    ;
// §3.10.6 Escape Sequences for Character and String Literals
fragment
EscapeSequence
    :   '\\' [btnfr"'\\]
    |   OctalEscape
    |   UnicodeEscape
    ;

fragment
OctalEscape
    :   '\\' OctalDigit
    |   '\\' OctalDigit OctalDigit
    |   '\\' ZeroToThree OctalDigit OctalDigit
    ;
fragment
OctalDigit
    :   [0-7]
    ;
fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment
ZeroToThree
    :   [0-3]
    ;

IF      : 'if';
IMP     : 'import';
INT_T   : 'int';
LONG_T  : 'long';
BYTE_T  : 'byte';
NULL    : 'null';
TRUE    : 'true';
ELSE    : 'else';
FALSE   : 'false';
FLOAT_T : 'float';
DOUBLE_T: 'double';
VOID_T  : 'void';
BOOL_T  : 'boolean';
LPAREN  : '(';
RPAREN  : ')';

LBR     : '{';
RBR     : '}';


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
SEMICOLON : ';';

NL      : '\u000A' | '\u000D' '\u000A';

semi: NL+ | SEMICOLON | SEMICOLON NL+;

INT
    : Digit+
    ;

HEX
    : '0' [xX] HexDigit+
    ;

FLOAT
    : Digit+ '.' Digit* ExponentPart?
    | '.' Digit+ ExponentPart?
    | Digit+ ExponentPart
    ;

HEX_FLOAT
    : '0' [xX] HexDigit+ '.' HexDigit* HexExponentPart?
    | '0' [xX] '.' HexDigit+ HexExponentPart?
    | '0' [xX] HexDigit+ HexExponentPart
    ;

fragment
ExponentPart
    : [eE] [+-]? Digit+
    ;

fragment
HexExponentPart
    : [pP] [+-]? Digit+
    ;

fragment
Digit
    : [0-9]
    ;

fragment
HexDigit
    : [0-9a-fA-F]
    ;
