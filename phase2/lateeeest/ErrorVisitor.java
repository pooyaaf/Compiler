package main.visitor.name;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.compileError.nameError.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.SymbolTableItem;
import main.symbolTable.items.VariableSymbolTableItem;
import main.visitor.*;

public class ErrorVisitor extends Visitor<Void> {
    public static  boolean err = false ;
   public long i=0;
   public long j=0;
   public long h=0;
    @Override
    public Void visit(Program program) {
        SymbolTable root = new SymbolTable();
        SymbolTable.root = root;
        SymbolTable.top = root;
        SymbolTable.push(root);


        for (StructDeclaration structDeclaration: program.getStructs())
            structDeclaration.accept(this);
        for (FunctionDeclaration functionDeclaration:program.getFunctions())
            functionDeclaration.accept(this);
        program.getMain().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDec) {
        //todo
        FunctionSymbolTableItem functionSymbolTableItem = new FunctionSymbolTableItem(functionDec);
        SymbolTable symbolTable = new SymbolTable(SymbolTable.root);
        functionSymbolTableItem.setFunctionSymbolTable(symbolTable);

        try {
            SymbolTable.root.put(functionSymbolTableItem);
        } catch (ItemAlreadyExistsException e) {

            DuplicateFunction exception =  new DuplicateFunction(functionDec.getLine(),functionDec.getFunctionName().getName());
            System.out.println(exception.getMessage());
            functionSymbolTableItem.setName(j+" "+functionSymbolTableItem.getName());
            j++;
            err=true;
            try {
                SymbolTable.root.put(functionSymbolTableItem);
            } catch (ItemAlreadyExistsException ex) {

            }

        }

        try {
            SymbolTable.root.getItem(StructSymbolTableItem.START_KEY+functionDec.getFunctionName().getName());
            System.out.println(new FunctionStructConflict(functionDec.getLine() ,functionDec.getFunctionName().getName()).getMessage());
            err=true;
        } catch (ItemNotFoundException e) {

        }

        SymbolTable.push(symbolTable);

        if(functionDec.getFunctionName() != null){
            functionDec.getFunctionName().accept(this);
        }
        for(VariableDeclaration variableDec: functionDec.getArgs()){
            variableDec.accept(this);
        }
        functionDec.getBody().accept(this);

        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDec) {
        //todo

        mainDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        //todo
        VariableSymbolTableItem variableSymbolTableItem = new VariableSymbolTableItem(variableDec.getVarName());
        SymbolTable symbolTable = new SymbolTable(SymbolTable.root);
        try {
            SymbolTable.root.put(variableSymbolTableItem);
        } catch (ItemAlreadyExistsException e) {

            DuplicateVar exception =  new DuplicateVar(variableDec.getLine(),variableDec.getVarName().getName());
            System.out.println(exception.getMessage());
            variableSymbolTableItem.setName(h+" "+variableSymbolTableItem.getName());
            h++;
            err=true;
            try {
                SymbolTable.root.put(variableSymbolTableItem);
            } catch (ItemAlreadyExistsException ex) {

            }

        }
        try {
            SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY+variableDec.getVarName().getName());
            System.out.println(new VarFunctionConflict(variableDec.getLine() ,variableDec.getVarName().getName()).getMessage());
            err=true;
        } catch (ItemNotFoundException e) {
        }
        try {
            SymbolTable.root.getItem(StructSymbolTableItem.START_KEY+variableDec.getVarName().getName());
            System.out.println(new VarStructConflict(variableDec.getLine() ,variableDec.getVarName().getName()).getMessage());
            err=true;
        } catch (ItemNotFoundException e) {
        }

