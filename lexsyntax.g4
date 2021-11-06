grammar TestGrammar;
cmm:NLINE* struct* NLINE* function* NLINE* main NLINE* EOF;

expr:
    (term (PLUS | MINUS) expr | term)
;

term:
    (ID|INTVAL) (DEVIDE | MULTIPICATION) term | LPAR expr RPAR (DEVIDE | MULTIPICATION) term | LPAR (MINUS)?expr RPAR | MINUS? sizefunc |  ID | INTVAL |function_call
;

struct:
    STRUCT ID (begin_struct |NLINE+ (function | assignment | function_call | built_in )* )
;

function:
    ((LIST SHARP)* KEYWORD | KEYWORD | VOID | fptr_call) ID LPAR ( parameters? (',' parameters)*) RPAR (begin | NLINE+ (built_in | assignment | ifstatement | whileloop | dostatement | function_call | returnfunc | fptr_call))
;

main:
    MAIN LPAR RPAR (begin|NLINE+ body)
;

begin_struct :
    BEGIN NLINE+  (assignment | function  | function_call | built_in )*  END ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
;


body:
    (built_in | assignment | ifstatement  elsestatement | ifstatement | whileloop | dostatement | function_call | returnfunc | fptr_call)* getset?
    (built_in | assignment | ifstatement  elsestatement | ifstatement | whileloop | dostatement | function_call | returnfunc | fptr_call)*
;


begin:
    BEGIN NLINE+ body END ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;

dot_id:
    ID ('.'(ID | function_call ))+
;

returnfunc:
    RETURN ((MINUS)? built_in | list_declare | BOOLEANVAL | dot_id | ID |expr | INTVAL) ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;


built_in:
    displayfunc | sizefunc | appendfunc
;

displayfunc:
    DISPLAY LPAR (SIZE | APPEND)? ((expr | BOOLEANVAL | ID(DOT ID)* | list_declare(ID))(','(expr | BOOLEANVAL | ID))* ) RPAR ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;

sizefunc:
    SIZE  LPAR ID(DOT ID)* RPAR ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;

appendfunc:
    APPEND LPAR ( recursive_in_list? | ID  ) COMMA (built_in_summerized) RPAR (ID | LSQUBRACE | RSQUBRACE)* ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;

recursive_in_list :
         recursive_in_list (ID|expr) LSQUBRACE (built_in_summerized)(COMMA (built_in_summerized| LSQUBRACE built_in_summerized?(COMMA built_in_summerized)* RSQUBRACE))* RSQUBRACE   | (ID|expr) LSQUBRACE (built_in_summerized)(COMMA (built_in_summerized| LSQUBRACE built_in_summerized?(COMMA built_in_summerized)* RSQUBRACE))* RSQUBRACE
;
built_in_summerized:
    (expr | INTVAL | BOOLEANVAL | ID(DOT ID)* | list_declare )*
;

assignment:
    KEYWORD ID (',' assignment)? (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
    |ID (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
    |fptr_call ID (ASSIGNMENT expr)? ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
    |list_declare (((KEYWORD | FPTR) ID) | struct_declation) (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
    |struct_declation
    |KEYWORD (ID(','ID)*|ID) ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
    |KEYWORD ID ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
    |dot_id (ASSIGNMENT expr)?  ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;

struct_declation:
    STRUCT ID ID (',' ID)*
;

ifstatement:
    IF NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr) RPAR? (begin | NLINE+ (built_in | assignment | ifstatement elsestatement | ifstatement | whileloop | dostatement | function_call | returnfunc | fptr_call))
;
elsestatement :
        ELSE (begin | (built_in | assignment | ifstatement | whileloop | dostatement | function_call | returnfunc | fptr_call))
;
whileloop:
    WHILE NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr) RPAR? (begin | body)
;

dostatement
        :
        DO (begin | body) WHILE LPAR? (relation (and_or relation)* | BOOLEANVAL | expr) RPAR?
;

function_call:
    ID (LPAR (BOOLEANVAL | INTVAL | ID | expr )? (','(BOOLEANVAL | INTVAL | ID | expr ))*   RPAR)+ ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;

parameters:
    expr
    | STRUCT ID ID
    | list_declare (STRUCT ID ID|KEYWORD ID)
    | KEYWORD ID
;


fptr_call :
    FPTR '<' (VOID|KEYWORD|STRUCT|list_declare (ID)?)?(','VOID|','KEYWORD|','STRUCT|','list_declare)* '-''>' (VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))?(VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))*  '>'
;


list_declare:
    list_declare LIST SHARP |LIST SHARP
;

relation:
     expr relation_symbols expr
;

relation_symbols:
     SMALLER | BIGGER | EQBIGGER | EQSMAALLER | EQUAL | NEQUAL | and_or
;


getset:
    SET BEGIN NLINE+ assignment* END ((SEMICOLON | NLINE+) | SEMICOLON NLINE+) GET NLINE+ returnfunc
;

and_or:
    (AND) | (OR)
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
PLUS:'+';
MINUS:'-';
MULTIPICATION:'*';
DEVIDE:'/';
DOT:'.';
AND:'&';
OR:'|';
//OPERATOR :
  //          '+' | '-' | '*' | '/'
//;

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
WS : [ \t\r] -> skip;
NLINE : WS*'\n'WS*;
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
