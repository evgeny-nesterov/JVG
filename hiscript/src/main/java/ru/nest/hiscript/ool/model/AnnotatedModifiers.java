package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.tokenizer.Token;

public class AnnotatedModifiers extends ParserUtil {
	private final NodeAnnotation[] annotations;

	private Modifiers modifiers;

	private final Token token;

	public AnnotatedModifiers(NodeAnnotation[] annotations, Modifiers modifiers, Token token) {
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.token = token;
	}

	public NodeAnnotation[] getAnnotations() {
		return annotations;
	}

	public Modifiers getModifiers() {
		if (modifiers == null) {
			modifiers = new Modifiers();
		}
		return modifiers;
	}

	public Token getToken() {
		return token;
	}
}
