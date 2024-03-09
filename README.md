# SimpleFilesDSL 

## 1.0 SimpleFiles Background

Introducing SimpleFiles, a cutting-edge Domain-Specific Language meticulously crafted for seamless and efficient file and folder manipulation. Tailored to meet the unique needs of developers, system administrators, and automation enthusiasts, SimpleFiles empowers users to navigate, organize, and transform file systems with unparalleled ease.

Key features of SimpleFiles include creating files, renaming files, and grouping files, enabling users to execute tasks with precision and flexibility. The language is built to enhance readability, reducing the learning curve and allowing users to quickly grasp its functionality.

Embark on a journey of efficiency and simplicity with SimpleFiles. Revolutionize your file and folder manipulation experience, and discover a new realm of possibilities in automation and organization. SimpleFiles – where command meets clarity for unparalleled file system control.

Demo video link for the project: https://drive.google.com/file/d/1hu8D5VPYH-yW7MrFvKROKu0Dbko2nzQA/view?usp=drive_link

## 2.0 Getting Started

The sections below act as the reference documentation for the SimpleFiles language. For a quick start, feel free to take a look at the examples in section _3.0.1_, as well as the additional test cases in the repository in the `testcases` folder. 

Once you have the text file written in SimpleFilesDSL, you can replace the file path at `src\index\Main.java` at line 22 with your own script file location. Alternatively, you can use the user interface by running main from `\src\ui\UI.java`. 

The UI has four buttons on top that generates template code for the action, run button to execute the script and bottom window displays if the instruction is execute correction. Since our UI was not in original plan and is coded up with very little time before project ends, error messages are not integrated to here, hence the display may not show correct error messages. Please refer to the console output for accurate error from executing the scripts.

### 2.0.1 Atomic Actions

SimpleFiles can perform actions related to file and folder manipulation. These actions can be embedded in an instruction component via specific keywords as described in the table below. Note that each action is considered atomic; it is the smallest "thing" that this language can do. It is possible to chain atomic actions.

The description of the action is a high level explanation on what the action will do. For specific syntax information and how to call it, refer to its respective sub-section.

| Action | Keyword | Description |
|-----------------|-----------------|-----------------|
| Create folders    | `:create_folder` | Call to create folder(s). Can create any number of folders so long the OS allows. Includes options to dynamically name folders.  |
| Create files    | `:create_file`    | Call to create file(s). A template file must be supplied and that template file is copied the specified number of time. Includes options to dynamically name files.   |
| Group files    | `:group`  | Call to group file(s) or folder(s) in a directory under a new folder. |
| Rename files    | `:rename`  | Call to rename file(s) or folder(s) in a directory to follow specified rules. |

### 2.0.2 How to use the DSL?

DSL language may be written down in any plain text editor. Recommended plain text editors would be NotePad++ for Window, Notepadqq for Linux, and CotEditor for Mac.

To start, an **instruction set** must be enclosed with `BEGIN` and `END`. Each component must end with a semicolon. 

This DSL does not have automated recovery. If the first component passes, but the second component fails, the changes that occurred with the first component will not be reverted.

```
BEGIN

    component 1;
    component 2;
    component 3;
    ... etc
    
END

```


### 2.0.2.1 Components in instruction set.

Component is the basic block to use the DSL; for now there are total of 3 components

| Component | Keyword | Description |
|-----------------|-----------------|-----------------|
| Instruction | `INST` | Used to start the declaration of an instruction component. |
| Condition | `COND` | Used to start combine with instruction and be used by execution component. |
| Execution | `EXEC` | Used to perform specified instruction component. |

Noted that the order of component is important. Component are performed starting from the top to bottom in the editor.

> **IMPORTANT**: All names for components must be unique within the instruction set.

> **IMPORTANT**: Execution component must be placed after the referencing components.
> 
> The following is an _**incorrect**_ demonstration of execution placement that reference an undefined instruction: 
```
EXEC abc;

INST abc -> action..
--> param: .... etc;

```


### 2.0.3 Instruction

