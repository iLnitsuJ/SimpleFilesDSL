package parser;

import errors.SimpleFilesErrorListener;
import errors.SimpleFilesExecutionException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {

    SimpleFilesParser.ProgramContext parseWhiteSpaceExample() {
        String command =
                "BEGIN\n" +
                        "INST create_some_folders_inst -> :create_folder\n;" +
                        "END\n";
        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(command));
        lexer.reset();
        TokenStream tokens = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokens);
        return parser.program();
    }

    @Nested
    class HappyPathTests {
        @Test
        void basicBeginEndNoBodyTest() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);

            List<? extends Token> tokens = lexer.getAllTokens();
            tokens.removeIf(t -> t.getChannel() != 0);

            assertEquals(2, tokens.size());
            assertEquals(SimpleFilesLexer.INST_SET_START, (tokens.get(0)).getType());
            assertEquals(SimpleFilesLexer.INST_SET_END, (tokens.get(1)).getType());
        }

        @Test
        void basicWhitespaceTest() {
            SimpleFilesParser.ProgramContext p = parseWhiteSpaceExample();
            assertEquals(4, p.children.size());

            assertInstanceOf(SimpleFilesParser.BodyContext.class, p.children.get(1));
            assertEquals("create_some_folders_inst", p.children.get(1).getChild(0).getChild(1).getText());
        }

        @Test
        void basicBodyOneInstruction() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                                    
                    INST create_some_folders_inst -> :create_folder
                    --> path = "/some/path"
                    --> name = "hello-folder_${ITERATOR}"
                    --> count = "30";
                                    
                    EXEC_INST create_some_files_inst;
                                    
                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
            List<? extends Token> tokens = lexer.getAllTokens();
            tokens.removeIf(t -> t.getChannel() != 0);

            assertEquals(22, tokens.size());

            // Check some random tokens for brevity
            assertEquals(SimpleFilesLexer.INST_SET_START, (tokens.get(0)).getType());
            assertEquals(SimpleFilesLexer.ACTION, (tokens.get(4)).getType());
            assertEquals(SimpleFilesLexer.SINGLE_EQUAL, (tokens.get(7)).getType());
            assertEquals("= ", tokens.get(7).getText());
            assertEquals(SimpleFilesLexer.PARAM_VAL, (tokens.get(12)).getType());
            assertEquals(SimpleFilesLexer.PARAM_KEY, (tokens.get(14)).getType());
            assertEquals(SimpleFilesLexer.LINE_END, (tokens.get(20)).getType());
            assertEquals(SimpleFilesLexer.INST_SET_END, (tokens.get(21)).getType());
        }

        @Test
        void basicBodyOneCondition() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                                        
                    COND txt_files_larger_than_300MB -> :condition
                    --> type = "file"
                    --> extension = ".txt"
                    --> size = "300"
                    --> comparator = "GT";
                                    
                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
            List<? extends Token> tokens = lexer.getAllTokens();
            tokens.removeIf(t -> t.getChannel() != 0);

            assertEquals(23, tokens.size());

            assertEquals(SimpleFilesLexer.INST_SET_START, (tokens.get(0)).getType());
            assertEquals(SimpleFilesLexer.KEY_CONDITION, (tokens.get(1)).getType());
            assertEquals(SimpleFilesLexer.ACTION, (tokens.get(4)).getType());
            assertEquals(SimpleFilesLexer.SINGLE_EQUAL, (tokens.get(7)).getType());
            assertEquals("= ", tokens.get(7).getText());
            assertEquals(SimpleFilesLexer.PARAM_VAL, (tokens.get(12)).getType());
            assertEquals(SimpleFilesLexer.PARAM_KEY, (tokens.get(14)).getType());
            assertEquals(SimpleFilesLexer.LINE_END, (tokens.get(21)).getType());
            assertEquals(SimpleFilesLexer.INST_SET_END, (tokens.get(22)).getType());
        }

        @Test
        void basicBodyCondVariable() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                                        
                    COND txt_files_larger_than_SIZE -> :condition
                    --> type = "file"
                    --> extension = ".txt"
                    --> size = "${SIZE}"
                    --> comparator = "GT";
                    
                    VAR SIZE = "300";
                    EXEC_COND_MAP move_files WITH_COND txt_files_larger_than_SIZE;
                                    
                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
            List<? extends Token> tokens = lexer.getAllTokens();
            tokens.removeIf(t -> t.getChannel() != 0);

            // check size
            assertEquals(33, tokens.size());

            // check token type
            assertEquals(SimpleFilesLexer.INST_SET_START, (tokens.get(0)).getType());
            assertEquals(SimpleFilesLexer.KEY_VARIABLE, (tokens.get(22)).getType());
            assertEquals(SimpleFilesLexer.PARAM_KEY, (tokens.get(23)).getType());
            assertEquals(SimpleFilesLexer.SINGLE_EQUAL, (tokens.get(24)).getType());
            assertEquals(SimpleFilesLexer.PARAM_VAL, (tokens.get(25)).getType());
            assertEquals(SimpleFilesLexer.LINE_END, (tokens.get(26)).getType());
            assertEquals(SimpleFilesLexer.INST_SET_END, (tokens.get(32)).getType());

            // check token values
            assertEquals((tokens.get(22)).getText().trim(), "VAR");
            assertEquals((tokens.get(23)).getText().trim(), "SIZE");
            assertEquals((tokens.get(25)).getText().trim(), "\"300\"");
        }

        @Nested
        public class AllowedVariedWhitespace {
            @Test
            void variedWhiteSpaceNameAction() {
                SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                                    
                    INST create_some_folders_inst->:create_folder;
                                    
                    END
                    """));
                lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
                List<? extends Token> tokens = lexer.getAllTokens();
                tokens.removeIf(t -> t.getChannel() != 0);

                assertEquals(SimpleFilesLexer.TEXT, (tokens.get(2)).getType());
                assertEquals(SimpleFilesLexer.SINGLE_ARROW, (tokens.get(3)).getType());
                assertEquals(SimpleFilesLexer.ACTION, (tokens.get(4)).getType());

                assertEquals((tokens.get(2)).getText().trim(), "create_some_folders_inst");
                assertEquals((tokens.get(3)).getText().trim(), "->");
                assertEquals((tokens.get(4)).getText().trim(), ":create_folder");
            }

            @Test
            void variedWhiteSpaceParameters() {
                SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                                    
                    INST create_some_folders_inst -> :create_folder
                    -->path="/some/path"    ;
                                    
                    END
                    """));
                lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
                List<? extends Token> tokens = lexer.getAllTokens();
                tokens.removeIf(t -> t.getChannel() != 0);

                assertEquals((tokens.get(5)).getType(), SimpleFilesLexer.DOUBLE_ARROW);
                assertEquals((tokens.get(6)).getType(), SimpleFilesLexer.PARAM_KEY);
                assertEquals((tokens.get(7)).getType(), SimpleFilesLexer.SINGLE_EQUAL);
                assertEquals((tokens.get(8)).getType(), SimpleFilesLexer.PARAM_VAL);

                assertEquals((tokens.get(5)).getText().trim(), "-->");
                assertEquals((tokens.get(6)).getText().trim(), "path");
                assertEquals((tokens.get(7)).getText().trim(), "=");
                assertEquals((tokens.get(8)).getText().trim(), "\"/some/path\"");
            }

            @Test
            void variedWhiteSpaceVariables() {
                SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                                    
                    VAR SIZE="300"        ;
                                    
                    END
                    """));
                lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
                List<? extends Token> tokens = lexer.getAllTokens();
                tokens.removeIf(t -> t.getChannel() != 0);

                assertEquals((tokens.get(1)).getType(), SimpleFilesLexer.KEY_VARIABLE);
                assertEquals((tokens.get(2)).getType(), SimpleFilesLexer.PARAM_KEY);
                assertEquals((tokens.get(3)).getType(), SimpleFilesLexer.SINGLE_EQUAL);
                assertEquals((tokens.get(4)).getType(), SimpleFilesLexer.PARAM_VAL);

                assertEquals((tokens.get(1)).getText().trim(), "VAR");
                assertEquals((tokens.get(2)).getText().trim(), "SIZE");
                assertEquals((tokens.get(3)).getText().trim(), "=");
                assertEquals((tokens.get(4)).getText().trim(), "\"300\"");
            }

            @Test
            void variedWhiteSpaceParamVal() {
                SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                                    
                    INST create_some_folders_inst -> :create_folder
                    -->path = "/some/path with a space"    ;
                                    
                    END
                    """));
                lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
                List<? extends Token> tokens = lexer.getAllTokens();
                tokens.removeIf(t -> t.getChannel() != 0);

                assertEquals((tokens.get(5)).getType(), SimpleFilesLexer.DOUBLE_ARROW);
                assertEquals((tokens.get(6)).getType(), SimpleFilesLexer.PARAM_KEY);
                assertEquals((tokens.get(7)).getType(), SimpleFilesLexer.SINGLE_EQUAL);
                assertEquals((tokens.get(8)).getType(), SimpleFilesLexer.PARAM_VAL);

                assertEquals((tokens.get(5)).getText().trim(), "-->");
                assertEquals((tokens.get(6)).getText().trim(), "path");
                assertEquals((tokens.get(7)).getText().trim(), "=");
                assertEquals((tokens.get(8)).getText().trim(), "\"/some/path with a space\"");
            }
        }
    }

    @Nested
    public class UnhappyPathTests {
        @Test
        void randomCharactersInputThrowsParseCancellationException() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN asdfasdfasdf
                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);

            Throwable exception = assertThrows(SimpleFilesExecutionException.class, lexer::getAllTokens);
            assertEquals("line 1:6 token recognition error at: 'a'", exception.getMessage());
        }
    }

    void printAllTokensHelper(List<? extends Token> tokens) {
        if (tokens == null) {
            System.out.println("Token list is null.");
        }
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(i + ": " + tokens.get(i).getText());
        }
    }
}
