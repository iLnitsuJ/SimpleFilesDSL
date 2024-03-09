parser grammar SimpleFilesParser;
options { tokenVocab=SimpleFilesLexer; }

// Grammar
program: INST_SET_START
         body*
         INST_SET_END
         EOF;

body: instruction | condition | execution | variable;

// Variable definition
variable: KEY_VARIABLE
          PARAM_KEY
          SINGLE_EQUAL
          PARAM_VAL
          LINE_END;

// Instruction definition
instruction: KEY_INSTRUCTION
             TEXT
             set_action
             set_param*
             LINE_END;

set_action: SINGLE_ARROW ACTION;
set_param: DOUBLE_ARROW PARAM_KEY SINGLE_EQUAL PARAM_VAL;

// Condition definition
condition: KEY_CONDITION
           TEXT
           set_action
           set_param*
           LINE_END;

// Execution definition
execution: exec_inst | exec_map | exec_cond_map;

exec_inst: EXEC_INST TEXT LINE_END;
exec_map: EXEC_MAP TEXT TEXT LINE_END;
exec_cond_map: EXEC_COND_MAP TEXT WITH_COND TEXT LINE_END;