Instruction component is used to start the declaration of an instruction component. Below is the basic layout of an instruction declaration template.
1. Start with `INST`
2. `<instruction_name>` must begin with at least one alphanumeric character and may contain hyphen(s) (`-`) and/or underscore(s) (`_`). No other special characters are allowed for names.
It is used to be referred by other component.
3. `<action_keyword>` is any action keywords listed in 2.0.1 and more detail in 2.1. Follow by `->` to denote action to be use in the instruction
4. Parameter type are followed by `-->` and specified belonging value after `=` inside `"`
5. End the component declaration with `;` 
```
BEGIN
    
    INST <component_name> -> <action_keyword>
    --> <param> = "<param_value>"
 
    ...<parameters required by the action is purposely omitted>... ;
    
END
```

### 2.0.4 Condition

The following is basic layout of a condition component declaration in similar style. This serves as a general way to express parameter which can later be combine with instruction and execute using `EXEC_COND_MAP`. User can specify list of parameters for multiple different actions and combine with the same condition.


```
BEGIN
    
    COND <component_name> -> :condition
    --> <param> = "<param_value>"
 
        ...<parameters purposely omitted>...;
    
END
```

> **IMPORTANT**: 
> In case where same kind parameter are declared by both condition and target instruction, program will always choose condition's parameter value. 
> When a parameter specified is unusalble by the action, it will be ignored.

Below are parameter ignore list for each action:

* `Create_file`
    * extension
    * group_target
    * mode
    * type
    * contains
    * regex
    * modified_date
    * size
    * create_folder
* `group`
    * template_path
    * name
    * name_file
    * count
* `rename`
    * template_path
    * name
    * name_file
    * count
    * group_target

Noted: Mode parameter will choose the default value if detected unusable value by the target action.
 


### 2.0.5 Execution

User can perform instruction through the keyword 'EXEC', follow by `_INST` or `_COND_MAP` and instruction name to execute the instruction of matched name.


The following is basic layout of an execution component;
```
BEGIN
    EXEC<optional keyword> <instruction_component_name>;
END
```
layout for conditional instruction execution.

```
BEGIN
    EXEC<optional keyword> <instruction_component_name> WITH_COND <condition_component_name> ;
END
```

### 2.0.5.1 Execution optional keyword

Execution can be further express through provided keywords below, otherwise will be treated as default instruction execution.

List of execution expressive keyword are appended following the `EXEC` keyword in an execution component. Options are:

| Execution option | Keyword | Description |
|-----------------|-----------------|-----------------|
| Execute instruction  | `_INST` | Used to start the declaration of an instruction component. |
| Execute based on condition | `_COND_MAP` | Used to start combine with instruction and be used by execution component. This option requires additional parameter that specifies the target condition component. |




## 2.1 Action Syntax

This subsection describes the parameters for all existing actions.

### 2.1.1 `:create_folder`

All fields of `:create_folder` is described below.

> **NOTE**: Parameters for instructions are always in lowercase.

```
BEGIN


INST create_folder_syntax -> :create_folder
--> path = "  "
--> name = "  "
--> count = "  ";


END
```

Required parameters:
* `name`: The name of the folder(s) to be created. If not specified, then `name_file` must be specified. To dynamically name folders, see 2.2.
* `path`:Where the folder(s) are created. Can be an absolute path or relative path. If not specified, the folders(s) will be placed where the instruction set is located.

Optional parameters:
* `count`: The maximum number of folder(s) to make. Default to 1 if not specified.

### 2.1.2 `:create_file`

All fields of `:create_file` is described below.

```
BEGIN


INST create_file_syntax -> :create_file
--> name = "  "
--> name_file = "  "
--> path = "  "
--> count = "  " ;

    
    
END
```

Required parameters:
* `name`: The name of the folder(s) to be created. If not specified, then `name_file` must be specified. To dynamically name files, see 2.2.
* `path`: Destination path for the file(s) created. Can be an absolute path or relative path. If not specified, the files(s) will be placed where the instruction set is located.

Optional parameters:
* `count`: The maximum number of folder(s) to make. If not specified, will make the maximum number of files(s) possible.

### 2.1.3 `:group`

All fields of `:group` is described below. The action will move/copy group target to a specified path based on the mode declared. To specify the targets, user can pass a folder path or an instruction name.
Passing folder path means the group action will target all stuff in the folder. Passing instruction name means the group action will target
all affected files by the specified instruction. Additional filtering can be specify through the optional parameters. 

