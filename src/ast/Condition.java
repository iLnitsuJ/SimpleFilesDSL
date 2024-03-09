package ast;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@RequiredArgsConstructor
public class Condition extends Statement {
    private final String name;
    private Action action;

    public Condition(String name, Action action) {
        this.name = name;
        this.action = action;
    }
    @Setter(AccessLevel.NONE)
    private ArrayList<Parameter> parameters = new ArrayList<>();

    public void addParameter(Parameter p) {
        this.parameters.add(p);
    }

    public void addParameter(String key, String value) {
        this.addParameter(new Parameter(key, value));
    }
    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {
        return v.visit(context, this);

    }
}
