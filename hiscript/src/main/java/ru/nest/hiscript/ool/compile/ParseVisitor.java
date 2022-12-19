package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.tokenizer.Tokenizer;

public interface ParseVisitor {
	boolean visit(Tokenizer tokenizer, CompileClassContext ctx);
}
