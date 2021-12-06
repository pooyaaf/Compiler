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
     $programRet.setLine(1);}
    (s = structDeclaration {$programRet.addStruct($s.structDeclarationRet);})*
    (f = functionDeclaration {$programRet.addFunction($f.functionDeclarationRet);})*
     m = main {$programRet.setMain($m.mainRet);};
//OK
//todo
main returns[MainDeclaration mainRet]:
    line = MAIN
    {
    $mainRet = new MainDeclaration();
    $mainRet.setLine($line.getLine());}
    LPAR RPAR b = body {$mainRet.setBody($b.bodyRet);};
//OK
//todo
structDeclaration returns[StructDeclaration structDeclarationRet]:
    {$structDeclarationRet = new StructDeclaration() ;}
    s = STRUCT id = identifier
    {
         $structDeclarationRet.setStructName($id.idRet);
         $structDeclarationRet.setLine($s.getLine());
    }
     ((BEGIN s1 = structBody {$structDeclarationRet.setBody($s1.structbodyRet);}NEWLINE+ END)
     | (NEWLINE+ s2 = singleStatementStructBody {$structDeclarationRet.setBody($s2.stmtStructBodyRet);}SEMICOLON?)) NEWLINE+;
//OK
//todo
singleVarWithGetAndSet returns[SetGetVarDeclaration setgetVarRet]:
    {$setgetVarRet = new SetGetVarDeclaration();}
    t = type {$setgetVarRet.setVarType($t.typeRet);}
    i = identifier {$setgetVarRet.setVarName($i.idRet); $setgetVarRet.setLine($i.idRet.getLine());}
    f = functionArgsDec {$setgetVarRet.setArgs($f.funcArgDecRet);}  BEGIN NEWLINE+
    s = setBody {$setgetVarRet.setSetterBody($s.setbodyRet);}
    g = getBody {$setgetVarRet.setGetterBody($g.getbodyRet);}
    END;
//OK
//todo
singleStatementStructBody returns[Statement stmtStructBodyRet]:
    v = varDecStatement {$stmtStructBodyRet = $v.varDecRet;}| s = singleVarWithGetAndSet {$stmtStructBodyRet = $s.setgetVarRet;};
//OK
//todo
structBody returns[BlockStmt structbodyRet]:
    {$structbodyRet = new BlockStmt();}
    (NEWLINE+ (s1 = singleStatementStructBody {$structbodyRet.addStatement($s1.stmtStructBodyRet);}SEMICOLON)*
    s2 = singleStatementStructBody {$structbodyRet.addStatement($s2.stmtStructBodyRet);} SEMICOLON?)+;
//OK
//todo
getBody returns[Statement getbodyRet]:
    GET b = body {$getbodyRet = $b.bodyRet;}NEWLINE+;
//OK
//todo
setBody returns[Statement setbodyRet]:
    SET b = body {$setbodyRet = $b.bodyRet;}NEWLINE+;
//OK
//todo
functionDeclaration returns[FunctionDeclaration functionDeclarationRet]:
    {$functionDeclarationRet = new FunctionDeclaration();}
    (t = type {$functionDeclarationRet.setReturnType($t.typeRet);}| v = VOID {$functionDeclarationRet.setReturnType(new VoidType());})
    i = identifier {$functionDeclarationRet.setLine($i.idRet.getLine()); $functionDeclarationRet.setFunctionName($i.idRet); }
    f = functionArgsDec {$functionDeclarationRet.setsetArgs($f.funcArgDecRet);}
    b = body {$functionDeclarationRet.setsetBody($b.bodyRet);}
    NEWLINE+;
