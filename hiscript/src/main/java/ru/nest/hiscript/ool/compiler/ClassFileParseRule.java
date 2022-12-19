package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.List;

public class ClassFileParseRule extends ParseRule<Node> {
	private final static ClassFileParseRule instance = new ClassFileParseRule();

	public static ClassFileParseRule getInstance() {
		return instance;
	}

	private ClassFileParseRule() {
	}

	@Override
	public Node visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		HiCompiler compiler = ctx != null ? ctx.getCompiler() : new HiCompiler(ctx.getClassLoader(), tokenizer);
		visit(tokenizer, compiler);
		return null;
	}

	public List<HiClass> visit(Tokenizer tokenizer, HiCompiler compiler) throws TokenizerException, ParseException {
		tokenizer.nextToken();

		List<HiClass> classes = new ArrayList<>();
		while (true) {
			ClassParseRule.getInstance().skipComments(tokenizer);

			HiClass clazz = ClassParseRule.getInstance().visit(tokenizer, getContext(compiler));
			if (clazz != null) {
				classes.add(clazz);
				continue;
			}

			HiClass interfaceClass = InterfaceParseRule.getInstance().visit(tokenizer, getContext(compiler));
			if (interfaceClass != null) {
				classes.add(interfaceClass);
				continue;
			}

			HiClass enumClass = EnumParseRule.getInstance().visit(tokenizer, getContext(compiler));
			if (enumClass != null) {
				classes.add(enumClass);
				continue;
			}

			HiClass recordClass = RecordParseRule.getInstance().visit(tokenizer, getContext(compiler));
			if (recordClass != null) {
				classes.add(recordClass);
				continue;
			}
			break;
		}

		if (tokenizer.hasNext()) {
			throw new ParseException("unexpected token", tokenizer.currentToken());
		}
		return classes;
	}

	private CompileClassContext getContext(HiCompiler compiler) {
		return new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP);
	}
}
