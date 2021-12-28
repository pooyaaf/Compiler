package main.visitor.type;

import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.expression.operators.UnaryOperator;
import main.ast.nodes.expression.values.primitive.BoolValue;
import main.ast.nodes.expression.values.primitive.IntValue;
import main.ast.types.*;
import main.ast.types.primitives.BoolType;
import main.ast.types.primitives.IntType;
import main.ast.types.primitives.VoidType;
import main.compileError.typeError.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.VariableSymbolTableItem;
import main.visitor.Visitor;

import java.util.ArrayList;

public class ExpressionTypeChecker extends Visitor<Type> {

    @Override
    public Type visit(BinaryExpression binaryExpression) {
        var typeF = binaryExpression.getFirstOperand().accept(this);
        var typeS = binaryExpression.getSecondOperand().accept(this);
        boolean fVoid = typeF instanceof VoidType;
        boolean sVoid = typeS instanceof VoidType;
        boolean hasVoid = fVoid || sVoid;
        if(fVoid)
            binaryExpression.addError(new CantUseValueOfVoidFunction(binaryExpression.getLine()));
        if(sVoid)
            binaryExpression.addError(new CantUseValueOfVoidFunction(binaryExpression.getLine()));

        var op = binaryExpression.getBinaryOperator();
        boolean boolOp = op == BinaryOperator.and || op == BinaryOperator.or;
        boolean eqOp = op == BinaryOperator.eq || op == BinaryOperator.assign;
        boolean intOp = !(boolOp || eqOp);
        boolean fSupport = !fVoid && (boolOp && !(typeF instanceof BoolType) || intOp && !(typeF instanceof IntType));
        boolean sSupport = !sVoid && (boolOp && !(typeS instanceof BoolType) || intOp && !(typeS instanceof IntType));

        if(typeF instanceof NoType && !(typeS instanceof NoType)) {
            if(sSupport)
                binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().toString()));
            return new NoType();
        } else if(!(typeF instanceof NoType) && typeS instanceof NoType) {
            if(fSupport)
                binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().toString()));
            return new NoType();
        } else if(typeF instanceof NoType) { // both no type
            return new NoType();
        } else {
            if(!TypeChecker.sameTypes(typeF,typeS) && eqOp) {
                binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(), "eq"));
                return new NoType();
            }
            if(fSupport) {
                binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().toString()));
                return new NoType();
            }
            if(sSupport) {
                binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().toString()));
                return new NoType();
            }
            return hasVoid? new NoType() : typeF;
        }
    }

    @Override
    public Type visit(UnaryExpression unaryExpression) {
        var typeO = unaryExpression.getOperand().accept(this);
        if(typeO instanceof VoidType) {
            unaryExpression.addError(new CantUseValueOfVoidFunction(unaryExpression.getLine()));
            return new NoType();
        }
        if(unaryExpression.getOperator() == UnaryOperator.minus && !(typeO instanceof IntType) && !(typeO instanceof NoType)) {
            unaryExpression.addError(new UnsupportedOperandType(unaryExpression.getLine(), unaryExpression.getOperator().toString()));
            return new NoType();
        }
        if(unaryExpression.getOperator() == UnaryOperator.not && !(typeO instanceof BoolType) && !(typeO instanceof NoType)) {
            unaryExpression.addError(new UnsupportedOperandType(unaryExpression.getLine(), unaryExpression.getOperand().toString()));
            return new NoType();
        }
        return typeO;
    }

    @Override
    public Type visit(FunctionCall funcCall) {
        var types = new ArrayList<Type>();
        for(var arg: funcCall.getArgs()) {
            types.add(arg.accept(this));
        }
        var type= funcCall.getInstance().accept(this);
        if(type instanceof NoType) return new NoType();
        else if(type instanceof VoidType) {
            funcCall.addError(new CantUseValueOfVoidFunction(funcCall.getLine()));
            return new NoType();
        }
        if(!(type instanceof FptrType))
            funcCall.addError(new CallOnNoneFptrType(funcCall.getLine()));
        else {
            var fType = (FptrType)type;
            if(fType.getArgsType().size() != types.size()) {
                funcCall.addError(new ArgsInFunctionCallNotMatchDefinition(funcCall.getLine()));
                return fType.getReturnType();
            }
            for (int i = 0; i < types.size(); i++) {
                if(!(types.get(i) instanceof NoType) && !TypeChecker.sameTypes(types.get(i),fType.getArgsType().get(i))) {
                    funcCall.addError(new ArgsInFunctionCallNotMatchDefinition(funcCall.getLine()));
                    return fType.getReturnType();
                }
            }
            return fType.getReturnType();
        }
        return new NoType();
    }

    @Override
    public Type visit(Identifier identifier) {
        var name = identifier.getName();
        try {
            var insideKey = (TypeChecker.structName == null)? "" : TypeChecker.structName + "$";
            var key = VariableSymbolTableItem.START_KEY + insideKey + name;
            var item = SymbolTable.top.getItem(key);
            return ((VariableSymbolTableItem)item).getType();
        } catch (ItemNotFoundException e) {
            identifier.addError(new VarNotDeclared(identifier.getLine(), identifier.getName()));
            return new NoType();
        }
    }

    @Override
    public Type visit(ListAccessByIndex listAccessByIndex) {
        var typeI = listAccessByIndex.getIndex().accept(this);
        var typeL = listAccessByIndex.getInstance().accept(this);
        if(typeI instanceof VoidType) {
            listAccessByIndex.addError(new CantUseValueOfVoidFunction(listAccessByIndex.getLine()));
            return new NoType();
        } else if(typeI instanceof NoType)
            return new NoType();
        else if(!(typeI instanceof IntType)) {
            listAccessByIndex.addError(new ListIndexNotInt(listAccessByIndex.getLine()));
            return new NoType();
        }
        if(typeL instanceof VoidType) {
            listAccessByIndex.addError(new CantUseValueOfVoidFunction(listAccessByIndex.getLine()));
            return new NoType();
        }
        else if(typeL instanceof NoType) {
            return new NoType();
        }
        else if(!(typeL instanceof ListType)) {
            listAccessByIndex.addError(new AccessByIndexOnNonList(listAccessByIndex.getLine()));
            return new NoType();
        }
        else return ((ListType)typeL).getType();
    }

    @Override
    public Type visit(StructAccess structAccess) {
        var type = structAccess.getInstance().accept(this);
        if(type instanceof VoidType) {
            structAccess.addError(new CantUseValueOfVoidFunction(structAccess.getLine()));
            return new NoType();
        } else if (type instanceof NoType) return new NoType();
        else if(!(type instanceof StructType)) {
            structAccess.addError(new AccessOnNonStruct(structAccess.getLine()));
            return new NoType();
        }
        var structName = ((StructType) type).getStructName().getName();
        var elName = structAccess.getElement().getName();
        try {
            var item = SymbolTable.top.getItem(
                    VariableSymbolTableItem.START_KEY + structName + "$" + elName
            );
            return ((VariableSymbolTableItem)item).getType();
        } catch (ItemNotFoundException e) {
            structAccess.addError(new StructMemberNotFound(structAccess.getLine(), structName, elName));
            return new NoType();
        }
    }

    @Override
    public Type visit(ListSize listSize) {
        var type = listSize.getArg().accept(this);
        if(type instanceof VoidType) {
            listSize.addError(new CantUseValueOfVoidFunction(listSize.getLine()));
            return new NoType();
        } else if(type instanceof NoType) return new NoType();
        else if(!(type instanceof ListType)) {
            listSize.addError(new GetSizeOfNonList(listSize.getLine()));
            return new NoType();
        }
        return new IntType();
    }

    @Override
    public Type visit(ListAppend listAppend) {
        var typeL = listAppend.getListArg().accept(this);
        boolean retNoType = false;
        var typeAr = listAppend.getElementArg().accept(this);
        if(typeAr instanceof VoidType) {
            listAppend.addError(new CantUseValueOfVoidFunction(listAppend.getLine()));
            retNoType = true;
        } else if(typeAr instanceof NoType) retNoType = true;

        if(typeL instanceof VoidType) {
            listAppend.addError(new CantUseValueOfVoidFunction(listAppend.getLine()));
            retNoType = true;
        }
        else if(typeL instanceof NoType) retNoType = true;
        else if(!(typeL instanceof ListType)) {
            listAppend.addError(new AppendToNonList(listAppend.getLine()));
            retNoType = true;
        }
        else {
            var typeEl = ((ListType) typeL).getType();
            if (!TypeChecker.sameTypes(typeEl, typeAr)) {
                listAppend.addError(new NewElementTypeNotMatchListType(listAppend.getLine()));
                retNoType = true;
            }
        }

        return retNoType? new NoType() : new VoidType();
    }

    @Override
    public Type visit(ExprInPar exprInPar) {
        return exprInPar.getInputs().get(0).accept(this);
    }

    @Override
    public Type visit(IntValue intValue) {
        return new IntType();
    }

    @Override
    public Type visit(BoolValue boolValue) {
        return new BoolType();
    }
}
