grammar Grammar;

file
    : NL* (packageDef NL*)? imp* NL* topLevelStatement* NL* EOF
    ;

script
    : NL* (packageDef NL*)? imp* NL* (scriptStatement semi)* (scriptStatement semi?)? NL* EOF
    ;

scriptStatement
    : statement
    | functionDef
    | varDef
    ;

topLevelStatement
    : (
    functionDef
    ) semi?
    | varDef semi
    ;

statement
    : block
    | whileStatement
    | forStatement
    | expression
    | varDef
    | returnStatement
    | SEMICOLON
    ;

expression
    : LPAREN NL* wrapped=expression NL* RPAREN
    | literal
    | preceeding=expression NL* DOT NL* id=IDENTIFIER
    | name=fqn
    | preceeding=expression NL* DOT NL* call=functionCall
    | call=functionCall
    | ifStatement
    | whenExpr
    | newStatement
    | listDef
    | preceeding=expression NL* DOT NL* assignment
    | assignment
    | typeCast
    | expression indices (NL* ASSIGN NL* assign=expression)?
    | lhs=expression NL* (RANGE) NL* rhs=expression
    | (PLUS | MINUS | NOT) NL* unaryOperand=expression
    | lhs=expression NL* (POW) NL* rhs=expression
    | lhs=expression NL* (MULT | DIV | MOD) NL* rhs=expression
    | lhs=expression NL* (PLUS | MINUS) NL* rhs=expression
    | lhs=expression NL* (GT | LT | GTE | LTE | EQUALS | NOT_EQ) NL* rhs=expression
    | lhs=expression NL* (AND | OR) NL* rhs=expression
    ;

indices
    :   (NL* '[' NL* expression NL* ']')+
    ;

assignment
    :   <assoc=right>
        IDENTIFIER NL*
        (   ASSIGN
        |   PLUS_EQ
        |   MINUS_EQ
        |   MULT_EQ
        |   DIV_EQ
        |   MOD_EQ
        |   POW_EQ
        )
        NL* val=expression
    ;

typeArguments
    : '<' NL* (typeArgument (NL* ',' typeArgument)* NL*)? '>'
    ;

typeArgument
    : fqn typeArguments?
    | arrayType
    ;

listDef
    : (typeArguments NL*)? '[' NL* (expressionList NL*)? ']'
    ;

typeCast
    : '(' NL* type NL* ')' NL* expression
    ;

varDef
    : (modifierList NL*)? (type | VAR | VAL) NL* IDENTIFIER (NL* ASSIGN NL* expression)?
    ;

functionCall
    : IDENTIFIER NL* LPAREN (NL* expression (NL* COMMA NL* expression)*)? NL* RPAREN
    ;

functionDef
    : (modifierList NL*)? FUN NL* IDENTIFIER NL* LPAREN functionParamDefs? RPAREN NL* (':' NL* (VOID_T | type) NL*)?
    (block? | ('=' NL* expression))
    ;

functionParamDefs
    : functionParam (NL* COMMA NL* functionParam)*
    ;

functionParam
    : (modifierList NL*)? type NL* IDENTIFIER
    ;

type
    : primitiveType
    | fqn typeArguments?
    | arrayType
    ;

arrayType
    : (primitiveType | fqn) (NL* '[' NL* ']')+
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

whenExpr
    : WHEN NL* (LPAREN NL* expression NL* RPAREN NL*)? '{' NL* (whenCase semi)* (whenElse semi?)? '}'
    ;

whenCase
    : whenCondition NL* '->' NL* (expression | block)
    ;

whenCondition
    : expression
    | ('is' | '!is') type
    ;

whenElse
    :   ELSE NL* '->' NL* (expression | block)
    ;

whileStatement
    : WHILE NL* LPAREN condition=expression RPAREN NL* body=statement
    | DO NL* body=statement NL* WHILE NL* LPAREN condition=expression RPAREN
    ;

returnStatement
    : RETURN expression?
    ;

newStatement
    : NEW NL* fqn NL* LPAREN (expression (NL* COMMA NL* expression)*)? NL* RPAREN
    | array=NEW NL* (fqn | primitiveType) NL* ('[' NL* expression NL* ']')+
    ;

forStatement
    : FOR NL* LPAREN NL* forControl NL* RPAREN NL* ((statement semi?) | SEMICOLON)
    | FOR NL* statement
    ;

forControl
    : (modifierList NL*)? (type | VAR | VAL) NL* IDENTIFIER NL* COLON NL* expression
    | ((varDef | init=expression) NL*)? SEMICOLON NL* (condition=expression NL*)? SEMICOLON (NL* expressionList)?
    ;

localVariable
    : type localVarDefList
    ;

localVarDefList
    : localVarDef (COMMA localVarDef)*
    ;

localVarDef
    : IDENTIFIER ASSIGN expression
    ;

expressionList
    : expression (NL* COMMA NL* expression)*
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
    : INT | HEX | FLOAT
    ;

string
    : StringLiteral
    ;

fqn
    : IDENTIFIER (DOT IDENTIFIER)*
    ;

modifierList
    : modifier+
    ;

modifier
    : visibilityModifier
    | 'final'
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
IS      : 'is';
DO      : 'do';
NEW     : 'new';
FUN     : 'fun';
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
WHEN    : 'when';
FALSE   : 'false';
WHILE   : 'while';
FLOAT_T : 'float';
DOUBLE_T: 'double';
RETURN  : 'return';
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
RANGE   : '..';


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
    : Digit+ '.' Digit* ExponentPart? [fF]?
    | '.' Digit+ ExponentPart? [fF]?
    | Digit+ ExponentPart [fF]?
    | Digit+ [fF]
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
