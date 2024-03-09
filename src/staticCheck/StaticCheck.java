package staticCheck;
import ast.*;
import errors.CircularAssignmentException;
import errors.MemoryAssignmentException;
import errors.UnknownVariableException;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Map;

import static evaluator.Memory.isVariableReference;

public class StaticCheck implements SimpleFilesVisitor<Object,Object>{
    HashMap<String, Instruction> instructions = new HashMap<>();
    HashMap<String, Condition> conditions = new HashMap<>();


    public Object visit(Object context, Program p) {
        if (p == null) {
            throw new IllegalArgumentException("Static check failed: Program object is null. Context: " + contextToString(context));
        }

        for (int index = 0; index < p.getStatements().size(); index++) {
            Statement s = p.getStatements().get(index);
            if (s == null) {
                throw new IllegalArgumentException("Static check failed: Program contains a null Statement at index " + index + ". Context: " + contextToString(context));
            }
            s.accept(context, this);
        }
        return null;
    }


    @Override
    public Object visit(Object context, Condition c) {
        if (c == null) {
            throw new IllegalArgumentException("Static check failed: Condition object is null. Context: " + contextToString(context));
        }
        if (c.getName() == null || c.getName().isEmpty()) {
            throw new IllegalArgumentException("Static check failed: Condition name is null or empty. Context: " + contextToString(context));
        }

        if (conditions.containsKey(c.getName())) {
            throw new IllegalArgumentException("Static check failed: Duplicate condition name '" + c.getName() + "'. Context: " + contextToString(context));
        }

        conditions.put(c.getName(), c);
        return null;
    }

    @Override
    public Object visit(Object context, ExecuteInstruction e) {
        if (e == null) {
            throw new IllegalArgumentException("Static check failed: ExecuteInstruction object is null. Context: " + contextToString(context));
        }

        String inst_name = e.getInstructionName();
        if (inst_name == null || inst_name.isEmpty()) {
            throw new IllegalArgumentException("Static check failed: Instruction name is null or empty. Context: " + contextToString(context));
        }
        Instruction inst = instructions.get(inst_name);
        if (inst == null) {
            throw new IllegalArgumentException("Instruction not found: " + inst_name);
        }
        return null;
    }

    @Override
    public Object visit(Object context, ExecuteCondMap e) {
        if (e == null) {
            throw new IllegalArgumentException("Static check failed: ExecuteCondMap object is null. Context: " + contextToString(context));
        }

        String instructionName = e.getInstructionName();
        String conditionName = e.getConditionName();

        if (instructionName == null || instructionName.isEmpty() || !instructions.containsKey(instructionName)) {
            throw new IllegalArgumentException("Static check failed: Invalid or undefined instruction name '" + instructionName + "'. Context: " + contextToString(context));
        }

        if (conditionName == null || conditionName.isEmpty() || !conditions.containsKey(conditionName)) {
            throw new IllegalArgumentException("Static check failed: Invalid or undefined condition name '" + conditionName + "'. Context: " + contextToString(context));
        }
        return null;
    }


    @Override
    public Object visit(Object context, Instruction i){
        if (i == null) {
            throw new IllegalArgumentException("Static check failed: Instruction object is null. Context: " + contextToString(context));
        }
        if (i.getName() == null || i.getName().isEmpty()) {
            throw new IllegalArgumentException("Static check failed: Instruction name is null or empty. Context: " + contextToString(context));
        }
        if (instructions.containsKey(i.getName())) {
            throw new IllegalArgumentException("Static check failed: Duplicate instruction name '" + i.getName() + "'. Context: " + contextToString(context));
        }
        switch (i.getAction().getAction()) {
            case ":create_folder":
                checkCreateFolderParams(i);
                break;
            case ":create_file":
                checkCreateFileParams(i);
                break;
            case ":group":
                checkGroupParams(i);
                break;
            case ":rename":
                checkRenameParams(i);
                break;
            default:
                throw new IllegalArgumentException("Static check failed: Unknown instruction type '" + i.getName() + "'. Context: " + contextToString(context));
        }
        instructions.put(i.getName(), i);
        return null;
    }

