# Check-In 1

## Purpose and Target User
* Faster file organization, especially in managing large numbers of files. Target users would be non-technical users. It enables users to automate redundant procedures of file organization without manual clicks and drags.


## Features
1. Batch creates folders and files and enables users to dynamically name each file/folder. Instructions for creating files/folders can be named so that the files/folders created can be referred to in a subsequent instruction.
2. Grouping files or folders according to users’ rules(e.g. Size, name, creation or modification date, type) and perform actions on the grouped objects such as copy and move. This instruction can refer to the files/folders created in a previously named instruction.
3. Deleting files with the permission of user

* Features 2 and 3 provide an advanced file management system for users which enable them to manage, archive and delete files in a highly customized way. To enhance data security and prevent unauthorized deletions, the system will first move files into a separate folder. This step ensure users have the opportunity to review the deletion, prevent loss of important data.
* Example snippets are available in the [README](./README.md)

## TA Discussion Changes and Feedback
* Adding more details regarding what are and aren't keywords
* Unclear whether operators would work on the types that were given. E.g. there was addition of something that looked like a string. Should this work in the dsl?
* Allow statements to effect subsequent statements.

## Planned follow-up tasks / future features
* Check that these features/combinations above are rich enough features.
* Add a rename instructions that allows users to specify criteria for a subset of files/folders and rename them.

# Check-In 2
* Once a standard representation for the parse tree and abstract syntax tree is chosen, we can start working on each
  component in parallel. We will split writing unit tests so that everyone creates the tests for the component they are
  responsible for. For our DSL we have identified the following components:
  * **Tokenization/Parsing**: Transform strings into individual tokens (meaningful units in our DSL e.g.,
    keywords/operators/user-defined strings) and parse into parse tree.
  * **AST Conversion**: Transform parse tree into a form that shows the structure of the code.
  * **Static Checks**: Check for compile time errors (e.g. uninitialized variables, referencing non-existent variables,
    etc...)
  * **Evaluation**: Run the validated AST to produce results and produce runtime errors if they exist.

## Tokenization/Parsing
Tokenizing and parsing will be done by Henry once the general language features is decided on. This stage involves defining the grammar rules using Antlr and tokenizing inputs. The tokens will then be used to build the parse tree. Henry will work with Philip to converge on the general structure of the parse tree.

## AST Conversion
At this stage the input is the parse tree from the previous stage and the output is the abstract syntax tree. Phillips
will be responsible for converting the parse tree to the ast. The data available here is the parse tree and the invariant
over this is it must be valid with respect to our grammar. With the standard representation of the parse tree, Phillips can start
writing the code that transforms the parse tree into the abstract syntax tree and write the related tests.

## Static check
Junkai is in charge of this component. The primary function is to validate the AST object, which is a structured representation of the source code, transformed from the initial input. This AST object serves as the foundation for further processing and evaluation. Input is an AST object created by AST conversion and the output is a validate AST.

If there are syntactic or structural errors in the AST, the static check will identify them. The component will inform the user of any errors detected. This includes specifying the location and nature of the errors within the AST or the corresponding source code, facilitating easier debugging and correction.Integrating the Static Check with the Evaluation component ensures a robust and error-resistant system, enhancing the reliability and efficiency of the overall code generation and execution process.
## Evaluation
Justin and Tianyang will be responsible for this component. The input to evaluator will be a validate AST object and output final result based on user input. There might not be much invariants beside the AST structure at the interpretation stage, however basic template of a java file that we are building on will remain the same regardless of the AST content.

The evaluator can be build independently by first setting up tests with mockup AST objects and dummy files/folder to compare with actual result. The component will consist of interpreting the AST recursively, breaking down each node by creating snippet code using java's file class and associated functions, and knit them together to a file that can be compile and execute to achieve the final result.


## Timeline
* Our timeline for the project closely follows the structure layed out on canvas for the weekly check-ins:
  * Jan 26 -------------------Check-In 2------------------
  * Jan 26 - Decide what the language can, cannot do, and verify that the language has 2 rich features.
  * Jan 28 - Complete a mockup of the concrete language.
  * Jan 29 -  Write initial tests for each identified component.
  * Jan 30 - Complete user study. Identify errors, potential errors that user may encounter/encountered.
  * Feb 02 -------------------Check-In 3------------------
  * Feb 06 - Language and tests should be updated based on the results of the initial user study.
  * Feb 09 - Fix bugs
  * Feb 09 -------------------Check-In 4-------------------
  * Feb 10 - Complete final user study
  * Feb 11 - Finish end-to-end tests
  * Feb 16 - Fix bugs
  * Feb 16 -------------------Check-In 5-------------------
  * Feb 16 - Add final improvements to language.
  * Feb 18 - Complete plans for the video presentation
  * Feb 20 - Finish video presentation

