lexer grammar lexers;
//literals :
KEYWORD :
            'int'|'bool'|'fptr'
 ;

//literals :
LIST:'list';
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
RELATIONS: '&&' | '||' | '&' | '|' | '~' | '==' | '>' | '<' | ',';         
OPERATOR :
            '+' | '-' | '*' | '/' 
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
USTART:'/*';
DSTAR:'*/';
WS : [ \t] -> skip;
JUNK : [@$`] -> skip;
