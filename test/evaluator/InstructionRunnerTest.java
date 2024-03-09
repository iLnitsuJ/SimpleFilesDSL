package evaluator;

import ast.Action;
import ast.Instruction;
import ast.Parameter;
import errors.SimpleFilesExecutionException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class InstructionRunnerTest {

    @TempDir
    File rootDirectory;
    @TempDir
    File otherDirectory;
    @TempDir
    File otherDirectory1;

    ArrayList<Parameter> params = new ArrayList<>();
    InstructionRunner instructionRunner = new InstructionRunner();

    Memory memory = Memory.getInstance();

    @BeforeEach
    void initializationBeforeMainTest() {

    }

    @AfterEach
    void cleanUpAfterEachTest() {
        params.clear();
        memory.clear();
    }

    @Nested
    public class RenamingTests {
        @BeforeEach
        void initializationBeforeRenamingTests() {
            try {
                // Setup some test files in the root directory
                boolean f1 = new File(rootDirectory, "apple.txt").createNewFile();
                boolean f2 = new File(rootDirectory, "orange.txt").createNewFile();
                boolean f3 = new File(rootDirectory, "grape.txt").createNewFile();
                boolean f4 = new File(rootDirectory, "mango.txt").createNewFile();

                if (!f1 || !f2 || !f3 || !f4) {
                    fail("Error creating files for renaming tests");
                }
            } catch (IOException e) {
                fail("Error creating files for renaming tests");
            }

            // Add necessary parameters
            params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
        }

        @AfterEach
        void cleanUpAfterEachRenamingTest() {

        }

        @Nested
        public class HappyPath {

            @Test
            void renameAppleToUpperCase() {
                // Setup
                // Create the instruction. This will rename apple.txt to APPLE.txt
                params.add(new Parameter("mode", "upper_case"));
                params.add(new Parameter("contains", "apple"));
                Instruction inst = buildInstructionHelper("rename_files", ":rename", params);

                // Invoke
                try {
                    instructionRunner.runRenameInstruction(inst, null);
                } catch (Exception e) {
                    fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
                }

                // Assert
                String[] files = rootDirectory.list();

                assertNotNull(files);
                assertEquals(files.length, 4);
                assertTrue(Arrays.asList(files).contains("APPLE.txt"));
            }
        }

        @Nested
        public class UnhappyPath {

            @Test
            void noPathParamThrowsExecutionException() {
                // Setup
                // Remove the path param and add the other params
                params.remove(0);
                params.add(new Parameter("mode", "upper_case"));
                params.add(new Parameter("contains", "apple"));
                Instruction inst = buildInstructionHelper("rename_files", ":rename", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runRenameInstruction(inst, null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: rename_files. Unknown targetPath or mode.");
            }

            @Test
            void noModeParamThrowsExecutionException() {
                // Setup
                // Do not add the path param
                params.add(new Parameter("contains", "apple"));
                Instruction inst = buildInstructionHelper("rename_files", ":rename", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runRenameInstruction(inst, null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: rename_files. Unknown targetPath or mode.");
            }

            @Test
            void cannotConvertPathToRealPath() {
                // Setup
                // Create the instruction.
                params.add(new Parameter("mode", "upper_case"));
                params.add(new Parameter("contains", "apple"));
                Instruction inst = buildInstructionHelper("rename_files", ":rename", params);

                // Mock failure to getting the path
                try (MockedStatic<Paths> path = mockStatic(Paths.class)) {
                    path.when(() -> Paths.get(anyString())).thenThrow(new InvalidPathException("place", "holder"));

                    // Invoke
                    instructionRunner.runRenameInstruction(inst, null);
                    fail("Error should have been thrown");
                } catch (SimpleFilesExecutionException e) {
                    // Assert
                    assertEquals(e.getMessage(), "Error encountered running instruction: rename_files. Unable to resolve path at runtime.");
                } catch (Exception e) {
                    fail("Wrong error thrown");
                }
            }

            @Test
            void failedToCreateFilePathIsNull() {
                // Setup
                // Create the instruction.
                params.add(new Parameter("mode", "upper_case"));
                params.add(new Parameter("contains", "apple"));
                Instruction inst = buildInstructionHelper("rename_files", ":rename", params);

                // Mock failure to getting the path
                try (MockedStatic<Paths> path = mockStatic(Paths.class)) {
                    path.when(() -> Paths.get(anyString())).thenReturn(null);

                    // Invoke
                    instructionRunner.runRenameInstruction(inst, null);
                    fail("Error should have been thrown");
                } catch (SimpleFilesExecutionException e) {
                    // Assert
                    assertEquals(e.getMessage(), "Error encountered running instruction: rename_files. Unable to resolve path at runtime.");
                } catch (Exception e) {
                    fail("Wrong error thrown");
                }
            }

            @Test
            void targetPathDoesNotExist() {
                // When !File.exists() is not testable with JUnit5 and Mockito5.10
            }

            @Test
            void targetPathIsNotADirectory() {
                // When !File.isDirectory() is not testable with JUnit5 and Mockito5.10
            }
        }
    }

    @Nested
    public class CreationTests {


        @BeforeEach
        void initializationBeforeCreationTests() {

        }

        @AfterEach
        void cleanUpAfterEachCreationTest() {

        }

        @Nested
        public class HappyPath {

            @Test
            void createFilesDefaultPathTest() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATOR:50}.txt"));
                params.add(new Parameter("count", "2"));

                Instruction inst = buildInstructionHelper("create_test", ":create_file", params);

                // Invoke
                try {
                    instructionRunner.runCreateInstruction(inst, "file", null);
                } catch (Exception e) {
                    fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
                }

                // Assert at default path which is current working dir
                String[] files = new File(System.getProperty("user.dir")).list();

                assertNotNull(files);
                assertTrue(Arrays.asList(files).contains("hello_50.txt"));
                assertTrue(Arrays.asList(files).contains("hello_51.txt"));

                // cleanup
                for (String s : files) {
                    if (Objects.equals(s, "hello_50.txt") || Objects.equals(s, "hello_51.txt")) {
                        (Paths.get(s)).toFile().delete();
                    }
                }
            }

            @Test
            void createFolderSpecifiedPathTest() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATOR:0:-2}"));
                params.add(new Parameter("count", "5"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);

                // Invoke
                try {
                    instructionRunner.runCreateInstruction(inst, "folder", null);
                } catch (Exception e) {
                    fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
                }

                // Assert at default path which is current working dir
                String[] files = (rootDirectory).list();

                assertNotNull(files);
                assertTrue(Arrays.asList(files).contains("hello_0"));
                assertTrue(Arrays.asList(files).contains("hello_-2"));
                assertTrue(Arrays.asList(files).contains("hello_-4"));
                assertTrue(Arrays.asList(files).contains("hello_-6"));
                assertTrue(Arrays.asList(files).contains("hello_-8"));

            }

            @Test
            void dynamicConstructTest1() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATOR:-1}.txt"));
                params.add(new Parameter("count", "5"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_file", params);
                try {
                    instructionRunner.runCreateInstruction(inst, "file", null);
                } catch (SimpleFilesExecutionException e) {
                    fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
                }

                // Invoke and assert
                String[] files = (rootDirectory).list();

                assertNotNull(files);
                assertTrue(Arrays.asList(files).contains("hello_-1.txt"));
                assertTrue(Arrays.asList(files).contains("hello_0.txt"));
                assertTrue(Arrays.asList(files).contains("hello_1.txt"));
                assertTrue(Arrays.asList(files).contains("hello_2.txt"));
                assertTrue(Arrays.asList(files).contains("hello_3.txt"));
            }

            @Test
            void dynamicConstructTest2() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATOR}.txt"));
                params.add(new Parameter("count", "3"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_file", params);
                try {
                    instructionRunner.runCreateInstruction(inst, "file", null);
                } catch (SimpleFilesExecutionException e) {
                    fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
                }

                // Invoke and assert
                String[] files = (rootDirectory).list();

                assertNotNull(files);
                assertTrue(Arrays.asList(files).contains("hello_0.txt"));
                assertTrue(Arrays.asList(files).contains("hello_1.txt"));
                assertTrue(Arrays.asList(files).contains("hello_2.txt"));

            }

        }
        @Nested
        public class UnhappyPath {

            @Test
            void createFileNoNameParamThrowsExecutionException() {
                // Setup
                Instruction inst = buildInstructionHelper("create_test_inst", ":create_file", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runCreateInstruction(inst, "file", null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: create_test_inst. Name parameter is missing");
            }

            @Test
            void createFolderNoNameParamThrowsExecutionException() {
                // Setup
                Instruction inst = buildInstructionHelper("create_folder_inst", ":create_folder", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runCreateInstruction(inst, "folder", null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: create_folder_inst. Name parameter is missing");
            }

            @Test
            void invalidFolderNameTest() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATOR:50:2}.txt"));
                params.add(new Parameter("count", "5"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runCreateInstruction(inst, "folder", null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: create_test. Found restricted symbol in specified folder name: hello_50.txt");
            }

            @Test
            void invalidDynamicConstructCase1() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATO}.txt"));
                params.add(new Parameter("count", "5"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runCreateInstruction(inst, "folder", null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: create_test. Error resolving dynamic construct");
            }

            @Test
            void invalidDynamicConstructCase2() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATOR::}.txt"));
                params.add(new Parameter("count", "5"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runCreateInstruction(inst, "folder", null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: create_test. Error resolving dynamic construct");
            }

            @Test
            void invalidDynamicConstructCase3() {
                // Setup
                params.add(new Parameter("name", "hello_${ITERATOR:-1:123a}.txt"));
                params.add(new Parameter("count", "5"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runCreateInstruction(inst, "folder", null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: create_test. Error resolving dynamic construct");
            }

            @Test
            void countAndDynamicConstructMismatchCase() {
                // Setup
                params.add(new Parameter("name", "hello.txt"));
                params.add(new Parameter("count", "5"));
                params.add(new Parameter("path", rootDirectory.getAbsolutePath()));

                Instruction inst = buildInstructionHelper("create_test", ":create_file", params);

                // Invoke and assert
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runCreateInstruction(inst, "file", null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: create_test. Count is more than 1. Missing dynamic construct for name");
            }


        }


    }

    @Nested
    public class GroupTests {

        @BeforeEach
        void setup() {
            params.clear();
        }

        @Nested
        public class HappyPathTests {
            @Test
            void groupSimpleFilesMoveNoDepth() throws Exception {
                // Setup 4 basic files. No folders. Assume correctly made.
                new File(rootDirectory, "fruit_apple.txt").createNewFile();
                new File(rootDirectory, "fruit_orange.txt").createNewFile();
                new File(rootDirectory, "fruit_mango.txt").createNewFile();
                new File(rootDirectory, "vegetable_lettuce.txt").createNewFile();

                // Setup group instruction. Moves all fruits to the other directory.
                params.add(new Parameter("group_target", rootDirectory.getAbsolutePath()));
                params.add(new Parameter("path", otherDirectory.getAbsolutePath()));
                params.add(new Parameter("mode", "move"));
                params.add(new Parameter("contains", "fruit"));
                Instruction instruction = buildInstructionHelper("group_move", ":group", params);

                // Invoke
                instructionRunner.runGroupAction(instruction, null);

                // Assert correct files in the other directory
                String[] filesInOtherDirectory = otherDirectory.list();
                assertNotNull(filesInOtherDirectory);
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_orange.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_mango.txt"));
                assertEquals(filesInOtherDirectory.length, 3);

                // Assert correct files in the root directory
                String[] filesInRootDirectory = rootDirectory.list();
                assertNotNull(filesInRootDirectory);
                assertTrue(Arrays.asList(filesInRootDirectory).contains("vegetable_lettuce.txt"));
                assertEquals(filesInRootDirectory.length, 1);

                // Assert the result of the group is correctly stored in memory
                assertEquals(otherDirectory.getAbsolutePath(), memory.getBasePath("group_move"));

                String apple = new File(otherDirectory, "fruit_apple.txt").getAbsolutePath();
                String orange = new File(otherDirectory, "fruit_orange.txt").getAbsolutePath();
                String mango = new File(otherDirectory, "fruit_mango.txt").getAbsolutePath();
                String[] newPaths = {apple, orange, mango};

                ArrayList<File> groupedFiles = memory.getGroupedFiles("group_move");
                assertEquals(groupedFiles.size(), 3);
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(0).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(1).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(2).getAbsolutePath()));
            }

            @Test
            void groupSimpleFilesCopyNoDepth() throws Exception {
                // Setup 4 basic files. No folders. Assume correctly made.
                new File(rootDirectory, "fruit_apple.txt").createNewFile();
                new File(rootDirectory, "fruit_orange.txt").createNewFile();
                new File(rootDirectory, "fruit_mango.txt").createNewFile();
                new File(rootDirectory, "vegetable_lettuce.txt").createNewFile();

                // Setup group instruction. Moves all fruits to the other directory.
                params.add(new Parameter("group_target", rootDirectory.getAbsolutePath()));
                params.add(new Parameter("path", otherDirectory.getAbsolutePath()));
                params.add(new Parameter("mode", "copy"));
                params.add(new Parameter("contains", "fruit"));
                Instruction instruction = buildInstructionHelper("group_copy", ":group", params);

                // Invoke
                instructionRunner.runGroupAction(instruction, null);

                // Assert correct files in the other directory
                String[] filesInOtherDirectory = otherDirectory.list();
                assertNotNull(filesInOtherDirectory);
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_orange.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_mango.txt"));
                assertEquals(filesInOtherDirectory.length, 3);

                // Assert correct files in the root directory
                String[] filesInRootDirectory = rootDirectory.list();
                assertNotNull(filesInRootDirectory);
                assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_orange.txt"));
                assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_mango.txt"));
                assertTrue(Arrays.asList(filesInRootDirectory).contains("vegetable_lettuce.txt"));
                assertEquals(filesInRootDirectory.length, 4);

                // Assert the result of the group is correctly stored in memory
                assertEquals(otherDirectory.getAbsolutePath(), memory.getBasePath("group_copy"));

                String apple = new File(otherDirectory, "fruit_apple.txt").getAbsolutePath();
                String orange = new File(otherDirectory, "fruit_orange.txt").getAbsolutePath();
                String mango = new File(otherDirectory, "fruit_mango.txt").getAbsolutePath();
                String[] newPaths = {apple, orange, mango};

                ArrayList<File> groupedFiles = memory.getGroupedFiles("group_copy");
                assertEquals(groupedFiles.size(), 3);
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(0).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(1).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(2).getAbsolutePath()));
            }

            @Test
            void groupSimpleFilesMoveWithDepth() throws Exception {
                // Setup some basic nested files. Assume correctly made.
                Files.createDirectories(Paths.get(rootDirectory.getAbsolutePath(), "f1"));
                Files.createDirectories(Paths.get(rootDirectory.getAbsolutePath(), "f2", "f3"));
                new File(rootDirectory, "fruit_apple.txt").createNewFile();
                new File(rootDirectory, "fruit_orange.txt").createNewFile();
                new File(rootDirectory, "fruit_mango.txt").createNewFile();
                new File(rootDirectory, "vegetable_lettuce.txt").createNewFile();
                new File(rootDirectory, "f1/fruit_apple.txt").createNewFile();
                new File(rootDirectory, "f1/fruit_orange.txt").createNewFile();
                new File(rootDirectory, "f1/vegetable_eggplant.txt").createNewFile();
                new File(rootDirectory, "f2/f3/fruit_mango.txt").createNewFile();

                // Setup group instruction. Moves all fruits to the other directory.
                params.add(new Parameter("group_target", rootDirectory.getAbsolutePath()));
                params.add(new Parameter("path", otherDirectory.getAbsolutePath()));
                params.add(new Parameter("mode", "move"));
                params.add(new Parameter("contains", "fruit"));
                Instruction instruction = buildInstructionHelper("group_move", ":group", params);

                // Invoke
                instructionRunner.runGroupAction(instruction, null);

                // Assert correct files in the other directory
                String[] filesInOtherDirectory = otherDirectory.list();
                assertNotNull(filesInOtherDirectory);
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_orange.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_mango.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("f1"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("f2"));
                assertEquals(filesInOtherDirectory.length, 5);

                String[] filesInOtherDirectoryF1 = new File(otherDirectory, "f1").list();
                assertNotNull(filesInOtherDirectoryF1);
                assertTrue(Arrays.asList(filesInOtherDirectoryF1).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectoryF1).contains("fruit_orange.txt"));
                assertEquals(filesInOtherDirectoryF1.length, 2);

                String[] filesInOtherDirectoryF2 = new File(otherDirectory, "f2").list();
                assertNotNull(filesInOtherDirectoryF2);
                assertTrue(Arrays.asList(filesInOtherDirectoryF2).contains("f3"));
                assertEquals(filesInOtherDirectoryF2.length, 1);

                String[] filesInOtherDirectoryF3 = new File(otherDirectory, "f2/f3").list();
                assertNotNull(filesInOtherDirectoryF3);
                assertTrue(Arrays.asList(filesInOtherDirectoryF3).contains("fruit_mango.txt"));
                assertEquals(filesInOtherDirectoryF3.length, 1);

                // Assert correct files in the root directory
                String[] filesInRootDirectory = rootDirectory.list();
                assertNotNull(filesInRootDirectory);
                assertTrue(Arrays.asList(filesInRootDirectory).contains("vegetable_lettuce.txt"));
                assertEquals(filesInRootDirectory.length, 3);

                String[] filesInRootDirectoryF1 = new File(rootDirectory, "f1").list();
                assertNotNull(filesInRootDirectoryF1);
                assertEquals(filesInRootDirectoryF1.length, 1);

                String[] filesInRootDirectoryF2 = new File(rootDirectory, "f2").list();
                assertNotNull(filesInRootDirectoryF2);
                assertEquals(filesInRootDirectoryF2.length, 1);

                String[] filesInRootDirectoryF3 = new File(rootDirectory, "f2/f3").list();
                assertNotNull(filesInRootDirectoryF3);
                assertEquals(filesInRootDirectoryF3.length, 0);

                // Assert the result of the group is correctly stored in memory
                assertEquals(otherDirectory.getAbsolutePath(), memory.getBasePath("group_move"));

                String apple = new File(otherDirectory, "fruit_apple.txt").getAbsolutePath();
                String orange = new File(otherDirectory, "fruit_orange.txt").getAbsolutePath();
                String mango = new File(otherDirectory, "fruit_mango.txt").getAbsolutePath();
                String apple2 = new File(otherDirectory, "f1/fruit_apple.txt").getAbsolutePath();
                String orange2 = new File(otherDirectory, "f1/fruit_orange.txt").getAbsolutePath();
                String mango2 = new File(otherDirectory, "f2/f3/fruit_mango.txt").getAbsolutePath();
                String[] newPaths = {apple, orange, mango, apple2, orange2, mango2};

                ArrayList<File> groupedFiles = memory.getGroupedFiles("group_move");
                assertEquals(groupedFiles.size(), 6);
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(0).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(1).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(2).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(3).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(4).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(5).getAbsolutePath()));
            }

            @Test
            void groupSimpleFilesCopyWithDepth() throws Exception {
                // Setup some basic nested files. Assume correctly made.
                Files.createDirectories(Paths.get(rootDirectory.getAbsolutePath(), "f1"));
                Files.createDirectories(Paths.get(rootDirectory.getAbsolutePath(), "f2", "f3"));
                new File(rootDirectory, "fruit_apple.txt").createNewFile();
                new File(rootDirectory, "fruit_orange.txt").createNewFile();
                new File(rootDirectory, "fruit_mango.txt").createNewFile();
                new File(rootDirectory, "vegetable_lettuce.txt").createNewFile();
                new File(rootDirectory, "f1/fruit_apple.txt").createNewFile();
                new File(rootDirectory, "f1/fruit_orange.txt").createNewFile();
                new File(rootDirectory, "f1/vegetable_eggplant.txt").createNewFile();
                new File(rootDirectory, "f2/f3/fruit_mango.txt").createNewFile();

                // Setup group instruction. Moves all fruits to the other directory.
                params.add(new Parameter("group_target", rootDirectory.getAbsolutePath()));
                params.add(new Parameter("path", otherDirectory.getAbsolutePath()));
                params.add(new Parameter("mode", "copy"));
                params.add(new Parameter("contains", "fruit"));
                Instruction instruction = buildInstructionHelper("group_copy", ":group", params);

                // Invoke
                instructionRunner.runGroupAction(instruction, null);

                // Assert correct files in the other directory
                String[] filesInOtherDirectory = otherDirectory.list();
                assertNotNull(filesInOtherDirectory);
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_orange.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_mango.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("f1"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("f2"));
                assertEquals(filesInOtherDirectory.length, 5);

                String[] filesInOtherDirectoryF1 = new File(otherDirectory, "f1").list();
                assertNotNull(filesInOtherDirectoryF1);
                assertTrue(Arrays.asList(filesInOtherDirectoryF1).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectoryF1).contains("fruit_orange.txt"));
                assertEquals(filesInOtherDirectoryF1.length, 2);

                String[] filesInOtherDirectoryF2 = new File(otherDirectory, "f2").list();
                assertNotNull(filesInOtherDirectoryF2);
                assertTrue(Arrays.asList(filesInOtherDirectoryF2).contains("f3"));
                assertEquals(filesInOtherDirectoryF2.length, 1);

                String[] filesInOtherDirectoryF3 = new File(otherDirectory, "f2/f3").list();
                assertNotNull(filesInOtherDirectoryF3);
                assertTrue(Arrays.asList(filesInOtherDirectoryF3).contains("fruit_mango.txt"));
                assertEquals(filesInOtherDirectoryF3.length, 1);

                // Assert correct files in the root directory. Just check root.
                String[] filesInRootDirectory = rootDirectory.list();
                assertNotNull(filesInRootDirectory);
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_apple.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_orange.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("fruit_mango.txt"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("f1"));
                assertTrue(Arrays.asList(filesInOtherDirectory).contains("f2"));
                assertTrue(Arrays.asList(filesInRootDirectory).contains("vegetable_lettuce.txt"));
                assertEquals(filesInRootDirectory.length, 6);

                // Assert the result of the group is correctly stored in memory
                assertEquals(otherDirectory.getAbsolutePath(), memory.getBasePath("group_copy"));

                String apple = new File(otherDirectory, "fruit_apple.txt").getAbsolutePath();
                String orange = new File(otherDirectory, "fruit_orange.txt").getAbsolutePath();
                String mango = new File(otherDirectory, "fruit_mango.txt").getAbsolutePath();
                String apple2 = new File(otherDirectory, "f1/fruit_apple.txt").getAbsolutePath();
                String orange2 = new File(otherDirectory, "f1/fruit_orange.txt").getAbsolutePath();
                String mango2 = new File(otherDirectory, "f2/f3/fruit_mango.txt").getAbsolutePath();
                String[] newPaths = {apple, orange, mango, apple2, orange2, mango2};

                ArrayList<File> groupedFiles = memory.getGroupedFiles("group_copy");
                assertEquals(groupedFiles.size(), 6);
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(0).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(1).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(2).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(3).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(4).getAbsolutePath()));
                assertTrue(Arrays.asList(newPaths).contains(groupedFiles.get(5).getAbsolutePath()));
            }
        }

        @Nested
        public class UnhappyPathTests {
            @Test
            void noGroupTargetSpecified() {
                // Setup group instruction.
                params.add(new Parameter("path", otherDirectory.getAbsolutePath()));
                params.add(new Parameter("mode", "copy"));
                params.add(new Parameter("contains", "fruit"));
                Instruction instruction = buildInstructionHelper("group_copy", ":group", params);

                // Invoke
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runGroupAction(instruction, null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: group_copy. Group target not specified.");

                // Check that memory is unchanged
                assertFalse(memory.hasGroupedFilesFromInstruction("group_copy"));
            }

            @Test
            void noPathSpecified() {
                // Setup group instruction.
                params.add(new Parameter("group_target", rootDirectory.getAbsolutePath()));
                params.add(new Parameter("mode", "copy"));
                params.add(new Parameter("contains", "fruit"));
                Instruction instruction = buildInstructionHelper("group_copy", ":group", params);

                // Invoke
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runGroupAction(instruction, null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: group_copy. Path not specified.");

                // Check that memory is unchanged
                assertFalse(memory.hasGroupedFilesFromInstruction("group_copy"));
            }

            @Test
            void noModeSpecified() {
                // Setup group instruction.
                params.add(new Parameter("group_target", rootDirectory.getAbsolutePath()));
                params.add(new Parameter("path", otherDirectory.getAbsolutePath()));
                params.add(new Parameter("contains", "fruit"));
                Instruction instruction = buildInstructionHelper("group_copy", ":group", params);

                // Invoke
                Exception exception = assertThrows(SimpleFilesExecutionException.class, () -> instructionRunner.runGroupAction(instruction, null));
                assertEquals(exception.getMessage(), "Error encountered running instruction: group_copy. Mode not specified.");

                // Check that memory is unchanged
                assertFalse(memory.hasGroupedFilesFromInstruction("group_copy"));
            }
        }

    }

    @Nested
    public class ChainingTests {

        ArrayList<Parameter> params = new ArrayList<>();
        ArrayList<Parameter> secondParams = new ArrayList<>();
        ArrayList<Parameter> thirdParams = new ArrayList<>();

        @BeforeEach
        void setup() {
           params.clear();
           secondParams.clear();
           thirdParams.clear();
        }
    @Test
    void CopyTest(){
        params.add(new Parameter("name", "hello_${ITERATOR:50}"));
        params.add(new Parameter("count", "2"));
        params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
        Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);
        // Invoke
        try {
            instructionRunner.runCreateInstruction(inst, "folder", null);
        } catch (Exception e) {
            fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
        }
        String[] filesInRootDirectory = rootDirectory.list();
        assertNotNull(filesInRootDirectory);
        assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_50"));
        assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_51"));
        assertEquals(filesInRootDirectory.length, 2);

        secondParams.add(new Parameter("group_target", "create_test"));
        secondParams.add(new Parameter("path", otherDirectory.getAbsolutePath()));
        secondParams.add(new Parameter("mode", "copy"));
        Instruction instruction = buildInstructionHelper("group_copy", ":group", secondParams);

        // Invoke
        try {
            instructionRunner.runGroupAction(instruction, null);
        } catch (Exception e) {
            fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
        }
        String[] filesInRootDirectoryAfterCopy = rootDirectory.list();
        String[] filesInRootDirectory1 = otherDirectory.list();
        assertNotNull(filesInRootDirectory1);
        assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_50"));
        assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_51"));
        assertEquals(filesInRootDirectory1.length, 2);
        assertEquals(filesInRootDirectoryAfterCopy.length, 2);
    }


        @Test
        void MoveTest(){
            params.add(new Parameter("name", "hello_${ITERATOR:50}.txt"));
            params.add(new Parameter("count", "3"));
            params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
            Instruction inst = buildInstructionHelper("create_test", ":create_file", params);
            // Invoke
            try {
                instructionRunner.runCreateInstruction(inst, "file", null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectory = rootDirectory.list();
            assertNotNull(filesInRootDirectory);
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_50.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_51.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52.txt"));
            assertEquals(filesInRootDirectory.length, 3);

            secondParams.add(new Parameter("group_target", "create_test"));
            secondParams.add(new Parameter("path", otherDirectory.getAbsolutePath()));
            secondParams.add(new Parameter("mode", "move"));
            Instruction instruction = buildInstructionHelper("group_move", ":group", secondParams);

            // Invoke
            try {
                instructionRunner.runGroupAction(instruction, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectoryAfterMove = rootDirectory.list();
            String[] filesInRootDirectory1 = otherDirectory.list();
            assertNotNull(filesInRootDirectory1);
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_50.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_51.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52.txt"));
            assertEquals(filesInRootDirectory1.length, 3);
            assertEquals(filesInRootDirectoryAfterMove.length, 0);
        }

        @Test
        void MultiLevelCopyTest(){
            params.add(new Parameter("name", "hello_${ITERATOR:50}.txt"));
            params.add(new Parameter("count", "3"));
            params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
            Instruction inst = buildInstructionHelper("create_test", ":create_file", params);
            // Invoke
            try {
                instructionRunner.runCreateInstruction(inst, "file", null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectory = rootDirectory.list();
            assertNotNull(filesInRootDirectory);
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_50.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_51.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52.txt"));
            assertEquals(filesInRootDirectory.length, 3);

            secondParams.add(new Parameter("group_target", "create_test"));
            secondParams.add(new Parameter("path", otherDirectory.getAbsolutePath()));
            secondParams.add(new Parameter("mode", "copy"));
            Instruction instruction = buildInstructionHelper("group_copy1", ":group", secondParams);

            // Invoke
            try {
                instructionRunner.runGroupAction(instruction, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectoryAfterCopy = rootDirectory.list();
            String[] filesInRootDirectory1 = otherDirectory.list();
            assertNotNull(filesInRootDirectory1);
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_50.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_51.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52.txt"));
            assertEquals(filesInRootDirectory1.length, 3);
            assertEquals(filesInRootDirectoryAfterCopy.length, 3);


            thirdParams.add(new Parameter("group_target", "group_copy1"));
            thirdParams.add(new Parameter("path", otherDirectory1.getAbsolutePath()));
            thirdParams.add(new Parameter("mode", "copy"));
            Instruction instruction2 = buildInstructionHelper("group_copy2", ":group", thirdParams);

            // Invoke
            try {
                instructionRunner.runGroupAction(instruction2, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectory4 = rootDirectory.list();
            String[] filesInRootDirectory5 = otherDirectory.list();
            String[] filesInRootDirectory6 = otherDirectory1.list();
            assertNotNull(filesInRootDirectory4);
            assertNotNull(filesInRootDirectory5);
            assertNotNull(filesInRootDirectory6);
            assertTrue(Arrays.asList(filesInRootDirectory6).contains("hello_50.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory6).contains("hello_51.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory6).contains("hello_52.txt"));

            assertEquals(filesInRootDirectory4.length, 3);
            assertEquals(filesInRootDirectory5.length, 3);
            assertEquals(filesInRootDirectory6.length, 3);
        }

        @Test
        void MultiLevelMoveTest(){
            params.add(new Parameter("name", "hello_${ITERATOR:50}"));
            params.add(new Parameter("count", "3"));
            params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
            Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);
            // Invoke
            try {
                instructionRunner.runCreateInstruction(inst, "folder", null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectory = rootDirectory.list();
            assertNotNull(filesInRootDirectory);
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52"));
            assertEquals(filesInRootDirectory.length, 3);

            secondParams.add(new Parameter("group_target", "create_test"));
            secondParams.add(new Parameter("path", otherDirectory.getAbsolutePath()));
            secondParams.add(new Parameter("mode", "move"));
            Instruction instruction = buildInstructionHelper("group_move1", ":group", secondParams);

            // Invoke
            try {
                instructionRunner.runGroupAction(instruction, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectoryAfterMove = rootDirectory.list();
            String[] filesInRootDirectory1 = otherDirectory.list();
            assertNotNull(filesInRootDirectory1);
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52"));
            assertEquals(filesInRootDirectory1.length, 3);
            assertEquals(filesInRootDirectoryAfterMove.length, 0);


            thirdParams.add(new Parameter("group_target", "group_move1"));
            thirdParams.add(new Parameter("path", rootDirectory.getAbsolutePath()));
            thirdParams.add(new Parameter("mode", "move"));
            Instruction instruction2 = buildInstructionHelper("group_move2", ":group", thirdParams);

            // Invoke
            try {
                instructionRunner.runGroupAction(instruction2, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectoryAfterMove2 = rootDirectory.list();
            String[] filesInRootDirectory2 = otherDirectory.list();
            assertNotNull(filesInRootDirectoryAfterMove2);
            assertTrue(Arrays.asList(filesInRootDirectoryAfterMove2).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterMove2).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterMove2).contains("hello_52"));
            assertEquals(filesInRootDirectory2.length, 0);
            assertEquals(filesInRootDirectoryAfterMove2.length, 3);

        }

        @Test
        void ChainingOnlyOnPreviousInstructionTest() throws Exception {
            new File(rootDirectory, "fruit_apple.txt").createNewFile();
            new File(rootDirectory, "fruit_orange.txt").createNewFile();
            new File(rootDirectory, "fruit_mango.txt").createNewFile();
            params.add(new Parameter("name", "hello_${ITERATOR:50}"));
            params.add(new Parameter("count", "3"));
            params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
            Instruction inst = buildInstructionHelper("create_test", ":create_file", params);
            // Invoke
            try {
                instructionRunner.runCreateInstruction(inst, "file", null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectory = rootDirectory.list();
            assertNotNull(filesInRootDirectory);
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_apple.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_orange.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_mango.txt"));
            assertEquals(filesInRootDirectory.length, 6);

            secondParams.add(new Parameter("group_target", "create_test"));
            secondParams.add(new Parameter("path", otherDirectory.getAbsolutePath()));
            secondParams.add(new Parameter("mode", "copy"));
            Instruction instruction = buildInstructionHelper("group_copy", ":group", secondParams);

            // Invoke
            try {
                instructionRunner.runGroupAction(instruction, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectoryAfterCopy = rootDirectory.list();
            String[] filesInRootDirectory1 = otherDirectory.list();
            assertNotNull(filesInRootDirectory1);
            assertNotNull(filesInRootDirectoryAfterCopy);
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterCopy).contains("hello_52"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterCopy).contains("fruit_apple.txt"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterCopy).contains("fruit_orange.txt"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterCopy).contains("fruit_mango.txt"));
            assertEquals(filesInRootDirectory1.length, 3);
            assertEquals(filesInRootDirectoryAfterCopy.length, 6);


        }


        @Test
        void ChainingOnlyOnPreviousInstructionTest1() throws Exception {
            new File(rootDirectory, "fruit_apple.txt").createNewFile();
            new File(rootDirectory, "fruit_orange.txt").createNewFile();
            new File(rootDirectory, "fruit_mango.txt").createNewFile();
            params.add(new Parameter("name", "hello_${ITERATOR:50}"));
            params.add(new Parameter("count", "3"));
            params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
            Instruction inst = buildInstructionHelper("create_test", ":create_folder", params);
            // Invoke
            try {
                instructionRunner.runCreateInstruction(inst, "folder", null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectory = rootDirectory.list();
            assertNotNull(filesInRootDirectory);
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_apple.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_orange.txt"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("fruit_mango.txt"));
            assertEquals(filesInRootDirectory.length, 6);

            secondParams.add(new Parameter("group_target", "create_test"));
            secondParams.add(new Parameter("path", otherDirectory.getAbsolutePath()));
            secondParams.add(new Parameter("mode", "move"));
            Instruction instruction = buildInstructionHelper("group_move", ":group", secondParams);

            // Invoke
            try {
                instructionRunner.runGroupAction(instruction, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectoryAfterMove = rootDirectory.list();
            String[] filesInRootDirectory1 = otherDirectory.list();
            assertNotNull(filesInRootDirectory1);
            assertNotNull(filesInRootDirectoryAfterMove);
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectory1).contains("hello_52"));

            assertFalse(Arrays.asList(filesInRootDirectoryAfterMove).contains("hello_52"));
            assertFalse(Arrays.asList(filesInRootDirectoryAfterMove).contains("hello_51"));
            assertFalse(Arrays.asList(filesInRootDirectoryAfterMove).contains("hello_50"));

            assertTrue(Arrays.asList(filesInRootDirectoryAfterMove).contains("fruit_apple.txt"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterMove).contains("fruit_orange.txt"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterMove).contains("fruit_mango.txt"));
            assertEquals(filesInRootDirectory1.length, 3);
            assertEquals(filesInRootDirectoryAfterMove.length, 3);


        }
        @Test
        void RenameTest(){
            params.add(new Parameter("name", "hello_${ITERATOR:50}"));
            params.add(new Parameter("count", "3"));
            params.add(new Parameter("path", rootDirectory.getAbsolutePath()));
            Instruction inst = buildInstructionHelper("create_test", ":create_file", params);
            // Invoke
            try {
                instructionRunner.runCreateInstruction(inst, "file", null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }
            String[] filesInRootDirectory = rootDirectory.list();
            assertNotNull(filesInRootDirectory);
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_50"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectory).contains("hello_52"));
            assertEquals(filesInRootDirectory.length, 3);

            secondParams.add(new Parameter("mode", "upper_case"));
            secondParams.add(new Parameter("contains", "hello_50"));
            secondParams.add(new Parameter("path", "create_test"));
            Instruction instruction = buildInstructionHelper("rename_files", ":rename", secondParams);

            // Invoke
            try {
                instructionRunner.runRenameInstruction(instruction, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }

            String[] filesInRootDirectoryAfterRename = rootDirectory.list();
            assertNotNull(filesInRootDirectoryAfterRename);
            assertTrue(Arrays.asList(filesInRootDirectoryAfterRename).contains("HELLO_50"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterRename).contains("hello_51"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterRename).contains("hello_52"));
            assertEquals(filesInRootDirectoryAfterRename.length, 3);

            thirdParams.add(new Parameter("mode", "upper_case"));
            thirdParams.add(new Parameter("contains", "hello_51"));
            thirdParams.add(new Parameter("path", "create_test"));
            Instruction instruction2 = buildInstructionHelper("rename_file2", ":rename", thirdParams);

            // Invoke
            try {
                instructionRunner.runRenameInstruction(instruction2, null);
            } catch (Exception e) {
                fail(String.format("Unexpected exception thrown: %s", e.getMessage()));
            }

            String[] filesInRootDirectoryAfterRename2 = rootDirectory.list();
            assertNotNull(filesInRootDirectoryAfterRename2);
            assertTrue(Arrays.asList(filesInRootDirectoryAfterRename2).contains("HELLO_50"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterRename2).contains("HELLO_51"));
            assertTrue(Arrays.asList(filesInRootDirectoryAfterRename2).contains("hello_52"));
            assertEquals(filesInRootDirectoryAfterRename.length, 3);
            for (String s: filesInRootDirectoryAfterRename2){
                System.out.println(s);
            }
        }
    }



    Instruction buildInstructionHelper(String name, String action, ArrayList<Parameter> listOfParams) {
        Instruction result = new Instruction(name);
        Action theAction = new Action(action);

        // Set fields of the instruction
        result.setAction(theAction);
        for (Parameter p: listOfParams) {
            result.addParameter(p);
        }

        return result;
    }
}
