package staticCheck;

import errors.SimpleFilesErrorListener;
import libs.Node;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ParseToASTVisitor;
import parser.SimpleFilesLexer;


import java.io.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


import parser.SimpleFilesParser;
import staticCheck.StaticCheck;

import static org.junit.jupiter.api.Assertions.*;
public class StaticCheckTest {
    @Test
    void testInvalidSyntaxForStaticCheck() {
        String input = """
            BEGIN

            INST create_some_folders_inst -> :create_folder
            --> name = "incomplete_folder"
            EXEC_INST create_some_folders_inst;

            END
            """;

        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);

        // Parse from input
        ParseTree parseTree = parser.program();

        // Convert to AST node
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);

        //call static check
        StaticCheck staticCheck = new StaticCheck();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            parsedProgram.accept(null, staticCheck);
        });

        String expectedMessage = "Static check failed: Invalid PATH parameter for :create_folder instruction.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testInvalidParameterValueForStaticCheck() {
        String input = """
            BEGIN

            INST create_some_folders_inst -> :create_file
            --> path = "/some/path"
            --> name = "file_name"
            --> count = "-5"; 
            EXEC_INST create_some_folders_inst;

            END
            """;

        // (Lexer, Parser, ParseTree, and AST conversion code similar to previous examples)

        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);

        // Parse from input
        ParseTree parseTree = parser.program();

        // Convert to AST node
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);

        StaticCheck staticCheck = new StaticCheck();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            parsedProgram.accept(null, staticCheck);
        });

        System.out.println(exception.getMessage());
        String expectedMessage = "Static check failed: COUNT parameter must be a positive integer for :create_file instruction.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void testConflictingParametersForStaticCheck() {
        String input = """
            BEGIN

            INST create_some_folders_inst -> :create_folder
            --> path = "/some/path"
            --> name = "folder_name"
            --> namefile = "/path/to/namefile.csv";
            EXEC_INST create_some_files_inst;

            END
            """;

        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);

        // Parse from input
        ParseTree parseTree = parser.program();

        // Convert to AST node
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);

        StaticCheck staticCheck = new StaticCheck();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            parsedProgram.accept(null, staticCheck);
        });

        String expectedMessage = "Static check failed: Only one of NAME or NAMEFILE must be specified";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void testNonExistentInstructionForStaticCheck() {
        String input = """
            BEGIN

            INSTnon_existent_inst -> :non_existent_action
            --> path = "/some/path"
            --> name = "folder_name";
            EXEC_INST non_existent_inst;

            END
            """;


        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);

        // Parse from input
        ParseTree parseTree = parser.program();

        // Convert to AST node
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);

        StaticCheck staticCheck = new StaticCheck();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            parsedProgram.accept(null, staticCheck);
        });

        String expectedMessage = "Static check failed: Unknown instruction type 'non_existent_inst'. Context: null";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
