package main.visitor.name;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.visitor.*;

public class ASTTreePrinter extends Visitor<Void> {
    public void messagePrinter(int line, String message){
        System.out.println("Line " + line + ": " + message);
    }

    @Override
    public Void visit(Program program) {
        messagePrinter(program.getLine(), program.toString());
        for (StructDeclaration structDeclaration: program.getStructs())
            structDeclaration.accept(this);
        for (FunctionDeclaration functionDeclaration:program.getFunctions())
            functionDeclaration.accept(this);
        program.getMain().accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDec) {
        //todo
        messagePrinter(functionDec.getLine(), functionDec.toString());
        // return type should be handled ! !!!!!!!

        if(functionDec.getFunctionName() != null){
            functionDec.getFunctionName().accept(this);
        }
        for(VariableDeclaration variableDec: functionDec.getArgs()){
            variableDec.accept(this);
        }
        functionDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDec) {
        //todo
        messagePrinter(mainDec.getLine(), mainDec.toString());
        mainDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        //todo
        messagePrinter(variableDec.getLine(), variableDec.toString());
        //type should be handled ! !!!!!!!
        if(variableDec.getVarName() != null) {
            variableDec.getVarName().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        //todo
        messagePrinter(structDec.getLine(), structDec.toString());
        if(structDec.getStructName() != null) {
            structDec.getStructName().accept(this);
        }
        structDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        //todo
        messagePrinter(setGetVarDec.getLine(), setGetVarDec.toString());
        //type should be handled ! !!!!!!!
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
        messagePrinter(assignmentStmt.getLine(), assignmentStmt.toString());
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
        messagePrinter(blockStmt.getLine(), blockStmt.toString());
        for(Statement stmt : blockStmt.getStatements()){
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        //todo
        messagePrinter(conditionalStmt.getLine(), conditionalStmt.toString());
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
        messagePrinter(functionCallStmt.getLine(), functionCallStmt.toString());
        if(functionCallStmt.getFunctionCall() != null){
            functionCallStmt.getFunctionCall().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        //todo
        messagePrinter(displayStmt.getLine(), displayStmt.toString());
        if(displayStmt.getArg() != null){
            displayStmt.getArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        //todo
        messagePrinter(returnStmt.getLine(), returnStmt.toString());
        if(returnStmt.getReturnedExpr() != null){
            returnStmt.getReturnedExpr().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        //todo
        messagePrinter(loopStmt.getLine(), loopStmt.toString());
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
        messagePrinter(varDecStmt.getLine(), varDecStmt.toString());
        for(VariableDeclaration variableDec : varDecStmt.getVars()){
            variableDec.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        //todo
        messagePrinter(listAppendStmt.getLine(), listAppendStmt.toString());
        if(listAppendStmt.getListAppendExpr() != null){
            listAppendStmt.getListAppendExpr().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        //todo
        messagePrinter(listSizeStmt.getLine(), listSizeStmt.toString());
        if(listSizeStmt.getListSizeExpr() != null){
            listSizeStmt.getListSizeExpr().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        //todo
        messagePrinter(binaryExpression.getLine(), binaryExpression.toString());
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
        messagePrinter(unaryExpression.getLine(), unaryExpression.toString());
        if(unaryExpression.getOperand() != null){
            unaryExpression.getOperand().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionCall funcCall) {
        //todo
        messagePrinter(funcCall.getLine(), funcCall.toString());
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
        messagePrinter(identifier.getLine(), identifier.toString());

        return null;
    }

    @Override
    public Void visit(ListAccessByIndex listAccessByIndex) {
        //todo
        messagePrinter(listAccessByIndex.getLine(), listAccessByIndex.toString());
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
        messagePrinter(structAccess.getLine(), structAccess.toString());
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
        messagePrinter(listSize.getLine(), listSize.toString());
        if(listSize.getArg() != null) {
            listSize.getArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListAppend listAppend) {
        //todo
        messagePrinter(listAppend.getLine(), listAppend.toString());
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
        messagePrinter(exprInPar.getLine(), exprInPar.toString());
        for(Expression expr : exprInPar.getInputs()){
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IntValue intValue) {
        //todo
        messagePrinter(intValue.getLine(), intValue.toString());

        return null;
    }

    @Override
    public Void visit(BoolValue boolValue) {
        //todo
        messagePrinter(boolValue.getLine(), boolValue.toString());
        return null;
    }
}
