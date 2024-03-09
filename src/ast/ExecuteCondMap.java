package ast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecuteCondMap extends Statement{
    String instructionName;
    String conditionName;

    public ExecuteCondMap(String instructionName, String conditionName) {
        this.instructionName = instructionName;
        this.conditionName = conditionName;
    }
    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {

        return v.visit(context, this);
    }
}
