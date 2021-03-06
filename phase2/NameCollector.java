package main.visitor.name;

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

public class NameCollector extends Visitor<Void>{
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
            functionDec.addError(exception);
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
    public Void visit(MainDeclaration mainDec) {
       // should be done !!!!!!!!!!!!!!!!!!!!!!
        mainDec.getBody().accept(this);
        return null;
    }
    @Override
    public Void visit(VariableDeclaration variableDec) {
        try {
            SymbolTable.top.put(new VariableSymbolTableItem(variableDec.getVarName()));
        } catch (ItemAlreadyExistsException e) {
            DuplicateVar exception = new DuplicateVar(variableDec.getLine(),variableDec.getVarName().getName());
            variableDec.addError(exception);
        }
        if(variableDec.getVarName() != null) {
            variableDec.getVarName().accept(this);
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
}
