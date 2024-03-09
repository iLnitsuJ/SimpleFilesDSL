package libs;

import ast.SimpleFilesVisitor;

public abstract class Node {
    abstract public <C,T> T accept(C context, SimpleFilesVisitor<C, T> v);
}
