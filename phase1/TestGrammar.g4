grammar TestGrammar;
cmm:struct* function* main EOF;

expr:
    (term + expr | term)
;

term:
    (ID|INTVAL) operation_symbols term | LPAR expr RPAR operation_symbols term | LPAR (MINUS)?expr RPAR | ID | INTVAL
;

struct:
    STRUCT ID (begin_struct | (function | assignment | function_call | built_in )* )
;

function:
    ((LIST SHARP)* KEYWORD | KEYWORD | VOID | fptr_call) ID LPAR ( parameters? (',' parameters)*) RPAR (begin|body)
;

main:
    MAIN LPAR RPAR (begin|body)
;

begin_struct :
    BEGIN   (assignment | function  | function_call | built_in )*  END
;


body:
    (built_in | assignment | ifstatement | elsestatement | whileloop | dostatement | function_call | returnfunc | fptr_call)* getset?
    (built_in | assignment | ifstatement | elsestatement | whileloop | dostatement | function_call | returnfunc | fptr_call)*
;


begin:
    BEGIN body END
;

dot_id:
    ID ('.'ID)+
;

returnfunc:
    RETURN (list_declare | BOOLEANVAL | dot_id | ID |expr | INTVAL) SEMICOLON?
;


built_in:
    DISPLAY LPAR (SIZE | APPEND)? ((expr | BOOLEANVAL | ID(DOT ID)* | list_declare(ID))(','(expr | BOOLEANVAL | ID))* ) RPAR SEMICOLON?
    |SIZE  LPAR ID(DOT ID)* RPAR SEMICOLON?
    |APPEND LPAR ( recursive_in_list? | ID  ) COMMA (built_in_summerized) RPAR SEMICOLON?
;
recursive_in_list :
         recursive_in_list (ID|expr) LSQUBRACE (built_in_summerized)(COMMA (built_in_summerized| LSQUBRACE built_in_summerized?(COMMA built_in_summerized)* RSQUBRACE))* RSQUBRACE   | (ID|expr) LSQUBRACE (built_in_summerized)(COMMA (built_in_summerized| LSQUBRACE built_in_summerized?(COMMA built_in_summerized)* RSQUBRACE))* RSQUBRACE
;
built_in_summerized:
    (expr| INTVAL | BOOLEANVAL | ID(DOT ID)* | list_declare)*
;

assignment:
    KEYWORD ID (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? SEMICOLON?
    |ID (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? SEMICOLON?
    |fptr_call ID (ASSIGNMENT expr)? SEMICOLON?
    |list_declare (((KEYWORD | FPTR) ID) | struct_declation) (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))? SEMICOLON?
    |struct_declation
    |KEYWORD (ID(','ID)*|ID) SEMICOLON?
    |KEYWORD ID SEMICOLON?
    |dot_id (ASSIGNMENT expr)?  SEMICOLON?
;

struct_declation:
    STRUCT ID ID (',' ID)*
;

ifstatement:
    IF NOT? LPAR? (relation | BOOLEANVAL | expr) RPAR? (begin | body)
;
elsestatement :
        ELSE ( begin | body)
;
whileloop:
    WHILE NOT? LPAR? (relation | BOOLEANVAL | expr) RPAR? (begin | body)
;

dostatement
        :
        DO (begin | body) WHILE LPAR? (relation | BOOLEANVAL | expr) RPAR?
;

function_call:
    ID LPAR (BOOLEANVAL | INTVAL | ID | expr )? (','(BOOLEANVAL | INTVAL | ID | expr ))*   RPAR SEMICOLON?
;

parameters:
    expr
    | STRUCT ID ID
    | list_declare KEYWORD ID
    | KEYWORD ID
;


fptr_call :
    FPTR '<' (KEYWORD|STRUCT|list_declare (ID)?)?(','KEYWORD|','STRUCT|','list_declare)* '-''>' (KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))  '>'
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


getset:
    SET BEGIN assignment* END GET returnfunc
;

operation_symbols:
    PLUS | MINUS | MULTIPICATION | DEVIDE
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
