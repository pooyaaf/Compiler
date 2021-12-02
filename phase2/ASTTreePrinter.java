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
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        //todo
        messagePrinter(variableDec.getLine(), variableDec.toString());
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        //todo
        messagePrinter(structDec.getLine(), structDec.toString());
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        //todo
        messagePrinter(setGetVarDec.getLine(), setGetVarDec.toString());
        return null;
    }

    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        //todo
        messagePrinter(assignmentStmt.getLine(), assignmentStmt.toString());
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        //todo
        messagePrinter(blockStmt.getLine(), blockStmt.toString());
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        //todo
        messagePrinter(conditionalStmt.getLine(), conditionalStmt.toString());
        return null;
    }

    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        //todo
        messagePrinter(functionCallStmt.getLine(), functionCallStmt.toString());
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        //todo
        messagePrinter(displayStmt.getLine(), displayStmt.toString());
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        //todo
        messagePrinter(returnStmt.getLine(), returnStmt.toString());
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        //todo
        messagePrinter(loopStmt.getLine(), loopStmt.toString());
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        //todo
        messagePrinter(varDecStmt.getLine(), varDecStmt.toString());
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        //todo
        messagePrinter(listAppendStmt.getLine(), listAppendStmt.toString());
        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        //todo
        messagePrinter(listSizeStmt.getLine(), listSizeStmt.toString());
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        //todo
        messagePrinter(binaryExpression.getLine(), binaryExpression.toString());
        return null;
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        //todo
        messagePrinter(unaryExpression.getLine(), unaryExpression.toString());
        return null;
    }

    @Override
    public Void visit(FunctionCall funcCall) {
        //todo
        messagePrinter(funcCall.getLine(), funcCall.toString());
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
        return null;
    }

    @Override
    public Void visit(StructAccess structAccess) {
        //todo
        messagePrinter(structAccess.getLine(), structAccess.toString());
        return null;
    }

    @Override
    public Void visit(ListSize listSize) {
        //todo
        messagePrinter(listSize.getLine(), listSize.toString());
        return null;
    }

    @Override
    public Void visit(ListAppend listAppend) {
        //todo
        messagePrinter(listAppend.getLine(), listAppend.toString());
        return null;
    }

    @Override
    public Void visit(ExprInPar exprInPar) {
        //todo
        messagePrinter(exprInPar.getLine(), exprInPar.toString());
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
