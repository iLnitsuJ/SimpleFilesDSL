package parser;

import errors.SimpleFilesErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ParserTest {

    @Nested
    class HappyPathTests {
        @Test
        void noInstructions() {
            // Set up tokenization
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN
                    END
                    """));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
            ParseTree parseTree = parser.program();

            assertEquals(parseTree.getChild(0).getText(), "BEGIN");
            assertEquals(parseTree.getChild(1).getText(), "END");

        }

        @Test
        void oneInstruction() {
            // Set up tokenization
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN

                    INST create_some_folders_inst -> :create_folder
                    --> path = "/some/path"
                    --> name = "hello-folder_${ITERATOR}"
                    --> count = "30";

                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);

            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
            SimpleFilesParser.ProgramContext p = parser.program();
            assertEquals(4, p.children.size());

            assertEquals(p.children.get(0).getText(), "BEGIN");

            SimpleFilesParser.BodyContext bodyCxt = (SimpleFilesParser.BodyContext) p.children.get(1);
            assertInstanceOf(SimpleFilesParser.BodyContext.class, bodyCxt);

            SimpleFilesParser.InstructionContext instructionCxt = (SimpleFilesParser.InstructionContext) bodyCxt.children.get(0);
            assertInstanceOf(SimpleFilesParser.InstructionContext.class, instructionCxt);
            assertEquals(7, instructionCxt.children.size());

            String body_option = instructionCxt.children.get(0).getText();
            assertEquals("INST ", body_option);

            SimpleFilesParser.Set_actionContext actionCxt = (SimpleFilesParser.Set_actionContext) instructionCxt.children.get(2);
            assertEquals("-> ", actionCxt.children.get(0).getText());
            assertEquals(":create_folder", actionCxt.children.get(1).getText());

            SimpleFilesParser.Set_paramContext param1Cxt = (SimpleFilesParser.Set_paramContext) instructionCxt.children.get(3);
            assertEquals("--> ", param1Cxt.children.get(0).getText());
            assertEquals("path", param1Cxt.children.get(1).getText());
            assertEquals("= ", param1Cxt.children.get(2).getText());
            assertEquals("\"/some/path\"", param1Cxt.children.get(3).getText());

            SimpleFilesParser.Set_paramContext param2Cxt = (SimpleFilesParser.Set_paramContext) instructionCxt.children.get(4);
            assertEquals("--> ", param2Cxt.children.get(0).getText());
            assertEquals("name", param2Cxt.children.get(1).getText());
            assertEquals("= ", param2Cxt.children.get(2).getText());
            assertEquals("\"hello-folder_${ITERATOR}\"", param2Cxt.children.get(3).getText());

            SimpleFilesParser.Set_paramContext param3Cxt = (SimpleFilesParser.Set_paramContext) instructionCxt.children.get(5);
            assertEquals("--> ", param3Cxt.children.get(0).getText());
            assertEquals("count", param3Cxt.children.get(1).getText());
            assertEquals("= ", param3Cxt.children.get(2).getText());
            assertEquals("\"30\"", param3Cxt.children.get(3).getText());

            String semicolon = instructionCxt.children.get(6).getText();
            assertEquals(";", semicolon);


            assertEquals(p.children.get(2).getText(), "END");
        }

        @Test
        void oneCondition() {
            // Set up tokenization
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

            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
            SimpleFilesParser.ProgramContext p = parser.program();
            assertEquals(4, p.children.size());

            SimpleFilesParser.BodyContext bodyCxt = (SimpleFilesParser.BodyContext) p.children.get(1);
            assertInstanceOf(SimpleFilesParser.BodyContext.class, bodyCxt);

            SimpleFilesParser.ConditionContext conditionCxt = (SimpleFilesParser.ConditionContext) bodyCxt.children.get(0);
            assertEquals(8, conditionCxt.children.size());

            assertEquals("COND ", conditionCxt.children.get(0).getText());
            assertEquals("txt_files_larger_than_300MB", conditionCxt.children.get(1).getText());

            SimpleFilesParser.Set_actionContext actionCxt = (SimpleFilesParser.Set_actionContext) conditionCxt.children.get(2);
            assertEquals("-> ", actionCxt.children.get(0).getText());
            assertEquals(":condition", actionCxt.children.get(1).getText());

            SimpleFilesParser.Set_paramContext param1Cxt = (SimpleFilesParser.Set_paramContext) conditionCxt.children.get(3);
            assertEquals("--> ", param1Cxt.children.get(0).getText());
            assertEquals("type", param1Cxt.children.get(1).getText());
            assertEquals("= ", param1Cxt.children.get(2).getText());
            assertEquals("\"file\"", param1Cxt.children.get(3).getText());

            SimpleFilesParser.Set_paramContext param2Cxt = (SimpleFilesParser.Set_paramContext) conditionCxt.children.get(4);
            assertEquals("--> ", param2Cxt.children.get(0).getText());
            assertEquals("extension", param2Cxt.children.get(1).getText());
            assertEquals("= ", param2Cxt.children.get(2).getText());
            assertEquals("\".txt\"", param2Cxt.children.get(3).getText());

            SimpleFilesParser.Set_paramContext param3Cxt = (SimpleFilesParser.Set_paramContext) conditionCxt.children.get(5);
            assertEquals("--> ", param3Cxt.children.get(0).getText());
            assertEquals("size", param3Cxt.children.get(1).getText());
            assertEquals("= ", param3Cxt.children.get(2).getText());
            assertEquals("\"300\"", param3Cxt.children.get(3).getText());

            SimpleFilesParser.Set_paramContext param4Cxt = (SimpleFilesParser.Set_paramContext) conditionCxt.children.get(6);
            assertEquals("--> ", param4Cxt.children.get(0).getText());
            assertEquals("comparator", param4Cxt.children.get(1).getText());
            assertEquals("= ", param4Cxt.children.get(2).getText());
            assertEquals("\"GT\"", param4Cxt.children.get(3).getText());

            String semicolon = conditionCxt.children.get(7).getText();
            assertEquals(";", semicolon);


        }

        @Test
            // NOTE: this is on the happy path because it is not the job of the
            // parser to determine if the executed instruction actually exists
        void oneInstExecution() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN

                    EXEC_INST delete_files;

                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);

            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
            SimpleFilesParser.ProgramContext p = parser.program();

            assertEquals(p.getChild(0).getText(), "BEGIN");

            SimpleFilesParser.BodyContext bodyCxt = (SimpleFilesParser.BodyContext) p.children.get(1);
            assertInstanceOf(SimpleFilesParser.BodyContext.class, bodyCxt);

            SimpleFilesParser.ExecutionContext execCxt = (SimpleFilesParser.ExecutionContext) bodyCxt.children.get(0);
            SimpleFilesParser.Exec_instContext execInstCxt = (SimpleFilesParser.Exec_instContext) execCxt.children.get(0);
            assertEquals("EXEC_INST ", execInstCxt.children.get(0).getText());
            assertEquals("delete_files", execInstCxt.children.get(1).getText());


            assertEquals(p.getChild(2).getText(), "END");

        }

        @Test
            // NOTE: this is on the happy path because it is not the job of the
            // parser to determine if the executed instruction actually exists
        void oneCondMapExecution() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN

                    EXEC_COND_MAP move_files WITH_COND txt_files_larger_than_300MB;

                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);

            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
            SimpleFilesParser.ProgramContext p = parser.program();

            assertEquals(p.getChild(0).getText(), "BEGIN");

            SimpleFilesParser.BodyContext bodyCxt = (SimpleFilesParser.BodyContext) p.children.get(1);
            assertInstanceOf(SimpleFilesParser.BodyContext.class, bodyCxt);

            SimpleFilesParser.ExecutionContext execContext = (SimpleFilesParser.ExecutionContext) bodyCxt.children.get(0);
            assertInstanceOf(SimpleFilesParser.ExecutionContext.class, execContext);

            SimpleFilesParser.Exec_cond_mapContext execInstCxt = (SimpleFilesParser.Exec_cond_mapContext) execContext.children.get(0);
            assertInstanceOf(SimpleFilesParser.Exec_cond_mapContext.class, execInstCxt);

            assertEquals("EXEC_COND_MAP ", execInstCxt.children.get(0).getText());
            assertEquals("move_files", execInstCxt.children.get(1).getText());
            assertEquals("WITH_COND ", execInstCxt.children.get(2).getText());
            assertEquals("txt_files_larger_than_300MB", execInstCxt.children.get(3).getText());


            assertEquals(p.getChild(2).getText(), "END");

        }

        @Test
        void oneVariable() {
            SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString("""
                    BEGIN

                    VAR SIZE = "300";

                    END
                    """));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);

            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
            SimpleFilesParser.ProgramContext p = parser.program();

            assertEquals(p.getChild(0).getText(), "BEGIN");

            SimpleFilesParser.BodyContext bodyCxt = (SimpleFilesParser.BodyContext) p.children.get(1);
            assertInstanceOf(SimpleFilesParser.BodyContext.class, bodyCxt);

            SimpleFilesParser.VariableContext varCxt = (SimpleFilesParser.VariableContext) bodyCxt.children.get(0);
            assertEquals("VAR ", varCxt.children.get(0).getText());
            assertEquals("SIZE", varCxt.children.get(1).getText());
            assertEquals("= ", varCxt.children.get(2).getText());
            assertEquals("\"300\"", varCxt.children.get(3).getText());
        }
    }
}
