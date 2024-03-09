package index;

import errors.SimpleFilesErrorListener;
import evaluator.Evaluator;
import libs.Node;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.ParseToASTVisitor;
import parser.SimpleFilesLexer;
import parser.SimpleFilesParser;
import staticCheck.StaticCheck;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        boolean debug = false; // Are we debugging?

        // Grab input from file
        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromFileName("testcases/var-createtest1"));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);

        // Debug only
        if (debug) {
            for (Token token : lexer.getAllTokens()) {
                System.out.println(token);
            }
        }
        lexer.reset();

        // Parse and convert to AST
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
        parser.addErrorListener(SimpleFilesErrorListener.INSTANCE);

        ParseTree parseTree = parser.program();
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);

        // Call the static check
        StaticCheck staticCheck = new StaticCheck();
        parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context

        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);
    }
}
