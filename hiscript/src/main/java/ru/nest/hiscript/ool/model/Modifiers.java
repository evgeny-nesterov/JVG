package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.io.IOException;

public class Modifiers implements ModifiersIF, Codeable {
	public final static Modifiers PUBLIC = new Modifiers(ACCESS_PUBLIC);

	public final static Modifiers PRIVATE = new Modifiers(ACCESS_PRIVATE);

	public final static Modifiers PUBLIC_FINAL = new Modifiers(ACCESS_PUBLIC | FINAL);

	public static int mapWordsToModification(int word) {
		switch (word) {
			case Words.PUBLIC:
				return ACCESS_PUBLIC;

			case Words.PROTECTED:
				return ACCESS_PROTECTED;

			case Words.PRIVATE:
				return ACCESS_PRIVATE;

			case Words.STATIC:
				return STATIC;

			case Words.FINAL:
				return FINAL;

			case Words.NATIVE:
				return NATIVE;

			case Words.ABSTRACT:
				return ABSTRACT;

			case Words.DEFAULT:
				return DEFAULT;

			case Words.SYNCHRONIZED:
				return SYNCHRONIZED;
		}
		return -1;
	}

	private int access = ACCESS_DEFAULT;

	private boolean isStatic = false;

	private boolean isFinal = false;

	private boolean isNative = false;

	private boolean isAbstract = false;

	private boolean isDefault = false;

	private boolean isSynchronized = false;

	public Modifiers() {
	}

	public Modifiers(int modifiers) {
		setModifiers(modifiers);
	}

	public Modifiers(Modifiers modifiers) {
		setModifiers(modifiers.getModifiers());
	}

	public Changeable change() {
		if (this instanceof Changeable) {
			return (Changeable) this;
		} else {
			return new Changeable(this);
		}
	}

	public boolean hasModifiers() {
		return access != ACCESS_DEFAULT || isFinal || isStatic || isAbstract || isNative || isDefault || isSynchronized;
	}

	public int getAccess() {
		return access;
	}

	public boolean isPublic() {
		return (access & ACCESS_PUBLIC) != 0;
	}

	public boolean isProtected() {
		return (access & ACCESS_PROTECTED) != 0;
	}

	public boolean isDefaultAccess() {
		return access == ACCESS_DEFAULT;
	}

