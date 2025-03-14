package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.HiCompiler;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.ClassLocationType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodePackage;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.List;

public class ClassFileParseRule extends ParseRule<HiNode> {
	private final static ClassFileParseRule instance = new ClassFileParseRule();

	public static ClassFileParseRule getInstance() {
		return instance;
	}

	private ClassFileParseRule() {
	}

	@Override
	public HiNode visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		HiCompiler compiler = ctx != null ? ctx.getCompiler() : new HiCompiler(ctx.getClassLoader(), tokenizer);
		visit(tokenizer, compiler);
		return null;
	}

	public List<HiClass> visit(Tokenizer tokenizer, HiCompiler compiler) throws TokenizerException, HiScriptParseException {
		tokenizer.nextToken();

		NodePackage packageNode = PackageParseRule.getInstance().visit(tokenizer, null);
		String[] packagePath = packageNode != null ? packageNode.getPath() : null;

		List<HiClass> classes = new ArrayList<>();
		while (true) {
			HiClass clazz = ClassParseRule.getInstance().visit(tokenizer, getContext(compiler));
			if (clazz != null) {
				clazz.packagePath = packagePath;
				classes.add(clazz);
				continue;
			}

			HiClass enumClass = EnumParseRule.getInstance().visit(tokenizer, getContext(compiler));
			if (enumClass != null) {
				enumClass.packagePath = packagePath;
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
			tokenizer.error("unexpected token");
		}
		return classes;
	}

	private CompileClassContext getContext(HiCompiler compiler) {
		return new CompileClassContext(compiler, null, null, ClassLocationType.top);
	}
}
