grammar Testnew;
cmm:NLINE* struct* NLINE* function* NLINE* main NLINE* EOF;


expr:
    (term (PLUS | MINUS) expr | term)
;

term:
    (ID|INTVAL) (DEVIDE | MULTIPICATION) term | LPAR expr RPAR (DEVIDE | MULTIPICATION) term | LPAR (MINUS)?expr RPAR | MINUS? sizefunc |  ID | INTVAL |func_expr
;

struct:
    STRUCT ID {System.out.println("StructDec : " + $ID.text);}(begin_struct |NLINE+ (function_instruct  | function_call | built_in | assignment )* )
;

function:
    ((LIST SHARP)* KEYWORD | VOID | fptr_func) ID {System.out.println("FunctionDec : " + $ID.text);}LPAR ( parameters? (',' parameters)*) RPAR (begin_funcs | NLINE+ (built_in  | ifstatement (elsestatement)? | whileloop | dostatement | function_call | returnfunc | fptr_call| assignment))
;

function_instruct:
    ((LIST SHARP)* KEYWORD | VOID | fptr_func) ID {System.out.println("VarDec : " + $ID.text);}LPAR ( parameters? (',' parameters)*) RPAR (begin_func_in_struct | NLINE+ (built_in  | ifstatement (elsestatement)? | whileloop | dostatement | function_call | returnfunc | fptr_call| assignment))

;

main:
{System.out.println("Main");}
    MAIN LPAR RPAR (main_begin | NLINE+ body)
;

begin_struct :
    BEGIN NLINE+  (function_instruct  | function_call | built_in | assignment | getset)*  END (NLINE+)
;


begin_funcs:
    BEGIN NLINE+ body END (NLINE+)
;

begin_func_in_struct:
    BEGIN NLINE+ body END ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
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
    RETURN {System.out.println("Return");}((MINUS)? built_in | list_declare | BOOLEANVAL |expr | dot_id | ID  | INTVAL) ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;


built_in:
    displayfunc | sizefunc | appendfunc
;

displayfunc:
    DISPLAY {System.out.println("Built-in : display");}LPAR (sizefunc | APPEND{System.out.println("Append");})? ((expr | BOOLEANVAL | ID((LSQUBRACE (ID | INTVAL) RSQUBRACE)*)?(DOT ID((LSQUBRACE (ID | INTVAL) RSQUBRACE)*)?)* | list_declare(ID((LSQUBRACE (ID | INTVAL) RSQUBRACE)*)?))(','(expr | BOOLEANVAL | ID((LSQUBRACE (ID | INTVAL) RSQUBRACE)*)?))* ) RPAR ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;

sizefunc:
    SIZE {System.out.println("Size");}  LPAR ID(DOT ID)* RPAR
;


appendfunc:
    APPEND LPAR ( recursive_in_list? | ID  ) COMMA (built_in_summerized) RPAR (ID (','ID)* | BOOLEANVAL | expr | list_declare | INTVAL | LSQUBRACE | RSQUBRACE)* ((SEMICOLON | NLINE+) |SEMICOLON NLINE+) {System.out.println("Append");}
;

recursive_in_list :
         recursive_in_list (ID|expr) LSQUBRACE (built_in_summerized)(COMMA (built_in_summerized| LSQUBRACE built_in_summerized?(COMMA built_in_summerized)* RSQUBRACE))* RSQUBRACE   | (ID|expr) LSQUBRACE (built_in_summerized)(COMMA (built_in_summerized| LSQUBRACE built_in_summerized?(COMMA built_in_summerized)* RSQUBRACE))* RSQUBRACE
;
built_in_summerized:
    (expr | INTVAL | BOOLEANVAL | ID(DOT ID)* | list_declare )*
;

