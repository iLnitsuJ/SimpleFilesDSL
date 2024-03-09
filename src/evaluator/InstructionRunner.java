package evaluator;

import ast.Condition;
import ast.Instruction;
import ast.Parameter;
import errors.SimpleFilesExecutionException;
import errors.UnknownVariableException;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;

import java.io.IOException;

import java.nio.file.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstructionRunner {
    Memory memory;
    IgnoreList ignoreList;
    InstructionRunner() {
        memory = Memory.getInstance();
        ignoreList = IgnoreList.getInstance();

        // Initialization for ignore list on parameter against specified action
        ignoreList.addToIgnoreList("create", "extension", "group_target", "mode", "type", "contains", "regex", "modified_date", "size");
        ignoreList.addToIgnoreList("group", "template_path", "name", "name_file", "count");
        ignoreList.addToIgnoreList("rename", "template_path", "name", "name_file", "count", "group_target");
    }



    public void runCreateInstruction(Instruction instruction, String creationType, Condition condition) throws SimpleFilesExecutionException {
        Map<String, String> parameters;

        ArrayList<File> filesCreatedByInstruction = new ArrayList<>();


        // setup default for required parameters
        String fileName = null;
        int count = 1;
        Path path = Paths.get("").toAbsolutePath();


        // optional param for create file
        Path templatePath = null;

        // merge with condition parameters if provided, or convert parameters to a map
        if (condition != null){
            parameters = mergeParameters("create", instruction.getParameters(), condition.getParameters());
        } else {
            parameters = makeParameterMap(instruction.getParameters());
        }


        // extracting parameters
        if (parameters.containsKey("name")){
            fileName = parameters.get("name");
        }

        if (parameters.containsKey("path")){
            path = Paths.get(parameters.get("path")).toAbsolutePath();
        }

        if (parameters.containsKey("count")){
            try {
                count = Integer.parseInt(parameters.get("count"));
            } catch (NumberFormatException e){
                String msg = String.format(
                        "Error encountered running instruction: %s. Invalid format for Count: %s.",
                        instruction.getName(), parameters.get("count"));
                throw new SimpleFilesExecutionException(msg);
            }

            if (count < 1){
                String msg = String.format(
                        "Error encountered running instruction: %s. Count can not be less than 1: %s.",
                        instruction.getName(), parameters.get("count"));
                throw new SimpleFilesExecutionException(msg);
            }
        }

        if (parameters.containsKey("template_path")){
            templatePath = Paths.get(parameters.get("template_path")).toAbsolutePath();
        }


        // null check for name just in case
        if (fileName == null){
            String msg = String.format("Error encountered running instruction: %s. Name parameter is missing",
                    instruction.getName());
            throw new SimpleFilesExecutionException(msg);
        }

        ArrayList<String> names;

        // Create list of name from dynamic construct
        try {
            names = getNameList(fileName, count);
        } catch (SimpleFilesExecutionException e) {
            String msg = String.format("Error encountered running instruction: %s. %s",
                    instruction.getName(), e.getMessage());
            throw new SimpleFilesExecutionException(msg);
        }


        if (names.isEmpty()){
            String msg = String.format("Error encountered running instruction: %s. Error resolving dynamic construct",
                    instruction.getName());
            throw new SimpleFilesExecutionException(msg);
        }

        // Create necessary folder along the file path
        File filePath = path.toFile();
        if (!filePath.isDirectory()){
            filePath.mkdirs();
        }

        if (!filePath.exists()){
            String msg = String.format("Error encountered running instruction: %s. File path does not exist",
                    instruction.getName());
            throw new SimpleFilesExecutionException(msg);
        }

        // Do create action on each name constructed
        for (String n: names){
            File newFile = new File(filePath, n);

            // file creation
            if ("file".equals(creationType)) {
                try {
                    // First filePath is not actually

                    if (templatePath != null){
                        Files.copy(templatePath, path);
                        System.out.println("File created using template: " + n + " at " + path);
                    } else {
                        if (newFile.createNewFile()) {
                            filesCreatedByInstruction.add(newFile);
                            System.out.println("File created: " + n + " at " + newFile.getAbsolutePath());
                        } else {
                            System.out.println("Failed to create file: " + n + " at " + newFile.getAbsolutePath());
                        }
                    }

                } catch (UnsupportedOperationException e) {
                    String msg = String.format(
                            "Error encountered running instruction: %s. During %s file creation, unsupportedOperationException caught",
                            instruction.getName(), n);
                    throw new SimpleFilesExecutionException(msg);
                } catch (FileAlreadyExistsException e) {
                    String msg = String.format(
                            "Error encountered running instruction: %s. During %s file creation, FileAlreadyExistsException caught",
                            instruction.getName(), n);
                    throw new SimpleFilesExecutionException(msg);
                } catch (SecurityException e) {
                    String msg = String.format(
                            "Error encountered running instruction: %s. During %s file creation, SecurityException caught",
                            instruction.getName(), n);
                    throw new SimpleFilesExecutionException(msg);
                } catch (DirectoryNotEmptyException e) {
                    String msg = String.format(
                            "Error encountered running instruction: %s. During %s file creation, DirectoryNotEmptyException caught",
                            instruction.getName(), n);
                    throw new SimpleFilesExecutionException(msg);
                } catch (IOException e) {
                    String msg = String.format(
                            "Error encountered running instruction: %s. Failed on %s file creation ",
                            instruction.getName(), n);
                    throw new SimpleFilesExecutionException(msg);
                }
            } else {
            // folder creation
                if (folderNameCheck(n)){
                    if (!newFile.mkdirs()) {
                        System.err.println("Failed to create folder: " + n + " at " + newFile.getAbsolutePath());
                    } else {
                        filesCreatedByInstruction.add(newFile);
                        System.out.println("Folder created: " + n + " at " + newFile.getAbsolutePath());
                    }


                } else {
                    String msg = String.format(
                            "Error encountered running instruction: %s. Found restricted symbol in specified folder name: %s",
                            instruction.getName(), n);
                    throw new SimpleFilesExecutionException(msg);
                }

            }
        }
        memory.fileGroups.put(instruction.getName(),filesCreatedByInstruction);
        memory.basePaths.put(instruction.getName(), path.toString());
    }



    public void runRenameInstruction(Instruction instruction, Condition condition) throws Exception {


        // Initialize necessary data structures
        HashMap<String, String> filterOptions = new HashMap<>();

        Map<String, String> parameters;

        // Set initial mandatory parameters to null
        File targetPath = null;
        String mode = null;
        boolean recursive = true;
        String pathName = "";
        String type;

        // merge with condition parameters if provided, or convert parameters to a map
        if (condition != null){
            parameters = mergeParameters("rename", instruction.getParameters(), condition.getParameters());
        } else {
            parameters = makeParameterMap(instruction.getParameters());
        }

        // extracting parameters
        if (parameters.containsKey("path")){
            try {
                Path path = Path.of("");
                if (memory.hasGroupedFilesFromInstruction(parameters.get("path"))){
                        path = Path.of(memory.getBasePath(parameters.get("path")));
                        pathName = parameters.get("path");
                } else {
                    path = Paths.get(parameters.get("path")).toAbsolutePath();
                }
                targetPath = new File(String.valueOf(path));
            } catch (Exception e) {
                String msg = String.format(
                        "Error encountered running instruction: %s. Unable to resolve path at runtime.",
                        instruction.getName());
                throw new SimpleFilesExecutionException(msg);
            }
            if (!targetPath.exists()) {
                String msg = String.format(
                        "Error encountered running instruction: %s. Are you sure the specified path exists?",
                        instruction.getName());
                throw new SimpleFilesExecutionException(msg);
            }
            if (!targetPath.isDirectory()) {
                String msg = String.format(
                        "Error encountered running instruction: %s. Path must be a valid folder!",
                        instruction.getName());
                throw new SimpleFilesExecutionException(msg);
            }

        }

        if (parameters.containsKey("mode")){
            mode = parameters.get("mode");

            // default to lower case when mode is declared in condition but for group action
            if (!Objects.equals(parameters.get("mode"), "upper_case") && !Objects.equals(parameters.get("mode"), "lower_case")) {
                mode = "lower_case";
            }
        }

        if (parameters.containsKey("recursive")){
            recursive = Boolean.parseBoolean(parameters.get("recursive"));
        }

        // initialize for filter parameters
        for (String key: parameters.keySet()){
            if (!Objects.equals(key, "mode") && !Objects.equals(key, "path") && !Objects.equals(key, "recursive")) {
                if (parameters.get(key) == null) {

                    String msg = String.format("Parameter value for %s is null. Double check variables are assigned before use.", key);

                    throw new SimpleFilesExecutionException(msg);
                }
                filterOptions.put(key, parameters.get(key));
            }
        }

        if (parameters.containsKey("type")) {
            type = parameters.get("type");
        } else {
            type = "";
        }

        // Null check just in case. Missing params should be caught in static check
        if (targetPath == null || mode == null) {
            String msg = String.format(
                    "Error encountered running instruction: %s. Unknown targetPath or mode.",
                    instruction.getName());
            throw new SimpleFilesExecutionException(msg);
        }

        // Grab all the files in the folder that satisfy the specified conditions.
        ArrayList<File> filteredList = new ArrayList<>();

        ArrayList<File> filesInDirectory;
        if (recursive) {
            filesInDirectory = new ArrayList<>();
            // Walk the directory and get all files that satisfies the filter
            Files.walk(targetPath.toPath()).forEach(walkedPath -> {
                if (type.equals("file") && walkedPath.toFile().isFile()){
                    filesInDirectory.add(new File(String.valueOf(walkedPath)));
                } else if (type.equals("folder") && walkedPath.toFile().isDirectory()) {
                    filesInDirectory.add(new File(String.valueOf(walkedPath)));
                } else {
                    // default to rename both folder and file
                    filesInDirectory.add(new File(String.valueOf(walkedPath)));
                }

            });
        } else {
            if (memory.hasGroupedFilesFromInstruction(pathName)) {
                filesInDirectory = (memory.getGroupedFiles(pathName));
            } else if (targetPath.listFiles() == null) {
                filesInDirectory = null;
            } else {
                filesInDirectory = new ArrayList<>();
                Collections.addAll(filesInDirectory, targetPath.listFiles());
            }

        }

        if (filesInDirectory == null) {
            // no files in the directly, just return without doing anything
            return;
        }

        for (File file: filesInDirectory) {
            if (isSatisfiedByOptions(file, filterOptions)) {
                filteredList.add(file);
            }
        }
        // Do the renaming
        for (File oldFile: filteredList) {
            String oldFileNameNoExt = getFileNameWithoutExtension(oldFile);
            String oldFileExt = getFileExtension(oldFile);

            // New file in the same directory as the old file
            String parentDir = oldFile.getAbsoluteFile().getParent();
            String newFileName = mode.equals("upper_case") ? oldFileNameNoExt.toUpperCase() : oldFileNameNoExt.toLowerCase();
            File newFile = new File(parentDir, newFileName+oldFileExt);

            // Check if was successfully renamed
            boolean success = oldFile.renameTo(newFile);
            if (!success) {
                throw new SimpleFilesExecutionException("Failed to rename file: " + oldFile.getAbsolutePath());
            }
        }
    }

    public void runGroupAction(Instruction instruction, Condition condition) throws Exception {
        // Initialize necessary data structures
        String instructionName = instruction.getName();
        HashMap<String, String> filterOptions = new HashMap<>();
        Map<String, String> parameters;


        // Set initial mandatory parameters to null
        final ArrayList<File> groupTarget = new ArrayList<>();
        String basePath;


        // merge with condition parameters if provided, or convert parameters to a map
        if (condition != null){
            parameters = mergeParameters("group", instruction.getParameters(), condition.getParameters());
        } else {
            parameters = makeParameterMap(instruction.getParameters());
        }

        // Set filter parameters first
        for (String key : parameters.keySet()) {
            if (!key.equals("group_target") && !key.equals("path") && !key.equals("mode")) {

                if (parameters.get(key) == null) {
                    String msg = String.format("Parameter value for %s is null. Double check variables are assigned before use.", parameters.get(key));
                    throw new SimpleFilesExecutionException(msg);
                }
                filterOptions.put(key, parameters.get(key));
            }
        }

        // Get the group target
        if (memory.hasGroupedFilesFromInstruction(instructionName)) {
            ArrayList<File> tempGroupTarget = memory.getGroupedFiles(instructionName);
            basePath = memory.getBasePath(instructionName);

            // iterate over all files in the group target for the ones that satisfy the filter
            for (File file : tempGroupTarget) {
                if (isSatisfiedByOptions(file, filterOptions)) {
                    groupTarget.add(file);
                }
            }
        } else {
            String groupTargetString;
            // Group target exists in the parameters. Get path to it.
            if (parameters.containsKey("group_target")){
                groupTargetString = parameters.get("group_target");
            } else {
                String msg = String.format(
                        "Error encountered running instruction: %s. Group target not specified.",
                        instruction.getName());
                throw new SimpleFilesExecutionException(msg);
            }

            Path groupTargetPath;
            try {
                if (memory.hasGroupedFilesFromInstruction(groupTargetString)){
                    groupTargetPath = Path.of(memory.getBasePath(groupTargetString));
                } else{
                    groupTargetPath = Paths.get(groupTargetString).toAbsolutePath();
                }
            } catch (Exception e) {
                String msg = String.format(
                        "Error encountered running instruction: %s. Unable to resolve path at runtime.",
                        instruction.getName());
                throw new SimpleFilesExecutionException(msg);
            }

            basePath = groupTargetPath.toAbsolutePath().toString();


            if (!Files.isDirectory(groupTargetPath)) {
                String msg = String.format(
                        "Error encountered running instruction: %s. Group target must be a directory.",
                        instruction.getName());
                throw new SimpleFilesExecutionException(msg);
            }

            // Walk the directory and get all files that satisfies the filter
            Files.walk(groupTargetPath).forEach(walkedPath -> {
                if (!walkedPath.equals(groupTargetPath) && isSatisfiedByOptions(walkedPath, filterOptions)) {
                    if (memory.hasGroupedFilesFromInstruction(groupTargetString)){
                        ArrayList<File>filesCreateByInstruction = memory.getGroupedFiles(groupTargetString);
                        for (File file : filesCreateByInstruction) {
                            if (file.getAbsolutePath().equals(walkedPath.toString())) {
                                groupTarget.add(new File(String.valueOf(walkedPath)));
                            }
                        }
                    }
                    else {
                    groupTarget.add(new File(String.valueOf(walkedPath)));
                    }
                }
            });
        }

        // Set up the output directory
        Path outputPath;
        String outputPathString;

        if (parameters.containsKey("path")){
            outputPathString = parameters.get("path");
        } else {
            String msg = String.format(
                    "Error encountered running instruction: %s. Path not specified.",
                    instruction.getName());
            throw new SimpleFilesExecutionException(msg);
        }

        try {
            outputPath = Paths.get(outputPathString).toAbsolutePath();
        } catch (Exception e) {
            String msg = String.format(
                    "Error encountered running instruction: %s. Unable to resolve path at runtime.",
                    instruction.getName());
            throw new SimpleFilesExecutionException(msg);
        }

        Files.createDirectories(outputPath);

        // Move/Copy all files
        ArrayList<File> groupedFilesResult = new ArrayList<>();
        String mode;
        if (parameters.containsKey("mode")){
            mode = parameters.get("mode");

            // default mode to be move if it is declared in condition but supposed to be use for rename action
            if (!Objects.equals(mode, "move") && !Objects.equals(mode, "copy")) {
                mode = "move";
            }
        } else {
            String msg = String.format(
                    "Error encountered running instruction: %s. Mode not specified.",
                    instruction.getName());
            throw new SimpleFilesExecutionException(msg);
        }

        String originalPath = "";

        if (mode.equals("move")) {
            for (File file : groupTarget) {
                if (file.exists()) {
                    originalPath = file.getAbsolutePath();
                    File moveFile;
//                    if (memory.hasGroupedFilesFromInstruction(basePath)){
//                        moveFile = new File(outputPathString, file.getName().replace(basePath,""));
//                    } else {
//                        moveFile = new File(outputPathString, file.getAbsolutePath().replace(basePath,""));
//                    }
                    moveFile = new File(outputPathString, file.getAbsolutePath().replace(basePath,""));
                    String movePath = moveFile.getAbsolutePath();
                    Files.createDirectories(Paths.get(moveFile.getParent()));

                    try {
                        Files.move(Paths.get(originalPath), Paths.get(movePath), StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        System.err.println("Unable to move file: " + file);
                    }
                    groupedFilesResult.add(moveFile);
                } else {
                    System.out.println("Could not find file: " + file);
                }
            }
        } else {
            for (File file : groupTarget) {
                if (file.exists()) {
                    originalPath= file.getAbsolutePath();
                    File copyFile;
//                    if (memory.hasGroupedFilesFromInstruction(basePath)){
//                         copyFile = new File(outputPathString, file.getName().replace(basePath,""));
//                    } else {
//                         copyFile = new File(outputPathString, file.getAbsolutePath().replace(basePath,""));
//                    }
                    copyFile = new File(outputPathString, file.getAbsolutePath().replace(basePath,""));

                    String copyPath = copyFile.getAbsolutePath();
                    Files.createDirectories(Paths.get(copyFile.getParent()));

                    try {
                        Files.copy(Paths.get(originalPath), Paths.get(copyPath), StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        System.err.println("Unable to copy file: " + file);
                    }
                    groupedFilesResult.add(copyFile);
                }
            }
        }



        memory.storeBasePath(instructionName, outputPath.toAbsolutePath().toString());
        memory.storeGroupResult(instructionName, groupedFilesResult);
    }


    private boolean isSatisfiedByOptions(String absolutePath, HashMap<String, String> filterOptions) {
        Path path = Paths.get(absolutePath).toAbsolutePath();

        File file = new File(String.valueOf(path));

        return isSatisfiedByOptions(file, filterOptions);
    }

    private boolean isSatisfiedByOptions(Path pathToTest, HashMap<String, String> filterOptions) {
        File file = new File(String.valueOf(pathToTest));

        return isSatisfiedByOptions(file, filterOptions);
    }

    private boolean isSatisfiedByOptions(File fileToTest, HashMap<String, String> filterOptions) {
        if (!fileToTest.exists()) {
            System.err.printf("A specified file does not exist: %s. Might have been moved or deleted. Ignoring.%n", fileToTest.getAbsolutePath());
            return false;
        }
        String fileName = fileToTest.getName();


        if (filterOptions.containsKey("contains")){
            if (!fileName.contains(filterOptions.get("contains"))){
                return false;
            }
        }

        if (filterOptions.containsKey("regex")){
            if (Pattern.compile(filterOptions.get("regex")).matcher(fileName).groupCount() < 1) {
                return false;
            }
        }


        if (filterOptions.containsKey("extension")){
            if (!fileName.substring(fileName.lastIndexOf(".") + 1).equals(filterOptions.get("extension"))) {
                return false;
            }
        }

        if (filterOptions.containsKey("type")){
            if (Objects.equals(filterOptions.get("type"), "folder") && !fileToTest.isDirectory()) {
                return false;
            } else if (Objects.equals(filterOptions.get("type"), "file") && fileToTest.isDirectory()){
                return false;
            }
        }

        if (filterOptions.containsKey("modified_date")){
            String[] split = filterOptions.get("modified_date").split(" ");

            if (split.length < 2 || split.length > 3) {
                throw new SimpleFilesExecutionException("Cannot properly parse parameter arguments for modified_date");
            }
            String rangeComparator = split[0]; // The before, after, or between
            if (!rangeComparator.matches("(?i)before|after|between")) {
                throw new SimpleFilesExecutionException("Unknown range comparator: " + rangeComparator);
            }

            // parse the dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm", Locale.getDefault());
            LocalDateTime firstDate;
            LocalDateTime secondDate;
            try {
                firstDate = LocalDateTime.parse(split[1], formatter);
                secondDate = split.length == 3 ? LocalDateTime.parse(split[2], formatter) : null;
            } catch (DateTimeParseException e) {
                throw new SimpleFilesExecutionException("Error encountered when trying to parse the given date: " + e.getMessage());
            }


            LocalDateTime fileModifiedTime;
            try {
                fileModifiedTime = LocalDateTime.ofInstant(Files.getLastModifiedTime(fileToTest.toPath()).toInstant(), ZoneId.systemDefault());
            } catch (IOException e) {
                throw new SimpleFilesExecutionException(String.format("IOException occurred when trying to access file: %s.", fileToTest.getAbsolutePath()));
            }

            if (rangeComparator.equalsIgnoreCase("BEFORE") && fileModifiedTime.isAfter(firstDate)) {
                return false;
            } else if (rangeComparator.equalsIgnoreCase("AFTER") && fileModifiedTime.isBefore(firstDate)) {
                return false;
            } else if (rangeComparator.equalsIgnoreCase("BETWEEN") && secondDate!= null && (fileModifiedTime.isBefore(firstDate) || fileModifiedTime.isAfter(secondDate))) {
                return false;
            } // not checking if range comparator is valid
        }

        if (filterOptions.containsKey("size") && !filterOptions.containsKey("comparator")) {
            throw new SimpleFilesExecutionException("Size comparison missing comparator parameter.");
        } else if (!filterOptions.containsKey("size") && filterOptions.containsKey("comparator")) {
            throw new SimpleFilesExecutionException("Size comparison missing size parameter.");
        } else if (filterOptions.containsKey("size") && filterOptions.containsKey("comparator")) {
            String comparator = filterOptions.get("comparator");

            int size;
            try {
                size = Integer.parseInt(filterOptions.get("size")) * 1024; // input size is kb, must convert to bytes
                if (comparator.equals("GT") && Files.size(fileToTest.toPath()) < size) {
                    return false;
                } else if (comparator.equals("LT") && Files.size(fileToTest.toPath()) > size) {
                    return false;
                } // not checking if comparator is valid
            } catch (IOException e) {
                throw new SimpleFilesExecutionException(String.format("IOException occurred when trying to access file: %s.", fileToTest.getAbsolutePath()));
            } catch (NumberFormatException e) {
                throw new SimpleFilesExecutionException("Specified size must be parsable integer.");
            } catch (Exception e) {
                throw new SimpleFilesExecutionException("Something went wrong: " + e.getMessage());
            }
        }

        return true;
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    private String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        return name.replaceFirst("[.][^.]+$", "");
    }


    // Get list of names from dynamic construct
    private ArrayList<String> getNameList(String name, int count) throws SimpleFilesExecutionException {
        ArrayList<String> name_list = new ArrayList<>();

        // default value
        int incr = 1;
        int startNum = 0;

        int money_sep = name.indexOf("$");

        // when not using dynamic construct
        if (money_sep == -1 && count > 1){
            throw new SimpleFilesExecutionException("Count is more than 1. Missing dynamic construct for name");
        } else if (money_sep == -1){
            name_list.add(name);
            return name_list;
        }

        // Dynamic construct validity check
        if (validDConstructSyntax(name)) {
            // prefixFileName is the file Name before $ which is start of iterator;
            String prefixFileName = name.substring(0, money_sep);
            String nameAfterIterator = name.substring(name.indexOf("}") + 1);
            String Iterator = name.substring(name.indexOf("{") + 1, name.indexOf("}"));

            // Splitting the fileName with ":"
            String[] dynamicIterator = Iterator.split(":");
            if (dynamicIterator.length == 3){
                // startNum is the element after first ":"
                startNum = Integer.parseInt(dynamicIterator[1]);
                // incr is the element after second ":"
                incr = Integer.parseInt(dynamicIterator[2]);
            } else if (dynamicIterator.length == 2) {
                startNum = Integer.parseInt(dynamicIterator[1]);
            }

            for (int i = 0; i < count; i++){
                name_list.add(prefixFileName + (startNum + i * incr) + nameAfterIterator);
            }
        }
        return name_list;
   }

    private boolean validDConstructSyntax(String name) {

        // Will match letter or _,
        // follow by 3 options: 1. ${ITERATOR} 2.${ITERATOR:int} 3. ${ITERATOR:int:int or -int} ,
        // follow by letter or . and letter for possible extension.
        String regex = "^[a-zA-Z_ ]+\\$\\{ITERATOR(:-?\\d+(:-?\\d+)?)?}+[a-zA-Z.]*[a-zA-Z]?$";

        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex);

        // Match the folder name against the pattern
        Matcher matcher = pattern.matcher(name);

        // Check if the folder name matches the pattern
        return matcher.matches();
    }

    boolean folderNameCheck(String folder_name) {
       // Define the regex pattern, first ^ is start and $ is ending
       String regex = "^[^.]+$";

       // Compile the regex pattern
       Pattern pattern = Pattern.compile(regex);

       // Match the folder name against the pattern
       Matcher matcher = pattern.matcher(folder_name);

       // Check if the folder name matches the pattern
       return matcher.matches();
    }

    // Helper that generate map of parameter key, value using instruction parameters and condition parameters, condition
    // parameter will take precedence.
    private Map<String, String> mergeParameters(String action, ArrayList<Parameter> instruction_parameters, ArrayList<Parameter> condition_parameters) {
        Map<String, String> parameter_map = new HashMap<>();

        // Add instruction parameters to the map
        for (Parameter p : instruction_parameters) {
            parameter_map.put(p.getKey(), p.getValue());
        }

        // Add condition parameters to the map, overriding existing ones
        for (Parameter p : condition_parameters) {
            if (!ignoreList.shouldIgnore(action, p.getKey())) {
                parameter_map.put(p.getKey(), p.getValue());
            }

        }

        return parameter_map;
    }

    // Convert to map from list for faster access
    private Map<String, String> makeParameterMap(ArrayList<Parameter> instruction_parameters){
        Map<String, String> parameter_map = new HashMap<>();

        // Add instruction parameters to the map
        for (Parameter p : instruction_parameters) {
            parameter_map.put(p.getKey(), p.getValue());
        }

        return parameter_map;
    }

}
