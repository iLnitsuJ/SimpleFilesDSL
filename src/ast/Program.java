package ast;

import libs.Node;
import lombok.Getter;

import java.util.List;

@Getter
public class Program extends Node {
    private final List<Statement> statements;
    public Program(List<Statement> statements) {
        this.statements = statements;
    }

    /*
     * Statements are one of:
     * - Instruction
     * - Condition
     * - Execution
     */

    @Override
    public <C, T> T accept(C context, SimpleFilesVisitor<C, T> v) {
        return v.visit(context, this);
    }
}
