package main.visitor.type;

import com.sun.jdi.LocalVariable;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.expression.operators.UnaryOperator;
import main.ast.nodes.expression.values.primitive.BoolValue;
import main.ast.nodes.expression.values.primitive.IntValue;
import main.ast.nodes.statement.SetGetVarDeclaration;
import main.ast.types.*;
import main.ast.types.primitives.BoolType;
import main.ast.types.primitives.IntType;
import main.compileError.typeError.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.VariableSymbolTableItem;
import main.visitor.Visitor;

import javax.lang.model.type.NullType;
import java.util.ArrayList;

public class ExpressionTypeChecker extends Visitor<Type> {
    private boolean lvalue = false;
    public void setLvalue(boolean val){this.lvalue = val;}
    public boolean getLvalue(){return this.lvalue;}
    //

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
        ////////////////not complete
        Type instanceType = funcCall.getInstance().accept(this);
        ArrayList<Type> argsTypes = new ArrayList<>();
        for(Expression arg : funcCall.getArgs()) {
            argsTypes.add(arg.accept(this));
        }
        if(!(instanceType instanceof FptrType || instanceType instanceof NoType)) {
            CallOnNoneFptrType exception = new CallOnNoneFptrType(funcCall.getLine());
            funcCall.addError(exception);
            return new NoType();
        }
        else if(instanceType instanceof NoType) {
            return new NoType();
        }
        else
            return new NoType();
    }

    @Override
    public Type visit(Identifier identifier) {
        //Todo
        VariableSymbolTableItem item;
        try{
            item = (VariableSymbolTableItem) SymbolTable.top.getItem(VariableSymbolTableItem.START_KEY + identifier.getName());
            return item.getType();
        }catch(ItemNotFoundException exp){
            identifier.addError(new VarNotDeclared(identifier.getLine() , identifier.getName()));
            return new NoType();
        }

    }

    @Override
    public Type visit(ListAccessByIndex listAccessByIndex) {
        //
        ///////////////////// guess have works !
        Type instanceType  = listAccessByIndex.getInstance().accept(this);

        Type indexType = listAccessByIndex.getIndex().accept(this);

        if(!(indexType instanceof IntType) && !(indexType instanceof NoType) )
            listAccessByIndex.addError(new ListIndexNotInt(listAccessByIndex.getLine()));
        if(!(instanceType  instanceof ListType) && !(instanceType  instanceof NoType) )
            listAccessByIndex.addError(new AccessByIndexOnNonList(listAccessByIndex.getLine() ) );
        if(instanceType  instanceof NoType)
            return new NoType();
        return new NoType();
    }

    @Override
    public Type visit(StructAccess structAccess) {
        //Todo
        Type instanceStructType = structAccess.getInstance().accept(this);
        Type elementStructType = structAccess.getElement().accept(this);
        /// ...
        if (!(instanceStructType instanceof StructType) && !(instanceStructType  instanceof NoType)){
            structAccess.addError(new AccessOnNonStruct(structAccess.getLine()));
        }
        if (instanceStructType instanceof NoType){
            return new NoType();
        }
        return new NoType();
    }

    @Override
    public Type visit(ListSize listSize) {
        //Todo
        Type argType = listSize.getArg().accept(this);
        if(!(argType instanceof ListType) && !((argType instanceof NoType))){
            listSize.addError(new GetSizeOfNonList(listSize.getLine()));
        }
        if(argType instanceof NoType){
            return new NoType();
        }
        return new NoType();
    }

    @Override
    public Type visit(ListAppend listAppend) {
        //Todo
        Type listArgType = listAppend.getListArg().accept(this);
        Type elementArgType = listAppend.getElementArg().accept(this);
        if(!(listArgType instanceof ListType) && !((listArgType instanceof NoType))){
            listAppend.addError(new AppendToNonList(listAppend.getLine()));
        }
        // to do !
        if(listArgType instanceof NoType){
            return new NoType();
        }
        return new NoType();
    }

    @Override
    public Type visit(ExprInPar exprInPar) {
        //Todo
        return new NoType();
    }

    @Override
    public Type visit(IntValue intValue) {
        //Todo
        return new IntType();
    }

    @Override
    public Type visit(BoolValue boolValue) {
        //Todo
        return new BoolType();
    }
    // is the second type(a) subtype of first type(b) or not
    public boolean isSubType(Type a, Type b){
        if(a instanceof NoType){
            return true;
        }
        if(b instanceof NoType){
            return false;
        }
        if((a instanceof IntType && b instanceof IntType)
                ||(a instanceof BoolType && b instanceof BoolType) || (a instanceof NullType && b instanceof NullType)){
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
}