//OK
//todo
functionArgsDec returns[ArrayList<VariableDeclaration> funcArgDecRet]:
    {$funcArgDecRet = new ArrayList<VariableDeclaration>();}
    LPAR (t1 = type i1 = identifier {VariableDeclaration myVar = new VariableDeclaration($i1.idRet , $t1.typeRet); $funcArgDecRet.add(myVar);}
    (COMMA t2 = type i2 = identifier {VariableDeclaration myVar = new VariableDeclaration($i2.idRet , $t2.typeRet); $funcArgDecRet.add(myVar);})*)? RPAR

    ;

//OK
//todo
functionArguments returns[ExprInPar funcArgRet]:
    {ArrayList<Expression> ex;}
    (e1 = expression {ex.add($e1.exprRet);} (COMMA e2 = expression {ex.add($e2.exprRet);})*)?
    {$funcArgRet = new ExprInPar(ex);};

//maybe $ in line 97
//todo
body returns[Statement bodyRet]:
     (b = blockStatement {$bodyRet = $b.blockRet;}
     | (NEWLINE+ s = singleStatement {$bodyRet = $s.stmtRet;}(SEMICOLON)?));
//OK
//todo
loopCondBody returns[Statement loopconRet]:
     (b = blockStatement {$loopconRet =  $b.blockRet;}
     | (NEWLINE+ s = singleStatement {$loopconRet = $s.stmtRet;}));
//OK
//todo
blockStatement returns[BlockStmt blockRet]:
    b = BEGIN {$blockRet = new BlockStmt(); $blockRet.setLine($b.getLine());}
    (NEWLINE+ (s1 = singleStatement {$blockRet.addStatement($s1.stmtRet);} SEMICOLON)*
     s2 = singleStatement {$blockRet.addStatement($s2.stmtRet);}(SEMICOLON)?)+ NEWLINE+ END;
//OK
//todo
varDecStatement returns[VarDecStmt varDecRet]:
    {$varDecRet = new VarDecStmt();}
    t = type i1 = identifier
    {myVar = new VariableDeclaration($i1.idRet , $t.typeRet); $varDecRet.setLine($i1.idRet.getLine());}
    (ASSIGN o1 = orExpression
    {myVar.setDefaultValue($o1.orExprRet);})?{$varDecRet.addVar(myVar);}
    (COMMA i2 = identifier {VariableDeclaration myVar = new VariableDeclaration($i2.idRet , $t.typeRet);}(ASSIGN o2 = orExpression
    {myVar.setDefaultValue($o2.orExprRet);
    $varDecRet.addVar(myVar);}
    )?)*

    ;

//OK

//todo
functionCallStmt returns[FunctionCallStmt funcCallStmtRet]:
     o = otherExpression ((l = LPAR
     {FunctionCall myfuncCall = new FunctionCall($o.otherExprRet);}
     f1 = functionArguments RPAR) {myfuncCall.setArgs($f1.funcArgRet);}
     | (DOT i = identifier))* (LPAR f2 = functionArguments {myfuncCall.setArgs($f2.funcArgRet);  myfuncCall.addArg($i.idRet);}RPAR)
      {$funcCallStmtRet = new FunctionCallStmt(myfuncCall); $funcCallStmtRet.setLine($l.getLine());};
//should work on line 134
//todo
returnStatement returns[ReturnStmt returnStmtRet]:
    r = RETURN {$returnStmtRet = new ReturnStmt(); $returnStmtRet.setLine($r.getLine());}(e = expression {$returnStmtRet.setReturnedExpr($e.exprRet);})?
    ;
//OK
//todo
ifStatement returns[ConditionalStmt ifRet]:
    if1 = IF
    e1 = expression {$ifRet = new ConditionalStmt($e1.exprRet); $ifRet.setLine($if1.getline());}
    (l = loopCondBody {$ifRet.setThenBody($l.loopconRet);}
    | b = body {$ifRet.setThenBody($b.bodyRet);}
    e2 = elseStatement {$ifRet.setElseBody($e2.elseRet);});
//OK
//todo
elseStatement returns[Statement elseRet]:
     NEWLINE* ELSE l = loopCondBody {$elseRet = $l.loopconRet;}
     ;
