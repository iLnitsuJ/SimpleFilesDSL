package ast;

public interface SimpleFilesVisitor<C, T>
{
        // Recall: one visit method per concrete AST node subclass
    T visit(C context, Condition c);
    T visit(C context, ExecuteInstruction e);
    T visit(C context, ExecuteCondMap e);
    T visit(C context, Instruction i);
    T visit(C context, Action a);
    T visit(C context, Parameter p);
    T visit(C context, Variable v);
    T visit(C context, Program program);
}
