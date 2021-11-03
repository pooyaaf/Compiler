lexer grammar lex;

expr
        :
        term + expr | term
        ;

term
        :
        ID OPERATOR term | LPAR expr RPAR OPERATOR term | LPAR expr RPAR | ID
        ;

struct
        :'struct' ID begin? (a function)*
        ;


function
        :
        (VOID | KEYWORD | lists*) ID LPAR ('' | parameter (',' parameter)*)

        ;

begin
        :'begin' a 'end'
        ;

a
        :
        KEYWORD | defstruct | do | while | if | relation | DISPLAY | SIZE | APPEND
        ;

while
        :
        'while' relation? LPAR ID RPAR begin
        ;
do
        :
        'do' (begin | a) 'while' LPAR ID relation RPAR
        ;


if
        :
        'if' LPAR relation? RPAR (begin | a)
        ;


relation
        :
        expr RELATION expr
        ;
parameter
        :
        KEYWORD ID
        ;


defstruct
        :
        'struct' ID ID
        ;



//literals :
KEYWORD :
            'int'|'bool'|'fptr'
 ;
LIST:'list';
//literals :
MAIN : 'main';
STRUCT :'struct';
DISPLAY:'display';
SIZE:'size';
APPEND:'append';
BEGIN:'begin';
END:'end';
SET:'set';
GET:'get';
RETURN : 'return';
VOID :'void';
IF:'if';
ELSE:'else';
WHILE:'while';
DO:'do';
//
ASSIGNMENT :'=';
SEMICOLON:';';
OPERATOR :
            '+' | '-' | '*' | '/'
;
RELATION :
          '&' | '|' | '~' | '==' | '>' | '<'
;
COMMA :
','
;
SHARP: '#';
LPAR :
        '('
;
RPAR :
    ')'
;
LBRACE : '{' ;
RBRACE : '}' ;
LSQUBRACE:'[';
RSQUBRACE:']';
//data types:
BOOLEAN : 'true' | 'false' | 'TRUE' | 'FALSE' |'True'|'False';
INT : '0' | [1-9][0-9]*;
//
ID :[a-zA-Z_][a-zA-Z0-9_]*;
//
NEWLINE : '\r'|'\n';
//skip
USTAR:'/*';
DSTAR:'*/';
WS : [ \t] -> skip;
JUNK : [@$`] -> skip;
