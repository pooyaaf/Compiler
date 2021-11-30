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
    LPAR RPAR b = body {$mainRet.setBody($b.bodyRet);};

//todo
structDeclaration returns[StructDeclaration structDeclarationRet]:
    s = STRUCT id = identifier
    {
         $structDeclarationRet = new StructDeclaration() ;
         $structDeclarationRet.setLine($s.getLine());
         $structDeclarationRet.setStructName($id.idRet);
    }
     ((BEGIN s1 = structBody {$structDeclarationRet.setBody($s1.structbodyRet);}NEWLINE+ END)
     | (NEWLINE+ s2 = singleStatementStructBody {$structDeclarationRet.setBody($s2.stmtStructBodyRet);}SEMICOLON?)) NEWLINE+;

//todo
singleVarWithGetAndSet returns[SetGetVarDeclaration setgetVarRet]:
    {$setgetVarRet = new SetGetVarDeclaration();}
    t = type {$setgetVarRet.setVarType($t.typeRet);}
    i = identifier {$setgetVarRet.setVarName($i.idRet); $setgetVarRet.setLine(i.getLine());}
    f = functionArgsDec {$setgetVarRet.setArgs($f.funcArgDecRet);}  BEGIN NEWLINE+
    s = setBody {$setgetVarRet.setSetterBody($s.setbodyRet);}
    g = getBody {$setgetVarRet.setGetterBody($g.getbodyRet);}
    END;

//todo
singleStatementStructBody returns[Statement stmtStructBodyRet]:
    v = varDecStatement {$stmtStructBodyRet = $v.varDecRet;}| s = singleVarWithGetAndSet {$stmtStructBodyRet = $s.setgetVarRet;};

//todo
structBody returns[Statement structbodyRet]:
    (NEWLINE+ (s1 = singleStatementStructBody {$structbodyRet = $s1.stmtStructBodyRet;}SEMICOLON)*
    s2 = singleStatementStructBody {$structbodyRet = $s2.stmtStructBodyRet;} SEMICOLON?)+;

//todo
getBody returns[Statement getbodyRet]:
    GET b = body {$getbodyRet = $b.bodyRet;}NEWLINE+;

//todo
setBody returns[Statement setbodyRet]:
    SET b = body {$setbodyRet = $b.bodyRet;}NEWLINE+;

//todo
functionDeclaration returns[FunctionDeclaration functionDeclarationRet]:
    {$functionDeclarationRet = new FunctionDeclaration();}
    (t = type {$functionDeclarationRet.setReturnType($t.typeRet);}| v = VOID {$functionDeclarationRet.setReturnType(new VoidType());})
    i = identifier {$functionDeclarationRet.setLine(i.getLine()); $functionDeclarationRet.setFunctionName($i.idRet); }
    f = functionArgsDec {$functionDeclarationRet.setsetArgs($f.funcArgDecRet);}
    b = body {$functionDeclarationRet.setsetBody($b.bodyRet);}
    NEWLINE+;

//todo
functionArgsDec returns[ArrayList<VarDecStmt> funcArgDecRet]:
    {$funcArgDecRet = new ArrayList<VarDecStmt>();}
    LPAR (t1 = type i1 = identifier {VariableDeclaration myVar = new VariableDeclaration($i1.idRet , $t1.typeRet); $funcArgDecRet.add(myVar);}
    (COMMA t2 = type i2 = identifier {VariableDeclaration myVar = new VariableDeclaration($i2.idRet , $t2.typeRet); $funcArgDecRet.add(myVar);})*)? RPAR

    ;


//todo
functionArguments returns[ExprInPar funcArgRet]:
    {ArrayList<Expression> ex;}
    (e1 = expression {ex.add($e1.exprRet;)} (COMMA e2 = expression {ex.add($e2.exprRet;)})*)?
    {$funcArgRet = new ExprInPar(ex);};

//todo
body returns[Statement bodyRet]:
     (b = blockStatement {$bodyRet = $b.blockRet;}
     | (NEWLINE+ s = singleStatement {$bodyRet = $s.stmtRet;}(SEMICOLON)?));

