grammar Cmm;

@header{
     import main.ast.nodes.*;
     import main.ast.nodes.declaration.*;
     import main.ast.nodes.declaration.struct.*;
     import main.ast.nodes.expression.*;
     import main.ast.nodes.expression.operators.*;
     import main.ast.nodes.expression.values.*;
     import main.ast.nodes.expression.values.primitive.*;
     import main.ast.nodes.statement.*;
     import main.ast.types.*;
     import main.ast.types.primitives.*;
     import java.util.*;
 }

cmm returns[Program cmmProgram]:
    NEWLINE* p = program {$cmmProgram = $p.programRet;} NEWLINE* EOF;

program returns[Program programRet]:
    {$programRet = new Program();
     int line = 1;
     $programRet.setLine(line);
     }
    (s = structDeclaration {$programRet.addStruct($s.structDeclarationRet);})*
    (f = functionDeclaration {$programRet.addFunction($f.functionDeclarationRet);})*
    m = main {$programRet.setMain($m.mainRet);};

//todo
main returns[MainDeclaration mainRet]:
    line = MAIN
    {
    $mainRet = new MainDeclaration();
    $mainRet.setLine($line.getLine());
    }
    LPAR RPAR body;

//todo
structDeclaration returns[StructDeclaration structDeclarationRet]:
    s = STRUCT id = identifier
    {
         $structDeclarationRet = new StructDeclaration() ;
         $structDeclarationRet.setLine($s.getLine());
    }
     ((BEGIN structBody NEWLINE+ END) | (NEWLINE+ singleStatementStructBody SEMICOLON?)) NEWLINE+;

//todo
singleVarWithGetAndSet :
    type identifier functionArgsDec BEGIN NEWLINE+ setBody getBody END;

//todo
singleStatementStructBody :
    varDecStatement | singleVarWithGetAndSet;

//todo
structBody :
    (NEWLINE+ (singleStatementStructBody SEMICOLON)* singleStatementStructBody SEMICOLON?)+;

//todo
getBody :
    GET body NEWLINE+;

//todo
setBody :
    SET body NEWLINE+;

//todo
functionDeclaration returns[FunctionDeclaration functionDeclarationRet]:
    (type | VOID ) identifier functionArgsDec body NEWLINE+;

//todo
functionArgsDec :
    LPAR (type identifier (COMMA type identifier)*)? RPAR ;

//todo
functionArguments :
    (expression (COMMA expression)*)?;

//todo
body :
     (blockStatement | (NEWLINE+ singleStatement (SEMICOLON)?));

//todo
loopCondBody :
     (blockStatement | (NEWLINE+ singleStatement ));

//todo
blockStatement :
    BEGIN (NEWLINE+ (singleStatement SEMICOLON)* singleStatement (SEMICOLON)?)+ NEWLINE+ END;

//todo
varDecStatement :
    type identifier (ASSIGN orExpression )? (COMMA identifier (ASSIGN orExpression)? )*;

//todo
functionCallStmt :
     otherExpression ((LPAR functionArguments RPAR) | (DOT identifier))* (LPAR functionArguments RPAR);

//todo
returnStatement :
    RETURN (expression)?;

//todo
ifStatement :
    IF expression (loopCondBody | body elseStatement);

//todo
elseStatement :
     NEWLINE* ELSE loopCondBody;

//todo
loopStatement :
    whileLoopStatement | doWhileLoopStatement;

//todo
whileLoopStatement :
    WHILE expression loopCondBody;

//todo
doWhileLoopStatement :
    DO body NEWLINE* WHILE expression;

//todo
displayStatement :
  DISPLAY LPAR expression RPAR;

//todo
assignmentStatement :
    orExpression ASSIGN expression;

//todo
singleStatement :
    ifStatement | displayStatement | functionCallStmt | returnStatement | assignmentStatement
    | varDecStatement | loopStatement | append | size;

//todo
expression returns[Expression exprRet]:
    orExpression (op = ASSIGN expression )? ;

//todo
orExpression:
    andExpression (op = OR andExpression )*;

//todo
andExpression:
    equalityExpression (op = AND equalityExpression )*;

//todo
equalityExpression:
    relationalExpression (op = EQUAL relationalExpression )*;

//todo
relationalExpression:
    additiveExpression ((op = GREATER_THAN | op = LESS_THAN) additiveExpression )*;

//todo
additiveExpression:
    multiplicativeExpression ((op = PLUS | op = MINUS) multiplicativeExpression )*;

