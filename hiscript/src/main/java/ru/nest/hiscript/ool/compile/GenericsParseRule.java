package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

public class GenericsParseRule extends ParseRule<NodeGenerics> {
	private final static GenericsParseRule instance = new GenericsParseRule();

	public static GenericsParseRule getInstance() {
		return instance;
	}

	private GenericsParseRule() {
	}

	@Override
	public NodeGenerics visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitSymbol(tokenizer, Symbols.LOWER) != -1) {
			if (visitSymbol(tokenizer, Symbols.GREATER) != -1) {
				return new NodeGenerics(new NodeGeneric[0]);
			}

			List<NodeGeneric> generics = new ArrayList<>();
			do {
				startToken = startToken(tokenizer);
				String name;
				if (visitSymbol(tokenizer, Symbols.QUESTION) != -1) {
					name = null;
				} else {
					name = expectWord(Words.NOT_SERVICE, tokenizer);
				}

				boolean isSuper;
				Type type;
				int extendsType = visitWordType(tokenizer, Words.EXTENDS, Words.SUPER);
				if (extendsType != -1) {
					isSuper = extendsType == Words.SUPER;
					type = visitType(tokenizer, false);
					if (type == null) {
						tokenizer.error("identifier is expected");
						type = Type.invalidType;
					}
				} else {
					isSuper = false;
					type = Type.objectType;
				}
				NodeGeneric generic = new NodeGeneric(name, isSuper, type);
				generic.setToken(tokenizer.getBlockToken(startToken));
				generics.add(generic);
			} while (visitSymbol(tokenizer, Symbols.COMMA) != -1);

			expectSymbol(tokenizer, Symbols.GREATER);
			return new NodeGenerics(generics.toArray(new NodeGeneric[generics.size()]));
		}
		return null;
	}
}
