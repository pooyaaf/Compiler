package main.visitor.type;

import com.sun.jdi.LocalVariable;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.struct.StructDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.expression.operators.UnaryOperator;
import main.ast.nodes.expression.values.primitive.BoolValue;
import main.ast.nodes.expression.values.primitive.IntValue;
import main.ast.nodes.statement.SetGetVarDeclaration;
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
    public boolean isCurrentLValue;
    private boolean lvalue = false;
    private boolean functioncall_statement = false;
    private boolean isStatement = false;
    public void set_functioncall_statement(boolean val){this.functioncall_statement = val;}
    public boolean get_functioncall_statemen(){return this.functioncall_statement;}
    public void setLvalue(boolean val){this.lvalue = val;}
    public boolean getLvalue(){return this.lvalue;}

    public void setAsStatement()
    {
        isStatement = true;
    }

    public Type checkBinaryLogicalOperator(Type firstType , Type secondType , BinaryExpression binaryExpression){
        if(firstType instanceof NoType && secondType instanceof NoType)
            return new NoType();
        else if((firstType instanceof NoType && !(secondType instanceof IntType)) ||
                (secondType instanceof NoType && !(firstType instanceof IntType))) {
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
        if (firstType instanceof NoType && secondType instanceof NoType)
        {
            return new NoType();
        }
        else if((firstType instanceof NoType && secondType instanceof ListType) ||
                (secondType instanceof NoType && firstType instanceof ListType)) {
            UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().name());
            binaryExpression.addError(exception);
            return new NoType();
        }
        else if(firstType instanceof NoType || secondType instanceof NoType){
            return new NoType();
        }
        if(firstType instanceof IntType || firstType instanceof BoolType) {
            if (firstType.toString().equals(secondType.toString())) {
                return new BoolType();
            }
        }
        if((firstType instanceof FptrType && secondType instanceof NullType) ||
                (firstType instanceof NullType && secondType instanceof FptrType) ||
                (firstType instanceof FptrType && secondType instanceof FptrType)) {
            return new BoolType();
        }
        if(firstType instanceof NullType && secondType instanceof NullType)
            return new BoolType();
        else
            binaryExpression.addError(new UnsupportedOperandType(binaryExpression.getLine() , binaryExpression.getBinaryOperator().name()));
        return new NoType();

    }
    public Type checkBinaryMathOperator(Type firstType , Type secondType , BinaryExpression binaryExpression){
        if(firstType instanceof NoType && secondType instanceof NoType)
            return new NoType();
        else if((firstType instanceof NoType && !(secondType instanceof IntType)) ||
                (secondType instanceof NoType && !(firstType instanceof IntType))) {
            UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().name());
            binaryExpression.addError(exception);
            return new NoType();
        }
        else if(firstType instanceof NoType || secondType instanceof NoType)
            return new NoType();
        if((firstType instanceof IntType) && (secondType instanceof IntType))
            return new IntType();

        return new NoType();
    }
    public Type checkLessThanOrGreaterThan (Type firstType , Type secondType , BinaryExpression binaryExpression){
        if(firstType instanceof NoType && secondType instanceof NoType)
            return new NoType();
        else if((firstType instanceof NoType && !(secondType instanceof IntType)) ||
                (secondType instanceof NoType && !(firstType instanceof IntType))) {
            UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getLine(), binaryExpression.getBinaryOperator().name());
            binaryExpression.addError(exception);
            return new NoType();
        }
        else if(firstType instanceof NoType || secondType instanceof NoType)
            return new NoType();
        if((firstType instanceof IntType) && (secondType instanceof IntType))
            return new BoolType();
        return  new NoType();
    }
    public Type checkAssignOperator(boolean l,Type firstType , Type secondType , BinaryExpression binaryExpression) {
        if (l) {
            binaryExpression.addError(new LeftSideNotLvalue(binaryExpression.getLine()));
        }
        if(firstType instanceof NoType)
            return new NoType();
        if (!isSubType(secondType, firstType)) {
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
//        BinaryOperator operator = binaryExpression.getBinaryOperator();
        this.lvalue = false;
        Type firstType = binaryExpression.getFirstOperand().accept(this);
        boolean l = this.lvalue;
        this.lvalue = true;
        Type secondType = binaryExpression.getSecondOperand().accept(this);
        //
        if((binaryExpression.getBinaryOperator() == BinaryOperator.and) || (binaryExpression.getBinaryOperator() == BinaryOperator.or) )
            return checkBinaryLogicalOperator(firstType , secondType , binaryExpression);// &  |  Logical Operators
        else if(binaryExpression.getBinaryOperator() == BinaryOperator.eq){
            return checkEqualityOperator(firstType,secondType,binaryExpression); // == equality
        }
        else if((binaryExpression.getBinaryOperator() == BinaryOperator.add) || (binaryExpression.getBinaryOperator() == BinaryOperator.sub) ||
                (binaryExpression.getBinaryOperator() == BinaryOperator.mult) || (binaryExpression.getBinaryOperator() == BinaryOperator.div))
        {
            return checkBinaryMathOperator(firstType,secondType,binaryExpression); //checkBinaryMathOperator
        }
        else if((binaryExpression.getBinaryOperator() == BinaryOperator.gt) || (binaryExpression.getBinaryOperator() == BinaryOperator.lt))
        {
            return checkLessThanOrGreaterThan(firstType,secondType,binaryExpression); // check > and <
        }
        else {
            return checkAssignOperator(l,firstType , secondType , binaryExpression);     //check = ( assignment )
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
            funcCall.addError(new CallOnNoneFptrType(funcCall.getLine()));
            return new NoType();
        }
        if (((FptrType)insType).getReturnType() instanceof VoidType && !isStatement)
        {
            funcCall.addError(new CantUseValueOfVoidFunction(funcCall.getLine()));
        }
        isStatement = false;

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

}