```

BEGIN


INST group_syntax -> :group
--> group_target = "  "
--> path = "  "
--> mode = "  "
--> regex = "  "
--> contains = "  "
--> type = "  "
--> extension = "  "
--> modified_date = "  "
--> size = "  "
--> comparator = "  " ;


END

```

**Required parameters**
* `group_target`: This is what needs to be grouped. Maybe several valid things:
  * An absolute or relative path to a folder. Everything in the specified folder will be "grouped"
  * The name of an `:create_folder`, `:create_file`, or `:group` instruction. Everything created by the named instruction will be grouped.
* `path`: The folder where the grouped result will be located. Can be an absolute or relative path. If the folder does not exist, it will be created.
* `mode`:  Will either be `copy` or `move`. If `copy` is specified, then the files to be grouped are copied into the `path`. If it is `move`, then the files to be grouped are moved. Default to be `move` in case of unusable value.

Optional parameters:
* To specify how to group the folder(s) and/or file(s). The following parameters may be used.

  * `regex`: A regex pattern to match folder/file names
  * `contains`: A simpler, more user-friendly version of regex matching. Will find all folders/files containing the specified term
  * `extension`: Specify the file extension to group (i.e. `"png"`). Specify the file extension without the "dot".
  * `modified_date`: The date range of the files/folders. Some example values are as follows
    * `"after YYYY/MM/DD-HH:SS"`, anything after the specified date
    * `"before YYYY/MM/DD-HH:SS"`, anything before the specified date
    * `"between YYYY/MM/DD-HH:SS YYYY/MM/DD-HH:SS"`, anything in between the first date and second date.
  * `size`: The size of the file in KB.
  * `comparator`: If using size, either `GT` (greater than) or `LT` (less than). Otherwise, ignored.



### 2.1.4 `:rename`

All fields of `:rename` is described below. If no target requirement is specified, then all files under the specified folder will be targeted to follow the specified 
naming convention.


```

BEGIN


INST rename_syntax -> :rename
--> path = "  "
--> mode = "  "
--> regex = "  "
--> contains = "  "
--> type = "  "
--> extension = "  "
--> modified_date = "  "
--> size = "  "
--> comparator = "  "
--> recursive = "  " ;


END

```
**Required parameters**


* `path`: This is the rename target folder path. Maybe several valid things:
  * An absolute or relative path to a folder. Everything in the specified folder will be "rename" unless optional target parameters are provided.
  * The name of an `:create_folder`, `:create_file`, `:group`, or `:rename` instruction. Everything effected by the named instruction will be renamed.
* `mode`: Either `upper_case` or `lower_case`. Default to `lower_case` in case of unusable value.

**Optional parameters**
* To specify target(s) to rename instead of everything under the folder, the following optional parameters may be used.
  * `regex`: A regex pattern to match folder/file names
  * `contains`: A simpler, more user-friendly version of regex matching. Will target all folders/files containing the specified term
  * `type`: Either `"folder"` or `"file"`.
  * `extension`: Specify the file extension to group (i.e. `".png"`)
  * `modified_date`: The date range of the files/folders. Some example values are as follows
    * `"after YYYY/MM/DD-HH:SS"`, anything after the specified date
    * `"before YYYY/MM/DD-HH:SS"`, anything before the specified date
    * `"between YYYY/MM/DD YYYY/MM/DD-HH:SS"`, anything in between the first date and second date.
    * Note that we can be more specific and specify the hour and second as well with `YYYY/MM/DD=HH:SS`.
  * `size`: The size of the file in KB.
  * `comparator`: If using size, either `GT` (greater than) or `LT` (less than). Otherwise ignored.
  * `recursive`: Either `"true"` or `"false"`. True applies the renaming recursively on targets in the child folder as long as it satisfy optional parameters. Defaults to `"true"` if not specified.


 

## 2.2 Dynamic Constructs

It is possible to dynamically name the file(s) and folder(s) that we create. All dynamic constructs will be enclosed in the form `${...}`. By default, all dynamic constructs only return 15 values. To generate more files/folders, use the `count` parameter.

### 2.2.1 Numerical Iterators

Numerical iterator allows for numbering of folders/files. The starting integer and the iteration constants may be user specified. Some example configurations are:

* `${ITERATOR}`: This is the default configuration. The iteration will start at 0 and increase by 1 each time.
* `${ITERATOR:100}`: This sets the start value at 100. It will then increase by 1 each time.
* `${ITERATOR:50:-2}`: This sets the start value at 50 and will decrease by 2 each time. Iterators may return a negative value.

The following example demonstrates how to use the numerical iterator in an instruction.


```
BEGIN


INST numerical_iterator_demo -> :create_folder
--> path = "/home/example/path"
--> name = "folder_${ITERATOR:10000:5}"
--> count = "1000";

END
```

This instruction will create folders at `/home/example/path`. The count is specified as 1000, so 1000 folders will be created. The name of the folders will be `folder_10000`, `folder_10005`, `folder_10010`, etc.

## 3.0 Examples

### 3.1 Example 1

```
BEGIN

    INST ex_create_some_folders -> :create_folder
    --> path = "/some/path"
    --> name = "hello_folder_${ITERATOR}"
    --> count = "30";
    
    INST ex_create_some_files -> :create_file
    --> template_path = "/path/to/some/template.txt"
    --> path = "/path/to/where/we/want/to/store/files"
    --> name = "hello_file_$(RANDOM).txt"
    --> count = "70";
    
    INST group_one -> :group
    --> group_target = "ex_create_some_folders"
    --> mode = "copy"
    --> path = "/some/other/path/grouped_one"
    --> count = "2";

    INST group_two -> :group
    --> group_target = "ex_create_some_files"
    --> mode = "copy"
    --> path = "/some/other/path/grouped_two"
    --> contains = "7"
    --> type = "file";
    
    INST group_three -> :group
    --> group_target = "ex_create_some_files"
    --> mode = "move"
    --> path = "/some/other/path/grouped_three"
    --> contains = "0"
    --> type = "file";
    
    INST rename_group_three -> :rename
    --> group_target = "group_three"
    --> mode = "camel_case";
    
    INST rename_some_to_uppercase -> :rename
    --> group_target = "rename_group_three"
    --> mode = "upper_case"
    --> contains = "cpsc"
    --> type: "file"
    
END
```

The following instructions was executed:

1. `ex_create_some_folders` instruction created 30 folders with the name `hello_folder_<number>` to `/some/path`.
2. `ex_create_some_files` instruction created 70 files based on the template folder named `hello_file_<random string>.txt` to `/path/to/where/we/want/to/store/files`.
3. `group_one` instruction grouped all the folders that was created by the `ex_create_some_folders` instruction which had "2" in them, made a copy of the folders (and its contents) into a new folder `/some/other/path/grouped_one`.
4. `group_two` instruction grouped all the files that was created by the `ex_create_some_files` instruction which had "7" in them and was a file type, made a copy of the files into a new folder `/some/other/path/grouped_two`.
5. `group_three` instruction grouped all the files that was grouped by the `ex_create_some_files` instruction which had "0" in them and was a file type, and moved the files into a new folder `/some/other/path/grouped_three`.
6. `rename_group_three` instruction renamed all the files that was grouped by the `group_three` instruction, which are files that had "0" in them and was a file type and were moved to a new folder '/some/other/path/grouped_three' , to follow camel case naming style.
7. `rename_some_to_uppercase` instruction renamed all the files that were renamed by the `rename_group_three` instruction, and contains "cpsc" in its file name to follow upper case naming style.

### Example 3.2 Basic File Creation

This instruction will create 30 files.


```
BEGIN

INST create_some_folders_inst -> :create_folder
--> path = "/some/path"
--> name = "hello_folder_${ITERATOR}"
--> count = "30";

EXEC_INST create_some_files_inst;

END
```

### Example 3.3 Conditionals

This following sets up a conditional. 

In the working directory of `/some/starting/folder`, all the `.txt` files larger than `300MB` will be moved to `/some/ending/folder1`. Then all remaining `.txt` files larger than `150MB` will be copied to `some/ending/folder2`. Finally, everything in the working directory will be deleted.

