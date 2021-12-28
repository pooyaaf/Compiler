package main.visitor.type;

import main.ast.nodes.Program;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.BinaryExpression;
import main.ast.nodes.expression.FunctionCall;
import main.ast.nodes.expression.UnaryExpression;
import main.ast.nodes.expression.values.primitive.BoolValue;
import main.ast.nodes.expression.values.primitive.IntValue;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.primitives.BoolType;
import main.ast.types.primitives.IntType;
import main.ast.types.primitives.VoidType;
import main.compileError.typeError.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.VariableSymbolTableItem;
import main.visitor.Visitor;

import java.util.ArrayList;

public class TypeChecker extends Visitor<Void> {
    ExpressionTypeChecker expressionTypeChecker;
    public static Type returnType = null;
    public static boolean inGetSet = false;
    public static String structName = null;

    private boolean containsReturn(Statement block) {
        if(!(block instanceof BlockStmt))
            return block instanceof ReturnStmt;
        int conditions = 0;
        var bodyStatements = new ArrayList<>(((BlockStmt)block).getStatements());
        for(var stmt: ((BlockStmt)block).getStatements())
            if(stmt instanceof LoopStmt) {
                if(((LoopStmt) stmt).getBody() instanceof BlockStmt) {
                    bodyStatements.addAll(((BlockStmt) ((LoopStmt) stmt).getBody()).getStatements());
                } else {
                    ((BlockStmt) block).addStatement(((LoopStmt) stmt).getBody());
                }
            }
        for(var stmt: bodyStatements)
            if(stmt instanceof ReturnStmt)
                return true;
        for(var stmt: bodyStatements) {
            if (stmt instanceof ConditionalStmt) {
                conditions++;
                var conditionalStmt = (ConditionalStmt)stmt;
                var ifRet = containsReturn((conditionalStmt.getThenBody()));
                var elseRet = false;
                if(conditionalStmt.getElseBody() != null)
                    elseRet = containsReturn((conditionalStmt.getElseBody()));
                if(!(ifRet & elseRet)) return false;
            }
        }
        return conditions != 0;
    }

    public static boolean sameTypes(Type type1, Type type2) {
        if(type1.getClass().equals(type2.getClass())) {
            if(type1 instanceof ListType) {
                return sameTypes(((ListType) type1).getType(), ((ListType) type2).getType());
            } else if(type1 instanceof StructType) {
                return ((StructType) type1).getStructName().getName().equals(
                        ((StructType) type2).getStructName().getName()
                );
            } else if(type1 instanceof FptrType) {
                var args1 = ((FptrType) type1).getArgsType();
                var args2 = ((FptrType) type2).getArgsType();
                if(args1.size() != args2.size())
                    return false;
                for (int i = 0; i < args1.size(); i++) {
                    if(!sameTypes(args1.get(i),args2.get(i)))
                        return false;
                }
                return sameTypes(((FptrType) type1).getReturnType(), ((FptrType) type2).getReturnType());
            } else return true;
        } else return false;
    }

    public static boolean notDeclaredStructType(Type type, Declaration varDec) {
        if(type instanceof StructType) {
            var name  = ((StructType) type).getStructName().getName();
            try {
                SymbolTable.top.getItem(StructSymbolTableItem.START_KEY + name);
                return false;
            } catch (ItemNotFoundException e) {
                varDec.addError(new StructNotDeclared(varDec.getLine(), name));
                return true;
            }
        } else if (type instanceof ListType) {
            return notDeclaredStructType(((ListType) type).getType(), varDec);
        } else if (type instanceof FptrType) {
            boolean declared = false;
            for(var argType: ((FptrType) type).getArgsType())
                declared |= notDeclaredStructType(argType, varDec);

            return declared | notDeclaredStructType(((FptrType) type).getReturnType(), varDec);
        } else return false;
    }

    public TypeChecker() {
        this.expressionTypeChecker = new ExpressionTypeChecker();
    }

