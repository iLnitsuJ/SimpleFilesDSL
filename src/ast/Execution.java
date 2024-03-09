//package ast;
//
//import common.ExecutionType;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//
//import java.util.Optional;
//
//@Getter
//@Setter
//@RequiredArgsConstructor
//public class Execution {
//    private final ExecutionType type;
//    private final String instructionName;
//
//    private String conditionName;
//
//    // Override getter for conditionName because it may be null for EXEC_INST
//    public Optional<String> getConditionName() {
//        return Optional.ofNullable(conditionName);
//    }
//}
