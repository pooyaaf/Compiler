package main.visitor.type;

import main.ast.nodes.Node;
import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.declaration.VariableDeclaration;
import main.ast.nodes.declaration.struct.StructDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.primitives.BoolType;
import main.ast.types.primitives.IntType;
import main.ast.types.primitives.VoidType;
import main.compileError.typeError.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.VariableSymbolTableItem;
import main.symbolTable.utils.Stack;
import main.visitor.Visitor;

import javax.lang.model.type.NullType;
import java.util.ArrayList;

class Scope {
    boolean hasReturn = false;
}

public class TypeChecker extends Visitor<Void> {
    private boolean firstVisit = true;
    ArrayList<VariableDeclaration> argsInSetGet;
    private boolean isInStruct = false;
    private String curStructName;
    private int newId = 1;
    ExpressionTypeChecker expressionTypeChecker;
    public boolean canUseReturn = true;
    public boolean isInSetGet = false;
    public boolean isInGet = false;
    private boolean isOnlyReturn = false;
    private FunctionDeclaration currentFunctionName;
    private StructDeclaration currentStructName;

    Identifier RETID = new Identifier("#RETURN");
    Scope top = new Scope();
    Stack<Scope> scopes;




    boolean noDeclare = false;
    boolean hasReturn = false;

    public TypeChecker() {
        this.expressionTypeChecker = new ExpressionTypeChecker();
        this.scopes = new Stack<>();
    }

    private void checkType(ListType type, Node node)
    {
        checkType(type.getType(), node);
    }

    private void checkType(FptrType type, Node node)
    {
        for (Type innerType : type.getArgsType()) {
            checkType(innerType, node);
        }
        checkType(type.getReturnType(), node);
    }

    private void checkType(StructType type, Node node) {
        try {
            SymbolTable.root.getItem(StructSymbolTableItem.START_KEY + type.getStructName().getName());
        } catch (ItemNotFoundException e) {
            node.addError(new StructNotDeclared(node.getLine(), type.getStructName().getName()));
        }
    }

    private void checkType(Type type, Node node)
    {
        if(type instanceof StructType) {
            checkType((StructType) type, node);
        }
        if(type instanceof ListType){
            checkType((ListType) type, node);
        }
        if(type instanceof FptrType){
            checkType((FptrType) type, node);
        }
    }

    private boolean isEqual(StructType type1, StructType type2)
    {
        return type1.getStructName().getName().equals(type2.getStructName().getName());
    }

    private boolean isEqual(FptrType type1, FptrType type2)
    {
        if(type1.getArgsType().size() != type2.getArgsType().size())
            return false;
        if(!isEqual(type1.getReturnType(), type2.getReturnType()))
            return false;
        for (int i =0; i < type1.getArgsType().size(); i++) {
            if(!isEqual(type1.getArgsType().get(i), type2.getArgsType().get(i)))
                return false;
        }
        return true;
    }

    private boolean isEqual(ListType type1, ListType type2) {
        return isEqual(type1.getType(), type2.getType());
    }

    private boolean isEqual(Type type1, Type type2)
    {
        if(type1 instanceof NoType || type2 instanceof NoType)
            return true;
        if(!type1.getClass().equals(type2.getClass()))
            return false;
        if(type1 instanceof StructType) {
            return isEqual((StructType) type1, (StructType) type2);
        }
        if(type1 instanceof ListType){
            return isEqual((ListType) type1, (ListType) type2);
        }
        if(type1 instanceof FptrType){
            return isEqual((FptrType) type1, (FptrType) type2);
        }
        return true;
    }

    @Override
    public Void visit(Program program) {
        for (StructDeclaration struct : program.getStructs()) {
            struct.accept(this);
        }
        for (FunctionDeclaration function : program.getFunctions()) {
            function.accept(this);
        }
        program.getMain().accept(this);
        return null;
    }

