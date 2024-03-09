package ast;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@RequiredArgsConstructor
public class ExecuteInstruction extends Statement {
    private String instructionName;

    public ExecuteInstruction(String instructionName) {
        this.instructionName = instructionName;
    }

    @Setter(AccessLevel.NONE)
    private ArrayList<Parameter> parameters = new ArrayList<>();

    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {
        return v.visit(context, this);
    }
}