assignment:
        KEYWORD ID{System.out.println("VarDec : "+$ID.text);} (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call | lparrpar))? (','ID {System.out.println("VarDec : "+$ID.text);}(ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call | lparrpar))?)* ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
       |ID ((LSQUBRACE (expr| ID | INTVAL) RSQUBRACE)*)? (ASSIGNMENT (INTVAL | BOOLEANVAL |MINUS? ID ((LSQUBRACE (expr| ID | INTVAL) RSQUBRACE)*)? | expr | function_call | lparrpar))? ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
       |fptr_func ID {System.out.println("VarDec : "+$ID.text);} (ASSIGNMENT expr)? ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
       |list_declare (((KEYWORD | FPTR) ID{System.out.println("VarDec : "+$ID.text);}) | STRUCT ID ID{System.out.println("VarDec : "+$ID.text);} (',' ID{System.out.println("VarDec : "+$ID.text);})*) (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call | lparrpar))? ((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
       |struct_declation
       |KEYWORD (ID{System.out.println("VarDec : "+$ID.text);}(','ID{System.out.println("VarDec : "+$ID.text);})*) (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call | lparrpar))? ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
       |KEYWORD ID {System.out.println("VarDec : "+$ID.text);}((SEMICOLON | NLINE+)| SEMICOLON NLINE+)
       |dot_id ((LSQUBRACE (ID | INTVAL) RSQUBRACE)*)? (ASSIGNMENT ((expr | LPAR expr (','expr)*) RPAR | function_call | BOOLEANVAL | ID ((LSQUBRACE (expr| ID | INTVAL) RSQUBRACE)*)? | INTVAL | lparrpar))?  (SEMICOLON NLINE+ | SEMICOLON | NLINE+)
;
lparrpar:
    LPAR (INTVAL | BOOLEANVAL | ID | expr | function_call) (',' (INTVAL | BOOLEANVAL | ID | expr | function_call))* RPAR
;
struct_declation:
    STRUCT ID ID{System.out.println("VarDec : "+$ID.text);} (',' ID{System.out.println("VarDec : "+$ID.text);})* ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;


condition_assignment:
    ID (ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr | function_call))
;

ifstatement:
    IF {System.out.println("Conditional : if");} NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr | condition_assignment ) RPAR? (begin | NLINE+ (built_in | ifstatement (elsestatement)?  | whileloop | dostatement | function_call | returnfunc | fptr_call | assignment))
;
elsestatement :
        ELSE {System.out.println("Conditional : else");}(begin | NLINE + (built_in |ifstatement (elsestatement)? | whileloop | dostatement | function_call | returnfunc | fptr_call | assignment))
;
whileloop:
    WHILE {System.out.println("Loop : while");}NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr | condition_assignment) RPAR? (begin | body)
;

dostatement
        :
        DO {System.out.println("Loop : do...while");}(do_begin |  NLINE + (built_in |ifstatement (elsestatement)? | whileloop | dostatement | function_call | returnfunc | fptr_call | assignment)) WHILE NOT? LPAR? (relation (and_or relation)* | BOOLEANVAL | expr | condition_assignment) RPAR? ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
;

function_call:
    ID (LPAR(BOOLEANVAL | INTVAL | ID | expr )? (','(BOOLEANVAL | INTVAL | ID | expr )) * RPAR)+ ((SEMICOLON | NLINE+) | SEMICOLON NLINE+) {System.out.println("FunctionCall");}
;

func_expr:
    ID LPAR((BOOLEANVAL | INTVAL | ID | expr )? (','(BOOLEANVAL | INTVAL | ID | expr ))* ) RPAR
;

parameters:
        expr {System.out.println("ArgumentDec : "+$expr.text);}
       | STRUCT ID  ID {System.out.println("ArgumentDec : "+$ID.text);}
       | list_declare (STRUCT ID ID {System.out.println("ArgumentDec : "+$ID.text);} |KEYWORD ID {System.out.println("ArgumentDec : "+$ID.text);})
       | KEYWORD ID {System.out.println("ArgumentDec : "+$ID.text);}
       | dot_id ((LSQUBRACE (ID {System.out.println("ArgumentDec : "+$ID.text);}| INTVAL{System.out.println("ArgumentDec : "+$INTVAL.text);}) RSQUBRACE)*)?
       | BOOLEANVAL {System.out.println("ArgumentDec : "+$BOOLEANVAL.text);}
;


fptr_call :
    FPTR '<' (VOID|KEYWORD|STRUCT|list_declare (ID)?)?(','VOID|','KEYWORD|','STRUCT|','list_declare)* '-''>' (VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))?(VOID|KEYWORD|STRUCT|list_declare* (KEYWORD | ID ))*  '>' ID {System.out.println("VarDec : "+$ID.text);} (ASSIGNMENT ID)? ((SEMICOLON | NLINE+) | SEMICOLON NLINE+)
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
     LPAR? expr relation_symbols expr RPAR?
;

relation_symbols:
     SMALLER | BIGGER | EQBIGGER | EQSMAALLER | EQUAL | NEQUAL | and_or
;


getset:
    SET {System.out.println("Setter");}BEGIN NLINE+ (assignment | function_call)* END ((SEMICOLON | NLINE+) | SEMICOLON NLINE+) GET {System.out.println("Getter");}NLINE+ returnfunc
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
