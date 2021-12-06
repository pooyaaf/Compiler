package main;

import main.visitor.name.ASTTreePrinter;
import main.visitor.name.ErrorVisitor;
import parsers.*;
import main.ast.nodes.Program;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class CmmCompiler {
    public void compile(CharStream textStream) {
        CmmLexer cmmLexer = new CmmLexer(textStream);
        CommonTokenStream tokenStream = new CommonTokenStream(cmmLexer);
        CmmParser cmmParser = new CmmParser(tokenStream);

        Program program = cmmParser.cmm().cmmProgram;

        //todo
        ErrorVisitor ErrorVisit = new ErrorVisitor();
        ErrorVisit.visit(program);

        if(ErrorVisitor.err == false){
            ASTTreePrinter astTreePrinter = new ASTTreePrinter();
            astTreePrinter.visit(program);
        }
    }
}
