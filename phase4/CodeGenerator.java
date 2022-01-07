package main.visitor.codeGenerator;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.*;
import main.ast.nodes.expression.values.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.primitives.*;
import main.symbolTable.*;
import main.symbolTable.exceptions.*;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.visitor.Visitor;
import main.visitor.type.ExpressionTypeChecker;

import javax.lang.model.type.NullType;
import java.io.*;
import java.util.*;

public class  CodeGenerator extends Visitor<String> {
    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker();
    private String outputPath;
    private FileWriter currentFile;
    private static int label = 0;
    private boolean hasConflict(String key) {
        try {
            SymbolTable.root.getItem(key);
            return true;
        } catch (ItemNotFoundException exception) {
            return false;
        }
    }
    private static String getLabel(int l)
    {
        return "Label" + Integer.toString(l);
    }

    private void copyFile(String toBeCopied, String toBePasted) {
        try {
            File readingFile = new File(toBeCopied);
            File writingFile = new File(toBePasted);
            InputStream readingFileStream = new FileInputStream(readingFile);
            OutputStream writingFileStream = new FileOutputStream(writingFile);
            byte[] buffer = new byte[1024];
            int readLength;
            while ((readLength = readingFileStream.read(buffer)) > 0)
                writingFileStream.write(buffer, 0, readLength);
            readingFileStream.close();
            writingFileStream.close();
        } catch (IOException e) {//unreachable
        }
    }

    private void prepareOutputFolder() {
        this.outputPath = "output/";
        String jasminPath = "utilities/jarFiles/jasmin.jar";
        String listClassPath = "utilities/codeGenerationUtilityClasses/List.j";
        String fptrClassPath = "utilities/codeGenerationUtilityClasses/Fptr.j";
        try{
            File directory = new File(this.outputPath);
            File[] files = directory.listFiles();
            if(files != null)
                for (File file : files)
                    file.delete();
            directory.mkdir();
        }
        catch(SecurityException e) {//unreachable

        }
        copyFile(jasminPath, this.outputPath + "jasmin.jar");
        copyFile(listClassPath, this.outputPath + "List.j");
        copyFile(fptrClassPath, this.outputPath + "Fptr.j");
    }

    private void createFile(String name) {
        try {
            String path = this.outputPath + name + ".j";
            File file = new File(path);
            file.createNewFile();
            this.currentFile = new FileWriter(path);
        } catch (IOException e) {//never reached
        }
    }

    private void addCommand(String command) {
        try {
            command = String.join("\n\t\t", command.split("\n"));
            if(command.startsWith("Label_"))
                this.currentFile.write("\t" + command + "\n");
            else if(command.startsWith("."))
                this.currentFile.write(command + "\n");
            else
                this.currentFile.write("\t\t" + command + "\n");
            this.currentFile.flush();
        } catch (IOException e) {//unreachable

        }
    }

