package evaluator;


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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


import parser.SimpleFilesParser;
import staticCheck.StaticCheck;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluatorTest {

    String pathToProject;

    @BeforeEach
    void initialization(){
        pathToProject = System.getProperty("user.dir");

    }

    //region createTests

    @Test
    void createFolderTest1() {


        String startingPath = "/test/evaluator/Examples/Example1";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }

        assertFalse(dirPath.exists());


        String input = """
                BEGIN

                INST create_some_folders_inst -> :create_folder
                --> path = \"""" + pathToProject + startingPath + "\"" + """
                --> name = "hello_folder_${ITERATOR}"
                --> count = "5";
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
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(5, contents.length);
        List<String> contentsName = Arrays.asList(contents);
        for (int i = 0; i < 5; i++) {
            String expectedFolderName = "hello_folder_" + i;
            assertTrue(contentsName.contains(expectedFolderName));
        }

    }

    @Test
    // This test has unintended behavior.
    void createSingleFolderTest() {


        String startingPath = "/test/evaluator/Examples/ExampleSingleFolder";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }

        assertFalse(dirPath.exists());

        String input = """
                BEGIN

                INST create_some_folders_inst -> :create_folder
                --> path = \"""" + pathToProject + startingPath +  "\"\n" + """
                --> name = "hello_folder_18"
                --> count = "1";
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
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(1, contents.length);
            String expectedFolderName = "hello_folder_18";
            assertEquals(expectedFolderName, contents[0]);
        }

    @Test
    void createSingleFileTest() {


        String startingPath = "/test/evaluator/Examples/ExampleSingleFile";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }

        assertFalse(dirPath.exists());

        String input = """
                BEGIN

                INST create_some_files_inst -> :create_file
                --> path = \"""" + pathToProject + startingPath +  "\"\n" + """
                --> name = "hello_file_10"
                --> count = "1";
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

        //call static check
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(1, contents.length);
        String expectedFolderName = "hello_file_10";
        assertEquals(expectedFolderName, contents[0]);
    }

    @Test
    void createManyFileTest() {


        String startingPath = "/test/evaluator/Examples/ExampleManyFiles";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }

        assertFalse(dirPath.exists());

        String input = """
                BEGIN

                INST create_some_files_inst -> :create_file
                --> path = \"""" + pathToProject + startingPath +  "\"\n" + """
                --> name = "hello_file_${ITERATOR}"
                --> count = "6";
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

        //call static check
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }

        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(6, contents.length);
        List<String> contentsName = Arrays.asList(contents);
        for (int i = 0; i < 6; i++) {
            String expectedFolderName = "hello_file_" + i;
            assertTrue(contentsName.contains(expectedFolderName));
        }
    }
    @Test
    void createManyFileWithIncrementTest() {


        String startingPath = "/test/evaluator/Examples/ExampleManyFilesWithInc";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }

        assertFalse(dirPath.exists());

        String input = """
                BEGIN

                INST create_some_files_inst -> :create_file
                --> path = \"""" + pathToProject + startingPath +  "\"\n" + """
                --> name = "hello_file_${ITERATOR:100:3}"
                --> count = "6";
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

        //call static check
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }

        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(6, contents.length);
        List<String> contentsName = Arrays.asList(contents);
        for (int i = 0; i < 6; i++) {
            String expectedFolderName = "hello_file_" + (100 + i * 3);
            assertTrue(contentsName.contains(expectedFolderName));
        }
    }

    @Test
    void createManyFileWithDecrementTest() {


        String startingPath = "/test/evaluator/Examples/ExampleManyFilesWithDecr";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }

        assertFalse(dirPath.exists());
        String input = """
                BEGIN

                INST create_some_files_inst -> :create_file
                --> path = \"""" + pathToProject + startingPath +  "\"\n" + """
                --> name = "hello_file_${ITERATOR:10:-5}"
                --> count = "6";
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

        //call static check
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(6, contents.length);
        List<String> contentsName = Arrays.asList(contents);
        for (int i = 0; i < 6; i++) {
            String expectedFolderName = "hello_file_" + (10 - i * 5);
            assertTrue(contentsName.contains(expectedFolderName));
        }
    }

    @Test
    void createManyFolderWithDecrementTest() {


        String startingPath = "/test/evaluator/Examples/ExampleManyFoldersWithDecr";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }
        assertFalse(dirPath.exists());


        String input = """
                BEGIN

                INST create_some_folders_inst -> :create_folder
                --> path = \"""" + pathToProject + startingPath +  "\"\n" + """
                --> name = "hello_folder_${ITERATOR:10:-5}"
                --> count = "5";
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
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }

        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(5, contents.length);
        List<String> contentsName = Arrays.asList(contents);
        for (int i = 0; i < 5; i++) {
            String expectedFolderName = "hello_folder_" + (10 - i * 5);
            assertTrue(contentsName.contains(expectedFolderName));
        }

    }

    @Test
    void createManyFolderWithIncrementTest() {


        String startingPath = "/test/evaluator/Examples/ExampleManyFoldersWithInc";

        File dirPath = new File(pathToProject + startingPath);

        if (dirPath.exists()){
            cleanFolder(pathToProject + startingPath);
        }

        assertFalse(dirPath.exists());
        String input = """
                BEGIN

                INST create_some_folders_inst -> :create_folder
                --> path = \"""" + pathToProject + startingPath +  "\"\n" + """
                --> name = "hello_folder_${ITERATOR:20:5}"
                --> count = "7";
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
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }

        // calling evaluator on ast tree
        Evaluator evaluator = new Evaluator();
        parsedProgram.accept(null, evaluator);


        dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();

        assert contents != null;
        assertEquals(7, contents.length);
        List<String> contentsName = Arrays.asList(contents);
        for (int i = 0; i < 7; i++) {
            String expectedFolderName = "hello_folder_" + (20 + i * 5);
            assertTrue(contentsName.contains(expectedFolderName));
        }

    }

    //endregion

    //region groupTests

     //commented since condition is not implemented yet
    @Test
    void groupBySizeTest(){
        // Test condition setup:

        // creating dummy files with specified size to starting folder

        String startingPath = "/test/evaluator/Examples/Example2/starting/";
        String endingPath = "/test/evaluator/Examples/Example2/ending/";


        File startingFolder = new File(pathToProject + startingPath);

        cleanFolder(startingPath);


        startingFolder.mkdirs();
        String dummy1_pathName = pathToProject + startingPath + "dummy1.txt";
        String dummy2_pathName = pathToProject + startingPath + "dummy2.txt";
        String smallDummy_pathName = pathToProject + startingPath + "smallDummy.txt";
        createDummyFile(4000000, dummy1_pathName);
        createDummyFile(3300000, dummy2_pathName);
        createDummyFile(100, smallDummy_pathName);


        File dirPath = new File(pathToProject + startingPath);
        String[] contents = dirPath.list();
        assert contents != null;
        assertEquals(3, contents.length);



        String input = """
                BEGIN
                COND txt_files_larger_than_3000KB -> :condition
                --> type = "file"
                --> extension = "txt"
                --> size = "3000"
                --> comparator = "GT";
                                
                INST move_files -> :group
                --> group_target = \"""" + pathToProject + startingPath + "\"\n" +
                "--> path = \"" + pathToProject + endingPath + "\"\n" +
                """
                --> mode = "move";
                                
                EXEC_COND_MAP move_files WITH_COND txt_files_larger_than_3000KB;
                                
                END
                """;


        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);

        ParseTree parseTree = parser.program();

        // calling evaluator on ast tree, evaluator should execute code as well as printing it to a template file
        Evaluator evaluator = new Evaluator();

        // Running the template file if code is not execute in evaluation step
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);

        parsedProgram.accept(null, evaluator);

        // set up testing info
        dirPath = new File(pathToProject + startingPath);
        contents = dirPath.list();
        assert contents != null;

        // only 1 file is not moved
        assertEquals(1, contents.length);
        assertEquals("smallDummy.txt", contents[0]);

        dirPath = new File(pathToProject + endingPath);
        contents = dirPath.list();
        assert contents != null;

        // 2 files that are large enough should be moved to ending directory
        assertEquals(2, contents.length);
        assertEquals( "dummy1.txt",contents[0]);
        assertEquals("dummy2.txt",contents[1]);

    }


    @Test
    void groupCopyFilesTest(){

        String startingPath = "/test/evaluator/Examples/Example3/starting/";
        String endingPath = "/test/evaluator/Examples/Example3/ending/";


        File startingFolder = new File(pathToProject + startingPath);
        File endingFolder = new File(pathToProject + endingPath);
        try {
            if (!startingFolder.exists()){
                startingFolder.mkdirs();

            } else {
                Files.delete(Paths.get(pathToProject + startingPath));
            }


            Files.createFile(Path.of(pathToProject + startingPath + "test1.txt"));
            Files.createFile(Path.of(pathToProject + startingPath + "test2.png"));
            Files.createFile(Path.of(pathToProject + startingPath + "test3.jpeg"));

        } catch (IOException e) {
            //e.printStackTrace();
        }

        assertEquals(3, Objects.requireNonNull(startingFolder.list()).length);

        // cleanup previous result
        if (endingFolder.exists()){
            cleanFolder(pathToProject + endingPath);
        }

        assertFalse(endingFolder.exists());



        String input = """
                BEGIN
                        
                INST group_files -> :group
                --> group_target =\"""" + pathToProject + startingPath + "\"\n" +
                "--> path = \"" + pathToProject + endingPath + "\"\n" +
                """
                --> contains = "1"
                --> mode = "copy"
                --> type = "file";
                
                
                EXEC_INST group_files;
                                
                END
                """;


        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
        ParseTree parseTree = parser.program();

        // calling evaluator on ast tree, evaluator should execute code as well as printing it to a template file
        Evaluator evaluator = new Evaluator();

        // Running the template file if code is not execute in evaluation step
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);
        //call static check
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        parsedProgram.accept(null, evaluator);

        // set up testing info

        String[] contents = startingFolder.list();
        assert contents != null;
        assertEquals(3, contents.length);

        contents = endingFolder.list();
        assert contents != null;
        assertEquals(1, contents.length);

    }

    @Test
    void groupMoveFilesTest(){

        String startingPath = "/test/evaluator/Examples/ExampleMoveFiles/starting/";
        String endingPath = "/test/evaluator/Examples/ExampleMoveFiles/ending/";


        File startingFolder = new File(pathToProject + startingPath);
        File endingFolder = new File(pathToProject + endingPath);
        try {
            if (!startingFolder.exists()){
                startingFolder.mkdirs();

            } else {
                cleanFolder(pathToProject + startingPath);
            }
            File newFolder1 = new File(startingFolder, "Folder100");
            newFolder1.mkdirs();
            Files.createFile(Path.of(pathToProject + startingPath + "test1.txt"));
            Files.createFile(Path.of(pathToProject + startingPath + "test2.png"));
            Files.createFile(Path.of(pathToProject + startingPath + "test3.jpeg"));


        } catch (IOException e) {
            System.err.println("Debugging: Error creating files.");
        }

        assertEquals(4, Objects.requireNonNull(startingFolder.list()).length);

        // cleanup previous result
        if (endingFolder.exists()){
            cleanFolder(pathToProject + endingPath);
        }

        assertFalse(endingFolder.exists());




        String input = """
                BEGIN
                        
                INST group_files -> :group
                --> group_target = \"""" + pathToProject + startingPath +  "\"\n" +
                "--> path = \"" + pathToProject + endingPath +  "\"\n" +
                """
                --> mode = "move"
                --> contains = "3"
                --> type = "file";
                
                EXEC_INST group_files;
                                
                END
                """;


        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
        ParseTree parseTree = parser.program();

        // calling evaluator on ast tree, evaluator should execute code as well as printing it to a template file
        Evaluator evaluator = new Evaluator();

        // Running the template file if code is not execute in evaluation step
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);
        //call static check
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        parsedProgram.accept(null, evaluator);

        // set up testing info

        String[] contents = startingFolder.list();
        assert contents != null;
        assertEquals(3, contents.length);

        contents = endingFolder.list();
        assert contents != null;
        assertEquals(1, contents.length);

    }


    @Test
    void groupMoveFolderTest(){

        String startingPath = "/test/evaluator/Examples/ExampleMoveFolders/starting/";
        String endingPath = "/test/evaluator/Examples/ExampleMoveFolders/ending/";


        File startingFolder = new File(pathToProject + startingPath);
        File endingFolder = new File(pathToProject + endingPath);
        try {
            if (!startingFolder.exists()){
                startingFolder.mkdirs();

            } else {
                cleanFolder(pathToProject + startingPath);
            }
            File newFolder1 = new File(startingFolder, "Folder100");
            newFolder1.mkdirs();
            Files.createFile(Path.of(pathToProject + startingPath + "test1.txt"));
            Files.createFile(Path.of(pathToProject + startingPath + "test2.png"));
            Files.createFile(Path.of(pathToProject + startingPath + "test3.jpeg"));


        } catch (IOException e) {
            //e.printStackTrace();
        }

        assertEquals(4, Objects.requireNonNull(startingFolder.list()).length);

        // cleanup previous result
        if (endingFolder.exists()){
            cleanFolder(pathToProject + endingPath);
        }

        assertFalse(endingFolder.exists());



        String input = """
                BEGIN
                        
                INST group_files -> :group
                --> group_target =\"""" + pathToProject + startingPath +  "\"\n" +
                "--> path = \"" + pathToProject + endingPath +  "\"\n" +
                """
                --> mode = "move"
                --> type = "folder";
                
                EXEC_INST group_files;
                                
                END
                """;


        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
        ParseTree parseTree = parser.program();

        // calling evaluator on ast tree, evaluator should execute code as well as printing it to a template file
        Evaluator evaluator = new Evaluator();

        // Running the template file if code is not execute in evaluation step
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);

        //call static check
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        parsedProgram.accept(null, evaluator);

        // set up testing info

        String[] contents = startingFolder.list();
        assert contents != null;
        assertEquals(3, contents.length);

        contents = endingFolder.list();
        assert contents != null;
        assertEquals(1, contents.length);

    }
