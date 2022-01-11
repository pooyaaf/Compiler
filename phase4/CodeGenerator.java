package main.visitor.codeGenerator;

import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.declaration.VariableDeclaration;
import main.ast.nodes.declaration.struct.StructDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.expression.values.primitive.BoolValue;
import main.ast.nodes.expression.values.primitive.IntValue;
import main.ast.nodes.statement.*;
import main.ast.types.FptrType;
import main.ast.types.ListType;
import main.ast.types.StructType;
import main.ast.types.Type;
import main.ast.types.primitives.BoolType;
import main.ast.types.primitives.IntType;
import main.ast.types.primitives.VoidType;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.visitor.Visitor;
import main.visitor.type.ExpressionTypeChecker;

import java.io.*;
import java.util.ArrayList;

public class  CodeGenerator extends Visitor<String> {
    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker();
    private String outputPath;
    private int numOfUsedTemp = 0;
    private FileWriter currentFile;
    private static int label = 0;
    public boolean isInStruct = false;
    private boolean inDefaultConst=false;
    public StructDeclaration currentStruct = new StructDeclaration();
    public FunctionDeclaration currentFunction = new FunctionDeclaration();
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


    private void initializeList(ListType listType) {
        addCommand("new List");
        addCommand("dup");
        addCommand("new java/util/ArrayList");
        addCommand("dup");
        addCommand("invokespecial java/util/ArrayList/<init>()V");

        Type element = listType.getType();
            addCommand("dup");

            if(element instanceof StructType || element instanceof FptrType){
                addCommand("aconst_null");
                addCommand("invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z");
            }
            else if(element instanceof IntType || element instanceof BoolType){
                addCommand("ldc 0");
                if(element instanceof IntType)
                    addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
                if(element instanceof BoolType)
                    addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
                addCommand("invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z");
            }
            else{
                initializeList((ListType) element);
                addCommand("invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z");
            }

            addCommand("pop");

        addCommand("invokespecial List/<init>(Ljava/util/ArrayList;)V");
    }


