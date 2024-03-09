lexer grammar SimpleFilesLexer;
// The begin and end of an instruction set
INST_SET_START: 'BEGIN';
INST_SET_END: 'END';
LINE_END: ';';

// Descriptors
KEY_CONDITION: 'COND' WS* -> mode(TEXT_MODE);
KEY_INSTRUCTION: 'INST' WS* -> mode(TEXT_MODE);
KEY_VARIABLE: 'VAR' WS* -> mode(PARAM_KEY_MODE);

// Action, paramter settings, punctuation
SINGLE_ARROW: '->' WS* -> mode(ACTION_MODE);
DOUBLE_ARROW: '-->' WS* -> mode(PARAM_KEY_MODE);
SINGLE_EQUAL: '=' WS* -> mode(PARAM_VAL_MODE);

// Execution Modes
EXEC_COND_MAP: 'EXEC_COND_MAP' WS* -> mode(TEXT_MODE);
WITH_COND: 'WITH_COND' WS* -> mode(TEXT_MODE);
EXEC_MAP: 'EXEC_MAP' WS* -> mode(TEXT_MODE);
EXEC_INST: 'EXEC_INST' WS* -> mode(TEXT_MODE);

// Line breaks are ignored during tokenization (note that this rule only applies in DEFAULT_MODE)
WS: [ \t\n\f\r] -> skip;

// Mode for tokenizing arbitrary names. All names are alphanumerical and the only symbol allowed is underscore.
mode TEXT_MODE;
TEXT: [a-zA-Z0-9_]+ -> mode(DEFAULT_MODE);

// Mode for tokenizing actions. All actions is in the form ":<name>"
mode ACTION_MODE;
ACTION: [a-zA-Z_:]+ -> mode(DEFAULT_MODE);

// Mode for getting the parameter key
mode PARAM_KEY_MODE;
PARAM_KEY: [a-zA-Z0-9_]+ -> mode(DEFAULT_MODE);

// Mode for setting the parameter value
mode PARAM_VAL_MODE;
PARAM_VAL: '"' [a-zA-Z0-9_\-:.&/\\;${} ]+ '"' -> mode(DEFAULT_MODE);

// Mode for list intake. Only supported delimiter is comma.
// Unused for now
//mode LIST_MODE;
//LIST: LIST_ITEM+ REST_OF_LIST? -> mode(DEFAULT_MODE);
//LIST_ITEM: ~[\t\n\f\r; ]+ WS*;
//REST_OF_LIST: ',' WS* LIST_ITEM WS*;
