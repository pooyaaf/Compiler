grammar TestGrammar;
/*parser | CFG */
cmm:( struct* | function* ) main +EOF;

//--- expr ---
expr
        :
        term + expr | term
        ;

term
        :
        (ID|INTVAL) OPERATOR term | LPAR expr RPAR OPERATOR term | LPAR expr RPAR | ID | INTVAL
        ;
//Functions & Main:

main:
    MAIN LPAR RPAR (begin|statement)
;
function:
    LIST SHARP KEYWORD ID LPAR ( parameters? (',' parameters)*) RPAR (return_begin|return_statement)
    | KEYWORD ID LPAR ( parameters? (',' parameters)*) RPAR (return_begin|return_statement)
    | VOID ID LPAR ( parameters? (',' parameters)*) RPAR (begin|statement)
;
body:
    statement
;
parameters :
    STRUCT ID ID
    | list_declare KEYWORD ID
    | KEYWORD ID
;
list_declare:
    list_declare LIST SHARP |LIST SHARP
;
//struct Declaring:
// should be worked on
struct:
    STRUCT ID (begin | statement)
;
//
return_begin :
 BEGIN statement*  RETURN (INTVAL | BOOLEANVAL |  (ID'.'ID) | ID | expr) (SEMICOLON)? END
;
//
begin :
     BEGIN  (statement* | (ifstatement elsestatement)* )  END
;
statement: (assignment | built_in | ifstatement | whileloop | dostatement | function_call) (SEMICOLON)?;
// -- if - while - do -- function-call
function_call :
    ID LPAR (BOOLEANVAL*|INTVAL*|ID*) RPAR SEMICOLON?
;
whileloop
        :
        WHILE NOT? LPAR (relation|BOOLEANVAL) RPAR begin? statement
        ;
dostatement
        :
        DO (begin)? WHILE LPAR relation RPAR
        ;
ifstatement
        :
        IF LPAR? (relation|BOOLEANVAL) RPAR? (begin)?
        |IF LPAR? (relation|BOOLEANVAL) RPAR? statement
        | IF LPAR? (relation|BOOLEANVAL) RPAR? return_statement
        ;
elsestatement :
        ELSE begin
        | ELSE statement
        | ELSE return_statement
;
relation
        :
        expr RELATION expr
        ;

//assignment
return_statement:
    RETURN (INTVAL | BOOLEANVAL | (ID'.'ID) | ID |expr) (SEMICOLON)?
   ;


// should be worked on
built_in :
        DISPLAY (LPAR ID)? LPAR(INTVAL | BOOLEANVAL | ID)?(','INTVAL | ','BOOLEANVAL | ','ID)* RPAR? RPAR
    |   SIZE  LPAR (INTVAL | BOOLEANVAL | ID) RPAR
    | APPEND LPAR (INTVAL | BOOLEANVAL | ID) COMMA (INTVAL | BOOLEANVAL | ID) RPAR
;
//fptr < type -> type > ID
fptr_call :
    FPTR '<' (KEYWORD|STRUCT|list_declare)?(','KEYWORD|','STRUCT|','list_declare)* '-''>' (KEYWORD|STRUCT|LIST)  '>' ID
;
//--assignment
assignment :
  KEYWORD ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID) (',' ID)*
| ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr )
| fptr_call (ASSIGNMENT ID)?
| list_declare KEYWORD ID
| STRUCT ID ID
| ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID | expr )
| KEYWORD (ID(','ID)*|ID)
;


/* lexical | Tokens */

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
RELATION :
          '&' | '|' | '==' | '!=' | '>' | '<'
;
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
//NLINE : '\n';
WS : [ \t\r\n] -> skip;

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
//WS:[ \t\r\n]->skip ;
