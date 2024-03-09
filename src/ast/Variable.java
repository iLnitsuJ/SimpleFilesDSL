package ast;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Variable extends Statement{
    private String key;
    private String value;

    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {
        return v.visit(context, this);
    }
}
