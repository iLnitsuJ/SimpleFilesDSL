package evaluator;

import ast.Variable;
import errors.CircularAssignmentException;
import errors.MemoryAssignmentException;
import errors.UnknownVariableException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Class to manage memory in the SimpleFiles language.
 * Lazy initializing singleton.
 */
public class Memory {
    // Hashmap for variables
    private final HashMap<String, Integer> varToIdx = new HashMap<>();
    private final HashMap<Integer, String> idxToVal = new HashMap<>();

    // Index of next available memory (implementation is just an int counter)
    private int availableMemoryIdx = 0;

    // Hashmap for group instruction
    HashMap<String, ArrayList<File>> fileGroups = new HashMap<>();
    HashMap<String, String> basePaths = new HashMap<>();

    private Memory() {}

    private static class SingletonHelper {
        private static final Memory INSTANCE = new Memory();
    }

    public static Memory getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public void clear() {
        clearVariableMemory();
        clearFileGroupings();
    }

    public void clearVariableMemory() {
        varToIdx.clear();
        idxToVal.clear();
        availableMemoryIdx = 0;
    }

    public void clearFileGroupings() {
        fileGroups.clear();
        basePaths.clear();
    }

    public void printMemory() {
        StringBuilder varToIdxString= new StringBuilder();
        for (Map.Entry<String, Integer> varToIdxItem : varToIdx.entrySet()) {
            varToIdxString.append(varToIdxItem.getKey());
            varToIdxString.append("\t").append(varToIdxItem.getValue()).append("\n");
        }
        System.out.println("Variable to Index Mapping");
        System.out.println(varToIdxString);

        StringBuilder idxToValString= new StringBuilder();
        for (Map.Entry<Integer, String> idxToValItem : idxToVal.entrySet()) {
            idxToValString.append(idxToValItem.getKey());
            idxToValString.append("\t").append(idxToValItem.getValue()).append("\n");
        }
        System.out.println("Index to Value Mapping");
        System.out.println(idxToValString);
    }

    public void assignVariable(Variable v) {
        assignVariable(v.getKey(), v.getValue());
    }

    public void assignVariable(String newKey, String newValue) {
        newValue = newValue.trim();
        if (isVariableReference(newValue)) {
            String valueAsVariable = unwrapVariable(newValue);
            if (!varToIdx.containsKey(valueAsVariable)) {
                String msg = String.format("Cannot assign a variable to another that does not exist: %s", newValue);
                throw new MemoryAssignmentException(msg);
            }
            if (valueAsVariable.equals(newKey)) {
                String msg = String.format("Cannot assign a variable to reference itself: %s", newValue);
                throw new CircularAssignmentException(msg);
            }
            varToIdx.put(newKey.trim(), varToIdx.get(valueAsVariable));
        } else {
            if (newKey.equals("ITERATOR")) {
                throw new MemoryAssignmentException("Variable name ITERATOR is a reserved name. Choose a different variable name!");
            }
            if (varToIdx.containsKey(newKey)) {
                idxToVal.put(varToIdx.get(newKey), newValue);
            } else {
                varToIdx.put(newKey, availableMemoryIdx);
                idxToVal.put(availableMemoryIdx, newValue);
                availableMemoryIdx++;
            }
        }
    }

    public String unwrapAndGetVariableValue(String wrappedVariable) {
        return this.getVariableValue(this.unwrapVariable(wrappedVariable));
    }

    public String getVariableValue(String variable) {
        if (varToIdx.containsKey(variable)) {
            return idxToVal.get(varToIdx.get(variable));
        }
        throw new UnknownVariableException(String.format("Unknown variable: %s", variable));
    }

    public void storeGroupResult(String instName, ArrayList<File> files) {
        fileGroups.put(instName, files);
    }

    public void storeBasePath(String instName, String basePath) {
        basePaths.put(instName, basePath);
    }

    public ArrayList<File> getGroupedFiles(String instName) throws UnknownVariableException {
        if (fileGroups.containsKey(instName)) {
            return fileGroups.get(instName);
        }
        throw new UnknownVariableException(String.format("File grouping for instruction %s not found", instName));
    }

    public String getBasePath(String instName) throws UnknownVariableException {
        if (basePaths.containsKey(instName)) {
            return basePaths.get(instName);
        }
        throw new UnknownVariableException(String.format("The base path for instruction %s not found", instName));
    }

    public boolean hasGroupedFilesFromInstruction(String instName) {
        return fileGroups.containsKey(instName) && basePaths.containsKey(instName);
    }

    public static boolean isVariableReference(String var) {
        if (var == null) {
            return false;
        }

        String regex = "\\$\\{.*}"; // This matches the pattern ${ ... }
        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(var).matches();
    }

    private String unwrapVariable(String var) throws UnknownVariableException {
        String unwrapped = var.substring(2, var.length()-1);
        if (!unwrapped.isEmpty()) {
            return unwrapped;
        }
        throw new UnknownVariableException("The referenced variable cannot be empty string.");
    }
}
