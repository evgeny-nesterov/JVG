package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;

public class AnnotatedModifiers extends ParserUtil {
	private NodeAnnotation[] annotations;

	private Modifiers modifiers;

	private boolean hasModifiers;

	public AnnotatedModifiers(NodeAnnotation[] annotations, Modifiers modifiers) {
		this.annotations = annotations;
		this.modifiers = modifiers;
		this.hasModifiers = modifiers != null;
	}

	public NodeAnnotation[] getAnnotations() {
		return annotations;
	}

	public boolean hasModifiers() {
		return hasModifiers;
	}

	public Modifiers getModifiers() {
		if (modifiers == null) {
			modifiers = new Modifiers();
		}
		return modifiers;
	}
}