        SymbolTable.push(symbolTable);
        if(variableDec.getVarName() != null) {
            variableDec.getVarName().accept(this);
        }
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        //todo
        StructSymbolTableItem structSymbolTableItem = new StructSymbolTableItem(structDec);
        SymbolTable symbolTable = new SymbolTable(SymbolTable.root);
        structSymbolTableItem.setStructSymbolTable(symbolTable);
         try {
             SymbolTable.root.put(structSymbolTableItem);
         } catch (ItemAlreadyExistsException e) {

             DuplicateStruct exception =  new DuplicateStruct(structDec.getLine(),structDec.getStructName().getName());
             System.out.println(exception.getMessage());
             structSymbolTableItem.setName(i+" "+structSymbolTableItem.getName());
             i++;
             err=true;
             try {
                 SymbolTable.root.put(structSymbolTableItem);
             } catch (ItemAlreadyExistsException ex) {

             }

         }
        SymbolTable.push(symbolTable);
        if(structDec.getStructName() != null) {
            structDec.getStructName().accept(this);
        }
        structDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        //todo

        if(setGetVarDec.getVarName() != null) {
            setGetVarDec.getVarName().accept(this);
        }
        //check this :
        for(VariableDeclaration variableDec: setGetVarDec.getArgs()){
            variableDec.accept(this);
        }
        if(setGetVarDec.getSetterBody() != null) {
            setGetVarDec.getSetterBody().accept(this);
        }
        if(setGetVarDec.getGetterBody() != null) {
            setGetVarDec.getGetterBody().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        //todo

        if(assignmentStmt.getLValue() != null){
            assignmentStmt.getLValue().accept(this);
        }
        if(assignmentStmt.getRValue() != null){
            assignmentStmt.getRValue().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        //todo

        for(Statement stmt : blockStmt.getStatements()){
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        //todo

        if(conditionalStmt.getCondition() != null){
            conditionalStmt.getCondition().accept(this);
        }
        if(conditionalStmt.getThenBody() != null){
            conditionalStmt.getThenBody().accept(this);
        }
        if(conditionalStmt.getElseBody() != null){
            conditionalStmt.getElseBody().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        //todo
        if(functionCallStmt.getFunctionCall() != null){
            functionCallStmt.getFunctionCall().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        //todo
        if(displayStmt.getArg() != null){
            displayStmt.getArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        //todo
        if(returnStmt.getReturnedExpr() != null){
            returnStmt.getReturnedExpr().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        //todo
        if(loopStmt.getCondition() != null){
            loopStmt.getCondition().accept(this);
        }
        if(loopStmt.getBody() != null){
            loopStmt.getBody().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        //todo
        for(VariableDeclaration variableDec : varDecStmt.getVars()){
            variableDec.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        //todo
        if(listAppendStmt.getListAppendExpr() != null){
            listAppendStmt.getListAppendExpr().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        //todo
        if(listSizeStmt.getListSizeExpr() != null){
            listSizeStmt.getListSizeExpr().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        //todo
        if(binaryExpression.getFirstOperand() != null){
            binaryExpression.getFirstOperand().accept(this);
        }
        if(binaryExpression.getSecondOperand() != null) {
            binaryExpression.getSecondOperand().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        //todo
        if(unaryExpression.getOperand() != null){
            unaryExpression.getOperand().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionCall funcCall) {
        //todo
        if(funcCall.getInstance() != null){
            funcCall.getInstance().accept(this);
        }
        for(Expression expr : funcCall.getArgs()){
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {
        //todo

        return null;
    }

    @Override
    public Void visit(ListAccessByIndex listAccessByIndex) {
        //todo
        if(listAccessByIndex.getInstance() != null){
            listAccessByIndex.getInstance().accept(this);
        }
        if(listAccessByIndex.getIndex() != null) {
            listAccessByIndex.getIndex().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(StructAccess structAccess) {
        //todo
        if(structAccess.getInstance()!= null){
            structAccess.getInstance().accept(this);
        }
        if(structAccess.getElement() != null) {
            structAccess.getElement().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListSize listSize) {
        //todo
        if(listSize.getArg() != null) {
            listSize.getArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListAppend listAppend) {
        //todo
        if(listAppend.getListArg() != null) {
            listAppend.getListArg().accept(this);
        }
        if(listAppend.getElementArg() != null) {
            listAppend.getElementArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ExprInPar exprInPar) {
        //todo
        for(Expression expr : exprInPar.getInputs()){
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IntValue intValue) {
        //todo

        return null;
    }

    @Override
    public Void visit(BoolValue boolValue) {
        //todo
        return null;
    }
}
