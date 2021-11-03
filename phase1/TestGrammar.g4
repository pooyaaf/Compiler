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
    LIST SHARP KEYWORD ID LPAR ( parameters? (',' parameters)*) RPAR (retrun_begin|retrun_statement)
    | KEYWORD ID LPAR ( parameters? (',' parameters)*) RPAR (retrun_begin|retrun_statement)
    | VOID ID LPAR ( parameters? (',' parameters)*) RPAR (begin|statement)
;
body:
    statement
;
parameters :
    STRUCT ID ID
    |KEYWORD ID
;
//struct Declaring:
// should be worked on
struct:
    STRUCT ID begin? statement
;
//
retrun_begin :
 BEGIN statement*  RETURN (INTVAL | BOOLEANVAL | (ID'.'ID) | ID) (SEMICOLON)? END
;
// -- if - while - do --
while
        :
        WHILE '~'? LPAR relation RPAR begin? statement
        ;
do
        :
        DO (begin)? WHILE LPAR relation RPAR
        ;
if
        :
        IF LPAR relation RPAR (begin)?
        |IF LPAR relation RPAR statement
        | IF LPAR relation RPAR retrun_statement
        ;
else :
        ELSE begin
        | ELSE statement
        | ELSE retrun_statement
;
relation
        :
        expr RELATION expr
        ;
//
begin :
     BEGIN statement* END
;
//assignment
retrun_statement:
    RETURN (INTVAL | BOOLEANVAL | (ID'.'ID) | ID) (SEMICOLON)?
   ;

//
statement: (assignment | built_in ) (SEMICOLON)?;

// should be worked on
built_in :
        DISPLAY (LPAR ID)? LPAR(INTVAL | BOOLEANVAL | ID)* RPAR? RPAR
    |   SIZE  LPAR (INTVAL | BOOLEANVAL | ID) RPAR
    | APPEND LPAR (INTVAL | BOOLEANVAL | ID) COMMA (INTVAL | BOOLEANVAL | ID) RPAR
;
assignment :
  KEYWORD ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID)
| ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID)
| STRUCT ID ID
| KEYWORD ID
;


/* lexical | Tokens */

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
          '&' | '|' | '~' | '==' | '!=' | '>' | '<'
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
