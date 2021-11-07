grammar TestGrammar;
cmm:NLINE* struct* NLINE* function* NLINE* main NLINE* EOF;


expr:
    (term (PLUS | MINUS) expr | term)
;

term:
    (ID|INTVAL) (DEVIDE | MULTIPICATION) term | LPAR expr RPAR (DEVIDE | MULTIPICATION) term | LPAR (MINUS)?expr RPAR | MINUS? sizefunc |  ID | INTVAL |func_expr
;

struct:
    STRUCT ID (begin_struct |NLINE+ (function  | function_call | built_in | assignment )* )
;

function:
    ((LIST SHARP)* KEYWORD | KEYWORD | VOID | fptr_func) ID LPAR ( parameters? (',' parameters)*) RPAR (begin_funcs | NLINE+ (built_in  | ifstatement (elsestatement)? | whileloop | dostatement | function_call | returnfunc | fptr_call| assignment))
;

main:
    MAIN LPAR RPAR (main_begin | NLINE+ body)
;

begin_struct :
    BEGIN NLINE+  (function  | function_call | built_in | assignment )*  END (NLINE+)
;


begin_funcs:
    BEGIN NLINE+ body END (NLINE+)
;

body:
    (built_in | ifstatement (elsestatement)?  |  dostatement |whileloop | function_call | returnfunc | fptr_call | assignment)* getset?
    (built_in | ifstatement (elsestatement)?  |  dostatement |whileloop | function_call | returnfunc | fptr_call | assignment)*
;


begin:
    BEGIN NLINE+ body END ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;

main_begin:
    BEGIN NLINE+ body END
;
do_begin:
    BEGIN NLINE+ body END
;
dot_id:
    ID ('.'(ID | function_call ))+
;

returnfunc:
    RETURN ((MINUS)? built_in | list_declare | BOOLEANVAL |expr | dot_id | ID  | INTVAL) ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;


built_in:
    displayfunc | sizefunc | appendfunc
;

displayfunc:
    DISPLAY LPAR (SIZE | APPEND)? ((expr | BOOLEANVAL | ID(DOT ID)* | list_declare(ID))(','(expr | BOOLEANVAL | ID))* ) RPAR ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;

sizefunc:
    SIZE  LPAR ID(DOT ID)* RPAR
;


appendfunc:
    APPEND LPAR ( recursive_in_list? | ID  ) COMMA (built_in_summerized) RPAR (ID (','ID)* | BOOLEANVAL | expr | list_declare | INTVAL | LSQUBRACE | RSQUBRACE)* ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
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
    |list_declare (((KEYWORD | FPTR) ID) | STRUCT ID ID (',' ID)*) (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
    |struct_declation
    |KEYWORD (ID(','ID)*) (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
    |KEYWORD ID ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
    |dot_id (ASSIGNMENT ((expr | LPAR expr (','expr)*) RPAR | function_call | BOOLEANVAL | ID | INTVAL))?  (SEMICOLON NLINE+ | SEMICOLON | NLINE+)
;

struct_declation:
    STRUCT ID ID (',' ID)* ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;


condition_assignment:
    ID (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))
;

ifstatement:
    IF NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr | condition_assignment ) RPAR? (begin | NLINE+ (built_in | ifstatement (elsestatement)?  | whileloop | dostatement | function_call | returnfunc | fptr_call | assignment))
;
elsestatement :
        ELSE (begin | NLINE + (built_in |ifstatement (elsestatement)? | whileloop | dostatement | function_call | returnfunc | fptr_call | assignment))
;
whileloop:
    WHILE NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr | condition_assignment) RPAR? (begin | body)
;

dostatement
        :
        DO (do_begin |  NLINE + (built_in |ifstatement (elsestatement)? | whileloop | dostatement | function_call | returnfunc | fptr_call | assignment)) WHILE NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr | condition_assignment) RPAR? ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;

function_call:
    ID (LPAR(BOOLEANVAL | INTVAL | ID | expr )? (','(BOOLEANVAL | INTVAL | ID | expr )) * RPAR)+ ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;

func_expr:
    ID LPAR((BOOLEANVAL | INTVAL | ID | expr )? (','(BOOLEANVAL | INTVAL | ID | expr ))* ) RPAR
;

parameters:
    expr
    | STRUCT ID ID
    | list_declare (STRUCT ID ID|KEYWORD ID)
    | KEYWORD ID
;


fptr_call :
    FPTR '<' (VOID|KEYWORD|STRUCT|list_declare (ID)?)?(','VOID|','KEYWORD|','STRUCT|','list_declare)* '-''>' (VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))?(VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))*  '>' ID (ASSIGNMENT ID)? ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;

fptr_func:
    FPTR '<' (VOID|KEYWORD|STRUCT|list_declare (ID)?)?(','VOID|','KEYWORD|','STRUCT|','list_declare)* '-''>' (VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))?(VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))*  '>'
;

list_declare:
    LIST SHARP list_prime
;

list_prime:
    LIST SHARP list_prime |
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
