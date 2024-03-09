package evaluator;


import ast.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;
import errors.SimpleFilesExecutionException;
import evaluator.DynamicChecks;


public class Evaluator implements SimpleFilesVisitor<Object,Object>{



    enum supported_actions {
        create_file,
        create_folder,
        group,
        rename


    }

    HashMap<String, Instruction> instructions = new HashMap<>();
    HashMap<String, Condition> conditions = new HashMap<>();
    boolean userOSIsWindows;

    InstructionRunner instructionRunner;

    Memory memory = Memory.getInstance();

    public Evaluator(){
        this.instructionRunner = new InstructionRunner();
    }

    @Override
    public Object visit(Object context, Program p) {
        for (Statement s : p.getStatements()) {
            s.accept(context, this);
        }
        return null;
    }

    @Override
    public Object visit(Object context, Instruction i) {
        instructions.put(i.getName(), i);
        return null;
    }


    @Override
    public Object visit(Object context, Condition c) {
        conditions.put(c.getName(), c);
        return null;
    }

    public Object visit(Object context, ExecuteInstruction e) {
        String inst_name = e.getInstructionName();
        Instruction inst = instructions.get(inst_name);
        String inst_action = inst.getAction().getAction();

//        memory.printMemory(); // uncomment for memory debugging
        if (Objects.equals(inst_action, ":" +supported_actions.create_file)){
            try {
                instructionRunner.runCreateInstruction(inst, "file", null);
            } catch (SimpleFilesExecutionException ex) {
                System.err.println(ex.getMessage());
            }
        }else if (Objects.equals(inst_action, ":" +supported_actions.create_folder)){
            try {
                instructionRunner.runCreateInstruction(inst, "folder", null);
            } catch (SimpleFilesExecutionException ex) {
                System.err.println(ex.getMessage());
            }
        }else if (Objects.equals(inst_action, ":" +supported_actions.group)){
            try {
                instructionRunner.runGroupAction(inst, null);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }else if (Objects.equals(inst_action, ":" + supported_actions.rename)){
            try {
                instructionRunner.runRenameInstruction(inst, null);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }else {
            // If inst_action is not supported
            System.err.println("Unsupported action: " + inst_action);
        }

        return null;
    }
    @Override
    public Object visit(Object context, ExecuteCondMap e) {
        String inst_name = e.getInstructionName();
        Instruction inst = instructions.get(inst_name);
        String inst_action = inst.getAction().getAction();

        Condition condition = conditions.get(e.getConditionName());
        if (Objects.equals(inst_action, ":" +supported_actions.create_file)){
            try {
                instructionRunner.runCreateInstruction(inst, "file", condition);
            } catch (SimpleFilesExecutionException ex) {
                System.err.println(ex.getMessage());
            }
        }else if (Objects.equals(inst_action, ":" +supported_actions.create_folder)){
            try {
                instructionRunner.runCreateInstruction(inst, "folder", condition);
            } catch (SimpleFilesExecutionException ex) {
                System.err.println(ex.getMessage());
            }
        }else if (Objects.equals(inst_action, ":" +supported_actions.group)){
            try {
                instructionRunner.runGroupAction(inst, condition);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }else if (Objects.equals(inst_action, ":" + supported_actions.rename)){
            try {
                instructionRunner.runRenameInstruction(inst, condition);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }else {
            // If inst_action is not supported
            System.err.println("Unsupported action: " + inst_action);
        }

        return null;
    }

//    private void doGroupAction(Instruction instruction){
//        ArrayList<Parameter> parameters = instruction.getParameters();
//        HashMap<String, String> enabled_options = new HashMap<>();
//        ArrayList<Path> filteredList = new ArrayList<>();
//        Path targetPath = null;
//        Path filePath = null;
//        String mode = null;
//        String groupType = null;
//        String fileType = null;
//
//
//
//        // Extracting parameters
//        for (Parameter p: parameters){
//            switch (p.getKey()) {
//                case "group_target":
//                    // replacing / with \\ since windows use \ as separator
//                    if (userOSIsWindows){
//                        targetPath = Path.of(p.getValue().replace('/', '\\'));
//                    } else {
//                        targetPath = Path.of(p.getValue().replace('\\', '/'));
//                    }
//
//                    break;
//                case "path":
//                    if (userOSIsWindows){
//                        filePath = Path.of(p.getValue().replace('/', '\\'));
//                    } else {
//                        filePath = Path.of(p.getValue().replace('\\', '/'));
//                    }
//                    break;
//                case "mode":
//                    mode = p.getValue();
//                    break;
//                case "regex":
//                    enabled_options.put("regex", p.getValue());
//                    break;
//                case "contains":
//                    enabled_options.put("contains", p.getValue());
//                    break;
//                case "type": groupType = p.getValue();
//                    break;
//                case "file_type": fileType = p.getValue();
//                    break;
//                case "modified_date": enabled_options.put("modified_date", p.getValue());
//                    break;
//            }
//        }
//
//        // Create path folder if not exist already
//        File dest_folder = new File(String.valueOf(filePath));
//        dest_folder.mkdirs();
//
//        // Filtering path to include only qualifying targets
//        List<Path> paths = null;
//
//        assert targetPath != null;
//        File targetFile = new File(targetPath.toString());
//        // check if path is a folder
//        if (targetFile.isDirectory()){
//            try {
//                // get list of paths in the folder
//                paths = Files.list(targetPath).toList();
//                for (Path p : paths){
//                    // add path to list if is qualified by the enable options
//                    if (isSatisfyByOptions(p.getFileName().toString(), enabled_options, p.getParent().toString())){
//                        filteredList.add(p);
//                    }
//
//                }
//            } catch (IOException e) {
//                DynamicChecks.runtimeExceptionHandling(e, "group", "failed on getting list of paths under target path");
//            }
//
//        } else {
//            // add the file path if not a folder
//            filteredList.add(targetPath);
//        }
//
//
//        // Perform grouping on file
//        if ("file".equals(groupType)){
//            for (Path singleFiles: filteredList){
//                if (Files.isRegularFile(singleFiles)){
//                    assert filePath != null;
//                    Path fileToCopy = filePath.resolve(singleFiles.getFileName());
//                    try {
//                        // do copy or move based on mode
//                        if ("copy".equals(mode)){
//                            Files.copy(singleFiles, fileToCopy, StandardCopyOption.REPLACE_EXISTING);
//                        } else {
//                            // Perform move all files to the path folder.
//                            Files.move(singleFiles, fileToCopy, StandardCopyOption.REPLACE_EXISTING);
//
//                        }
//                    } catch (IOException e) {
//                        DynamicChecks.runtimeExceptionHandling(e, "group", "failed to " + mode + " " + singleFiles.getFileName());
//                    }
//                }
//                System.out.println("Successfully " + mode + " file " + singleFiles.getFileName() + " from " + targetPath + " to " + filePath);
//            }
//        // Perform grouping on folders and contents within these folders to the path folder.
//        } else{
//
//            // Recursively perform copy/move on filtered list of paths.
//            for (Path p: filteredList){
//                try{
//                    assert filePath != null;
//                    Path folderToCopy = filePath.resolve(p.getFileName());
//                    // do copy or move based on mode
//                    if ("copy".equals(mode)){
//                        Files.copy(p, folderToCopy, StandardCopyOption.REPLACE_EXISTING);
//                        copyFolders(p, folderToCopy);
//                    } else {
//                        Files.move(p, folderToCopy, StandardCopyOption.REPLACE_EXISTING);
//                        moveFolders(p, folderToCopy);
//                    }
//
//                } catch (IOException e){
//                    DynamicChecks.runtimeExceptionHandling(e , "group", "Failed to " + mode + " folders " + targetPath + "to " + filePath);
//                }
//                System.out.println("Successfully " + mode + " folder " + p.getFileName() + " from " + targetPath + " to " + filePath);
//            }
//        }
//    }
//
//    private void doCreateFileorFolder(Instruction instruction, String creationType) {
//        ArrayList<Parameter> parameters = instruction.getParameters();
//        File filePath = null;
//        String fileName = "";
//        String fileNameWithInc = "";
//        int count = 1;
//        int incr = 0;
//        int startNum = 0;
//        String newFileName;
//        String prefixFileName = "";
//
//        for (Parameter p : parameters) {
//            switch (p.getKey()){
//                case "path":
//                    if (userOSIsWindows){
//                        filePath = new File(p.getValue().replace('/', '\\'));
//                    } else {
//                        filePath = new File(p.getValue().replace('\\', '/'));
//                    }
//                    break;
//                case "name":
//                    fileName = p.getValue();
//                    break;
//                case "count":
//                    try {
//                        count = Integer.parseInt(p.getValue());
//                    } catch (NumberFormatException e){
//                        System.err.println("Invalid format for Count: " + p.getValue());
//                    }
//                    break;
//                default:
//                    System.err.println("Error in extracting parameter key on value: " + p.getValue());
//            }
//        }
//
//
//        // Filtering the fileName without the "}" symbol.
//        fileNameWithInc = fileName.substring(12, fileName.length() - 1);
//        // Splitting the fileName with ":"
//        String[] dynamicIterator = fileNameWithInc.split(":");
//        if (dynamicIterator.length == 3){
//            // startNum is the element after first ":"
//            startNum = Integer.parseInt(dynamicIterator[1]);
//            // incr is the element after second ":"
//            incr = Integer.parseInt(dynamicIterator[2]);
//            // prefixFileName is the file Name until reach "$" before the ITERATOR;
//            prefixFileName = fileName.substring(0, fileName.indexOf("$"));
//        }
//        // Create necessary folder from the path
//        if (!filePath.isDirectory()){
//            filePath.mkdirs();
//        }
//
//        // Check if it is file going to be created.
//        if (filePath.isDirectory() && "file".equals(creationType)){
//        // Check if only creating 1 file, if yes, then use the user-defined file name
//            if (count == 1){
//                File newFile = new File(filePath, fileName);
//                try {
//                    if (newFile.createNewFile()) {
//                        System.out.println("File created: " + fileName + " at " + newFile.getAbsolutePath());
//                    } else {
//                        System.out.println("Failed to create File: " + fileName + " at " + newFile.getAbsolutePath());
//                    }
//                } catch (IOException e) {
//                    DynamicChecks.runtimeExceptionHandling(e, "create_file", "failed on new file creation");
//                }
//            } else {
//                for (int i = 0; i < count; i++){
//                    if (dynamicIterator.length == 3){
//                        newFileName = prefixFileName + (startNum + i * incr);
//                    } else {
//                        newFileName = fileName.replace("${ITERATOR}", Integer.toString(i));
//                    }
//                    File newFile = new File(filePath, newFileName);
//                    try {
//                        if (newFile.createNewFile()) {
//                            System.out.println("File created: " + newFileName + " at " + newFile.getAbsolutePath());
//                        } else {
//                            System.out.println("Failed to create file: " + newFileName + " at " + newFile.getAbsolutePath());
//                        }
//                    } catch (IOException e) {
//                        DynamicChecks.runtimeExceptionHandling(e, "create_file", "failed on new file creation");
//                    }
//                }
//            }
//            // Check if it is folder going to be created.
//        } else if (filePath.isDirectory() && "folder".equals(creationType)){
//            // Check if only creating 1 folder, if yes, then use the user-defined folder name
//            if (count == 1){
//                File newFile = new File(filePath, fileName);
//                if (!newFile.mkdirs()) {
//                    System.err.println("Failed to create folder: " + fileName + " at " + newFile.getAbsolutePath());
//                } else {
//                    System.out.println("Folder created: " + fileName + " at " + newFile.getAbsolutePath());
//                }
//            } else {
//                for (int i = 0; i < count; i++){
//                    if (dynamicIterator.length == 3){
//                    newFileName = prefixFileName + (startNum + i * incr);
//                } else {
//                    newFileName = fileName.replace("${ITERATOR}", Integer.toString(i));
//                }
//                    File newFile = new File(filePath, newFileName);
//                    if (!newFile.mkdirs()) {
//                        System.err.println("Failed to create folder: " + newFileName + " at " + newFile.getAbsolutePath());
//                    } else {
//                        System.out.println("Folder created: " + newFileName + " at " + newFile.getAbsolutePath());
//                    }
//                }
//            }
//        } else {
//            System.err.println("Provided path is not a directory " + filePath + " " + fileName);
//        }
//    }
//
//    void doRename(Instruction instruction){
//        ArrayList<Parameter> parameters = instruction.getParameters();
//        HashMap<String, String> enabled_options = new HashMap<>();
//        ArrayList<String> filteredList = new ArrayList<>();
//        File targetPath = null;
//        String mode = null;
//
//        // Extracting parameters
//        for (Parameter p: parameters){
//            switch (p.getKey()) {
//                case "path":
//                    if (userOSIsWindows){
//                        targetPath = new File(p.getValue().replace('/', '\\'));
//                    } else {
//                        targetPath = new File(p.getValue().replace('\\', '/'));
//                    }
//                    break;
//                case "mode":
//                    mode = p.getValue();
//                    break;
//                case "regex":
//                    enabled_options.put("regex", p.getValue());
//                    break;
//                case "contains":
//                    enabled_options.put("contains", p.getValue());
//                    break;
//                case "type":
//                    enabled_options.put("type", p.getValue());
//                    break;
//                case "extension":
//                    enabled_options.put("extension", p.getValue());
//                    break;
//            }
//        }
//
//        // Filtering files in directory based on parameter provided
//
//        // assuming path provided by user is the folder, and filter condition applies to the stuff within the folder
//        assert targetPath != null;
//        if (targetPath.isDirectory()) {
//            String[] listOfNames = targetPath.list();
//            assert listOfNames != null;
//            for (String s: listOfNames) {
//                    if (isSatisfyByOptions(s, enabled_options, targetPath.getPath())){
//                        filteredList.add(s);
//                    }
//                }
//            }
//
//        // do rename on filtered list
//        for (String s: filteredList){
//            renameHelper(mode, s, targetPath.getPath());
//        }
//    }

    @Override
    public Object visit(Object context, Action a) {
        return null;
    }

    @Override
    public Object visit(Object context, Parameter p) {
        return null;
    }

    @Override
    public Object visit(Object context, Variable v) {
        memory.assignVariable(v);
        return null;
    }

    // -------- HELPER FUNCTIONS BELOW

//    // This function recursively move everything in a folder to the path folder.
//    private void moveFolders(Path targetPath, Path filePath) {
//        try{
//            List<Path> folders = Files.list(targetPath).toList();
//            for (Path singleFolder : folders){
//                Path folderToCopy = filePath.resolve(singleFolder.getFileName());
//                Files.move(singleFolder, folderToCopy, StandardCopyOption.REPLACE_EXISTING);
//                moveFolders(singleFolder, folderToCopy);
//            }
//        } catch (IOException e){
//            System.out.println("Failed to copy folders " + targetPath + "to " + filePath);
//        }
//    }
//
//    // This function recursively copying everything in a folder.
//    void copyFolders(Path targetPath, Path filePath){
//        try{
//            List<Path> folders = Files.list(targetPath).toList();
//            for (Path singleFolder : folders){
//                Path folderToCopy = filePath.resolve(singleFolder.getFileName());
//                Files.copy(singleFolder, folderToCopy, StandardCopyOption.REPLACE_EXISTING);
//                copyFolders(singleFolder, folderToCopy);
//            }
//        } catch (IOException e){
//            System.out.println("Failed to copy folders " + targetPath + "to " + filePath);
//        }
//    }
//
//    boolean isSatisfyByOptions(String s, HashMap<String, String> enabled_options, String parent_path){
//        Pattern pattern;
//        int satisfiedCount = 0;
//        int num_options = enabled_options.size();
//
//        if (enabled_options.containsKey("contains")){
//            if (s.contains(enabled_options.get("contains"))){
//                satisfiedCount++;
//            }
//        }
//
//        if (enabled_options.containsKey("regex")){
//            pattern = Pattern.compile(enabled_options.get("regex"));
//            if (pattern.matcher(s).groupCount() > 0){
//                satisfiedCount++;
//            }
//        }
//
//        if (enabled_options.containsKey("extension")){
//            int separator_index = s.lastIndexOf('.');
//            String ext = s.substring(separator_index);
//            if (ext.equals(enabled_options.get("extension"))){
//                satisfiedCount++;
//            }
//        }
//
//        if (enabled_options.containsKey("type")){
//            if (enabled_options.get("type").equals("folder")){
//                if (new File(parent_path + s).isDirectory()){
//                    satisfiedCount++;
//                }
//
//            }
//        }
//
//        return satisfiedCount == num_options;
//
//    }
//
//    void renameHelper(String mode, String target, String parent_path){
//        File oldFile = new File(parent_path + "\\" + target);
//        String old_name = oldFile.getName();
//        int sep_index = target.lastIndexOf('.');
//        String fileName = target.substring(0, sep_index);
//        String ext = target.substring(sep_index);
//        String newName = null;
//        if (Objects.equals(mode, "upper_case")){
//            newName = fileName.toUpperCase();
//        } else if (Objects.equals(mode, "lower_case")){
//            newName = fileName.toLowerCase();
//        } else if (Objects.equals(mode, "camal_case")){
//            // cant really differentiate words that are together like hellothere, will now have to split by _ or "space"
//            StringBuilder sb = new StringBuilder();
//            String[] words = fileName.split("[_]+");
//
//            for (int i = 0; i < words.length; i++){
//                if (i == 0){
//                 sb.append(words[i].toLowerCase()).append("_");
//                } else {
//                    sb.append(Character.toUpperCase(words[i].charAt(0)));
//                    if (words[i].length() > 1 && (i < words.length - 1)){
//                        sb.append(words[i].substring(1).toLowerCase()).append("_");
//                    } else {
//                        sb.append(words[i].substring(1).toLowerCase());
//                    }
//                }
//
//            }
//            newName = sb.toString();
//        }
//        System.out.println(newName);
//        if(oldFile.renameTo(new File(parent_path + "\\" + newName + ext))){
//            System.out.println("Successfully rename file " + old_name + " to " + newName + ext);
//        }
//
//    }
//

}