//OK
//todo
loopStatement returns[LoopStmt loopStmtRet]:
    w = whileLoopStatement {$loopStmtRet = $w.whileRet ;}
    | d = doWhileLoopStatement {$loopStmtRet = $d.doRet;}
;
//OK
//todo
whileLoopStatement returns[LoopStmt whileRet]:

    w = WHILE {$whileRet = new LoopStmt(); $whileRet.setLine($w.getLine());}
    e = expression l = loopCondBody
    {$whileRet.setBody($l.loopconRet); $whileRet.setCondition($e.exprRet);}
    ;
//OK
//todo
doWhileLoopStatement returns[LoopStmt doRet]:
    d = DO {$doRet = new LoopStmt(); $doRet.setLine($d.getLine());}
    b = body NEWLINE* w = WHILE e = expression
    {$doRet.setBody($b.bodyRet); $doRet.setCondition($e.exprRet);}
    ;
//OK
//todo
displayStatement returns[DisplayStmt dispRet]:
  d = DISPLAY LPAR e = expression RPAR {$dispRet = new DisplayStmt($e.exprRet); $dispRet.setLine($d.getLine());}
  ;
//OK
//todo
assignmentStatement returns[AssignmentStmt assignStmtRet]:
    o = orExpression a = ASSIGN e = expression
    {$assignStmtRet = new AssignmentStmt($o.orExprRet , $e.exprRet);
     $assignStmtRet.setLine($a.getLine());} ;
//OK
//todo
singleStatement returns[Statement stmtRet]:
    i = ifStatement {$stmtRet = $i.ifRet;}
    | d = displayStatement {$stmtRet = $d.dispRet;}
    | f = functionCallStmt {$stmtRet = $f.funcCallStmtRet;}
    | r = returnStatement {$stmtRet = $r.returnStmtRet;}
    | a1 = assignmentStatement {$stmtRet = $a1.assignStmtRet;}
    | v = varDecStatement {$stmtRet = $v.varDecRet;}
    | l = loopStatement {$stmtRet = $l.loopStmtRet;}
    | a2 = append {$stmtRet = new ListAppendStmt($a2.appendRet);}
    | s = size {$stmtRet = ListSizeStmt($s.sizeRet);};
//OK
//todo
expression returns[Expression exprRet]:

    oe = orExpression  { $exprRet = $oe.orExprRet; }
    (a = ASSIGN e = expression
        {BinaryOperator op = BinaryOperator.assign;
            $exprRet = new BinaryExpression($oe.orExprRet, $e.exprRet, op);
            $exprRet.setLine($a.getLine());})?;
//OKallExpr
//todo
orExpression returns[Expression orExprRet]:
    a1 = andExpression    { $orExprRet = $a1.andExprRet; }
    (o = OR a2 = andExpression {
                                        BinaryOperator op = BinaryOperator.or;
                                        $orExprRet = new BinaryExpression($orExprRet, $a2.andExprRet, op);
                                        $orExprRet.setLine($o.getLine());
                                    }
                                    )*;

//todo
andExpression returns[Expression andExprRet]:
    e1 = equalityExpression {$andExprRet = $e1.eqExprRet;}
    (a = AND e2 = equalityExpression {
                                              BinaryOperator op = BinaryOperator.and;
                                              $andExprRet = new BinaryExpression($andExprRet, $e2.eqExprRet, op);
                                              $andExprRet.setLine($a.getLine());
                                          }
                                          )*;

//todo
equalityExpression returns[Expression eqExprRet] locals[BinaryOperator op]:
    r1 = relationalExpression {$eqExprRet = $r1.relExprRet;}
    (eq = EQUAL r2 = relationalExpression  {

                                                  $op = BinaryOperator.eq;
                                                  $eqExprRet = new BinaryExpression($eqExprRet, $r2.relExprRet, $op);
                                                  $eqExprRet.setLine( $eq.getLine());
                                              })*;