@Test
    void groupCopyFolderTest(){

        String startingPath = "/test/evaluator/Examples/ExampleCopyFolder/starting/";
        String endingPath = "/test/evaluator/Examples/ExampleCopyFolder/ending/";


        File startingFolder = new File(pathToProject + startingPath);
        File endingFolder = new File(pathToProject + endingPath);



        if (!startingFolder.exists()){
            startingFolder.mkdirs();

        } else {
            cleanFolder(pathToProject + startingPath);
        }




        File newFolder1 = new File(startingFolder, "Folder1");
        newFolder1.mkdirs();
        File newFolder2 = new File(startingFolder, "Folder2");
        newFolder2.mkdirs();
        File newFolder3 = new File(startingFolder, "Folder3");
        newFolder3.mkdirs();

        assertEquals(3, Objects.requireNonNull(startingFolder.list()).length);

        // cleanup previous result
        if (endingFolder.exists()){
            cleanFolder(pathToProject + endingPath);
        }

        assertFalse(endingFolder.exists());

        System.out.println("Finish test setup");

        String input = """
                BEGIN
                        
                INST group_files -> :group
                --> group_target = \"""" + pathToProject + startingPath +  "\"\n" +
                "--> path =\"" + pathToProject + endingPath +  "\"\n" +
                """
                --> contains = "1"
                --> mode = "copy"
                --> type = "folder";
                
                EXEC_INST group_files;
                                
                END
                """;


        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
        ParseTree parseTree = parser.program();

        // calling evaluator on ast tree, evaluator should execute code as well as printing it to a template file
        Evaluator evaluator = new Evaluator();

        // Running the template file if code is not execute in evaluation step
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        parsedProgram.accept(null, evaluator);

        // set up testing info

        String[] contents = startingFolder.list();
        assert contents != null;
        assertEquals(3, contents.length);

        contents = endingFolder.list();
        assert contents != null;
        assertEquals(1, contents.length);

    }

    //endregion

    //region renameTest
    @Test
    void renameTest(){

        String startingPath = "/test/evaluator/Examples/Example4/";


        File startingFolder = new File(pathToProject + startingPath);

        if (!startingFolder.exists()){
            startingFolder.mkdirs();
        } else {
            cleanFolder(pathToProject + startingPath);
        }
        try {
            startingFolder.mkdirs();
            Files.createFile(Path.of(pathToProject + startingPath + "test_file1.txt"));
            Files.createFile(Path.of(pathToProject + startingPath + "abcd.txt"));
            Files.createFile(Path.of(pathToProject + startingPath + "apple.txt"));
            Files.createFile(Path.of(pathToProject + startingPath + "orange.txt"));


        } catch (IOException e) {
            System.err.println("Debugging: Error creating files.");
        }

        String[] contents = startingFolder.list();
        assert contents != null;
        assertEquals(4, contents.length);




        String input = """
                BEGIN
                        
                INST rename_files -> :rename
                --> path = \"""" + pathToProject + startingPath + "\"\n" +

                """
                --> mode = "upper_case"
                --> contains = "apple";
                                
                EXEC_INST rename_files;
                                
                END
                """;

        SimpleFilesLexer lexer = new SimpleFilesLexer(CharStreams.fromString(input));
        lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);

        ParseTree parseTree = parser.program();

        // calling evaluator on ast tree, evaluator should execute code as well as printing it to a template file
        Evaluator evaluator = new Evaluator();

        // Running the template file if code is not execute in evaluation step
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);
        StaticCheck staticCheck = new StaticCheck();
        try {
            parsedProgram.accept(null, staticCheck); // Assuming 'null' is an acceptable context
        } catch (Exception e) {
            fail("Static check failed: " + e.getMessage());
        }
        parsedProgram.accept(null, evaluator);


        // set up testing info

        contents = startingFolder.list();
        assert contents != null;
        assertEquals(4, contents.length);
        int count = 0;
        for (String s : contents){
            if (s.contains("APPLE")){
                assertEquals(s, s.substring(0,5).toUpperCase()+ ".txt");
                count++;
            } else if (s.contains("orange.txt")){
                // making sure other files are not changed
                assertEquals(s, s.toLowerCase());
            }
        }

        assertEquals(1, count);
        //

    }

    //endregion



    // For local testing.
