package parser;

import ast.*;
import errors.SimpleFilesErrorListener;
import libs.Node;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AstTest {
    @Nested
    class HappyPathTests {
        private final String INSTRUCTION_CLASS_NAME = "class ast.Instruction";
        private final String CONDITION_CLASS_NAME = "class ast.Condition";
        private final String EXECUTECONDMAP_CLASS_NAME = "class ast.ExecuteCondMap";
        private final String EXECUTEINSTURCTION_CLASS_NAME = "class ast.ExecuteInstruction";
        private final String VARIABLE_CLASS_NAME = "class ast.Variable";

        public Program parseToAst(String input) {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
            lexer.reset();
            TokenStream tokens = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokens);
            ParseToASTVisitor visitor = new ParseToASTVisitor();
            Node parsedProgram = parser.program().accept(visitor);
            return (Program) parsedProgram;
        }

        @Test
        void oneShotInstruction() {
            // Set up tokenization
            String input = """
                    BEGIN
                    INST create_some_folders_inst -> :create_folder
                    --> path = "/some/path"
                    --> name = "hello_folder_${ITERATOR}"
                    --> count = "30";
                    END
                    """;
            Program inputProgram = parseToAst(input);
            Instruction s = (Instruction) inputProgram.getStatements().get(0);
            assertEquals(INSTRUCTION_CLASS_NAME, s.getClass().toString());
            assertEquals("create_some_folders_inst", s.getName());
            assertEquals(":create_folder", s.getAction().getAction());
            assertEquals(new Parameter("path", "/some/path"), s.getParameters().get(0));
            assertEquals(new Parameter("name", "hello_folder_${ITERATOR}"), s.getParameters().get(1));
            assertEquals(new Parameter("count", "30"), s.getParameters().get(2));
        }

        @Test
        void multiInstruction() {
            // Set up tokenization
            String input = """
                    BEGIN
                    INST move_files -> :move
                    --> from_path = "/some/starting/folder"
                    --> to_path = "/some/ending/folder1";
                    END
                    """;
            Program inputProgram = parseToAst(input);
            Instruction s = (Instruction) inputProgram.getStatements().get(0);
            assertEquals(INSTRUCTION_CLASS_NAME, s.getClass().toString());
            assertEquals("move_files", s.getName());
            assertEquals(":move", s.getAction().getAction());
            assertEquals(2, s.getParameters().size());
            assertEquals(new Parameter("from_path", "/some/starting/folder"), s.getParameters().get(0));
            assertEquals(new Parameter("to_path", "/some/ending/folder1"), s.getParameters().get(1));
        }

        @Test
        void condInstruction() {
            // Set up tokenization
            String input = """
                    BEGIN
                    COND txt_files_larger_than_300MB -> :condition
                    --> type = "file"
                    --> extension = ".txt"
                    --> size = "300"
                    --> comparator = "GT";
                    END
                    """;

            Program inputProgram = parseToAst(input);
            Condition s = (Condition) inputProgram.getStatements().get(0);
            assertEquals(CONDITION_CLASS_NAME, s.getClass().toString());
            assertEquals("txt_files_larger_than_300MB", s.getName()); //weird whitepspace issue here
            assertEquals(":condition", s.getAction().getAction());
            assertEquals(4, s.getParameters().size());
            assertEquals(new Parameter("type", "file"), s.getParameters().get(0));
            assertEquals(new Parameter("extension", ".txt"), s.getParameters().get(1));
            assertEquals(new Parameter("size", "300"), s.getParameters().get(2));
            assertEquals(new Parameter("comparator", "GT"), s.getParameters().get(3));
        }

        @Test
        void execInstruction() {
            // Set up tokenization
            String input = """
                    BEGIN
                    EXEC_COND_MAP move_files WITH_COND txt_files_larger_than_300MB;
                    EXEC_COND_MAP copy_files WITH_COND txt_files_larger_than_150MB;
                    EXEC_INST delete_files;
                    END
                    """;
            Program inputProgram = parseToAst(input);
            ExecuteCondMap stmt0 = (ExecuteCondMap) inputProgram.getStatements().get(0);
            ExecuteCondMap stmt1 = (ExecuteCondMap) inputProgram.getStatements().get(1);
            ExecuteInstruction stmt2 = (ExecuteInstruction) inputProgram.getStatements().get(2);

            assertEquals(EXECUTECONDMAP_CLASS_NAME, stmt0.getClass().toString());
            assertEquals("move_files", stmt0.getInstructionName());
            assertEquals("txt_files_larger_than_300MB", stmt0.getConditionName());

            assertEquals(EXECUTECONDMAP_CLASS_NAME, stmt1.getClass().toString());
            assertEquals("copy_files", stmt1.getInstructionName());
            assertEquals("txt_files_larger_than_150MB", stmt1.getConditionName());

            assertEquals(EXECUTEINSTURCTION_CLASS_NAME, stmt2.getClass().toString());
            assertEquals("delete_files", stmt2.getInstructionName());
        }

        @Test
        void setVariable() {
            // Set up tokenization
            String input = """
                    BEGIN

                    VAR SIZE = "300";

                    END
                    """;
            Program inputProgram = parseToAst(input);
            Variable stmt0 = (Variable) inputProgram.getStatements().get(0);

            assertEquals(VARIABLE_CLASS_NAME, stmt0.getClass().toString());
            assertEquals("SIZE", stmt0.getKey());
            assertEquals("300", stmt0.getValue());
        }
    }
}
