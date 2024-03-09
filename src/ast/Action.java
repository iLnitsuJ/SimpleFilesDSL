package ast;

import lombok.Getter;

@Getter
public class Action extends Statement{
    private final String action;

    public Action(String action) {
        this.action = action;
    }

    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {
        return v.visit(context, this);
    }
}
