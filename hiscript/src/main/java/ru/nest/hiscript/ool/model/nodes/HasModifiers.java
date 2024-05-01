package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Modifiers;

public interface HasModifiers {
	Modifiers getModifiers();

	default boolean hasModifiers() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.hasModifiers();
	}

	default boolean isAbstract() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isAbstract();
	}

	default boolean isStatic() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isStatic();
	}

	default boolean isFinal() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isFinal();
	}

	default boolean isDefault() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isDefault();
	}

	default boolean isPrivate() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isPrivate();
	}

	default boolean isProtected() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isProtected();
	}

	default boolean isDefaultAccess() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isDefaultAccess();
	}

	default boolean isPublic() {
		Modifiers modifiers = getModifiers();
		return modifiers != null && modifiers.isPublic();
	}
}
