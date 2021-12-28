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
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.SymbolTableItem;
import main.symbolTable.items.VariableSymbolTableItem;
import main.visitor.Visitor;

import javax.lang.model.type.NullType;
import java.util.ArrayList;

public class ExpressionTypeChecker extends Visitor<Type> {
    private boolean lvalue = false;
    private boolean functioncall_statement = false;
    private boolean isStatement = false;
    public void set_functioncall_statement(boolean val){this.functioncall_statement = val;}

    public void setLvalue(boolean val){this.lvalue = val;}
    public boolean getLvalue(){return this.lvalue;}

    public void setAsStatement()
    {
        isStatement = true;
    }

    public void setAsNoneStatement()
    {
        isStatement = false;
    }

    public Type checkBinaryLogicalOperator(Type firstType , Type secondType , BinaryExpression binaryExpression){
        if((firstType instanceof BoolType && secondType instanceof BoolType))
        {
            return new BoolType();
        }
        else if(firstType instanceof NoType && secondType instanceof NoType)
            return new NoType();
        else if((firstType instanceof NoType && !(secondType instanceof BoolType)) ||
                (secondType instanceof NoType && !(firstType instanceof BoolType))) {
            UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().name());
            binaryExpression.addError(exception);
            return new NoType();
        }
        else if(firstType instanceof NoType || secondType instanceof NoType)
            return new NoType();
        if((firstType instanceof IntType) && (secondType instanceof IntType))
            return new BoolType();
        return new NoType();
    }

    public Type checkEqualityOperator(Type firstType , Type secondType , BinaryExpression binaryExpression){
        if(firstType instanceof ListType || secondType instanceof ListType)
        {
            binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(),binaryExpression.getBinaryOperator().name()));
        }
        else if((firstType instanceof IntType && secondType instanceof IntType) ||
                (firstType instanceof BoolType && secondType instanceof BoolType) ||
                (firstType instanceof StructType && secondType instanceof StructType) ||
                (firstType instanceof FptrType && secondType instanceof FptrType) )
        {
            return new BoolType();
        }
        else if (!(firstType instanceof NoType) && !(secondType instanceof NoType)){
            binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(),binaryExpression.getBinaryOperator().name()));
        }
        return new NoType();

    }
    public Type checkBinaryMathOperator(Type firstType , Type secondType , BinaryExpression binaryExpression){
        if((firstType instanceof IntType && secondType instanceof IntType) ||
                (firstType instanceof BoolType && secondType instanceof BoolType) ||
                (firstType instanceof StructType && secondType instanceof StructType) ||
                (firstType instanceof FptrType && secondType instanceof FptrType && checkFptrs((FptrType)firstType, (FptrType)secondType)) ||
                (firstType instanceof ListType && secondType instanceof ListType && checkLists((ListType)firstType,(ListType)secondType)))
        {
            return secondType;
        }
        else if (!(firstType instanceof NoType) && !(secondType instanceof NoType)){
            binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine(),binaryExpression.getBinaryOperator().name()));
        }
        return new NoType();
    }
    public Type checkLessThanOrGreaterThan (Type firstType , Type secondType , BinaryExpression binaryExpression){
        if((firstType instanceof IntType && secondType instanceof IntType))
        {
            if(binaryExpression.getBinaryOperator() == BinaryOperator.lt || binaryExpression.getBinaryOperator() == BinaryOperator.eq ||
                    binaryExpression.getBinaryOperator() == BinaryOperator.gt  )
                return new BoolType();
            return new IntType();
        }

        else if(firstType instanceof NoType && secondType instanceof NoType)
            return new NoType();

        else if(!(firstType instanceof NoType && secondType instanceof IntType) || !(secondType instanceof NoType && firstType instanceof IntType)) {
            UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().name());
            binaryExpression.addError(exception);
            return new NoType();
        }
        else if(firstType instanceof NoType || secondType instanceof NoType)
            return new NoType();

        return  new NoType();
    }
    public Type checkAssignOperator(boolean l,Type firstType , Type secondType , BinaryExpression binaryExpression) {
        if((firstType instanceof IntType && secondType instanceof IntType) ||
                (firstType instanceof BoolType && secondType instanceof BoolType) ||
                (firstType instanceof StructType && secondType instanceof StructType) ||
                (firstType instanceof FptrType && secondType instanceof FptrType && checkFptrs((FptrType)firstType, (FptrType)secondType)) ||
                (firstType instanceof ListType && secondType instanceof ListType && checkLists((ListType)firstType,(ListType)secondType)))
        {
            return firstType;
        }

        else if(firstType instanceof NoType)
            return new NoType();
        else if (!isSubType(secondType, firstType)) {
            UnsupportedOperandType err = new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().toString());
            binaryExpression.addError(err);
            return new NoType();
        } else {
            if (l)
                return new NoType();
            return firstType;
        }
    }


    @Override
    public Type visit(BinaryExpression binaryExpression) {
        //Todo
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
        //Todo
        this.lvalue=false;
        Type operand = unaryExpression.getOperand().accept(this);
        boolean l = this.lvalue;
        this.lvalue = true;
        UnaryOperator operator = unaryExpression.getOperator();
        switch (operator){
            case minus -> {
                if (operand instanceof IntType)
                    return new IntType();
                if (operand instanceof NoType) {
                    return new NoType();
                } else {
                    UnsupportedOperandType err = new UnsupportedOperandType(unaryExpression.getLine(), operator.toString());
                    unaryExpression.addError(err);
                }
                return new NoType();
            }
            case not ->{
                if (operand instanceof BoolType)
                    return new BoolType();
                if (operand instanceof NoType) {
                    return new NoType();
                } else {
                    UnsupportedOperandType err = new UnsupportedOperandType(unaryExpression.getLine(), operator.toString());
                    unaryExpression.addError(err);
                }
                return new NoType();
            }
        }

        return null;
    }


    @Override
    public Type visit(FunctionCall funcCall) {
        //Todo
        Type insType = funcCall.getInstance().accept(this);
        if(!(insType instanceof FptrType))
        {
            if(!(insType instanceof NoType))
                funcCall.addError(new CallOnNoneFptrType(funcCall.getLine()));
            return new NoType();
        }
        if (((FptrType)insType).getReturnType() instanceof VoidType && !isStatement)
        {
            funcCall.addError(new CantUseValueOfVoidFunction(funcCall.getLine()));
        }
        if(((FptrType) insType).getArgsType().size() > 0 && ((FptrType) insType).getArgsType().get(0) instanceof VoidType && funcCall.getArgs().size() == 0)
        {
            return ((FptrType) insType).getReturnType();
        }
        ArrayList<Type> args = new ArrayList<>();
        for(Expression arg :funcCall.getArgs())
        {
            Type item = arg.accept(this);
            args.add(item);
        }
        if(args.size() != ((FptrType) insType).getArgsType().size())
        {
            funcCall.addError(new ArgsInFunctionCallNotMatchDefinition(funcCall.getLine()));
            return ((FptrType) insType).getReturnType();
        }
        if(!checkTwoArrayType(((FptrType) insType).getArgsType(),args)){
            funcCall.addError(new ArgsInFunctionCallNotMatchDefinition(funcCall.getLine()));
            return ((FptrType) insType).getReturnType();
        }
        return ((FptrType) insType).getReturnType();
    }

    @Override
    public Type visit(Identifier identifier) {
        //Todo
        try {


            SymbolTableItem varItem = SymbolTable.top.getItem(VariableSymbolTableItem.START_KEY + identifier.getName());
            //cases
            Type ident = ((VariableSymbolTableItem) varItem).getType();
            if (ident instanceof StructType) {
                Identifier structName = ((StructType) ident).getStructName();

                try {
                    SymbolTable.top.getItem(StructSymbolTableItem.START_KEY + structName.getName());
                    return ident;
                } catch (ItemNotFoundException excep) {
                    return new NoType();
                }


            } else {
                return ident;
            }
        }
        catch (ItemNotFoundException excep){
            try {
                FunctionSymbolTableItem funItem = (FunctionSymbolTableItem) SymbolTable.top.getItem(FunctionSymbolTableItem.START_KEY + identifier.getName());
                return new FptrType(funItem.getArgTypes(), funItem.getReturnType());
            }catch (ItemNotFoundException exceptionSecond)
            {
                identifier.addError(new VarNotDeclared(identifier.getLine(), identifier.getName()));
            }
        }


        return new NoType();
    }

    @Override
    public Type visit(ListAccessByIndex listAccessByIndex) {
        Type indexType = listAccessByIndex.getIndex().accept(this);
        Type instanceType = listAccessByIndex.getInstance().accept(this);
        //
        if ((instanceType instanceof NoType) || (indexType instanceof NoType)){
            return new NoType();
        }
        if (indexType instanceof IntType && instanceType instanceof ListType)
        {
            return ((ListType) instanceType).getType();
        }

        if ( !(indexType instanceof IntType)) {
            listAccessByIndex.addError(new ListIndexNotInt(listAccessByIndex.getLine()));

        }
        if (!(instanceType instanceof ListType)) {
            listAccessByIndex.addError(new AccessByIndexOnNonList(listAccessByIndex.getLine()));
            return new NoType();
        }
        return new NoType();
    }

    @Override
    public Type visit(StructAccess structAccess) {
        //Todo
        Type instType = structAccess.getInstance().accept(this);
        if(instType instanceof NoType)
        {
            return new NoType();
        }
        if (!(instType instanceof StructType))
        {
            structAccess.addError(new AccessOnNonStruct(structAccess.getLine()));
            return new NoType();
        }
        String varName = structAccess.getElement().getName();
        String structName = ((StructType) instType).getStructName().getName();
        try
        {
            StructSymbolTableItem struct = (StructSymbolTableItem) SymbolTable.root.getItem(StructSymbolTableItem.START_KEY+structName);
            SymbolTable structTable = struct.getStructSymbolTable();
            try
            {
                VariableSymbolTableItem element = (VariableSymbolTableItem) structTable.getItem(VariableSymbolTableItem.START_KEY+varName);
                return element.getType();
            }
            catch (ItemNotFoundException ex)
            {
                structAccess.addError(new StructMemberNotFound(structAccess.getLine(),structName,varName));
                return new NoType();
            }
        }
        catch (ItemNotFoundException ex)
        {
            return new NoType();
        }

    }


    @Override
    public Type visit(ListSize listSize) {
        //Todo
        Type argType = listSize.getArg().accept(this);
        if(argType instanceof NoType){
            return new NoType();
        }
        if(!(argType instanceof ListType) && !((argType instanceof NoType))){
            listSize.addError(new GetSizeOfNonList(listSize.getLine()));
        }

        if(argType instanceof ListType)
        {
            return new IntType();
        }
        return new NoType();
    }

    @Override
    public Type visit(ListAppend listAppend) {
        //Todo
        Type listArgType = listAppend.getListArg().accept(this);
        Type elementArgType = listAppend.getElementArg().accept(this);

        if(listArgType instanceof NoType){
            return new NoType();
        }
        if(!(listArgType instanceof ListType) && !((listArgType instanceof NoType))){
            listAppend.addError(new AppendToNonList(listAppend.getLine()));
            return new NoType();
        }
        //
        Type elementArgTypes = ((ListType) listArgType).getType();
        if((elementArgType instanceof BoolType && elementArgTypes instanceof BoolType ) ||
                (elementArgType instanceof IntType && elementArgTypes instanceof IntType ) ||
                (elementArgType instanceof StructType && elementArgTypes instanceof StructType ) ||
                (elementArgType instanceof ListType && elementArgTypes instanceof ListType ) )
        {
            return new VoidType();
        }
        if(!(elementArgTypes instanceof NoType))
        {
            listAppend.addError(new NewElementTypeNotMatchListType(listAppend.getLine()));
        }
        return new NoType();
    }

    @Override
    public Type visit(ExprInPar exprInPar) {
        //Todo
        return exprInPar.getInputs().get(0).accept(this);
    }

    @Override
    public Type visit(IntValue intValue) {
        //Todo
        this.lvalue = true;
        return new IntType();
    }

    @Override
    public Type visit(BoolValue boolValue) {
        //Todo
        this.lvalue = true ;
        return new BoolType();
    }
    // additionals
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


    private boolean checkTwoArrayType(ArrayList<Type> a,ArrayList<Type> b)
    {
        for(int i = 0;i <a.size();i++)
        {
            if((b.get(i) instanceof BoolType && a.get(i) instanceof BoolType ) ||
                    (b.get(i) instanceof IntType && a.get(i) instanceof IntType ) ||
                    (b.get(i) instanceof StructType && a.get(i) instanceof StructType ) ||
                    (b.get(i) instanceof ListType && a.get(i) instanceof ListType ) ||
                    (b.get(i) instanceof FptrType && a.get(i) instanceof FptrType )) {
            }else{
                return false;
            }
        }
        return true;
    }
    private boolean checkFptrs(FptrType l, FptrType r)
    {
        if(l.getReturnType() instanceof IntType && !(r.getReturnType() instanceof IntType))
            return false;
        if(l.getReturnType() instanceof BoolType && !(r.getReturnType() instanceof BoolType))
            return false;
        if(l.getReturnType() instanceof StructType && !(r.getReturnType() instanceof StructType))
            return false;
        if(l.getReturnType() instanceof ListType && !(r.getReturnType() instanceof ListType))
            return false;
        if(l.getReturnType() instanceof VoidType && !(r.getReturnType() instanceof VoidType))
            return false;
        if(l.getReturnType() instanceof FptrType && !(r.getReturnType() instanceof FptrType))
            return false;
        if(l.getArgsType().size() != r.getArgsType().size())
            return false;
        return checkTwoArrayType(l.getArgsType(),r.getArgsType());
    }
    private boolean checkLists(ListType l, ListType r)
    {
        if(l.getType() instanceof IntType && r.getType() instanceof IntType)
            return true;
        if(l.getType() instanceof BoolType && r.getType() instanceof BoolType)
            return true;
        if(l.getType() instanceof StructType && r.getType() instanceof StructType)
            return true;
        return l.getType() instanceof ListType && r.getType() instanceof ListType;
    }
}