    private void checkCreateFolderParams(Instruction i) {
        Map<String, String> params = new HashMap<>();
        for (Parameter p : i.getParameters()) {
            params.put(p.getKey().toLowerCase(), p.getRawValue());
        }

        // Checking NAME and NAMEFILE parameters
        String name = params.get("name");
        String nameFile = params.get("namefile");
        if (name == null && nameFile == null) {
            throw new IllegalArgumentException("Static check failed: Either NAME or NAMEFILE must be specified for :create_file instruction.");
        }
        if ((name != null && nameFile != null)) {
            throw new IllegalArgumentException("Static check failed: Only one of NAME or NAMEFILE must be specified for :create_file instruction.");
        }

        // Checking PATH
        String path = params.get("path");
        if (!isValidPath(path) && !isVariableReference(path)) {
            throw new IllegalArgumentException("Static check failed: Invalid PATH parameter for :create_file instruction.");
        }

        // Checking COUNT
        String count = params.get("count");
        if (count != null && !isVariableReference(count)) {
            try {
                int countValue = Integer.parseInt(count);
                if (countValue <= 0) {
                    throw new IllegalArgumentException("Static check failed: COUNT parameter must be a positive integer for :create_folder instruction.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Static check failed: COUNT parameter must be an integer for :create_folder instruction.");
            }
        }
    }
    private void checkCreateFileParams(Instruction i) {
        Map<String, String> params = new HashMap<>();
        for (Parameter p : i.getParameters()) {
            params.put(p.getKey().toLowerCase(), p.getRawValue());
        }

        String name = params.get("name");
        String nameFile = params.get("namefile");
        if (name == null && nameFile == null) {
            throw new IllegalArgumentException("Static check failed: Either NAME or NAMEFILE must be specified for :create_file instruction.");
        }
        if ((name != null && nameFile != null)) {
            throw new IllegalArgumentException("Static check failed: Only one of NAME or NAMEFILE must be specified for :create_file instruction.");
        }

        String path = params.get("path");
        if (!isValidPath(path) && !isVariableReference(path)) {
            throw new IllegalArgumentException("Static check failed: Invalid PATH parameter for :create_file instruction.");
        }

        String count = params.get("count");
        if (count != null && !isVariableReference(count)) {
            try {
                int countValue = Integer.parseInt(count);
                if (countValue <= 0) {
                    throw new IllegalArgumentException("Static check failed: COUNT parameter must be a positive integer for :create_file instruction.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Static check failed: COUNT parameter must be an integer for :create_file instruction.");
            }
        }
    }

    private void checkGroupParams(Instruction i) {
        Map<String, String> params = new HashMap<>();
        for (Parameter p : i.getParameters()) {
            params.put(p.getKey().toLowerCase(), p.getRawValue());
        }

        String groupTarget = params.get("group_target");
        if ((groupTarget == null || groupTarget.isEmpty()) && !isVariableReference(groupTarget)) {
            throw new IllegalArgumentException("Static check failed: GROUP_TARGET parameter is required for :group instruction.");
        }

        String path = params.get("path");
        if ((path == null || path.isEmpty()) && !isVariableReference(path)) {
            throw new IllegalArgumentException("Static check failed: PATH parameter is required for :group instruction.");
        }

        String mode = params.get("mode");
        if ((mode == null || mode.isEmpty()) && !isVariableReference(mode)) {
            throw new IllegalArgumentException("Static check failed: MODE parameter is required for :group instruction.");
        }
    }


    private void checkRenameParams(Instruction i) {
        Map<String, String> params = new HashMap<>();
        for (Parameter p : i.getParameters()) {
            params.put(p.getKey().toLowerCase(), p.getRawValue());
        }

        // Check PATH parameter
        String path = params.get("path");
        if ((path == null || path.isEmpty()) && !isVariableReference(path)) {
            throw new IllegalArgumentException("Static check failed: PATH parameter is required for :rename instruction.");
        }

        // Check MODE parameter
        String mode = params.get("mode");
        if ((mode == null || mode.isEmpty()) && !isVariableReference(mode)) {
            throw new IllegalArgumentException("Static check failed: MODE parameter is required for :rename instruction.");
        }
    }


    @Override
    public Object visit(Object context, Action a) {
        if (a == null) {
            throw new IllegalArgumentException("Static check failed: Action object is null. Context: " + contextToString(context));
        }
        String actionType = a.getAction();
        switch (actionType) {
            case ":create_folder":
            case ":create_file":
            case ":group":
            case ":rename":
                break;
            default:
                throw new IllegalArgumentException("Static check failed: Action type is not supported. Context: " + contextToString(context));
        }
        return null;
    }

    @Override
    public Object visit(Object context, Parameter p) {
        if (p == null) {
            throw new IllegalArgumentException("Static check failed: Parameter object is null. Context: " + contextToString(context));
        }

        String key = p.getKey();
        String value = p.getValue();

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Static check failed: Parameter has an invalid or empty key. Context: " + contextToString(context));
        }

        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Static check failed: Parameter with key '" + key + "' has an empty value. Context: " + contextToString(context));
        }
        switch (key) {
            case "group_target":
            case "path":
            case "mode":
            case "regex":
            case "contains":
            case "type":
            case "file_type":
            case "modified_date":
                // Valid parameter keys, no action needed
                break;
            default:
                throw new IllegalArgumentException("Static check failed: Parameter with key '" + key + "is invalid" );
        }
        return null;
    }

    @Override
    public Object visit(Object context, Variable v) {
        return null;
    }
    private String contextToString(Object context) {
        return context != null ? context.toString() : "null";
    }

    private boolean isValidPath(String path) {
        return path != null && !path.trim().isEmpty();
    }


}