    @Override
    public Void visit(Program program) {
        for(var structDec: program.getStructs())
            structDec.accept(this);
        for(var functionDec: program.getFunctions())
            functionDec.accept(this);
        program.getMain().accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDec) {
        var argsTypes = new ArrayList<Type>();
        for(var arg: functionDec.getArgs()) {
            argsTypes.add(arg.getVarType());
        }
        var type = functionDec.getReturnType();
        var item = new VariableSymbolTableItem(functionDec.getFunctionName());
        item.setType(new FptrType(argsTypes, type));
        try {
            SymbolTable.top.put(item);
        } catch (ItemAlreadyExistsException ignored) {}

        if(notDeclaredStructType(type, functionDec))
            item.setType(new FptrType(argsTypes, new NoType()));

        for(var varDec: functionDec.getArgs())
            varDec.accept(this);

        if(!(type instanceof VoidType) && !containsReturn(functionDec.getBody()))
            functionDec.addError(new MissingReturnStatement(functionDec.getLine(),functionDec.getFunctionName().getName()));

        SymbolTable.push(new SymbolTable(SymbolTable.top));
        returnType = type;
        functionDec.getBody().accept(this);
        returnType = null;
        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDec) {
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        returnType = null;
        mainDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        var item = new VariableSymbolTableItem(variableDec.getVarName());
        var type = variableDec.getVarType();
        item.setType(type);

        if(notDeclaredStructType(type, variableDec))
            item.setType(new NoType());

        try {
            if(inGetSet) {
                type = new NoType();
                item.setType(type);
            }
            if(structName != null)
                item.setName(structName + "$" + item.getName());
            SymbolTable.top.put(item);
            var defVal = variableDec.getDefaultValue();
            if(defVal != null) {
                var defValType = defVal.accept(this.expressionTypeChecker);
                if(defValType instanceof VoidType)
                    variableDec.addError(new CantUseValueOfVoidFunction(variableDec.getLine()));
                else if(!sameTypes(type, defValType) && !(type instanceof NoType) && !(defValType instanceof NoType))
                    variableDec.addError(new UnsupportedOperandType(variableDec.getLine(), "assign"));
            }
        } catch (ItemAlreadyExistsException ignored) {}

        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        try {
            SymbolTable.top.put(new StructSymbolTableItem(structDec));
        } catch (ItemAlreadyExistsException ignored) {}
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        structName = structDec.getStructName().getName();
        structDec.getBody().accept(this);
        structName = null;
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        setGetVarDec.getVarDec().accept(this);
        var type = setGetVarDec.getVarType();

        SymbolTable.push(new SymbolTable(SymbolTable.top));
        for(var varDec: setGetVarDec.getArgs())
            varDec.accept(this);
        returnType = null;
        inGetSet = true;
        setGetVarDec.getSetterBody().accept(this);
        inGetSet = false;
        SymbolTable.pop();

        SymbolTable.push(new SymbolTable(SymbolTable.top));
        inGetSet = true;
        returnType = type;
        setGetVarDec.getGetterBody().accept(this);
        returnType = null;
        inGetSet = false;
        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        var typeR = assignmentStmt.getRValue().accept(this.expressionTypeChecker);
        if(typeR instanceof VoidType) {
            assignmentStmt.addError(new CantUseValueOfVoidFunction(assignmentStmt.getLine()));
            typeR = new NoType();
        }
        var lVal = assignmentStmt.getLValue();
        if(lVal instanceof BoolValue || lVal instanceof IntValue ||
            lVal instanceof BinaryExpression || lVal instanceof UnaryExpression ||
            lVal instanceof FunctionCall)
            assignmentStmt.addError(new LeftSideNotLvalue(assignmentStmt.getLine()));

        var typeL = lVal.accept(this.expressionTypeChecker);
        if(!(typeR instanceof NoType) && !(typeL instanceof NoType) && !sameTypes(typeL,typeR))
            assignmentStmt.addError(new UnsupportedOperandType(assignmentStmt.getLine(), "assign"));

        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        for(var stmt: blockStmt.getStatements())
            stmt.accept(this);
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        var type = conditionalStmt.getCondition().accept(this.expressionTypeChecker);
        if(type instanceof VoidType)
            conditionalStmt.addError(new CantUseValueOfVoidFunction(conditionalStmt.getLine()));
        else if(!(type instanceof BoolType || type instanceof NoType))
            conditionalStmt.addError(new ConditionNotBool(conditionalStmt.getLine()));
        SymbolTable.push(new SymbolTable(SymbolTable.top));
        conditionalStmt.getThenBody().accept(this);
        SymbolTable.pop();
        if(conditionalStmt.getElseBody() != null) {
            SymbolTable.push(new SymbolTable(SymbolTable.top));
            conditionalStmt.getElseBody().accept(this);
            SymbolTable.pop();
        }

        return null;
    }

    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        functionCallStmt.getFunctionCall().accept(this.expressionTypeChecker);
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        var type = displayStmt.getArg().accept(this.expressionTypeChecker);
        if(type instanceof VoidType)
            displayStmt.addError(new CantUseValueOfVoidFunction(displayStmt.getLine()));
        else if(!(type instanceof BoolType) && !(type instanceof IntType) && !(type instanceof NoType))
            displayStmt.addError(new UnsupportedTypeForDisplay(displayStmt.getLine()));
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        if(returnType == null)
            returnStmt.addError(new CannotUseReturn(returnStmt.getLine()));

        var exp = returnStmt.getReturnedExpr();
        Type type = new VoidType();
        if(exp != null) {
            type = exp.accept(this.expressionTypeChecker);
            if(type instanceof VoidType)
                returnStmt.addError(new CantUseValueOfVoidFunction(returnStmt.getLine()));
        }
        if(returnType != null && !(type instanceof NoType) && !sameTypes(type, returnType)) {
            returnStmt.addError(new ReturnValueNotMatchFunctionReturnType(returnStmt.getLine()));
        }
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        var type = loopStmt.getCondition().accept(this.expressionTypeChecker);
        if(type instanceof VoidType)
            loopStmt.addError(new CantUseValueOfVoidFunction(loopStmt.getLine()));
        else if(!(type instanceof BoolType || type instanceof NoType))
            loopStmt.addError(new ConditionNotBool(loopStmt.getLine()));

        loopStmt.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        if(inGetSet)
            varDecStmt.addError(new CannotUseDefineVar(varDecStmt.getLine()));
        for(var varDec: varDecStmt.getVars())
            varDec.accept(this);
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        listAppendStmt.getListAppendExpr().accept(this.expressionTypeChecker);
        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        listSizeStmt.getListSizeExpr().accept(this.expressionTypeChecker);
        return null;
    }
}