//todo
loopCondBody returns[Statement loopconRet]:
     (b = blockStatement {$loopconRet =  $b.blockRet;}
     | (NEWLINE+ s = singleStatement {$loopconRet = $s.stmtRet;}));

//todo
blockStatement returns[BlockStmt blockRet]:
    b = BEGIN {$blockRet = new BlockStmt(); $blockRet.setLine($b.getLine());}
    (NEWLINE+ (s1 = singleStatement {$blockRet.addStatement($s1.stmtRet);} SEMICOLON)*
     s2 = singleStatement {$blockRet.addStatement($s2.stmtRet);}(SEMICOLON)?)+ NEWLINE+ END;

//todo
varDecStatement returns[VarDecStmt varDecRet]:
    {$varDecRet = new VarDecStmt();}
    t = type i1 = identifier
    {VariableDeclaration myVar = new VariableDeclaration($i1.idRet , $t.typeRet); $varDecRet.setLine(i1.getLine());}
    (ASSIGN o1 = orExpression
    {myVar.setDefaultValue($o1.orExprRet);
    $varDecRet.addVar(myVar);})?
    (COMMA i2 = identifier {VariableDeclaration myVar = new VariableDeclaration($i2.idRet , $t.typeRet);}(ASSIGN o2 = orExpression
    {myVar.setDefaultValue($o2.orExprRet);
    $varDecRet.addVar(myVar);}
    )?)*

    ;

//todo
functionCallStmt returns[FunctionCallStmt funcCallStmtRet]:
     o = otherExpression ((l = LPAR
     {FunctionCall myfuncCall = new FunctionCall($o.otherExprRet);}
     f1 = functionArguments RPAR) {myfuncCall.setArgs($f1.funcArgRet);}
     | (DOT i = identifier))* (LPAR f2 = functionArguments {myfuncCall.setArgs($f2.funcArgRet);  myfuncCall.addArg($i.idRet);}RPAR)
      {$funcCallStmtRet = new FunctionCallStmt(myfuncCall.getArgs()); $funcCallStmtRet.setLine($l.getLine());};

//todo
returnStatement returns[ReturnStmt returnStmtRet]:
    r = RETURN {$returnStmtRet = new ReturnStmt(); $returnStmtRet.setLine($r.getLine());}(e = expression {$returnStmtRet.setReturnedExpr($e.exprRet);})?
    ;

//todo
ifStatement returns[ConditionalStmt ifRet]:
    if1 = IF
    e1 = expression {$ifRet = new ConditionalStmt($e1.exprRet); $ifRet.setLine($if1.getline());}
    (l = loopCondBody {$ifRet.setThenBody($l.loopconRet);}
    | b = body {$ifRet.setThenBody($b.bodyRet);}
    e2 = elseStatement {$ifRet.setElseBody($e2.elseRet);});

//todo
elseStatement returns[Statement elseRet]:
     NEWLINE* ELSE l = loopCondBody {$elseRet = $l.loopconRet;}
     ;

//todo
loopStatement returns[LoopStmt loopStmtRet]:
    w = whileLoopStatement {$loopStmtRet = new LoopStmt(); $loopStmtRet.setLine(w.getLine());}
    | d = doWhileLoopStatement {$loopStmtRet = new LoopStmt(); $loopStmtRet.setLine(d.getLine());}
;
//todo
whileLoopStatement returns[LoopStmt whileRet]:

    w = WHILE {$whileRet = new LoopStmt(); $whileRet.setLine($w.getLine());}
    e = expression l = loopCondBody
    {$whileRet.setBody($l.loopconRet); $whileRet.setCondition($e.exprRet);}
    ;

//todo
doWhileLoopStatement returns[LoopStmt doRet]:
    d = DO {$doRet = new LoopStmt(); $doRet.setLine($d.getLine());}
    b = body NEWLINE* w = WHILE e = expression
    {$doRet.setBody($b.bodyRet); $doRet.setCondition($e.exprRet);}
    ;

