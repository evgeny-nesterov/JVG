package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
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
	public Node visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.nextToken();
		while (true) {
			ClassParseRule.getInstance().skipComments(tokenizer);

			HiClass clazz = ClassParseRule.getInstance().visit(tokenizer, new CompileContext(tokenizer, null, null, HiClass.CLASS_TYPE_TOP));
			if (clazz != null) {
				continue;
			}

			HiClass interfac = InterfaceParseRule.getInstance().visit(tokenizer, new CompileContext(tokenizer, null, null, HiClass.CLASS_TYPE_TOP));
			if (interfac != null) {
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
