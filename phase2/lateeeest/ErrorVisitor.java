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

import java.util.ArrayList;

public class ErrorVisitor extends Visitor<Void> {
    public static boolean err = false;
    public static boolean isInSetGet = false;
    public int i = 0;
    public int j = 0;
    public int h = 0;

    @Override
    public Void visit(Program program) {
        SymbolTable.root = new SymbolTable();
        SymbolTable.top = SymbolTable.root;
        SymbolTable.push(SymbolTable.root);


        for (StructDeclaration structDeclaration : program.getStructs()) {
            structDeclaration.accept(this);
        }
        for (FunctionDeclaration functionDeclaration : program.getFunctions())
            functionDeclaration.accept(this);
        program.getMain().accept(this);
        SymbolTable.pop();
        return null;
    }


    @Override


    public Void visit(StructDeclaration structDec) {
        //todo
        StructSymbolTableItem structSymbolTableItem = new StructSymbolTableItem(structDec);
        SymbolTable.push(new SymbolTable(SymbolTable.root));
        structSymbolTableItem.setStructSymbolTable(SymbolTable.top);

        try {
            SymbolTable.root.put(structSymbolTableItem);
        } catch (ItemAlreadyExistsException e) {

            DuplicateStruct exception = new DuplicateStruct(structDec.getLine(), structDec.getStructName().getName());
            System.out.println(exception.getMessage());
            Integer temp = i;
            structSymbolTableItem.setName(structSymbolTableItem.getName() + temp.toString());
            i++;
            err = true;
            try {
                SymbolTable.root.put(structSymbolTableItem);
            } catch (ItemAlreadyExistsException ex) {

            }

        }

        structDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }


    @Override
    public Void visit(FunctionDeclaration functionDec) {
        //todo
        FunctionSymbolTableItem functionSymbolTableItem = new FunctionSymbolTableItem(functionDec);
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        SymbolTable symbolTable = new SymbolTable(SymbolTable.top);
        functionSymbolTableItem.setFunctionSymbolTable(symbolTable);

        try {
            SymbolTable.root.put(functionSymbolTableItem);
        } catch (ItemAlreadyExistsException e) {

            DuplicateFunction exception = new DuplicateFunction(functionDec.getLine(), functionDec.getFunctionName().getName());
            System.out.println(exception.getMessage());
            Integer temp = j;
            functionSymbolTableItem.setName(functionSymbolTableItem.getName().toString() + temp.toString());
            j++;
            err = true;
            try {
                SymbolTable.root.put(functionSymbolTableItem);
            } catch (ItemAlreadyExistsException ex) {

            }

        }

        try {
            SymbolTable.root.getItem(StructSymbolTableItem.START_KEY + functionDec.getFunctionName().getName());
            System.out.println(new FunctionStructConflict(functionDec.getLine(), functionDec.getFunctionName().getName()).getMessage());
            err = true;
        } catch (ItemNotFoundException e) {

        }

        if (functionDec.getFunctionName() != null) {
            functionDec.getFunctionName().accept(this);
        }
        for (VariableDeclaration variableDec : functionDec.getArgs()) {
            variableDec.accept(this);
        }
        functionDec.getBody().accept(this);

        SymbolTable.pop();
        return super.visit(functionDec);
    }

    @Override
    public Void visit(MainDeclaration mainDec) {
        //todo
        SymbolTable symbolTable = new SymbolTable(SymbolTable.root);
        SymbolTable.push(symbolTable);
        mainDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        //todo
        if (isInSetGet == false){
            VariableSymbolTableItem variableSymbolTableItem = new VariableSymbolTableItem(variableDec.getVarName());
           // SymbolTable symbolTable = new SymbolTable(SymbolTable.root);
            try {
                SymbolTable.top.getItem(variableSymbolTableItem.getKey());
                System.out.println(new DuplicateVar(variableDec.getLine(), variableDec.getVarName().getName()).getMessage());
                Integer temp = h;
                variableSymbolTableItem.setName(variableSymbolTableItem.getName() + temp.toString());
                h++;
                err = true;
            } catch (ItemNotFoundException e) {
            }

            try {
                SymbolTable.top.put(variableSymbolTableItem);
            } catch (ItemAlreadyExistsException e) {

                DuplicateVar exception = new DuplicateVar(variableDec.getLine(), variableDec.getVarName().getName());
                System.out.println(exception.getMessage());
                Integer temp = h;
                variableSymbolTableItem.setName(variableSymbolTableItem.getName() + temp.toString());
                h++;
                err = true;
                try {
                    SymbolTable.top.put(variableSymbolTableItem);
                } catch (ItemAlreadyExistsException ex) {

                }

            }
            try {
                SymbolTable.root.getItem(FunctionSymbolTableItem.START_KEY + variableDec.getVarName().getName());
                System.out.println(new VarFunctionConflict(variableDec.getLine(), variableDec.getVarName().getName()).getMessage());
                err = true;
            } catch (ItemNotFoundException e) {
            }
            try {
                SymbolTable.root.getItem(StructSymbolTableItem.START_KEY + variableDec.getVarName().getName());
                System.out.println(new VarStructConflict(variableDec.getLine(), variableDec.getVarName().getName()).getMessage());
                err = true;
            } catch (ItemNotFoundException e) {
            }

            //SymbolTable.push(symbolTable);
            if (variableDec.getVarName() != null) {
                variableDec.getVarName().accept(this);
            }
            //SymbolTable.pop();
            return super.visit(variableDec);
        } else
            return null;
    }


    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        //todo
        VariableSymbolTableItem variableSymbolTableItem = new VariableSymbolTableItem(setGetVarDec.getVarName());
        SymbolTable symbolTable = new SymbolTable(SymbolTable.root);
        try {
            SymbolTable.top.put(variableSymbolTableItem);
        } catch (ItemAlreadyExistsException e) {

            DuplicateVar exception = new DuplicateVar(setGetVarDec.getLine(), setGetVarDec.getVarName().getName());
            System.out.println(exception.getMessage());
            Integer temp = h;
            variableSymbolTableItem.setName(variableSymbolTableItem.getName() + temp.toString());
            h++;
            err = true;
            try {
                SymbolTable.top.put(variableSymbolTableItem);
            } catch (ItemAlreadyExistsException ex) {

            }

        }
        try {
            SymbolTable.top.getItem(FunctionSymbolTableItem.START_KEY + setGetVarDec.getVarName().getName());
            System.out.println(new VarFunctionConflict(setGetVarDec.getLine(), setGetVarDec.getVarName().getName()).getMessage());
            err = true;
        } catch (ItemNotFoundException e) {
        }
        try {
            SymbolTable.top.getItem(StructSymbolTableItem.START_KEY + setGetVarDec.getVarName().getName());
            System.out.println(new VarStructConflict(setGetVarDec.getLine(), setGetVarDec.getVarName().getName()).getMessage());
            err = true;
        } catch (ItemNotFoundException e) {
        }


