grammar Testnew;

cmm:NLINE* struct* NLINE* function* NLINE* main NLINE* EOF;

struct:
    STRUCT ID {System.out.println("StructDec : " + $ID.text);}(struct_begin |NLINE+ struct_body )
;

function:
    (list_star? (KEYWORD | fptr_func | ID ) | VOID) ID {System.out.println("FunctionDec : " + $ID.text);}
     LPAR argument_in_func_dec RPAR(func_begin | NLINE+ func_body)
;

func_body:
    main_body | return_func
;

func_begin:
    BEGIN NLINE+ func_body* END NLINE+
;

main:
    {System.out.println("Main");}
    MAIN LPAR RPAR (main_begin | NLINE+ main_body)
;
//main return?
main_body:
    ((ifstatement elsestatement?) | whileloop | statement | dostatement )
;

main_begin:
    BEGIN NLINE+ main_body* END NLINE+
;

struct_body:
    (function_in_struct | statement | setget )
;

struct_begin :
    BEGIN NLINE+  struct_body*  END NLINE+
;

function_in_struct:
    (list_star? (KEYWORD | fptr_func | ID ) | VOID) ID
    {System.out.println("VarDec : " + $ID.text);}
    LPAR argument_in_func_dec RPAR BEGIN NLINE+ ((setget|func_body)  )*  END  nlinesemi
;

statement:
    (display | assignment | vardec | function_call_in_statement |expr)  nlinesemi
;

vardec:
    (list_star? (KEYWORD | fptr_func | STRUCT ID )) (ID {System.out.println("VarDec : " + $ID.text);} ('=' expr)?)
    (','ID {System.out.println("VarDec : " + $ID.text);} ('=' expr)?)*
;

assignment:
    expr '=' LPAR? expr (','expr)* RPAR?

;

ifstatement:
    IF {System.out.println("Conditional : if");} LPAR? expr RPAR?  (begin_state_for_conditions |NLINE+ func_body)
;

elsestatement:
    ELSE {System.out.println("Conditional : else");} (begin_state_for_conditions|NLINE+ func_body)
;

whileloop:
    WHILE {System.out.println("Loop : while");} LPAR? expr RPAR? (begin_state_for_conditions  |NLINE+ func_body)
;

dostatement:
    DO {System.out.println("Loop : do...while");} (begin_state_for_conditions |NLINE+ func_body) WHILE LPAR? expr RPAR? nlinesemi
;

begin_state_for_conditions:
    BEGIN NLINE+ func_body* END nlinesemi
;


expr:
    expr '|' orExpr { System.out.println("Operator : |"); }
    |orExpr
;

orExpr:
    orExpr '&' andExpr { System.out.println("Operator : &"); }
    |andExpr
;

andExpr:
    andExpr '+' symbolsExpr { System.out.println("Operator : +"); }
    |andExpr '-' symbolsExpr { System.out.println("Operator : -"); }
    |andExpr '*' symbolsExpr { System.out.println("Operator : *"); }
    |andExpr '/' symbolsExpr { System.out.println("Operator : /"); }
    |symbolsExpr
;

symbolsExpr:
    symbolsExpr '==' eqExpr { System.out.println("Operator : =="); }
    |eqExpr
;

eqExpr:
    eqExpr '>' ineqExpr { System.out.println("Operator : >"); }
    |eqExpr '<' ineqExpr { System.out.println("Operator : <"); }
    |ineqExpr
;

ineqExpr:
    '~' tilda { System.out.println("Operator : ~"); }
    |tilda
;

tilda:  appendex append  | append | appendex
;

appendex:
      sizeex r=MINUS?  size { System.out.println("Operator : "+$r.text); }| r=MINUS?  size { System.out.println("Operator : "+$r.text); } | sizeex
;


sizeex:
    sizeex LSQUBRACE expr RSQUBRACE
    |types LSQUBRACE expr RSQUBRACE
    |types
;

types: LPAR expr RPAR | (MINUS { System.out.println("Operator : -"); })? function_call_in_expr |
    KEYWORD | (MINUS{ System.out.println("Operator : -"); })?(INTVAL | dot_id | BOOLEANVAL)| (LSQUBRACE expr (','expr)* RSQUBRACE | LSQUBRACE RSQUBRACE)

;

function_call_in_expr:
     dot_id LPAR argument_in_func_call RPAR
;

fptr_func:
    FPTR '<' types_of_return (','types_of_return)* '-''>' types_of_return (','types_of_return)* '>'
;

display:
    DISPLAY {System.out.println("Built-in : display");} LPAR expr RPAR
;

append:
    APPEND {System.out.println("Append");} LPAR expr COMMA (expr | LSQUBRACE expr (COMMA expr)* RSQUBRACE) RPAR
;

size:
    SIZE {System.out.println("Size"); } LPAR expr RPAR
;
setget:
    SET {System.out.println("Setter");}BEGIN? NLINE+ statement* (END nlinesemi)?
    GET BEGIN? {System.out.println("Getter");}NLINE+ return_func (END nlinesemi)?
;

function_call_in_statement:
    ID LPAR argument_in_func_call RPAR {System.out.println("FunctionCall");}(LPAR argument_in_func_call RPAR)*
;

argument_in_func_call:
    expr? (','expr)*
;

argument_in_func_dec:
     types_of_arg_dec (','types_of_arg_dec)*

;

return_func:
    RETURN { System.out.println("Return"); } expr (LSQUBRACE (INTVAL | BOOLEANVAL | ID) RSQUBRACE)* nlinesemi
    |RETURN { System.out.println("Return"); } (LSQUBRACE (INTVAL | BOOLEANVAL | ID) RSQUBRACE)* nlinesemi
;

types_of_return:
    VOID | list_star?(KEYWORD | ID)
;

types_of_arg_dec:
    list_star? (STRUCT ID | KEYWORD)  ID ('=' (ID | INTVAL | BOOLEANVAL))?{System.out.println("ArgumentDec : "+$ID.text);}
    |KEYWORD BOOLEANVAL {System.out.println("ArgumentDec : "+$BOOLEANVAL.text);}
    |KEYWORD INTVAL {System.out.println("ArgumentDec : "+$INTVAL.text);}
    |
;


list_star:
   (LIST SHARP)+
;


nlinesemi:
    ((SEMICOLON | NLINE+) |SEMICOLON NLINE+)
;


dot_id:
    ID (DOT expr)*
;

KEYWORD :'int'|'bool';
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
NOT:'~';
COMMA :',';
SHARP: '#';
LPAR :'(';
RPAR :')';
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
BlockComment:'/*' .*? '*/'-> skip;
WS : [ \t\r] -> skip;
NLINE : WS*'\n'WS*;
