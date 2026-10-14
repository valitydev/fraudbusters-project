package dev.vality.fraudo.visitor;

import org.antlr.v4.runtime.tree.ParseTree;

import java.io.Closeable;

public interface TemplateVisitor<T, R> extends Closeable {

    R visit(ParseTree tree, T model);

}