	public boolean isPrivate() {
		return (access & ACCESS_PRIVATE) != 0;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public boolean isNative() {
		return isNative;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public boolean isSynchronized() {
		return isSynchronized;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isPublic()) {
			sb.append("public ");
		} else if (isProtected()) {
			sb.append("protected ");
		} else if (isPrivate()) {
			sb.append("private ");
		}
		if (isFinal) {
			sb.append("final ");
		}
		if (isStatic) {
			sb.append("static ");
		}
		if (isAbstract) {
			sb.append("abstract ");
		}
		if (isNative) {
			sb.append("native ");
		}
		if (isDefault) {
			sb.append("default ");
		}
		if (isSynchronized) {
			sb.append("synchronized ");
		}
		return sb.toString();
	}

	public static String getName(int modifier) {
		switch (modifier) {
			case ACCESS_PUBLIC:
				return "public";
			case ACCESS_PROTECTED:
				return "protected";
			case ACCESS_PRIVATE:
				return "private";
			case ACCESS_DEFAULT:
				return "";
			case FINAL:
				return "final";
			case STATIC:
				return "static";
			case ABSTRACT:
				return "abstract";
			case NATIVE:
				return "native";
			case DEFAULT:
				return "default";
			case SYNCHRONIZED:
				return "synchronized";
		}
		return null;
	}

	public int getModifiers() {
		int modifiers = access;
		if (isStatic) {
			modifiers |= STATIC;
		}

		if (isFinal) {
			modifiers |= FINAL;
		}

		if (isNative) {
			modifiers |= NATIVE;
		}

		if (isAbstract) {
			modifiers |= ABSTRACT;
		}

		if (isDefault) {
			modifiers |= DEFAULT;
		}

		if (isSynchronized) {
			modifiers |= SYNCHRONIZED;
		}
		return modifiers;
	}

	private void setModifiers(int code) {
		isStatic = (code & STATIC) != 0;
		isFinal = (code & FINAL) != 0;
		isNative = (code & NATIVE) != 0;
		isAbstract = (code & ABSTRACT) != 0;
		isDefault = (code & DEFAULT) != 0;
		isSynchronized = (code & SYNCHRONIZED) != 0;

		if ((code & ACCESS_PUBLIC) != 0) {
			access = ACCESS_PUBLIC;
		} else if ((code & ACCESS_PROTECTED) != 0) {
			access = ACCESS_PROTECTED;
		} else if ((code & ACCESS_PRIVATE) != 0) {
			access = ACCESS_PRIVATE;
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeShort(getModifiers());
	}

	public static Modifiers decode(DecodeContext os) throws IOException {
		Modifiers modifiers = new Modifiers();
		modifiers.setModifiers(os.readShort());
		return modifiers;
	}

	public boolean check(Tokenizer tokenizer, Token modifiersToken, int... allowed) throws TokenizerException {
		int allowedMask = ACCESS_DEFAULT;
		for (int word : allowed) {
			allowedMask |= mapWordsToModification(word);
		}

		boolean valid = true;
		if ((allowedMask & access) == 0) {
			Token token = getToken(tokenizer, access, modifiersToken);
			tokenizer.error("modifier '" + getName(access) + "' is not allowed", token);
			valid = false;
		}

		if (isFinal && (allowedMask & FINAL) == 0) {
			Token token = getToken(tokenizer, FINAL, modifiersToken);
			tokenizer.error("modifier '" + getName(FINAL) + "' is not allowed", token);
			valid = false;
		}

		if (isStatic && (allowedMask & STATIC) == 0) {
			Token token = getToken(tokenizer, STATIC, modifiersToken);
			tokenizer.error("modifier '" + getName(STATIC) + "' is not allowed", token);
			valid = false;
		}

		if (isAbstract && (allowedMask & ABSTRACT) == 0) {
			Token token = getToken(tokenizer, ABSTRACT, modifiersToken);
			tokenizer.error("modifier '" + getName(ABSTRACT) + "' is not allowed", token);
			valid = false;
		}

		if (isNative && (allowedMask & NATIVE) == 0) {
			Token token = getToken(tokenizer, NATIVE, modifiersToken);
			tokenizer.error("modifier '" + getName(NATIVE) + "' is not allowed", token);
			valid = false;
		}

		if (isDefault && (allowedMask & DEFAULT) == 0) {
			Token token = getToken(tokenizer, DEFAULT, modifiersToken);
			tokenizer.error("modifier '" + getName(DEFAULT) + "' is not allowed", token);
			valid = false;
		}

		if (isSynchronized && (allowedMask & SYNCHRONIZED) == 0) {
			Token token = getToken(tokenizer, SYNCHRONIZED, modifiersToken);
			tokenizer.error("modifier '" + getName(SYNCHRONIZED) + "' is not allowed", token);
			valid = false;
		}
		return valid;
	}

	public int check(int... allowed) {
		int allowedMask = ACCESS_DEFAULT;
		for (int word : allowed) {
			allowedMask |= mapWordsToModification(word);
		}

		if ((allowedMask & access) == 0) {
			return access;
		}

		if (isFinal && (allowedMask & FINAL) == 0) {
			return FINAL;
		}

		if (isStatic && (allowedMask & STATIC) == 0) {
			return STATIC;
		}

		if (isAbstract && (allowedMask & ABSTRACT) == 0) {
			return ABSTRACT;
		}

		if (isNative && (allowedMask & NATIVE) == 0) {
			return NATIVE;
		}

		if (isDefault && (allowedMask & DEFAULT) == 0) {
			return DEFAULT;
		}

		if (isSynchronized && (allowedMask & SYNCHRONIZED) == 0) {
			return SYNCHRONIZED;
		}
		return -1;
	}

	public static Token getToken(Tokenizer tokenizer, int modifier, Token modifiersToken) {
		if (modifiersToken == null) {
			return null;
		}
		String modifierName = getName(modifier);
		return modifiersToken.getInnerToken(tokenizer, modifierName);
	}

	public boolean validateRewriteAccess(Modifiers rewrittenModifiers, ValidationInfo validationInfo, Token token) {
		if (rewrittenModifiers.isPublic()) {
			if (!isPublic()) {
				validationInfo.error("attempting to assign weaker access privileges: was public", token);
				return false;
			}
		} else if (rewrittenModifiers.isProtected()) {
			if (!isPublic() && !isProtected()) {
				validationInfo.error("attempting to assign weaker access privileges: was protected", token);
				return false;
			}
		} else if (rewrittenModifiers.isDefaultAccess()) {
			if (!isPublic() && !isProtected() && !isDefaultAccess()) {
				validationInfo.error("attempting to assign weaker access privileges: was packageLocal", token);
				return false;
			}
		}
		return true;
	}

	public static class Changeable extends Modifiers {
		public Changeable() {
			super();
		}

		public Changeable(int modifiers) {
			super(modifiers);
		}

		public Changeable(Modifiers modifiers) {
			super(modifiers);
		}

		public Changeable setAccess(int access) {
			assert access == ACCESS_PUBLIC || access == ACCESS_PRIVATE || access == ACCESS_DEFAULT || access == ACCESS_PROTECTED;
			super.access = access;
			return this;
		}

		public Changeable setPublic() {
			super.access = ACCESS_PUBLIC;
			return this;
		}

		public Changeable setProtected() {
			super.access = ACCESS_PROTECTED;
			return this;
		}

		public Changeable setPrivate() {
			super.access = ACCESS_PRIVATE;
			return this;
		}

		public Changeable setDefaultAccess() {
			super.access = ACCESS_DEFAULT;
			return this;
		}

		public Modifiers immutable() {
			return new Modifiers(this);
		}

		public Changeable setStatic(boolean isStatic) {
			super.isStatic = isStatic;
			return this;
		}

		public Changeable setFinal(boolean isFinal) {
			super.isFinal = isFinal;
			return this;
		}

		public Changeable setNative(boolean isNative) {
			super.isNative = isNative;
			return this;
		}

		public Changeable setAbstract(boolean isAbstract) {
			super.isAbstract = isAbstract;
			return this;
		}

		public Changeable setDefault(boolean isDefault) {
			super.isDefault = isDefault;
			return this;
		}

		public Changeable setSynchronized(boolean isSynchronized) {
			super.isSynchronized = isSynchronized;
			return this;
		}

		public void setModifiers(int code) {
			super.setModifiers(code);
		}
	}
}