```
BEGIN

COND txt_files_larger_than_300MB -> :condition
--> type = "file"
--> extension = ".txt"
--> size = "300"
--> comparator = "GT";

COND txt_files_larger_than_150MB -> :condition
--> type = "file"
--> extension = ".txt"
--> size = "150"
--> comparator = "GT";

INST move_files -> :move
--> from_path = "/some/starting/folder"
--> to_path = "/some/ending/folder1";

INST copy_files -> :copy
--> from_path = "/some/starting/folder"
--> to_path = "/some/ending/folder2";

INS delete_files -> :delete
--> path = "/some/starting/folder"
--> force = "true";

EXEC_COND_MAP move_files WITH_COND txt_files_larger_than_300MB;
EXEC_COND_MAP copy_files WITH_COND txt_files_larger_than_150MB;
EXEC_INST delete_files;

END
```
In pseudocode of a "normal" coding language, the above instruction would look something like the following:

```
OS os; // operating system
os.set_working_dir("/some/starting/folder");

files = os.get_all_files(); // get all files on the directory

for (file in files) {
  if (file.type == "txt") {
    if (file.size > 300 MB) {
      os.move_file(file, "/some/ending/folder1");
    } else if (file.size > 150 MB) {
      os.copy_file(file, "/some/ending/folder2");
    }
  }
}

os.delete_files("/some/starting/folder", "txt");
```

### Example 3.4 Single Variables and Conditions

The following shows variable usage and how we can transform conditionals into function-like conditional.

```
BEGIN

COND txt_files_larger_than_SIZE -> :condition
--> type = "file"
--> extension = "txt"
--> size = "${SIZE}"
--> comparator = "GT";

INST move_files -> :move
--> from_path = "/some/starting/folder"
--> to_path = "/some/ending/folder1";

VAR SIZE = "300";
EXEC_COND_MAP move_files WITH_COND txt_files_larger_than_SIZE;

VAR SIZE = "150";
EXEC_COND_MAP move_files WITH_COND txt_files_larger_than_SIZE;

END
```

The conditional `txt_files_larger_than_SIZE` defines a condition for `txt` files larger than some declared `SIZE`. Right before the first `EXEC_COND_MAP` the size is declared to be 300 (MB). Thus, the first `EXEC_COND_MAP` moves the text files larger than 300 KB. The second `EXEC_COND_MAP` will only move text files larger than 150 KB.

> **NOTE**: Since only an `EXEC` type instruction will "do something", we can make reference to variables that do not exist yet. As long as those variables are declared prior to being used in an execution, the program will still run. 

> **NOTE**: Static check done at compile time will not throw an error if a variable is referenced before use. 

### Example 3+, Many, many more examples

Included in the folder `./testcases` are many more examples of the SimpleFiles DSL in action. To play around with them, simply adjust the file path in [Main.java](https://github.students.cs.ubc.ca/CPSC410-2023W-T2/Group1Project1/blob/main/src/index/Main.java) to point to the test case you wish to run.

By default, any resultant folders/files will be created in `./testoutputs/<testcase name>`. 

Also included is `./testRefDirectory` which is a subdirectory containing various file of different types and sizes, as well as nested folders. This directory can be helpful when playing around with the conditions. 


## 4.0 Additional work to do if we had more time

### 4.1 Transactional Instruction Sets

Originally, it was our intent that a single instruction set is equivalent to a single transaction. This means that all instructions specified in the instruction set must be completed successfully, or else no instruction will complete. In other words, if any of the instructions fail, the original state of the file system will remain unchanged. 

However, as we started to work on this more, we realized how difficult it was to keep track of state changes and revert them in case of failures. As such, we abandoned this idea in the interest of time.

### 4.2 A more robust way to create files/folders

Right now, to create multiple files or folders, we are limited by iterator construct and cannot create them with custom names. Additionally, there may be a need to dynamically create a number of subdirectories within a directory (which we do not support currently).

### 4.3 A more robust way to rename files

Files can only be renamed to upper case and lower case. Given more time, we would have like to give users more control over how they want to rename the files.

### 4.4 Random strings

We originally wanted to support a construct to generate random strings as names in folder or file. These strings were to be alphanumerical with no special characters like so:

* `${RANDOM}`: This will generate a random string with 5 characters (default behaviour).
* `${RANDOM:10}`: This will generate a random string with 10 characters.

However, time constraints forced us to abandon this idea.
