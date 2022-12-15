package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class ClassFileParseRule extends ParseRule<Node> {
	private final static ClassFileParseRule instance = new ClassFileParseRule();

	public static ClassFileParseRule getInstance() {
		return instance;
	}

	private ClassFileParseRule() {
	}

	@Override
	public Node visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		HiCompiler compiler = ctx != null ? ctx.getCompiler() : new HiCompiler(tokenizer);

		tokenizer.nextToken();
		while (true) {
			ClassParseRule.getInstance().skipComments(tokenizer);

			HiClass clazz = ClassParseRule.getInstance().visit(tokenizer, new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP));
			if (clazz != null) {
				continue;
			}

			HiClass interfaceClass = InterfaceParseRule.getInstance().visit(tokenizer, new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP));
			if (interfaceClass != null) {
				continue;
			}

			HiClass enumClass = EnumParseRule.getInstance().visit(tokenizer, new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP));
			if (enumClass != null) {
				continue;
			}

			HiClass recordClass = RecordParseRule.getInstance().visit(tokenizer, new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP));
			if (recordClass != null) {
				continue;
			}
			break;
		}

		if (tokenizer.hasNext()) {
			throw new ParseException("unexpected token", tokenizer.currentToken());
		}
		return null;
	}
}