//todo
relationalExpression returns[Expression relExprRet] locals[BinaryOperator op, int line]:
    a1 = additiveExpression  { $relExprRet = $a1.addExprRet; }
    ((gt = GREATER_THAN
         {
             $op = BinaryOperator.gt;
             $line = $gt.getLine();
         }
     | lt = LESS_THAN
        {
                $op = BinaryOperator.lt;
                $line = $lt.getLine();
         }
     ) a2 = additiveExpression      {
                                       $relExprRet = new BinaryExpression($relExprRet, $a2.addExprRet, $op);
                                       $relExprRet.setLine($line);
                                   })*;

//todo
additiveExpression returns[Expression addExprRet] locals[BinaryOperator op, int line]:
    m1 = multiplicativeExpression {$addExprRet = $m1.multExprRet;}
    ((add = PLUS
            {
                    $op = BinaryOperator.add;
                    $line = $add.getLine();
             }
    | sub = MINUS
            {
                    $op = BinaryOperator.sub;
                    $line = $sub.getLine();
            }
    ) m2 = multiplicativeExpression {
                                            $addExprRet = new BinaryExpression($addExprRet, $m2.multExprRet, $op);
                                            $addExprRet.setLine($line);
                                        })*;

//todo
multiplicativeExpression returns[Expression multExprRet]   locals[BinaryOperator op, int line]:
    p1 = preUnaryExpression {$multExprRet = $p1.preUnaryExprRet;}
    ((mult = MULT
    {
            $op = BinaryOperator.mult;
            $line = $mult.getLine();
        }
        | div = DIVIDE
        {
                $op = BinaryOperator.div;
                $line = $div.getLine();
            }
        ) p2 = preUnaryExpression  {
                                          $multExprRet = new BinaryExpression($multExprRet, $p2.preUnaryExprRet, $op);
                                          $multExprRet.setLine($line);
                                      })*;

//todo
preUnaryExpression returns[Expression preUnaryExprRet] locals[UnaryOperator op, int line]:
    ((not= NOT
    {
            $op = UnaryOperator.not;
            $line = $not.getLine();
        }
    | minus = MINUS
        {
            $op = UnaryOperator.minus;
            $line = $minus.getLine();
        }) pre= preUnaryExpression ) {
                                             $preUnaryExprRet = new UnaryExpression($pre.preUnaryExprRet, $op);
                                             $preUnaryExprRet.setLine($line);
                                         }
    | a = accessExpression  { $preUnaryExprRet = $a.accessExprRet; }
    ;

//todo
accessExpression returns[Expression accessExprRet]:
    o = otherExpression  { $accessExprRet = $o.otherExprRet;}
    ((l = LPAR f1 = functionArguments  {
                                               $accessExprRet = new ExprInPar($f1.funcArgRet);
                                               $accessExprRet.setLine($l.getLine());
                                           } RPAR) | (DOT i1 = identifier {
                                               $accessExprRet = new Identifier($i1.idRet);
                                               $accessExprRet.setLine($i1.idRet.line);
                                               }))*
    ((l1 = LBRACK e2 = expression    {
                                     $accessExprRet = new ListAccessByIndex($accessExprRet, $e2.exprRet);
                                     $accessExprRet.setLine($l1.getLine());
                                 } RBRACK) |
    (DOT i2 = identifier ))*;

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
        $valueRet.setLine($b.boolRet.getLine());
    }
    |
     i = INT_VALUE
     {
     $valueRet = new IntValue($i.int);
     $valueRet.setLine($i.getLine());
     }
     ;

//todo
boolValue returns[BoolValue boolRet]:
    t=TRUE
    {
         $boolRet.setConstant(true);
         $boolRet.setLine($t.getLine());
    }
    |
     f=FALSE
      {
          $boolRet.setConstant(false);
          $boolRet.setLine($f.getline());
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
