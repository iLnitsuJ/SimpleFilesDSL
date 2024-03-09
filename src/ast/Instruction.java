package ast;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@RequiredArgsConstructor
public class Instruction extends Statement {
    private final String name;
    private Action action;


    public Instruction(String name, Action action) {
        this.name = name;
        this.action = action;

    }
    @Setter(AccessLevel.NONE)
    private ArrayList<Parameter> parameters = new ArrayList<>();


    public void addParameter(Parameter p) {
        parameters.add(p);
    }

    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {
        return v.visit(context, this);
    }
}