//todo
displayStatement returns[DisplayStmt dispRet]:
  d = DISPLAY LPAR e = expression RPAR {$dispRet = new DisplayStmt($e.exprRet); $dispRet.setLine($d.getLine());}
  ;

//todo
assignmentStatement returns[AssignmentStmt assignStmtRet]:
    o = orExpression a = ASSIGN e = expression
    {$assignStmtRet = new AssignmentStmt($o.orExprRet , $e.exprRet);
     $assignStmtRet.setLine($a.getLine());} ;

//todo
singleStatement returns[Statement stmtRet]:
    i = ifStatement {$stmtRet = $i.ifRet}
    | d = displayStatement {$stmtRet = $d.dispRet}
    | f = functionCallStmt {$stmtRet = $f.funcCallStmtRet}
    | r = returnStatement {$stmtRet = $r.returnStmtRet}
    | a1 = assignmentStatement {$stmtRet = $a1.assignStmtRet}
    | v = varDecStatement {$stmtRet = $v.varDecRet}
    | l = loopStatement {$stmtRet = $l.loopStmtRet}
    | a2 = append {$stmtRet = $a2.appendRet}
    | s = size {$stmtRet = $s.sizeRet};

//todo
expression returns[Expression exprRet]:
    {$exprRet = new Expression();}
    o = orExpression {$exprRet = $o.orExprRet;}
    (op = ASSIGN e = expression {$exprRet = $e.exprRet})? ;

//todo
orExpression returns[Expression orExprRet]:
    a1 = andExpression {$orExprRet = $a1.andExprRet;}
    (op = OR a2 = andExpression {$orExprRet = $a2.andExprRet;})*;

//todo
andExpression returns[Expression andExprRet]:
    e1 = equalityExpression {$andExprRet = $e1.eqExprRet;}
    (op = AND e2 = equalityExpression {$andExprRet = $e2.eqExprRet;})*;

//todo
equalityExpression returns[Expression eqExprRet]:
    r1 = relationalExpression {$eqExprRet = $r1.relExprRet;}
    (op = EQUAL r2 = relationalExpression {$eqExprRet = $r2.relExprRet;})*;

//todo
relationalExpression returns[Expression relExprRet]:
    a1 = additiveExpression {$relExprRet = $a1.addExprRet;}
    ((op = GREATER_THAN | op = LESS_THAN) a2 = additiveExpression {$relExprRet = $a2.addExprRet;})*;

//todo
additiveExpression returns[Expression addExprRet]:
    m1 = multiplicativeExpression {$addExprRet = $m1.multExprRet;}
    ((op = PLUS | op = MINUS) m2 = multiplicativeExpression {$addExprRet = $m2.multExprRet;})*;

//todo
multiplicativeExpression returns[Expression multExprRet]:
    p1 = preUnaryExpression {$multExprRet = $p1.preunExprRet;}
    ((op = MULT | op = DIVIDE) p2 = preUnaryExpression {$multExprRet = $p2.preunExprRet;})*;

//todo
preUnaryExpression returns[Expression preunExprRet]:
    ((op = NOT | op = MINUS) p = preUnaryExpression )  {$preunExprRet = $p.preunExprRet;}
    | a = accessExpression {$preunExprRet = $a.accessExprRet;}
    ;

//todo
accessExpression returns[Expression accessExprRet]:
    o = otherExpression  { $accessExprRet = $o.otherExprRet;}
    ((LPAR f1 = functionArguments {$accessExprRet = $f1.funcArgRet;} RPAR) | (DOT i1 = identifier {$accessExprRet = $i1.idRet;}))*
    ((LBRACK e2 = expression {$accessExprRet = $e2.exprRet;} RBRACK) | (DOT i2 = identifier {$accessExprRet = $i2.idRet;}))*;

//todo
otherExpression returns [Expression otherExprRet]:
    v = value
    { $otherExprRet = $v.valueRet; }
    | id = identifier { $otherExprRet = $id.idRet; }
    | LPAR (f = functionArguments {$otherExprRet = $f.funcArgRet;} ) RPAR
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