## TA Discussion Changes and Feedback
* Discussed with TA about rich features of the language. TA mentioned that we don't need to be too concerned
  about whether our language features are rich enough since we can add those features in after, and it's more important
  to start on the tokenization/standard representation stuff.
* We have updated our language and have moved onto agreeing on standard representations
  so that we can work on each of the stages discussed above in parallel.

# Check-In 3
A mockup of the concrete language design as well as descriptions of the syntax and what is meant to happen when
evaluated can be found in the [README](./README.md)

## User Study Results
For our user study, the user was able to complete the task of: copy less than 5kb to a new folder called "foo".
However, the user found the syntax of the language confusing. It was unclear to them whether the`begin` and `end`
keywords were needed. They also didn't know what `GT`, `INST`, `COND`, `EXEC`, `force`, `ONESHOT`, `${ITERATOR}`, or
`MULTI` meant. They also found the `::` and  `-->` syntax confusing and wondered if they had a special function.
Lastly, they were unsure what the semicolons did. The study showed the mismatch between what we thought was easy to
understand because of our experiences and what a less technical user would understand. For the final user study, we
would like to make the documentation for our DSL more clear, so they know whether something is a keyword or a
variable name, and what the options are for the keywords at certain positions. We are considering simplifying the DSL
by removing the `::` and `->`, and changing some of the keywords such as `INST` and `COND` to something that more
easily understandable for non-technical users.

## TA Discussion Changes and Feedback
* Confirmed with TA about whether the conditional in our language fit the rich feature criteria. TA was pretty confident
  that it did and said he'll confirm.
* TA asked us some questions regarding the semantics of our language and wanted us to clarify syntax decision such as
  the use of ::.

# Check-In 4
### Implementation status
* The parser and lexer are mostly finished they just needed to be updated to account for variables.
* The conversion from the parse tree to the AST is also mostly finished it just needs to be updated to account for variables.
* The static check is almost finish, specific checks will be added accorading to tests and user staudies.
* The evaluator is still work in progress: 
    1. Create file/folder actions are working except dynamic construct not fully implemented
    2. Group action on files is working, group on folder is still WIP.
    3. Rename action is working.
    4. Dynamic checking and error display is still WIP, for now some error occured does not match with description.
    5. Chaining instruction is still WIP.
    6. Execution on condition map is still WIP as some interactions for condition against different actions is ambiguous.
    
* Tests are passing; however, we need to add more tests for what we currently have and add new tests for the variable feature.

### Second User Study Results
* The user was given the readme.md and tried to play around with all actions. User thought that the language is understandable and able to follow the example layout, but he does have a CS background. Overall, he thinks the language can be useful and below are some suggestions that were made:
    1. Found a bug on create_file/folder when using dynamic construct on name, specifically when start and increment index is provided.
    2. Condition may need more documentation as interaction between different actions like create_folder and rename is unclear. He wasnt sure what is being target and     compared
    3. Error display needs improvement. Sometimes the error shown does not reflect to actual error that prevent the instruction to execute.
    

### Planned timeline
* Our timeline remains the same as the one that was described in checkin-2. We plan to have things mostly finished by Feb 11, leaving us time for bug fixes.

# Check-In 5
### Status of user study (should be completed this week at the latest)
* The final user study was completed last week and the results were summarized in the last checkin. We have put the results here as well:
* The user was given the readme.md and tried to play around with all actions. User thought that the language is understandable and able to follow the example layout, but he does have a CS background. Overall, he thinks the language can be useful and below are some suggestions that were made:
  1. Found a bug on create_file/folder when using dynamic construct on name, specifically when start and increment index is provided.
  2. Condition may need more documentation as interaction between different actions like create_folder and rename is unclear. He wasnt sure what is being target and     compared
  3. Error display needs improvement. Sometimes the error shown does not reflect to actual error that prevent the instruction to execute.
### Are there any last changes to your design, implementation or tests?
* We plan to add functions as a language features, more descriptive errors, and update the documentation for the language. 
### Plans for final video (possible draft version).
* We will all participate in filming the video responsible for the video. We plan to film the video presentation over the break and
will split it so that everyone will present the aspect of the DSL they worked on. (Henry will present the tokenization/lexing stage,
Phillips will present the AST Conversion and first user study, Junkai will present the static checks, and Justin and TianYang will 
present the evaluation stage and the results of the final user study).

### Planned timeline for the remaining days.
* We plan to write more tests and finalize the addition of the variable feature. We also plan to add non-recursive
functions as a feature and will have to work on the parse tree, ast conversion, static evaluation, and evaluation stages
to support this. We also plan to film a draft video by Tuesday to see what parts needs to be shortened/elaborated/removed/added.

## TA Discussion Changes and Feedback
* Confirmed with TA about whether our language was rich enough. TA confirmed that the way our DSL implemented
conditionals and variables were rich features and wanted us to think about more rich features. We
looked over a feature that would have produce behavior that was comparable to a for loop
but TA didn't consider this a for loop in the language. 
