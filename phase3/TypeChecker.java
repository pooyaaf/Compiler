package main.visitor.type;

import main.ast.nodes.Program;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.Identifier;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.primitives.BoolType;
import main.ast.types.primitives.IntType;
import main.ast.types.primitives.VoidType;
import main.compileError.nameError.DuplicateFunction;
import main.compileError.nameError.DuplicateStruct;
import main.compileError.nameError.DuplicateVar;
import main.compileError.typeError.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.VariableSymbolTableItem;
import main.visitor.Visitor;

import javax.lang.model.type.NullType;
import java.util.ArrayList;

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

    public void TypeChecker(){
        this.expressionTypeChecker = new ExpressionTypeChecker();
    }




    private void createStructSymbolTable(StructDeclaration structDec) {
        SymbolTable newSymbolTable = new SymbolTable();
        StructSymbolTableItem newSymbolTableItem = new StructSymbolTableItem(structDec);
        newSymbolTableItem.setStructSymbolTable(newSymbolTable);
        try {
            SymbolTable.root.put(newSymbolTableItem);

        } catch (ItemAlreadyExistsException e) {
            DuplicateStruct exception = new DuplicateStruct(structDec.getLine(), structDec.getStructName().getName());
            structDec.addError(exception);
            String newName = newId + "@";
            newId += 1;
            structDec.setStructName(new Identifier(newName));
            try {
                StructSymbolTableItem newStructSym = new StructSymbolTableItem(structDec);
                newStructSym.setStructSymbolTable(newSymbolTable);
                SymbolTable.root.put(newStructSym);
            } catch (ItemAlreadyExistsException e1) { //Unreachable
            }
        }
    }

    private void createFunctionSymbolTable(FunctionDeclaration funcDec) {
        FunctionSymbolTableItem newSymbolTableItem = new FunctionSymbolTableItem(funcDec);
        try {
            SymbolTable.root.put(newSymbolTableItem);

        } catch (ItemAlreadyExistsException e) {
            DuplicateFunction exception = new DuplicateFunction(funcDec.getLine(), funcDec.getFunctionName().getName());
            funcDec.addError(exception);
            String newName = newId + "@";
            newId += 1;
            funcDec.setFunctionName(new Identifier(newName));
            try {
                FunctionSymbolTableItem newFuncSym = new FunctionSymbolTableItem(funcDec);
                SymbolTable.root.put(newFuncSym);
            } catch (ItemAlreadyExistsException e1) { //Unreachable
            }
        }
    }


    @Override
    public Void visit(Program program) {
        //Todo
        this.expressionTypeChecker = new ExpressionTypeChecker();
        argsInSetGet = new ArrayList<VariableDeclaration>();
        SymbolTable root = new SymbolTable();
        SymbolTable.root = root;
        SymbolTable.push(root);
        for (StructDeclaration structDeclaration: program.getStructs()) {
            createStructSymbolTable(structDeclaration);


        }
        for (FunctionDeclaration functionDeclaration:program.getFunctions()) {
            createFunctionSymbolTable(functionDeclaration);
        }

        for (StructDeclaration structDec : program.getStructs()){
            try {
                String key = StructSymbolTableItem.START_KEY + structDec.getStructName().getName();
                StructSymbolTableItem structSymbolTableItem = (StructSymbolTableItem) SymbolTable.root.getItem(key);
                SymbolTable.push(structSymbolTableItem.getStructSymbolTable());
                isInStruct = true;
                curStructName = structDec.getStructName().getName();
                currentStructName = structDec;

                structDec.accept(this);
                isInStruct = false;
                SymbolTable.pop();
            } catch (ItemNotFoundException e) { //Unreachable
            }
        }
        for (FunctionDeclaration funcDec : program.getFunctions()) {
            SymbolTable.push(new SymbolTable());
            funcDec.accept(this);
            SymbolTable.pop();
        }

        SymbolTable.push(new SymbolTable());
        program.getMain().accept(this);
        SymbolTable.pop();
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
        //Todo
        String name = variableDec.getVarName().getName();
        VariableSymbolTableItem variableSymbolTableItem = new VariableSymbolTableItem(variableDec.getVarName());
        try {
            SymbolTable.top.getItem(variableSymbolTableItem.getKey());
        } catch (ItemNotFoundException exception2) {
            try {
                SymbolTable.top.put(variableSymbolTableItem);
            } catch (ItemAlreadyExistsException exception3) { //unreachable
            }
        }
        if(variableDec.getDefaultValue() != null){
            AssignmentStmt assign = new AssignmentStmt(variableDec.getVarName(),variableDec.getDefaultValue());
            assign.accept(this);
        }
        variableDec = CheckVarDec(variableDec, variableDec.getVarType());
        return null;
    }







    public VariableDeclaration CheckVarDec(VariableDeclaration varDeclaration, Type varDeclarationType) {
        if (varDeclarationType instanceof StructType) {
            StructType stype = (StructType) varDeclarationType;
            try {
                SymbolTable.root.getItem(StructSymbolTableItem.START_KEY + stype.getStructName().getName());
            } catch (ItemNotFoundException exc) {
                varDeclaration.addError(new StructNotDeclared(varDeclaration.getLine(), stype.getStructName().getName()));
                varDeclaration.setVarType(new NoType());
                return varDeclaration;
            }
        } else if (varDeclarationType instanceof ListType) {
            //todo



        } else if (varDeclarationType instanceof FptrType) {
            FptrType fptrType = (FptrType) varDeclarationType;
            for (Type arg : fptrType.getArgsType()) {
                varDeclaration = CheckVarDec(varDeclaration, arg);
            }
            varDeclaration = CheckVarDec(varDeclaration, fptrType.getReturnType());
        }
        return varDeclaration;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        //Todo
        structDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        //Todo

        String name = setGetVarDec.getVarName().getName();
        if (firstVisit) {
            setGetVarDec.getVarDec().accept(this);
            SymbolTable newSym = new SymbolTable();
            FunctionDeclaration funcDec = new FunctionDeclaration();

            funcDec.setFunctionName(new Identifier(name));
            funcDec.setReturnType(setGetVarDec.getVarType());
            funcDec.setArgs(setGetVarDec.getArgs());
            for(VariableDeclaration arg : setGetVarDec.getArgs()){
                argsInSetGet.add(arg);
            }
            FunctionSymbolTableItem newItem = new FunctionSymbolTableItem(funcDec);
            newItem.setFunctionSymbolTable(newSym);
            currentFunctionName = funcDec;
            try {
                SymbolTable.top.put(newItem);
            } catch (ItemAlreadyExistsException e) {
                setGetVarDec.setVarName(new Identifier(name + "@" + newId));
                funcDec.setFunctionName(new Identifier(name + "@" + newId));
                newId += 1;
                FunctionSymbolTableItem fSym = new FunctionSymbolTableItem(funcDec);
                fSym.setFunctionSymbolTable(newSym);
                try{
                    SymbolTable.top.put(fSym);
                }catch (ItemAlreadyExistsException e2) {//unreachable
                }
            }
        }
        else {
            try {
                String key = FunctionSymbolTableItem.START_KEY + name;
                FunctionSymbolTableItem fItem = (FunctionSymbolTableItem) SymbolTable.top.getItem(key);
                SymbolTable sym = fItem.getFunctionSymbolTable();
                sym.pre = SymbolTable.top;
                SymbolTable.push(sym);
                for (VariableDeclaration arg : setGetVarDec.getArgs())
                    arg.accept(this);
                SymbolTable.pop();
            } catch (ItemNotFoundException e) {//unreachable
            }
        }
        setGetVarDec.getVarName().accept(this);

        for (VariableDeclaration varDec: setGetVarDec.getArgs())
            varDec.accept(this);
        isInSetGet = true;
        canUseReturn = false;
        setGetVarDec.getSetterBody().accept(this);
        canUseReturn = true;
        isInGet = true;
        setGetVarDec.getGetterBody().accept(this);
        isInGet =false;
        isInSetGet = false;
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
        if(!(condType instanceof BoolType))
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
        //Todo
        Type type = displayStmt.getArg().accept(expressionTypeChecker);
        if(!(type instanceof IntType || type instanceof BoolType || type instanceof ListType || type instanceof  NoType)){
            displayStmt.addError(new UnsupportedTypeForDisplay(displayStmt.getLine()));
        }
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        //Todo
        if(canUseReturn == false){
            returnStmt.addError(new CannotUseReturn(returnStmt.getLine()));
            return null;
        }
        if(isInGet == true){
            for (VariableDeclaration arg : argsInSetGet) {
                String retName = returnStmt.getReturnedExpr().toString();
                String argName = arg.getVarName().toString();
                if (argName.equals(retName)) {
                    returnStmt.addError(new VarNotDeclared(returnStmt.getLine(), arg.getVarName().getName()));
                    return  null;
                }
            }
        }
        if(returnStmt != null){
            if(returnStmt.getReturnedExpr() != null){
                Type retType = returnStmt.getReturnedExpr().accept(expressionTypeChecker);
                Type funcType = currentFunctionName.getReturnType();
                if(!isSubType(retType,funcType)){
                    returnStmt.addError(new ReturnValueNotMatchFunctionReturnType(returnStmt.getLine()));
                }
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
        //Todo
        if(isInSetGet == true){
            varDecStmt.addError(new CannotUseDefineVar(varDecStmt.getLine()));
        }
        for(VariableDeclaration variableDeclaration : varDecStmt.getVars()){
            variableDeclaration.accept(this);
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
