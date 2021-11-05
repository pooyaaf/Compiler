grammar TestGrammar;
cmm:( struct* | function*) main EOF;

expr:
    (term + expr | term)
;

term:
    (ID|INTVAL) OPERATOR term | LPAR expr RPAR OPERATOR term | LPAR expr RPAR | ID | INTVAL
;

struct:
    STRUCT ID (begin_struct | (function | assignment | function_call | built_in )* )
;

function:
    ((LIST SHARP)* KEYWORD | KEYWORD | VOID) ID LPAR ( parameters? (',' parameters)*) RPAR (begin|body)
;

main:
    MAIN LPAR RPAR (begin|body)
;

begin_struct :
    BEGIN   (function | assignment | function_call | built_in )*  END
;


body:
    (built_in | assignment | ifstatement | whileloop | dostatement | function_call | returnfunc)*
;


begin:
    BEGIN body END
;

returnfunc:
    RETURN (INTVAL | BOOLEANVAL | (ID'.'ID) | ID |expr)
;


built_in:
    DISPLAY LPAR (expr | BOOLEANVAL)
    |SIZE  LPAR ID RPAR
    |APPEND LPAR ( ID | list_declare) COMMA ( list_declare | BOOLEANVAL | ID | INTVAL ) RPAR
;

assignment:
    KEYWORD ID ASSIGNMENT ((INTVAL | BOOLEANVAL | ID)',')*
    |ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr )
    |fptr_call (ASSIGNMENT ID)?
    |list_declare (((KEYWORD | FPTR) ID) | struct_declation)
    |struct_declation
    |KEYWORD (ID(','ID)*|ID)
;

struct_declation:
    STRUCT ID ID
;

ifstatement:
    IF NOT? LPAR (relation | BOOLEANVAL | expr) RPAR (begin | body)
;

whileloop:
    WHILE NOT? LPAR (relation | BOOLEANVAL | expr) RPAR (begin | body)
;

dostatement
        :
        DO (begin | body) WHILE LPAR relation RPAR
;

function_call:
    ID LPAR (BOOLEANVAL | INTVAL | ID)* RPAR SEMICOLON?
;

parameters:
    expr
    | STRUCT ID ID
    | list_declare KEYWORD ID
    | KEYWORD ID
;


fptr_call :
    FPTR '<' (KEYWORD|STRUCT|list_declare)?(','KEYWORD|','STRUCT|','list_declare)* '-''>' (KEYWORD|STRUCT|LIST)  '>' ID
;


list_declare:
    list_declare LIST SHARP |LIST SHARP
;

relation:
    expr relation_symbols expr
;

relation_symbols:
    SMALLER | BIGGER | EQBIGGER | EQSMAALLER | EQUAL | NEQUAL
;


KEYWORD :
            'int'|'bool'
 ;
FPTR:'fptr';
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

//RELATION :
 //         '<'
//;
BIGGER: '>';
SMALLER: '<';
EQBIGGER: '>=';
EQSMAALLER: '<=';
EQUAL: '==';
NEQUAL: '!=';


NOT:
'~'
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
BOOLEANVAL : 'true' | 'false' | 'TRUE' | 'FALSE' |'True'|'False';
INTVAL : '0' | [1-9][0-9]*;
//
ID :[a-zA-Z_][a-zA-Z0-9_]*;
//
//NEWLINE : '\r'|'\r\n';
//skip
BlockComment
    :   '/*' .*? '*/'
        -> skip
    ;
WS : [ \t\r\n] -> skip;
//NLINE : '\n';

JUNK : [@$`] -> skip;

//prog:(statement)+EOF;
//statement:assignment SEMICOLON;
//assignment:KEYWORD ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID);
/*tokens*/
//KEYWORD:'int'|'boolean';
//ASSIGNMENT:':=';
//SEMICOLON:';';
//INTVAL:'0'|[1-9][0-9]+;
//BOOLEANVAL:'true'|'false';
//ID:[a-zA-Z_][A-Za-z0-9_]*;
