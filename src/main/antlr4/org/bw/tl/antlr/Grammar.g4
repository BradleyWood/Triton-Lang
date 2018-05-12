grammar Grammar;

file
    : NL* (packageDef NL*)? imp* NL* topLevelStatement* NL* EOF
    ;

topLevelStatement
    : (
    functionDef
    ) semi?
    | varDef semi
    ;

statement
    : block
    | ifStatement
    | whileStatement
    | forStatement
    | expression
    | varDef
    ;

expression
    : LPAREN NL* expression NL* RPAREN
    | literal
    | expression NL* DOT NL* fqn
    | fqn
    | preceeding=expression NL* DOT NL* functionCall
    | functionCall
    | (PLUS | MINUS | NOT) NL* expression
    | LPAREN NL* expression NL* RPAREN
    | expression NL* (POW) NL* expression
    | expression NL* (MULT | DIV | MOD) NL* expression
    | expression NL* (PLUS | MINUS) NL* expression
    | expression NL* (GT | LT | GTE | LTE | EQUALS | NOT_EQ) NL* expression
    | expression NL* (AND | OR) NL* expression
    | assignment
    ;

assignment
    : <assoc=right>
        fqn NL*
        (   ASSIGN
        |   PLUS_EQ
        |   MINUS_EQ
        |   MULT_EQ
        |   DIV_EQ
        |   MOD_EQ
        |   POW_EQ
        )
        NL* expression
    ;

varDef
    : modifierList? (type | VAR | VAL) NL* IDENTIFIER (ASSIGN NL* expression)?
    ;

functionCall
    : IDENTIFIER NL* LPAREN (expression (NL* COMMA NL* expression)*)? NL* RPAREN
    ;

functionDef
    : modifierList? (VOID_T | primitiveType) NL* IDENTIFIER NL* LPAREN functionParamDefs? RPAREN NL* block
    ;

functionParamDefs
    : functionParam (NL* COMMA NL* functionParam)*
    ;

functionParam
    : modifierList? type NL* IDENTIFIER
    ;

type
    : primitiveType
    | fqn
    ;

primitiveType
    : INT_T
    | LONG_T
    | BOOL_T
    | BYTE_T
    | FLOAT_T
    | DOUBLE_T
    ;

packageDef
    : PACKAGE fqn semi
    ;

imp
    : IMP fqn semi
    ;

block
    : LBR NL* (statement semi)* (statement semi?)? NL* RBR
    ;

ifStatement
    : IF NL* LPAREN condition=expression RPAREN NL* body=statement SEMICOLON?
    (NL* ELSE NL* else_=statement)?
    ;

whileStatement
    : WHILE NL* LPAREN condition=expression RPAREN NL* body=statement
    | DO NL* body=statement NL* WHILE NL* LPAREN condition=expression RPAREN
    ;

forStatement
    : FOR NL* forControl statement semi?
    ;

forControl
    : LPAREN NL* forControl NL* RPAREN
    | (type | VAR | VAL) NL* IDENTIFIER NL* COLON NL* expression
    ; // todo;

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
    : IDENTIFIER
    | fqn DOT IDENTIFIER
    ;

modifierList
    : modifier+
    ;

modifier
    : visibilityModifier
    ;

visibilityModifier
    : PUBLIC
    | PRIVATE
    | PROTECT
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
DO      : 'do';
VAR     : 'var';
VAL     : 'val';
FOR     : 'for';
IMP     : 'import';
INT_T   : 'int';
LONG_T  : 'long';
BYTE_T  : 'byte';
NULL    : 'null';
TRUE    : 'true';
ELSE    : 'else';
FALSE   : 'false';
WHILE   : 'while';
FLOAT_T : 'float';
DOUBLE_T: 'double';
PUBLIC  : 'public';
PRIVATE : 'private';
PACKAGE : 'package';
PROTECT : 'protected';
VOID_T  : 'void';
BOOL_T  : 'boolean';
LPAREN  : '(';
RPAREN  : ')';

LBR     : '{';
RBR     : '}';


IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]*;

WS  : [\u0020\u0009\u000C] -> skip;

NL: '\u000A' | '\u000D' '\u000A' ;

semi: NL+ | SEMICOLON | SEMICOLON NL+;

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
COMMA   : ',';
SEMICOLON : ';';

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