//todo
multiplicativeExpression:
    preUnaryExpression ((op = MULT | op = DIVIDE) preUnaryExpression )*;

//todo
preUnaryExpression:
    ((op = NOT | op = MINUS) preUnaryExpression ) | accessExpression;

//todo
accessExpression returns[Expression accessExprRet]:
    o = otherExpression  { $accessExprRet = $o.otherExprRet; } ((LPAR e = functionArguments RPAR) | (DOT identifier))*  ((LBRACK expression RBRACK) | (DOT identifier))*;

//todo
otherExpression returns [Expression otherExprRet]:
    v = value
    { $otherExprRet = $v.valueRet; }
    | id = identifier { $otherExprRet = $id.idRet; }
    | LPAR (functionArguments) RPAR
    | s = size { $otherExprRet = $s.sizeRet; }
    | a = append { $otherExprRet = $a.appendRet; };

//todo
size returns[ListSize sizeRet]:
    s = SIZE LPAR e1 = expression RPAR
    {
        $sizeRet = new ListSize($e1.exprRet);
        $sizeRet.setLine($s.getLine());
    }
    ;
//todo
append returns[ListAppend appendRet]:
    a = APPEND

    LPAR e1 = expression COMMA e2 = expression RPAR
    {
        $appendRet = new ListAppend($e1.exprRet , $e2.exprRet);
        $appendRet.setLine($a.getLine());
    }
    ;
//todo
value returns[Value valueRet]:
    b = boolValue
    {
        $valueRet = new BoolValue($b.boolRet);
        $valueRet.setLine($b.line)
    }
    |
     i = INT_VALUE
     {
     $valueRet = new IntValue($i.int);
     $valueRet.setLine($i.getLine());
     }
     ;

//todo
boolValue returns[BoolValue boolRet , int line]:
    t=TRUE
    {
         $boolRet = true;
         $line = $t.getLine();
    }
    |
     f=FALSE
      {
          $boolRet = false;
          $line = f.getline();
      }
;
//todo
identifier returns[Identifier idRet]:
    id = IDENTIFIER
    {
        $idRet = new Identifier($id.text);
        $idRet.setLine($id.getLine());
    }
    ;
//todo
type returns[Type typeRet]:
    i = INT
    {
         $typeRet= new IntType();
    }
    |
    b = BOOL
    {
         $typeRet= new BoolType();
    }
    |
    LIST SHARP t=type
    { $typeRet = new ListType($t.typeRet); }
    |
    STRUCT iden=identifier
    { $typeRet = new StructType($iden.idRet); }
    |
     f = fptrType
     {
      $typeRet = $f.fptrTypeRet;
     }
     ;

//todo
fptrType returns[FptrType fptrTypeRet]:
    FPTR
     { $fptrTypeRet = new FptrType(); }
     LESS_THAN (VOID { $fptrTypeRet.setArgsType((new ArrayList<Type>()); }
     |
     (types = type {$fptrTypeRet.setArgsType($types.typeRet); } (COMMA types = type {$fptrTypeRet.setArgsType($types.typeRet); })* ))
      ARROW (t = type{ $fptrTypeRet.setReturnType($t.typeRet); } | VOID{ $fptrTypeRet.setReturnType(new VoidType()); }) GREATER_THAN;

MAIN: 'main';
RETURN: 'return';
VOID: 'void';

SIZE: 'size';
DISPLAY: 'display';
APPEND: 'append';

IF: 'if';
ELSE: 'else';

PLUS: '+';
MINUS: '-';
MULT: '*';
DIVIDE: '/';


EQUAL: '==';
ARROW: '->';
GREATER_THAN: '>';
LESS_THAN: '<';


AND: '&';
OR: '|';
NOT: '~';

TRUE: 'true';
FALSE: 'false';

BEGIN: 'begin';
END: 'end';

INT: 'int';
BOOL: 'bool';
LIST: 'list';
STRUCT: 'struct';
FPTR: 'fptr';
GET: 'get';
SET: 'set';
WHILE: 'while';
DO: 'do';

ASSIGN: '=';
SHARP: '#';
LPAR: '(';
RPAR: ')';
LBRACK: '[';
RBRACK: ']';

COMMA: ',';
DOT: '.';
SEMICOLON: ';';
NEWLINE: '\n';

INT_VALUE: '0' | [1-9][0-9]*;
IDENTIFIER: [a-zA-Z_][A-Za-z0-9_]*;


COMMENT: ('/*' .*? '*/') -> skip;
WS: ([ \t\r]) -> skip;
