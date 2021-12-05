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

public class NameChecker extends Visitor<Void>{
    public Boolean hasNotError = true;
    Program root;
    private String currentFunctionName;
    private String currentVarName;
    private String currentStructName;


    private SymbolTable getCurrentFunctionSymbolTable() {
        try {
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem)
                    SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + this.currentFunctionName);
            return functionSymbolTableItem.getFunctionSymbolTable();
        } catch (ItemNotFoundException ignored) {
            return null;
        }
    }


    private SymbolTable getCurrentStructSymbolTable(){
        try {
            StructSymbolTableItem structSymbolTableItem = (StructSymbolTableItem)
                    SymbolTable.root.getItem(StructSymbolTableItem.START_KEY + this.currentStructName);
            return structSymbolTableItem.getStructSymbolTable();
        } catch (ItemNotFoundException ignored) {
            return null;
        }
    }

    @Override
    public Void visit(Program program) {
        this.root = program;
        //mytodo
        for (StructDeclaration structDeclaration: program.getStructs())
            structDeclaration.accept(this);

        for(FunctionDeclaration functionDeclaration : program.getFunctions()) {
            this.currentFunctionName = functionDeclaration.getFunctionName().getName();
            functionDeclaration.accept(this);
        }
        program.getMain().accept(this);
        return null;
    }


    @Override
    public Void visit(FunctionDeclaration functionDec) {

        try {
            SymbolTable functionSymbolTable = this.getCurrentFunctionSymbolTable();
            functionSymbolTable.getItem(FunctionSymbolTableItem.START_KEY + functionDec.getFunctionName().getName());
            DuplicateFunction exception = new DuplicateFunction(functionDec.getLine() , functionDec.getFunctionName().getName());
            functionDec.addError(exception);
        } catch (ItemNotFoundException ignored) {
        }

        boolean errored = false;



        try {
            SymbolTable functionSymbolTable = this.getCurrentFunctionSymbolTable();
            functionSymbolTable.getItem(StructSymbolTableItem.START_KEY + functionDec.getFunctionName().getName());
            FunctionStructConflict exception = new FunctionStructConflict(functionDec.getLine() , functionDec.getFunctionName().getName());
            functionDec.addError(exception);
            errored = true;
        } catch (ItemNotFoundException ignored) {
        }



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
    public Void visit(StructDeclaration structDec) {
        try {
            SymbolTable functionSymbolTable = this.getCurrentFunctionSymbolTable();
            functionSymbolTable.getItem(StructSymbolTableItem.START_KEY + structDec.getStructName().getName());
            DuplicateStruct exception = new DuplicateStruct(structDec.getLine() , structDec.getStructName().getName());
            structDec.addError(exception);
        } catch (ItemNotFoundException ignored) {
        }

        if(structDec.getStructName() != null) {
            structDec.getStructName().accept(this);
        }
        structDec.getBody().accept(this);
        return null;
    }


    @Override
    public Void visit(VariableDeclaration variableDec) {
        try {
            SymbolTable functionSymbolTable = this.getCurrentFunctionSymbolTable();
            functionSymbolTable.getItem(VariableSymbolTableItem.START_KEY + variableDec.getVarName().getName());
            DuplicateVar exception = new DuplicateVar(variableDec.getLine() , variableDec.getVarName().getName());
            variableDec.addError(exception);
        } catch (ItemNotFoundException ignored) {
        }


        try {
            SymbolTable functionSymbolTable = this.getCurrentStructSymbolTable();
            functionSymbolTable.getItem(VariableSymbolTableItem.START_KEY + variableDec.getVarName().getName());
            VarFunctionConflict exception = new VarFunctionConflict(variableDec.getLine() , variableDec.getVarName().getName());
            variableDec.addError(exception);

        } catch (ItemNotFoundException ignored) {
        }





        try {
            SymbolTable structSymbolTable = this.getCurrentFunctionSymbolTable();
            structSymbolTable.getItem(VariableSymbolTableItem.START_KEY + variableDec.getVarName().getName());
            VarStructConflict exception = new VarStructConflict(variableDec.getLine() , variableDec.getVarName().getName());
            variableDec.addError(exception);

        } catch (ItemNotFoundException ignored) {
        }

        if(variableDec.getVarName() != null) {
            variableDec.getVarName().accept(this);
        }

        return null;
    }





    public Void visit(SetGetVarDeclaration setGetVarDec) {
        if(setGetVarDec.getVarName() != null) {
            setGetVarDec.getVarName().accept(this);
        }

        try {
            SymbolTable functionSymbolTable = this.getCurrentFunctionSymbolTable();
            functionSymbolTable.getItem(VariableSymbolTableItem.START_KEY + setGetVarDec.getVarName().getName());
            DuplicateVar exception = new DuplicateVar(setGetVarDec.getLine() , setGetVarDec.getVarName().getName());
            setGetVarDec.addError(exception);
        } catch (ItemNotFoundException ignored) {
        }


        try {
            SymbolTable functionSymbolTable = this.getCurrentStructSymbolTable();
            functionSymbolTable.getItem(VariableSymbolTableItem.START_KEY + setGetVarDec.getVarName().getName());
            VarFunctionConflict exception = new VarFunctionConflict(setGetVarDec.getLine() , setGetVarDec.getVarName().getName());
            setGetVarDec.addError(exception);

        } catch (ItemNotFoundException ignored) {
        }





        try {
            SymbolTable structSymbolTable = this.getCurrentFunctionSymbolTable();
            structSymbolTable.getItem(VariableSymbolTableItem.START_KEY + setGetVarDec.getVarName().getName());
            VarStructConflict exception = new VarStructConflict(setGetVarDec.getLine() , setGetVarDec.getVarName().getName());
            setGetVarDec.addError(exception);

        } catch (ItemNotFoundException ignored) {
        }




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