    private void addDefaultConstructor(String structName)
    {
        inDefaultConst = true;
        addCommand(".method public <init>()V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("aload 0");
        addCommand("invokespecial java/lang/Object/<init>()V");



        BlockStmt blk = (BlockStmt) currentStruct.getBody();
        for(Statement stmt:blk.getStatements()) {
            if (stmt.toString().equals("VarDecStmt")) {
                VarDecStmt myvar = (VarDecStmt) stmt;
                for(VariableDeclaration var:myvar.getVars()) {
                    String varName = var.getVarName().getName();
                    Type varType = var.getVarType();

                    if (varType instanceof StructType || varType instanceof FptrType) {
                        addCommand("aload 0");
                        addCommand("aconst_null");
                        addCommand("putfield " + structName + "/" + varName + " L" + makeTypeSignature(varType) + ";\n");
                    } else if (varType instanceof IntType || varType instanceof BoolType) {
                        addCommand("aload 0");
                        addCommand("ldc 0");
                        if (varType instanceof IntType)
                            addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
                        if (varType instanceof BoolType)
                            addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
                        addCommand("putfield " + structName + "/" + varName + " L" + makeTypeSignature(varType) + ";\n");
                    } else {
                        addCommand("aload 0");
                        initializeList((ListType) varType);
                        addCommand("putfield " + structName + "/" + varName + " L" + makeTypeSignature(varType) + ";\n");
                    }
                }
            }
        }
        addCommand("return");
        addCommand(".end method");

/*
        addCommand(".method public <init>()V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("aload 0");
        if(currentStruct.getParentClassName() == null)
            addCommand("invokespecial java/lang/Object/<init>()V");
        else
            addCommand("invokespecial " + currentClass.getParentClassName().getName() + "/<init>()V");
        for(FieldDeclaration fieldDeclaration : currentClass.getFields())
            this.initializeVar(fieldDeclaration.getVarDeclaration(), true);
        addCommand("return");
        addCommand(".end method\n ");
  */

        inDefaultConst = false;

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
        int count = 1;
        for(VariableDeclaration arg : currentFunction.getArgs()){
            if(arg.getVarName().getName().equals(identifier))
                return count;
            count++;
        }
        if (identifier.equals("")){
            int temp = numOfUsedTemp;
            numOfUsedTemp++;
            return count + temp;
        }
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
        isInStruct = true;

        try{
            String structKey = StructSymbolTableItem.START_KEY + structDeclaration.getStructName().getName();
            StructSymbolTableItem structSymbolTableItem = (StructSymbolTableItem)SymbolTable.root.getItem(structKey);
            SymbolTable.push(structSymbolTableItem.getStructSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }
        currentStruct = structDeclaration;
        //todo
        String header = "";
        createFile(structDeclaration.getStructName().getName());
        header += ".class " + structDeclaration.getStructName().getName();
        addCommand(header);
        addCommand(".super java/lang/Object\n ");
        structDeclaration.getBody().accept(this);
        addDefaultConstructor(structDeclaration.getStructName().getName());

        SymbolTable.pop();

        isInStruct = false;
        return null;
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        //todo
        try{
            String functionKey = FunctionSymbolTableItem.START_KEY + functionDeclaration.getFunctionName().getName();
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }
        String header = "";
        header += ".method public " + functionDeclaration.getFunctionName().getName() + "(";
        for(VariableDeclaration arg : functionDeclaration.getArgs()){
            header += "L" + makeTypeSignature(arg.getVarType()) + ";";
        }
        if (functionDeclaration.getReturnType() instanceof VoidType)
            header += ")V";
        else
            header += ")L"  + makeTypeSignature(functionDeclaration.getReturnType()) + ";";



        addCommand(header);
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("aload 0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        functionDeclaration.getBody().accept(this);
        if(functionDeclaration.getReturnType() instanceof VoidType)
            addCommand("return");
        addCommand(".end method");
        numOfUsedTemp = 0;
        //todo

        SymbolTable.pop();

        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        //todo
        try{
            String functionKey = FunctionSymbolTableItem.START_KEY + "main";
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }
        addCommand(".class public Main");
        addCommand(".super java/lang/Object");
        addStaticMainMethod();
        addCommand(".method public <init>()V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("aload 0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        mainDeclaration.getBody().accept(this);
        addCommand("return");
        addCommand(".end method");
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(VariableDeclaration variableDeclaration) {
        //todo
/*
        if(isInStruct){
            String varName = variableDeclaration.getVarName().getName();
            Type varType = variableDeclaration.getVarType();

            if(varType instanceof StructType || varType instanceof FptrType){
                addCommand("aload 0");
                addCommand("aconst_null");
                addCommand("putfield " + currentStruct.getStructName().getName() + "/" + varName + " L" + makeTypeSignature(varType) + ";\n");
            }
            else if(varType instanceof IntType || varType instanceof BoolType){
                addCommand("aload 0");
                addCommand("ldc 0");
                if(varType instanceof IntType)
                    addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
                if(varType instanceof BoolType)
                    addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
                addCommand("putfield " + currentStruct.getStructName().getName() + "/" + varName + " L" + makeTypeSignature(varType) + ";\n");
            }
            else{
                addCommand("aload 0");
                initializeList((ListType) varType);
                addCommand("putfield " + currentStruct.getStructName().getName() + "/" + varName + " L" + makeTypeSignature(varType) + ";\n");
            }
        }
*/


        String varName = variableDeclaration.getVarName().getName();
        int slot = slotOf(varName);
        //
        Type type = variableDeclaration.getVarType();
        if(variableDeclaration.getDefaultValue() != null){
            addCommand(variableDeclaration.getDefaultValue().accept(this));
        }
        //
        else{
            String commands = "";
            String initCommands ="";
            if (type instanceof FptrType){
                initCommands+="aconst_null"+"\n";
            }
            else if(type instanceof StructType){
                initCommands +="new "+((StructType) type).getStructName().getName()+"\n";
                initCommands += "dup\n";
                initCommands += "invokespecial "+((StructType) type).getStructName().getName()+"/<init>()V\n";
            }
            else if(type instanceof BoolType || type instanceof IntType){
                initCommands += "ldc 0\n";
            }
            else {
                int temp = slotOf("");
                initCommands += "new List\n"+
                        "dup\n"+
                        "new java/util/ArrayList\n"+
                        "dup\n"+
                        "invokespecial java/util/ArrayList/<init>()V\n"+
                        "astore " + temp +"\n"+
                        "new List\n"+
                        "dup\n"+ "aload " + temp + "\n"+
                        "invokespecial List/(Ljava/util/ArrayList;)V\n";
            }
            //Default constructor:
            if(inDefaultConst){
                commands += "aload 0\n" + initCommands + "putfield " + currentStruct.getStructName().getName() +
                        "/" + varName + " L" + type + ";\n";
            }
            else{
                commands = initCommands;
            }
            addCommand(commands);
        }
        //
        if(type instanceof IntType){
            addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
        }
        if(type instanceof BoolType){
            addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
        }

        addCommand("astore "+slot);


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
        String command = "";
        for (Statement stmt: blockStmt.getStatements())
            command += stmt.accept(this);
        return command;
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
        String ELSE = getFreshLabel();
        String END = getFreshLabel();
        addCommand(conditionalStmt.getCondition().accept(this));
        addCommand("ifeq " + ELSE);
        conditionalStmt.getThenBody().accept(this);
        addCommand("goto " + END);
        addCommand(ELSE + ":");
        if (conditionalStmt.getElseBody() != null)
            conditionalStmt.getElseBody().accept(this);
        addCommand(END + ":");
        return null;
    }

    @Override
    public String visit(FunctionCallStmt functionCallStmt) {
        //todo
        expressionTypeChecker.setInFunctionCallStmt(true);
        addCommand(functionCallStmt.getFunctionCall().accept(this));
        addCommand("pop");
        expressionTypeChecker.setInFunctionCallStmt(false);
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
        if(retType instanceof VoidType){
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
            String labelStart = getFreshLabel();
            String labelAfter = getFreshLabel();
            String labelUpdate = getFreshLabel();
            addCommand(labelStart + ":");
            if (loopStmt.getCondition() != null) {
                addCommand(loopStmt.getCondition().accept(this));
                addCommand("ifeq " + labelAfter);
            }
            loopStmt.getBody().accept(this);
            addCommand(labelUpdate + ":");
            addCommand("goto " + labelStart);
            addCommand(labelAfter + ":");

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
    return null;
    }

    @Override
    public String visit(VarDecStmt varDecStmt) {
        //todo
        for(VariableDeclaration var:varDecStmt.getVars()){
            var.accept(this);
        }
        return null;
    }

    @Override
    public String visit(ListAppendStmt listAppendStmt) {
        //todo
        expressionTypeChecker.setInFunctionCallStmt(true);
        addCommand(listAppendStmt.getListAppendExpr().accept(this));
        expressionTypeChecker.setInFunctionCallStmt(false);
        return null;
    }

    @Override
    public String visit(ListSizeStmt listSizeStmt) {
        //todo
        addCommand(listSizeStmt.getListSizeExpr().accept(this));
        //addCommand("pop");
        return null;
    }


    private String getFreshLabel(){
        String label = "Label_";
        label += numOfUsedTemp;
        numOfUsedTemp++;
        return label;
    }


    private String makeTypeSignature(Type t) {
        if (t instanceof IntType)
            return "java/lang/Integer";
        if (t instanceof BoolType)
            return "java/lang/Boolean";
        if (t instanceof ListType)
            return "List";
        if (t instanceof FptrType)
            return "Fptr";
        if (t instanceof StructType)
            return ((StructType)t).getStructName().getName();
        return null;
    }
    @Override
    public String visit(BinaryExpression binaryExpression) {
        //todo

        BinaryOperator operator = binaryExpression.getBinaryOperator();
        Type operandType = binaryExpression.getFirstOperand().accept(expressionTypeChecker);
        String commands = "";
        if (operator == BinaryOperator.add) {
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += binaryExpression.getSecondOperand().accept(this);
            commands += "iadd\n";
        }
        else if (operator == BinaryOperator.sub) {
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += binaryExpression.getSecondOperand().accept(this);
            commands += "isub\n";
        }
        else if (operator == BinaryOperator.mult) {
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += binaryExpression.getSecondOperand().accept(this);
            commands += "imul\n";
        }
        else if (operator == BinaryOperator.div) {
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += binaryExpression.getSecondOperand().accept(this);
            commands += "idiv\n";
        }

        else if((operator == BinaryOperator.gt) || (operator == BinaryOperator.lt)) {
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += binaryExpression.getSecondOperand().accept(this);
            String labelFalse = getFreshLabel();
            String labelAfter = getFreshLabel();
            if(operator == BinaryOperator.gt)
                commands += "if_icmple " + labelFalse + "\n";
            else
                commands += "if_icmpge " + labelFalse + "\n";
            commands += "ldc " + "1\n";
            commands += "goto " + labelAfter + "\n";
            commands += labelFalse + ":\n";
            commands += "ldc " + "0\n";
            commands += labelAfter + ":\n";
        }
        else if((operator == BinaryOperator.eq)) {
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += binaryExpression.getSecondOperand().accept(this);
            String labelFalse = getFreshLabel();
            String labelAfter = getFreshLabel();
            if(operator == BinaryOperator.eq){
                if (!(operandType instanceof IntType) && !(operandType instanceof BoolType))
                    commands += "if_acmpne " + labelFalse + "\n";
                else
                    commands += "if_icmpne " + labelFalse + "\n";
            }
            else{
                if (!(operandType instanceof IntType) && !(operandType instanceof BoolType))
                    commands += "if_acmpeq " + labelFalse + "\n";
                else
                    commands += "if_icmpeq " + labelFalse + "\n";

            }
            commands += "ldc " + "1\n";
            commands += "goto " + labelAfter + "\n";
            commands += labelFalse + ":\n";
            commands += "ldc " + "0\n";
            commands += labelAfter + ":\n";
        }
        else if(operator == BinaryOperator.and) {
            String labelFalse = getFreshLabel();
            String labelAfter = getFreshLabel();
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += "ifeq " + labelFalse + "\n";
            commands += binaryExpression.getSecondOperand().accept(this);
            commands += "ifeq " + labelFalse + "\n";
            commands += "ldc " + "1\n";
            commands += "goto " + labelAfter + "\n";
            commands += labelFalse + ":\n";
            commands += "ldc " + "0\n";
            commands += labelAfter + ":\n";
        }
        else if(operator == BinaryOperator.or) {
            String labelTrue = getFreshLabel();
            String labelAfter = getFreshLabel();
            commands += binaryExpression.getFirstOperand().accept(this);
            commands += "ifne " + labelTrue + "\n";
            commands += binaryExpression.getSecondOperand().accept(this);
            commands += "ifne " + labelTrue + "\n";
            commands += "ldc " + "0\n";
            commands += "goto " + labelAfter + "\n";
            commands += labelTrue + ":\n";
            commands += "ldc " + "1\n";
            commands += labelAfter + ":\n";
        }
        else if(operator == BinaryOperator.assign) {
            Type firstType = binaryExpression.getFirstOperand().accept(expressionTypeChecker);
            Type secondType = binaryExpression.getSecondOperand().accept(expressionTypeChecker);
            String secondOperandCommands = binaryExpression.getSecondOperand().accept(this);
            if(firstType instanceof ListType) {
                secondOperandCommands = "new List\ndup\n" + secondOperandCommands + "invokespecial List/<init>(LList;)V\n";
            }

            if(secondType instanceof IntType)
                secondOperandCommands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
            if(secondType instanceof BoolType)
                secondOperandCommands += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";


            if(binaryExpression.getFirstOperand() instanceof Identifier) {
                Identifier identifier = (Identifier)binaryExpression.getFirstOperand();
                int slot = slotOf(identifier.getName());
                commands += secondOperandCommands;
                commands += "astore " + slot + "\n";
                commands += "aload " + slot + "\n";
                if (secondType instanceof IntType)
                    commands += "invokevirtual java/lang/Integer/intValue()I\n";
                if (secondType instanceof BoolType)
                    commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
            }
            else if(binaryExpression.getFirstOperand() instanceof ListAccessByIndex) {
                Expression instance = ((ListAccessByIndex) binaryExpression.getFirstOperand()).getInstance();
                Expression index = ((ListAccessByIndex) binaryExpression.getFirstOperand()).getIndex();
                commands += instance.accept(this);
                commands += index.accept(this);
                commands += secondOperandCommands;
                commands += "invokevirtual List/setElement(ILjava/lang/Object;)V\n";

                commands += instance.accept(this);
                commands += index.accept(this);
                commands += "invokevirtual List/getElement(I)Ljava/lang/Object;\n";
                commands += "checkcast " + makeTypeSignature(secondType) + "\n";
                if (secondType instanceof IntType)
                    commands += "invokevirtual java/lang/Integer/intValue()I\n";
                if (secondType instanceof BoolType)
                    commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";


            }
        }
        return commands;
    }

    @Override
    public String visit(UnaryExpression unaryExpression){
        return null;
    }

    @Override
    public String visit(StructAccess structAccess){
        //todo
        String name =  structAccess.getInstance().accept(this);
        int slotNum = slotOf(name);
        Type type = structAccess.accept(this.expressionTypeChecker);
        //
        String commands = "";
        commands += "aload " + slotNum + "\n";
        if(type instanceof IntType)
            commands += "invokevirtual java/lang/Integer/intValue()I\n";
        if(type instanceof BoolType)
            commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
        return commands;
    }

    @Override
    public String visit(Identifier identifier){
        //todo

        String commands = "";
        Type t = identifier.accept(this.expressionTypeChecker);
        int slot = slotOf(identifier.getName());
        if(hasConflict(FunctionSymbolTableItem.START_KEY + identifier.getName())){
            //should be done
            commands += "new Fptr\n";
            commands += "dup\n";
            commands += "aload_0\n";
            commands += "ldc " + identifier.getName() + "\n";
            commands += ("invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;\n");
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
        Type type = listAccessByIndex.accept(expressionTypeChecker);
        commands += listAccessByIndex.getInstance().accept(this);
        commands += listAccessByIndex.getIndex().accept(this);
        commands += "invokevirtual List/getElement(I)Ljava/lang/Object;\n";

        commands += "checkcast " + makeTypeSignature(type) + "\n";

        if (type instanceof IntType)
            commands += "invokevirtual java/lang/Integer/intValue()I\n";
        if (type instanceof BoolType)
            commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
        return commands;

    }

    @Override
    public String visit(FunctionCall functionCall){
        //todo


        String commands = "";
        int tempIndexing = slotOf("");
        ArrayList<Expression> allArgs = functionCall.getArgs();
        Type returnType = ((FptrType) functionCall.getInstance().accept(expressionTypeChecker)).getReturnType();
        commands += functionCall.getInstance().accept(this);
        commands += "new java/util/ArrayList\n";
        commands += "dup\n";
        commands += "invokespecial java/util/ArrayList/<init>()V\n";
        commands += "astore " + tempIndexing + "\n";


        for(Expression arg : allArgs){
            commands += "aload " + tempIndexing + "\n";

            Type argType = arg.accept(expressionTypeChecker);

            if(argType instanceof ListType) {
                commands += "new List\n";
                commands += "dup\n";
            }

            commands += arg.accept(this);

            if(argType instanceof IntType)
                commands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";

            if(argType instanceof BoolType)
                commands += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";

            if(argType instanceof ListType) {
                commands += "invokespecial List/<init>(LList;)V\n";
            }

            commands += "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\n";
            commands += "pop\n";
        }

        commands += "aload " + tempIndexing + "\n";
        commands += "invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;\n";

        if(!(returnType instanceof VoidType))
            commands += "checkcast " + makeTypeSignature(returnType) + "\n";

        if (returnType instanceof IntType)
            commands += "invokevirtual java/lang/Integer/intValue()I\n";
        if (returnType instanceof BoolType)
            commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
        return commands;


        //        String commands = "";
//        commands += functionCall.getInstance().accept(this);
//        commands += "new add/ArrayList\n";
//        commands +="dup\n";
//        commands += "invokespecial java/util/ArrayList/<init>()V\n";
//        for (var arg:functionCall.getArgs()   ) {
//            commands += "dup\n";
//            commands +=arg.accept(this);
//            commands += "invokevirtual List/getNameObject(Ljava/lang/Object;)Ljava/lang/Object;\n";
//        }
//        commands +="invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;\n";
//        return  commands;
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
        Type type = listAppend.getElementArg().accept(expressionTypeChecker);
        commands += listAppend.getListArg().accept(this);
        commands += listAppend.getElementArg().accept(this);
        if(type instanceof IntType){
            addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
        }
        if(type instanceof BoolType){
            addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
        }

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