    private void addStaticMainMethod() {
        addCommand(".method public static main([Ljava/lang/String;)V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("new Main");
        addCommand("invokespecial Main/<init>()V");
        addCommand("return");
        addCommand(".end method");
    }

    private int slotOf(String identifier) {
        //todo

        return 0;
    }

    @Override
    public String visit(Program program) {
        prepareOutputFolder();

        for(StructDeclaration structDeclaration : program.getStructs()){
            structDeclaration.accept(this);
        }

        createFile("Main");

        program.getMain().accept(this);

        for (FunctionDeclaration functionDeclaration: program.getFunctions()){
            functionDeclaration.accept(this);
        }
        return null;
    }

    @Override
    public String visit(StructDeclaration structDeclaration) {
        //todo

        return null;
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        //todo
        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        //todo
        return null;
    }

    @Override
    public String visit(VariableDeclaration variableDeclaration) {
        //todo
        return null;
    }

    @Override
    public String visit(SetGetVarDeclaration setGetVarDeclaration) {
        return null;
    }

    @Override
    public String visit(AssignmentStmt assignmentStmt) {
        //todo
        BinaryExpression assignExpr = new BinaryExpression(assignmentStmt.getLValue(), assignmentStmt.getRValue(), BinaryOperator.assign);
        addCommand(assignExpr.accept(this));
        addCommand("pop");
        return null;
    }

    @Override
    public String visit(BlockStmt blockStmt) {
        //todo
        for (Statement stmt: blockStmt.getStatements())
            stmt.accept(this);
        return null;
    }

    @Override
    public String visit(ConditionalStmt conditionalStmt) {
        //todo
             /*
                    [condition]
                    ifeq ELSE
                    [consequenceBody]
                    goto END
              ELSE:
                    [alternativeBody]
              END:
             */
        int elseLabel = label++;
        int endLabel = label++;
        String commands = "";
        commands += conditionalStmt.getCondition().accept(this);
        commands += "   ifeq " + getLabel(elseLabel) + "\n";
        if(conditionalStmt.getThenBody() != null)
            conditionalStmt.getThenBody().accept(this);
        commands += "   goto " + getLabel(endLabel) + "\n" + getLabel(elseLabel) + ":\n" ;
        if(conditionalStmt.getElseBody() != null)
            commands += conditionalStmt.getElseBody().accept(this);
        commands += getLabel(endLabel) + ":\n";

        addCommand(commands);
        return commands;
    }

    @Override
    public String visit(FunctionCallStmt functionCallStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(DisplayStmt displayStmt) {
        addCommand("getstatic java/lang/System/out Ljava/io/PrintStream;");
        Type argType = displayStmt.getArg().accept(expressionTypeChecker);
        String commandsOfArg = displayStmt.getArg().accept(this);

        addCommand(commandsOfArg);
        if (argType instanceof IntType)
            addCommand("invokevirtual java/io/PrintStream/println(I)V");
        if (argType instanceof BoolType)
            addCommand("invokevirtual java/io/PrintStream/println(Z)V");

        return null;
    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        //todo
        Type retType = returnStmt.getReturnedExpr().accept(expressionTypeChecker);
        if(retType instanceof VoidType || retType instanceof NullType){
            addCommand("return");
        }
        else{
            addCommand(returnStmt.getReturnedExpr().accept(this));
            if(retType instanceof IntType)
                addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
            else if(retType instanceof BoolType)
                addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
            addCommand("ireturn");  // check this  ' areturn ' was for return by refrence this is for int return
        }
        return null;
    }

    @Override
    public String visit(LoopStmt loopStmt) {
        //todo
           /*
                CHECK_COND :
                    [cond]
                    ifeq END
                    [statement]
                    goto CHECK_COND
                END :
             */
        if(loopStmt.getIsDoWhile() != true){
            int labelCond = label++;
            int labelEnd = label++;
            String commands = getLabel(labelCond) + ":\n";
            commands += loopStmt.getCondition().accept(this) +
                    "   ifeq " + getLabel(labelEnd) + "\n";
            commands += loopStmt.getBody().accept(this);
            commands += "   goto " + getLabel(labelCond) + " \n" + getLabel(labelEnd) + ":\n";

            addCommand(commands);
            return commands;

        }

        /*
         *   DO:
         *   [statement]
         *   CHECK_COND:
         *   [cond] ifeq END
         *   goto DO
         *   END :
         */
        else{
            int labelDO = label++;
            int labelCond = label++;
            int labelEnd = label++;
            String commands = getLabel(labelDO) + ":\n";
            commands += loopStmt.getBody().accept(this);
            commands += getLabel(labelCond) + ":\n";
            commands += loopStmt.getCondition().accept(this) +"   ifeq " + getLabel(labelEnd) + "\n";
            commands += "   goto " +getLabel(labelDO)+ " \n"+ getLabel(labelEnd) + ":\n";
            addCommand(commands);
            return commands;
        }


    }

    @Override
    public String visit(VarDecStmt varDecStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(ListAppendStmt listAppendStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(ListSizeStmt listSizeStmt) {
        //todo

        return null;
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        //todo

        Type firstType = binaryExpression.getFirstOperand().accept(expressionTypeChecker);
        Type secondType = binaryExpression.getSecondOperand().accept(expressionTypeChecker);
        String firstOperandCommands = binaryExpression.getFirstOperand().accept(this);
        String secondOperandCommands = binaryExpression.getSecondOperand().accept(this);
        String commands = "";
        commands += firstOperandCommands;
        commands += secondOperandCommands;

        BinaryOperator op = binaryExpression.getBinaryOperator();
        if (op == BinaryOperator.add)
            commands += "   iadd\n";
        else if (op == BinaryOperator.sub)
            commands += "   isub\n";
        else if (op == BinaryOperator.mult)
            commands += "   imul\n";
        else if (op == BinaryOperator.div)
            commands += "   idiv\n";
        else if (op == BinaryOperator.eq || op == BinaryOperator.gt || op == BinaryOperator.lt) {
            if (binaryExpression.getFirstOperand().accept(this.expressionTypeChecker) instanceof IntType
                    || binaryExpression.getSecondOperand().accept(this.expressionTypeChecker) instanceof BoolType) {
                String cond = "";
                if (op == BinaryOperator.eq)
                    cond = "   if_icmpne ";
                else if (op == BinaryOperator.gt)
                    cond = "   if_icmple ";
                else if (op == BinaryOperator.lt)
                    cond = "   if_icmpge ";

                commands += cond +
                        getLabel(label) + "\n" +
                        "   iconst_1" + "\n" +
                        "   goto " + getLabel(label + 1) + "\n" +
                        getLabel(label) + ":\n" +
                        "   iconst_0" + "\n" +
                        getLabel(label + 1) + ":\n";
                label += 2;
            } else {
                commands += "   invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z\n";
            }
        } else if (op == BinaryOperator.and || op == BinaryOperator.or) {
            String cond = op == BinaryOperator.and ? "   ifeq " : "   ifne ";
            String const1 = op == BinaryOperator.and ? "   iconst_1\n" : "   iconst_0\n";
            String const2 = op == BinaryOperator.and ? "   iconst_0\n" : "   iconst_1\n";
            int label1 = label++;
            int label2 = label++;
            commands = "";
            commands += binaryExpression.getFirstOperand().accept(this.expressionTypeChecker);
            commands += cond + getLabel(label1) + "\n";
            commands += binaryExpression.getSecondOperand().accept(this.expressionTypeChecker);
            commands += cond + getLabel(label1) + "\n" +
                    const1 +
                    "   goto " + getLabel(label2) + "\n" +
                    getLabel(label1) + ":\n" +
                    const2 +
                    getLabel(label2) + ":\n";

        } else if (op == BinaryOperator.assign) {
            if (firstType instanceof ListType)
                secondOperandCommands = "new List\ndup\n" + secondOperandCommands + "invokespecial List/<init>(LList;)V\n";
            if (binaryExpression.getFirstOperand() instanceof Identifier) {
                commands += secondOperandCommands;
                String id = ((Identifier) binaryExpression.getFirstOperand()).getName();
                if (secondType instanceof IntType)
                    commands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
                if (secondType instanceof BoolType)
                    commands += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
                Integer slot = slotOf(id);
                commands += "astore " + slot.toString() + "\n";
                commands += binaryExpression.getFirstOperand().accept(this);
            }
        } else if (binaryExpression.getFirstOperand() instanceof ListAccessByIndex) {
            ListAccessByIndex la = (ListAccessByIndex) binaryExpression.getFirstOperand();
            commands += la.getInstance().accept(this);
            commands += la.getIndex().accept(this);
            commands += secondOperandCommands;
            if (secondType instanceof IntType)
                commands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
            if (secondType instanceof BoolType)
                commands += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
            commands += "invokevirtual List/setElement(ILjava/lang/Object;)V\n";
            commands += binaryExpression.getFirstOperand().accept(this);
        }

        else if (binaryExpression.getFirstOperand() instanceof StructAccess) {
            Expression instance = ((StructAccess) binaryExpression.getFirstOperand()).getInstance();
            binaryExpression.getFirstOperand().accept(expressionTypeChecker);
            Type instanceType = instance.accept(expressionTypeChecker);
            if (instanceType instanceof ListType) {
                StructAccess firstOperand = (StructAccess) binaryExpression.getFirstOperand();
                commands += firstOperand.getInstance().accept(this);
                commands += secondOperandCommands;
                if (secondType instanceof IntType)
                    commands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
                if (secondType instanceof BoolType)
                    commands += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
                commands += "invokevirtual List/setElement(ILjava/lang/Object;)V\n";
                commands += binaryExpression.getFirstOperand().accept(this);
            }
        }
        addCommand(commands);
        return commands;
    }
    @Override
    public String visit(UnaryExpression unaryExpression){
        return null;
    }

    @Override
    public String visit(StructAccess structAccess){
        //todo
        return null;
    }

    @Override
    public String visit(Identifier identifier){
        //todo

        String commands = "";
        Type t = identifier.accept(this.expressionTypeChecker);
        int slot = slotOf(identifier.getName());
        if(hasConflict(FunctionSymbolTableItem.START_KEY + identifier.getName())){
            //should be done
        }
        else{
            commands += "aload " + slot + "\n";
            if (t instanceof BoolType)
                commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
            else if (t instanceof IntType)
                commands += "invokevirtual java/lang/Integer/intValue()I\n";
        }
        return commands;

    }
    //
    private String checkCast(Type t){
        if(t instanceof IntType)
            return "checkcast java/lang/Integer\n";
        else if(t instanceof  BoolType)
            return "checkcast java/lang/Boolean\n";
        else if(t instanceof ListType)
            return "checkcast List\n";
        else if(t instanceof FptrType)
            return "checkcast Fptr\n";
        else if(t instanceof StructType)
            return "checkcast"+ ((StructType) t).getStructName() +"\n";
        else
            return "";
    }
    @Override
    public String visit(ListAccessByIndex listAccessByIndex){
        //todo
        String commands = "";
        Type memberType = listAccessByIndex.accept(this.expressionTypeChecker);
        commands += listAccessByIndex.getInstance().accept(this);
        commands += listAccessByIndex.getIndex().accept(this);
        commands += "invokevirtual List/getElement(I)Ljava/lang/Object;\n";
        commands += checkCast(memberType);
        if(memberType instanceof IntType)
            commands += "invokevirtual java/lang/Integer/intValue()I\n";
        else if(memberType instanceof  BoolType)
            commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
        return commands;

    }

    @Override
    public String visit(FunctionCall functionCall){
        //todo
        StringBuilder commands = new StringBuilder();
        commands.append(functionCall.getInstance().accept(this));
        commands.append("new java/util/ArrayList\ndup\ninvokespecial java/util/ArrayList/<init>()V\n");
        for(Expression e : functionCall.getArgs()) {
            commands.append("dup\n");
            commands.append(e.accept(this));
            Type t = e.accept(this.expressionTypeChecker);
            if(t instanceof IntType)
                commands.append("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n");
            if(t instanceof BoolType)
                commands.append("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n");
            commands.append("invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\npop\n");
        }
        commands.append("invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;\n");
        Type t = functionCall.accept(expressionTypeChecker);
        commands.append(checkCast(t));
        if(t instanceof IntType)
            commands.append("invokevirtual java/lang/Integer/intValue()I\n");
        else if(t instanceof  BoolType)
            commands.append("invokevirtual java/lang/Boolean/booleanValue()Z\n");
        return commands.toString();
    }

    @Override
    public String visit(ListSize listSize){
        //todo
        String commands = "";
        commands += listSize.getArg().accept(this);
        commands += "invokevirtual java/util/ArrayList/size()I\n";
        return commands;
    }

    @Override
    public String visit(ListAppend listAppend) {
        //todo
        String commands = "";
        commands += listAppend.getListArg();
        commands += listAppend.getElementArg();
        commands += "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\n";
        return commands;
    }

    @Override
    public String visit(IntValue intValue) {
        //todo
        String commands = "";
        commands += "ldc " + intValue.getConstant() + "\n";
        return commands;

    }

    @Override
    public String visit(BoolValue boolValue) {
        //todo
        String commands = "";
        if(boolValue.getConstant())
            commands += "ldc 1\n";
        else
            commands += "ldc 0\n";
        return commands;
    }

    @Override
    public String visit(ExprInPar exprInPar) {
        return exprInPar.getInputs().get(0).accept(this);
    }
}
