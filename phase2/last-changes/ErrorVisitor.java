package main.visitor.name;

import main.compileError.nameError.FunctionStructConflict;
import main.compileError.nameError.VarFunctionConflict;
import main.compileError.nameError.VarStructConflict;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.SymbolTableItem;
import main.visitor.Visitor;
import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.compileError.nameError.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.*;
import main.symbolTable.items.*;
import main.symbolTable.utils.*;
import main.visitor.*;


import javax.swing.plaf.synth.SynthCheckBoxMenuItemUI;

public class ErrorVisitor extends Visitor<Void> {
    public static boolean err=false;
    @Override
    public Void visit(Program program) {

        SymbolTable.push(new SymbolTable());
        SymbolTable.root = SymbolTable.top;
        //
        for (StructDeclaration structDeclaration: program.getStructs())
            structDeclaration.accept(this);
        for (FunctionDeclaration functionDeclaration:program.getFunctions())
            functionDeclaration.accept(this);
        program.getMain().accept(this);



        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDec) {

        FunctionSymbolTableItem functionSymbolTableItem = new FunctionSymbolTableItem(functionDec);
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        functionSymbolTableItem.setFunctionSymbolTable(SymbolTable.top);

        try {
            SymbolTable.root.put(functionSymbolTableItem);
        }catch (ItemAlreadyExistsException e){
            DuplicateFunction exception = new DuplicateFunction(functionDec.getLine(),functionDec.getFunctionName().getName());
            System.out.println(new DuplicateFunction(functionDec.getLine(),functionDec.getFunctionName().getName()));
            functionDec.addError(exception);
        }
        try {
            SymbolTable.root.getItem(StructSymbolTableItem.START_KEY+functionDec.getFunctionName().getName());
            System.out.println(new FunctionStructConflict(functionDec.getLine() ,functionDec.getFunctionName().getName()).getMessage());
            err=true;
        } catch (ItemNotFoundException e) {

        }

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
    public Void visit(MainDeclaration mainDeclaration) {
        mainDeclaration.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDeclaration) {
        try {
            SymbolTable.top.put(new VariableSymbolTableItem(variableDeclaration.getVarName()));
        } catch (ItemAlreadyExistsException e) {
            DuplicateVar exception = new DuplicateVar(variableDeclaration.getLine(),variableDeclaration.getVarName().getName());
            variableDeclaration.addError(exception);
        }
        try {
            SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY+variableDeclaration.getVarName().getName());
            System.out.println(new VarFunctionConflict(variableDeclaration.getLine() ,variableDeclaration.getVarName().getName()).getMessage());
        } catch (ItemNotFoundException e) {
        }
        try {
            SymbolTable.root.getItem(StructSymbolTableItem.START_KEY+variableDeclaration.getVarName().getName());
            System.out.println(new VarStructConflict(variableDeclaration.getLine() ,variableDeclaration.getVarName().getName()).getMessage());
        } catch (ItemNotFoundException e) {
        }
        if(variableDeclaration.getVarName() != null) {
            variableDeclaration.getVarName().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        StructSymbolTableItem structSymbolTableItem = new StructSymbolTableItem(structDec);
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        structSymbolTableItem.setStructSymbolTable(SymbolTable.top);
        try {
            SymbolTable.root.put(structSymbolTableItem);
        }catch (ItemAlreadyExistsException e){
            DuplicateStruct exception = new DuplicateStruct(structDec.getLine(),structDec.getStructName().getName());
            structDec.addError(exception);
        }
        if(structDec.getStructName() != null) {
            structDec.getStructName().accept(this);
        }
        structDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
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
        return  null;
    }

    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        return super.visit(assignmentStmt);
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        return super.visit(blockStmt);
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        return super.visit(conditionalStmt);
    }

    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        return super.visit(functionCallStmt);
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        return super.visit(displayStmt);
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        return super.visit(returnStmt);
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        return super.visit(loopStmt);
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        return super.visit(varDecStmt);
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        return super.visit(listAppendStmt);
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        return super.visit(listSizeStmt);
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        return super.visit(binaryExpression);
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        return super.visit(unaryExpression);
    }

    @Override
    public Void visit(Identifier identifier) {
        return super.visit(identifier);
    }

    @Override
    public Void visit(ListAccessByIndex listAccessByIndex) {
        return super.visit(listAccessByIndex);
    }

    @Override
    public Void visit(StructAccess structAccess) {
        return super.visit(structAccess);
    }

    @Override
    public Void visit(FunctionCall functionCall) {
        return super.visit(functionCall);
    }

    @Override
    public Void visit(IntValue intValue) {
        return super.visit(intValue);
    }

    @Override
    public Void visit(BoolValue boolValue) {
        return super.visit(boolValue);
    }

    @Override
    public Void visit(ListSize listSize) {
        return super.visit(listSize);
    }

    @Override
    public Void visit(ListAppend listAppend) {
        return super.visit(listAppend);
    }

    @Override
    public Void visit(ExprInPar exprInPar) {
        return super.visit(exprInPar);
    }
}
