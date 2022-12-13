package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.tokenizer.Tokenizer;

public interface ParseVisitor {
	boolean visit(Tokenizer tokenizer, CompileContext ctx);
}