        if (setGetVarDec.getVarName() != null) {
            setGetVarDec.getVarName().accept(this);
        }

        //check this :
        for (VariableDeclaration variableDec : setGetVarDec.getArgs()) {
            variableDec.accept(this);
        }
        isInSetGet = true;
        if (setGetVarDec.getSetterBody() != null) {
            setGetVarDec.getSetterBody().accept(this);
        }
        if (setGetVarDec.getGetterBody() != null) {
            setGetVarDec.getGetterBody().accept(this);
        }
        isInSetGet = false;
        return null;
    }

    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        //todo

        if (assignmentStmt.getLValue() != null) {
            assignmentStmt.getLValue().accept(this);
        }
        if (assignmentStmt.getRValue() != null) {
            assignmentStmt.getRValue().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        //todo

        for (Statement stmt : blockStmt.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        //todo

        if (conditionalStmt.getCondition() != null) {
            conditionalStmt.getCondition().accept(this);
        }
        if (conditionalStmt.getThenBody() != null) {
            SymbolTable.push(new SymbolTable(SymbolTable.top));
            conditionalStmt.getThenBody().accept(this);
            SymbolTable.pop();
        }
        if (conditionalStmt.getElseBody() != null) {
            SymbolTable.push(new SymbolTable(SymbolTable.top));
            conditionalStmt.getElseBody().accept(this);
            SymbolTable.pop();
        }
        return null;
    }

    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        //todo
        if (functionCallStmt.getFunctionCall() != null) {
            functionCallStmt.getFunctionCall().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        //todo
        if (displayStmt.getArg() != null) {
            displayStmt.getArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        //todo
        if (returnStmt.getReturnedExpr() != null) {
            returnStmt.getReturnedExpr().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        //todo
        SymbolTable symbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(symbolTable);
        if (loopStmt.getCondition() != null) {
            loopStmt.getCondition().accept(this);
        }

        if (loopStmt.getBody() != null) {
            loopStmt.getBody().accept(this);
        }
        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        //todo
        for (VariableDeclaration variableDec : varDecStmt.getVars()) {
            variableDec.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        //todo
        if (listAppendStmt.getListAppendExpr() != null) {
            listAppendStmt.getListAppendExpr().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        //todo
        if (listSizeStmt.getListSizeExpr() != null) {
            listSizeStmt.getListSizeExpr().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        //todo
        if (binaryExpression.getFirstOperand() != null) {
            binaryExpression.getFirstOperand().accept(this);
        }
        if (binaryExpression.getSecondOperand() != null) {
            binaryExpression.getSecondOperand().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        //todo
        if (unaryExpression.getOperand() != null) {
            unaryExpression.getOperand().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionCall funcCall) {
        //todo
        if (funcCall.getInstance() != null) {
            funcCall.getInstance().accept(this);
        }
        for (Expression expr : funcCall.getArgs()) {
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
        if (listAccessByIndex.getInstance() != null) {
            listAccessByIndex.getInstance().accept(this);
        }
        if (listAccessByIndex.getIndex() != null) {
            listAccessByIndex.getIndex().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(StructAccess structAccess) {
        //todo
        if (structAccess.getInstance() != null) {
            structAccess.getInstance().accept(this);
        }
        if (structAccess.getElement() != null) {
            structAccess.getElement().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListSize listSize) {
        //todo
        if (listSize.getArg() != null) {
            listSize.getArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ListAppend listAppend) {
        //todo
        if (listAppend.getListArg() != null) {
            listAppend.getListArg().accept(this);
        }
        if (listAppend.getElementArg() != null) {
            listAppend.getElementArg().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ExprInPar exprInPar) {
        //todo
        for (Expression expr : exprInPar.getInputs()) {
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
