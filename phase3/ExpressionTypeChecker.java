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
import main.visitor.Visitor;

import javax.lang.model.type.NullType;

public class ExpressionTypeChecker extends Visitor<Type> {
    public boolean isCurrentLValue;

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
    public Type checkAssignOperator(Type firstType , Type secondType , BinaryExpression binaryExpression) {

        return new NoType();
    }
    @Override
    public Type visit(BinaryExpression binaryExpression) {
        //Todo


//        BinaryOperator operator = binaryExpression.getBinaryOperator();

        Type firstType = binaryExpression.getFirstOperand().accept(this);


        Type secondType = binaryExpression.getSecondOperand().accept(this);
        this.isCurrentLValue = false;
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
    //////////////////////////////////////// should be done!!!!!!!!!
            return checkAssignOperator(firstType , secondType , binaryExpression);     //check = ( assignment )
        }


    }
    private Type checkNot(Type operandType , UnaryExpression unaryExpression){
        if(operandType instanceof NoType)
            return new NoType();
        if(operandType instanceof BoolType)
            return operandType;
        UnsupportedOperandType exception = new UnsupportedOperandType(unaryExpression.getLine(), unaryExpression.getOperator().name());
        unaryExpression.addError(exception);
        return new NoType();
    }

    private Type checkMinus(Type operandType , UnaryExpression unaryExpression){
        if(operandType instanceof NoType)
            return new NoType();
        if(operandType instanceof IntType)
            return operandType;
        UnsupportedOperandType exception = new UnsupportedOperandType(unaryExpression.getLine(), unaryExpression.getOperator().name());
        unaryExpression.addError(exception);
        return new NoType();
    }

    @Override
    public Type visit(UnaryExpression unaryExpression) {
        //Todo
        Type operandType = unaryExpression.getOperand().accept(this);
        //
        if(unaryExpression.getOperator() == UnaryOperator.not)
            return checkNot(operandType , unaryExpression); // not
        else
            return checkMinus(operandType , unaryExpression); // minus

    }

    @Override
    public Type visit(FunctionCall funcCall) {
        //Todo
        return null;
    }

    @Override
    public Type visit(Identifier identifier) {
        //Todo
        return null;
    }

    @Override
    public Type visit(ListAccessByIndex listAccessByIndex) {
        //
        ///////////////////// guess have works !
        boolean prevIsCurrentLValue;
        Type instanceType  = listAccessByIndex.getInstance().accept(this);
        prevIsCurrentLValue = this.isCurrentLValue;
        Type indexType = listAccessByIndex.getIndex().accept(this);
        this.isCurrentLValue = prevIsCurrentLValue;
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
}