/*
    @Test
    void testRunningInputFromFile(){

        String input_DSL_file_path = "C:\\Users\\lv480\\Desktop\\test3.txt";

        SimpleFilesLexer lexer = null;
        try {
            lexer = new SimpleFilesLexer(CharStreams.fromPath(Paths.get(input_DSL_file_path)));
            lexer.addErrorListener(SimpleFilesErrorListener.INSTANCE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SimpleFilesParser parser = new SimpleFilesParser(tokenStream);
        ParseTree parseTree = parser.program();

        // calling evaluator on ast tree, evaluator should execute code as well as printing it to a template file
        Evaluator evaluator = new Evaluator();

        // Running the template file if code is not execute in evaluation step
        ParseToASTVisitor parseToASTVisitor = new ParseToASTVisitor();
        Node parsedProgram = parseTree.accept(parseToASTVisitor);
        parsedProgram.accept(null, evaluator);

        System.out.println("file execution complete");

    }
*/

    //region helperFunctions

    // Helper to remove content in a specified folder path and the folder itself
    void cleanFolder(String folder_path){
        File folder = new File(folder_path);
        if (folder.exists()){
            File[] files = folder.listFiles();

            for (File f : files){
                if (f.delete()){
                    System.out.println("Deleted: " + f.getName());
                } else {
                    cleanFolder(f.getPath());
                }

                f.delete();

            }
            // deleting the folder
            if (folder.delete()){
                System.out.println("Deleted: " + folder.getName());
            }
            System.out.println("Finish cleaning folder at path: " + folder_path);

        }

    }


    // Helper to create dummy files
    void createDummyFile(long sizeInBytes, String pathName){
        RandomAccessFile raf;
        File file = new File(pathName);

        try {
            if (!Files.exists(Path.of(file.getParent()))){
                Files.createDirectories(Path.of(file.getParent()));
            }
            raf = new RandomAccessFile(file, "rw");
            raf.setLength(sizeInBytes);
            raf.close();
        } catch (IOException e) {
            String msg = String.format("Error creating dummy file at %s.\n%s", pathName, e.getMessage());
            System.err.println(msg);
        }

    }

    //endregion

}



