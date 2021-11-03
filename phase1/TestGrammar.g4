grammar TestGrammar;
/*parser | CFG */
cmm:( struct* | function* ) main +EOF;
//Functions & Main:

main:
    MAIN LPAR RPAR (begin|statement)
;
function:
    (VOID|KEYWORD) ID LPAR ( parameters? (',' parameters)*) RPAR (begin|statement)
;
body:
    statement
;
parameters :
    KEYWORD ID
;
//struct Declaring:
struct:
    STRUCT ID begin?
;
begin :
     BEGIN statement* END
;
//assignment
statement:assignment (SEMICOLON)?;
assignment :
KEYWORD ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID)
| ID ASSIGNMENT (INTVAL | BOOLEANVAL | ID)
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
