package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

import java.util.ArrayList;
import java.util.List;

import static ru.nest.hiscript.tokenizer.WordType.*;

public class GenericsParseRule extends ParseRule<NodeGenerics> {
	private final static GenericsParseRule instance = new GenericsParseRule();

	public static GenericsParseRule getInstance() {
		return instance;
	}

	private GenericsParseRule() {
	}

	@Override
	public NodeGenerics visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitSymbol(tokenizer, SymbolType.LOWER) != null) {
			if (visitGreater(tokenizer, false)) {
				return new NodeGenerics(new NodeGeneric[0]);
			}

			List<NodeGeneric> generics = new ArrayList<>();
			int index = 0;
			do {
				startToken = startToken(tokenizer);
				String name;
				if (visitSymbol(tokenizer, SymbolType.QUESTION) != null) {
					name = null;
				} else {
					name = expectWords(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
				}

				boolean isSuper;
				Type type;
				WordType extendsType = visitWordType(tokenizer, EXTENDS, SUPER);
				if (extendsType != null) {
					isSuper = extendsType == SUPER;
					Token typeToken = startToken(tokenizer);
					type = visitType(tokenizer, false, ctx.getEnv());
					if (type == null) {
						tokenizer.error("identifier is expected", typeToken);
						type = Type.invalidType;
					} else if (type.isPrimitive() || type.isArray()) {
						tokenizer.error("invalid type", typeToken);
					}
				} else {
					isSuper = false;
					type = Type.objectType;
				}
				NodeGeneric generic = new NodeGeneric(name, isSuper, type, index++);
				generic.setToken(tokenizer.getBlockToken(startToken));
				generics.add(generic);
			} while (visitSymbol(tokenizer, SymbolType.COMMA) != null);

			visitGreater(tokenizer, true);
			return new NodeGenerics(generics.toArray(new NodeGeneric[generics.size()]));
		}
		return null;
	}
}
