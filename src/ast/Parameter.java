package ast;

import evaluator.Memory;
import libs.Node;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static evaluator.Memory.isVariableReference;


@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Parameter extends Node {
    private String key;
    private String value;

    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {
        return v.visit(context, this);
    }

    public String getValue() {
        if (isVariableReference(value)) {
            return Memory.getInstance().unwrapAndGetVariableValue(value);
        }
        return value;
    }

    public String getRawValue() {
        return value;
    }
}
