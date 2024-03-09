package evaluator;

import errors.CircularAssignmentException;
import errors.MemoryAssignmentException;
import errors.UnknownVariableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static evaluator.Memory.isVariableReference;
import static org.junit.jupiter.api.Assertions.*;

public class MemoryTest {
    Memory memory = Memory.getInstance();
    @BeforeEach
    void initializationBeforeMemoryTest() {
        memory.clear();
    }

    @Nested
    public class MemoryTests {

        @Nested
        public class HappyPath {
            @Test
            void assignTwoNewVariables() {
                // Invoke
                memory.assignVariable("first", "first_value");
                memory.assignVariable("second", "second_value");

                // Assert
                String firstValue = memory.getVariableValue("first");
                String secondValue = memory.getVariableValue("second");

                assertEquals(firstValue, "first_value");
                assertEquals(secondValue, "second_value");
            }

            @Test
            void assignTwoNewVariablesWithSameValueAndChangeOneOfThem() {
                // Invoke
                memory.assignVariable("first", "test");
                memory.assignVariable("second", "test");

                // Assert
                String firstValue = memory.getVariableValue("first");
                String secondValue = memory.getVariableValue("second");

                assertEquals(firstValue, "test");
                assertEquals(secondValue, "test");

                // Invoke. Change the second value to something else
                try {
                    memory.assignVariable("first", "test");
                    memory.assignVariable("second", "something else");
                } catch (Exception e) {
                    fail(String.format("Failed to assign variables with error: %s", e.getMessage()));
                }

                // Assert
                firstValue = memory.getVariableValue("first");
                secondValue = memory.getVariableValue("second");

                assertEquals(firstValue, "test");
                assertEquals(secondValue, "something else");
            }

            @Test
            void assignVariableReference() {
                // Invoke
                memory.assignVariable("first", "test");
                memory.assignVariable("second", "${first}");

                // Assert
                String firstValue = memory.getVariableValue("first");
                String secondValue = memory.getVariableValue("second");

                assertEquals(firstValue, "test");
                assertEquals(secondValue, "test");
            }

            @Test
            void assignVariableReferenceThenChangeValue() {
                // Invoke
                memory.assignVariable("first", "test");
                memory.assignVariable("second", "${first}");

                // Assert
                String firstValue = memory.getVariableValue("first");
                String secondValue = memory.getVariableValue("second");

                assertEquals(firstValue, "test");
                assertEquals(secondValue, "test");

                // Invoke. Assign the variable a different value
                memory.assignVariable("second", "different value");

                // Assert
                firstValue = memory.getVariableValue("first");
                secondValue = memory.getVariableValue("second");

                assertEquals(firstValue, "different value");
                assertEquals(secondValue, "different value");
            }

            @Test
            void storeOneGroupedResult() {
                // Setup
                ArrayList<File> files = new ArrayList<>();
                files.add(new File("here.txt"));
                files.add(new File("there.txt"));

                // Invoke
                memory.storeGroupResult("inst_name", files);

                // Assert
                ArrayList<File> filesFromMemory = memory.getGroupedFiles("inst_name");
                assertEquals(filesFromMemory.size(), 2);
                assertEquals(filesFromMemory.get(0).getName(), "here.txt");
                assertEquals(filesFromMemory.get(1).getName(), "there.txt");
            }

            @Test
            void someValidVariableReferences() {
                // This test is to check the static method isVariableReference
                assertTrue(isVariableReference("${}"));
                assertTrue(isVariableReference("${{}"));
                assertTrue(isVariableReference("${}}}$$&}"));
                assertTrue(isVariableReference("${test}"));
                assertTrue(isVariableReference("${test_test}"));
                assertTrue(isVariableReference("${/test/path/computer}"));
                assertTrue(isVariableReference("${/test/path/with a lot of spaces}"));
                assertTrue(isVariableReference("${/test/files/with.txt}"));
            }
        }

        @Nested
        public class UnhappyPath {
            @Test
            void getVariableThatIsNoStored() {
                // Invoke and assert
                Exception exception = assertThrows(UnknownVariableException.class, () -> memory.getVariableValue("unknown"));
                assertEquals(exception.getMessage(), "Unknown variable: unknown");
            }

            @Test
            void assignToUnknownVariable() {
                // Invoke and assert
                Exception exception = assertThrows(MemoryAssignmentException.class, () -> memory.assignVariable("unknown", "${asdf}"));
                assertEquals(exception.getMessage(), "Cannot assign a variable to another that does not exist: ${asdf}");
            }

            @Test
            void assignCircularReference() {
                // Setup
                memory.assignVariable("first", "test");

                // Invoke and assert
                Exception exception = assertThrows(CircularAssignmentException.class, () -> memory.assignVariable("first", "${first}"));
                assertEquals(exception.getMessage(), "Cannot assign a variable to reference itself: ${first}");
            }

            @Test
            void assignITERATORAsVariableName() {
                // Invoke and assert
                Exception exception = assertThrows(MemoryAssignmentException.class, () -> memory.assignVariable("ITERATOR", "asdf"));
                assertEquals(exception.getMessage(), "Variable name ITERATOR is a reserved name. Choose a different variable name!");
            }

            @Test
            void someInvalidVariableReferences() {
                // This test is to check the static method isVariableReference
                assertFalse(isVariableReference("${"));
                assertFalse(isVariableReference("$}"));
                assertFalse(isVariableReference("{}"));
            }
        }
    }
}
