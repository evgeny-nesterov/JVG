package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.nodes.NodePackage;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;

import java.util.ArrayList;
import java.util.List;

import static ru.nest.hiscript.tokenizer.WordType.NOT_SERVICE;
import static ru.nest.hiscript.tokenizer.WordType.PACKAGE;

public class PackageParseRule extends ParseRule<NodePackage> {
	private final static PackageParseRule instance = new PackageParseRule();

	public static PackageParseRule getInstance() {
		return instance;
	}

	private PackageParseRule() {
	}

	@Override
	public NodePackage visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWordType(tokenizer, PACKAGE) != null) {
			List<WordToken> path = new ArrayList<>();
			WordToken nameToken;
			if ((nameToken = visitWordToken(NOT_SERVICE, tokenizer)) != null) {
				path.add(nameToken);
				while (visitSymbol(tokenizer, SymbolType.POINT) != null) {
					nameToken = expectWordToken(NOT_SERVICE, tokenizer);
					if (nameToken != null) {
						path.add(nameToken);
					} else {
						break;
					}
				}
			}
			if (path.size() > 0) {
				return new NodePackage(path.toArray(new WordToken[path.size()]), tokenizer.getBlockToken(startToken));
			} else {
				tokenizer.error("package with not empty path expected");
			}
		}
		return null;
	}
}