    public boolean doesHaveReturn(Statement body){
        if(body instanceof ConditionalStmt){
            Statement stmt1 = ((ConditionalStmt) body).getThenBody();
            Statement stmt2 = ((ConditionalStmt) body).getElseBody();
            if(stmt1 instanceof ReturnStmt || stmt2 instanceof ReturnStmt)
                return true;

        }
        if(body instanceof BlockStmt){
            ArrayList<Statement> stmt1 = ((BlockStmt) body).getStatements();
            for (Statement stmt : stmt1){
                if(stmt instanceof ConditionalStmt){
                    Statement stmtA = ((ConditionalStmt) stmt).getThenBody();
                    Statement stmtB = ((ConditionalStmt) stmt).getElseBody();
                    if(stmtA instanceof ReturnStmt || stmtB instanceof ReturnStmt)
                        return true;
                }
                if(stmt instanceof ReturnStmt)
                    return true;
            }
        }
        return false;
    }
    @Override
    public Void visit(FunctionDeclaration functionDec) {
        //Todo
        boolean has_return = false;
        currentFunctionName = functionDec;

        for(VariableDeclaration arg: functionDec.getArgs()){
            arg.accept(this);
        }

        if(functionDec.getBody() instanceof ReturnStmt){
            has_return = true;

        }
        else{
            has_return = doesHaveReturn(functionDec.getBody());
        }

        if(!has_return && !(functionDec.getReturnType() instanceof NullType)){
            functionDec.addError(new MissingReturnStatement(functionDec.getLine(),functionDec.getFunctionName().getName()));
        }

        functionDec.getBody().accept(this);

        return null;
    }
    @Override
    public Void visit(MainDeclaration mainDec) {
        //Todo
        canUseReturn = false;
        mainDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        var item = new VariableSymbolTableItem(variableDec.getVarName());
        checkType(variableDec.getVarType(), variableDec);
        item.setType(variableDec.getVarType());
        try {
            SymbolTable.top.put(item);
        } catch (ItemAlreadyExistsException ignore) {
        }
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        //Todo
        structDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        noDeclare = true;
        checkType(setGetVarDec.getVarType(), setGetVarDec);
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        var item = new VariableSymbolTableItem(setGetVarDec.getVarName());
        item.setType(setGetVarDec.getVarType());
        try {
            SymbolTable.top.put(item);
        } catch (ItemAlreadyExistsException ignore) {
        }
        for (VariableDeclaration arg : setGetVarDec.getArgs()) {
            arg.accept(this);
        }
        setGetVarDec.getSetterBody().accept(this);
        SymbolTable.pop();
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        var returnItem = new VariableSymbolTableItem(RETID);
        returnItem.setType(setGetVarDec.getVarType());
        try {
            SymbolTable.top.put(returnItem);
        } catch (ItemAlreadyExistsException ignore) {
        }
        setGetVarDec.getGetterBody().accept(this);
        SymbolTable.pop();
        //added this
        ArrayList<Type> args = new ArrayList<>();
        for(VariableDeclaration i: setGetVarDec.getArgs()){
            args.add(i.getVarType());
        }
        item.setType(new FptrType(args, setGetVarDec.getVarType()));
        try {
            SymbolTable.top.put(item);
        } catch (ItemAlreadyExistsException ignore) {
        }
        noDeclare = false;
        return null;
    }



    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        //Todo
        expressionTypeChecker.setLvalue(false);
        Type lhsType = assignmentStmt.getLValue().accept(expressionTypeChecker);
        boolean lval = expressionTypeChecker.getLvalue();
        Type rhsType = assignmentStmt.getRValue().accept(expressionTypeChecker);
        if (lval){
            int line = assignmentStmt.getLine();
            assignmentStmt.addError(new LeftSideNotLvalue(line));
        }
        if(lhsType instanceof NoType)
            return null;
        if (!isSubType(rhsType, lhsType)) {
            int line = assignmentStmt.getLine();
            assignmentStmt.addError(new UnsupportedOperandType(line, BinaryOperator.assign.toString()));
        }
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        //Todo
        for(Statement stmt : blockStmt.getStatements())
            stmt.accept(this);
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        //Todo
        Type condType = conditionalStmt.getCondition().accept(expressionTypeChecker);
        if(!(condType instanceof BoolType || condType instanceof  NoType))
            conditionalStmt.addError(new ConditionNotBool(conditionalStmt.getLine()));
        if(conditionalStmt.getThenBody() != null) {
            SymbolTable ifScope = new SymbolTable(SymbolTable.top);
            SymbolTable.push(ifScope);
            conditionalStmt.getThenBody().accept(this);
            SymbolTable.pop();
        }
        if(conditionalStmt.getElseBody() != null) {
            SymbolTable elseScope = new SymbolTable(SymbolTable.top);
            SymbolTable.push(elseScope);
            conditionalStmt.getElseBody().accept(this);
            SymbolTable.pop();
        }
        return null;
    }


    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        //Todo
        expressionTypeChecker.set_functioncall_statement(true);
        //Type retType =
        functionCallStmt.getFunctionCall().accept(expressionTypeChecker);
        expressionTypeChecker.set_functioncall_statement(false);
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        var type = mustBeValue(displayStmt.getArg());
        if (!(type instanceof BoolType || type instanceof IntType || type instanceof ListType || type instanceof NoType)) {
            displayStmt.addError(new UnsupportedTypeForDisplay(displayStmt.getLine()));
        }
        return null;
    }

    private Type mustBeValue(Expression expression)
    {
        var type = expression.accept(expressionTypeChecker);
        if(type instanceof VoidType && !(expression instanceof ListAppend)){
            return new NoType();
        }
        if(expression instanceof ListAppend){//added
            expression.addError(new CantUseValueOfVoidFunction((expression.getLine())));
            return new NoType();
        }
        if(expression instanceof StructAccess && type instanceof FptrType)//added
            return ((FptrType) type).getReturnType();
        return type;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        top.hasReturn = true;
        VariableSymbolTableItem item = null;
        var retType = returnStmt.getReturnedExpr() == null ? new VoidType() : returnStmt.getReturnedExpr().accept(expressionTypeChecker);
        if(retType instanceof FptrType && ((FptrType) retType).getArgsType().size() == 0){
            ((FptrType) retType).addArgType(new VoidType());
        }
        try {
            item = (VariableSymbolTableItem) SymbolTable.top.getItem(VariableSymbolTableItem.START_KEY + RETID.getName());
        } catch (ItemNotFoundException ignore) {
            returnStmt.addError(new CannotUseReturn(returnStmt.getLine()));
            return null;
        }
        if(returnStmt.getReturnedExpr() instanceof ListAppend ){//added
            returnStmt.addError(new CantUseValueOfVoidFunction(returnStmt.getLine()));
            return null;
        }
        if (returnStmt.getReturnedExpr() == null && !(item.getType() instanceof VoidType)) {
            returnStmt.addError(new ReturnValueNotMatchFunctionReturnType(returnStmt.getLine()));
        } else {
            if (!isEqual(retType, item.getType())) {
                returnStmt.addError(new ReturnValueNotMatchFunctionReturnType(returnStmt.getLine()));
            }
        }
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        //Todo
        Type conType = loopStmt.getCondition().accept(expressionTypeChecker);
        if(!(conType instanceof BoolType)){
            loopStmt.addError(new ConditionNotBool(loopStmt.getLine()));
        }
        SymbolTable loopScope = new SymbolTable(SymbolTable.top);
        SymbolTable.push(loopScope);
        loopStmt.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        if (noDeclare) {
            varDecStmt.addError(new CannotUseDefineVar(varDecStmt.getLine()));
        }
        for (VariableDeclaration var : varDecStmt.getVars()) {
            var item = new VariableSymbolTableItem(var.getVarName());
            checkType(var.getVarType(), varDecStmt);
            item.setType(var.getVarType());
            try {
                SymbolTable.top.put(item);
            } catch (ItemAlreadyExistsException ignore) {
            }
            if (var.getDefaultValue() != null) {
                var type = mustBeValue(var.getDefaultValue());
                if (!isEqual(type, var.getVarType()) && !(type instanceof NoType)) {
                    var.addError(new UnsupportedOperandType(var.getLine(), BinaryOperator.assign.toString()));
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        //Todo
        Type type = listAppendStmt.getListAppendExpr().accept(expressionTypeChecker);
        if(!(type instanceof ListType)){
            listAppendStmt.addError(new AppendToNonList(listAppendStmt.getLine()));
        }
        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        //Todo
        Type type = listSizeStmt.getListSizeExpr().accept(expressionTypeChecker);
        if(!(type instanceof ListType)){
            listSizeStmt.addError(new GetSizeOfNonList(listSizeStmt.getLine()));
        }
        return null;
    }



    public boolean isSubType(Type a, Type b){
        if(a instanceof NoType){
            return true;
        }
        if(b instanceof NoType){
            return false;
        }
        if((a instanceof IntType && b instanceof IntType) ||(a instanceof BoolType && b instanceof BoolType)
                || (a instanceof NullType && b instanceof NullType)){
            return true;
        }

        if((a instanceof FptrType || a instanceof NullType) && (b instanceof FptrType || b instanceof NullType)){
            if(a instanceof NullType)
                return true;
            if(b instanceof NullType)
                return false;
            FptrType fptrA = (FptrType) a;
            FptrType fptrB = (FptrType) b;
            if(!(isSubType(fptrA.getReturnType(), fptrB.getReturnType()))){
                return false;
            }
            else{
                ArrayList<Type> argsA = fptrA.getArgsType();
                ArrayList<Type> argsB = fptrB.getArgsType();
                if(argsA.size() != argsB.size()){
                    return false;
                }
                int size = argsA.size();
                for(int j = 0; j < size; j++){
                    if(!isSubType(argsB.get(j), argsA.get(j))){
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

}
