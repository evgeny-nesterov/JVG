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
	public static Modifiers PUBLIC() {
		Modifiers modifiers = new Modifiers();
		modifiers.setAccess(ACCESS_PUBLIC);
		return modifiers;
	}

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
		}
		return -1;
	}

	public Modifiers() {
	}

	public Modifiers(int modifiers) {
		setModifiers(modifiers);
	}

	public boolean hasModifiers() {
		return access != ACCESS_DEFAULT || isFinal || isStatic || isAbstract || isNative;
	}

	private int access = ACCESS_DEFAULT;

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
		return (access & ACCESS_DEFAULT) != 0;
	}

	public boolean isPrivate() {
		return (access & ACCESS_PRIVATE) != 0;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	private boolean isStatic = false;

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	private boolean isFinal = false;

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	private boolean isNative = false;

	public boolean isNative() {
		return isNative;
	}

	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	private boolean isAbstract = false;

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	private boolean isDefault = false;

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		switch (access) {
			case ACCESS_PUBLIC:
				sb.append("public ");
				break;

			case ACCESS_PROTECTED:
				sb.append("protected ");
				break;

			case ACCESS_PRIVATE:
				sb.append("private ");
				break;
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
		return modifiers;
	}

	public void setModifiers(int code) {
		setStatic((code & STATIC) != 0);
		setFinal((code & FINAL) != 0);
		setNative((code & NATIVE) != 0);
		setAbstract((code & ABSTRACT) != 0);
		setDefault((code & DEFAULT) != 0);

		if ((code & ACCESS_PUBLIC) != 0) {
			setAccess(ACCESS_PUBLIC);
		} else if ((code & ACCESS_PROTECTED) != 0) {
			setAccess(ACCESS_PROTECTED);
		} else if ((code & ACCESS_PRIVATE) != 0) {
			setAccess(ACCESS_PRIVATE);
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeByte(getModifiers());
	}

	public static Modifiers decode(DecodeContext os) throws IOException {
		Modifiers modifiers = new Modifiers();
		modifiers.setModifiers(os.readByte());
		return modifiers;
	}

	public boolean check(Tokenizer tokenizer, Token modifiersToken, int... allowed) throws TokenizerException {
		int allowedMask = ACCESS_DEFAULT;
		for (int word : allowed) {
			allowedMask |= mapWordsToModification(word);
		}

		boolean valid = true;
		if ((allowedMask & access) == 0) {
			Token token = Modifiers.getToken(tokenizer, access, modifiersToken);
			tokenizer.error("modifier '" + Modifiers.getName(access) + "' is not allowed", token);
			valid = false;
		}

		if (isFinal && (allowedMask & FINAL) == 0) {
			Token token = Modifiers.getToken(tokenizer, FINAL, modifiersToken);
			tokenizer.error("modifier '" + Modifiers.getName(FINAL) + "' is not allowed", token);
			valid = false;
		}

		if (isStatic && (allowedMask & STATIC) == 0) {
			Token token = Modifiers.getToken(tokenizer, STATIC, modifiersToken);
			tokenizer.error("modifier '" + Modifiers.getName(STATIC) + "' is not allowed", token);
			valid = false;
		}

		if (isAbstract && (allowedMask & ABSTRACT) == 0) {
			Token token = Modifiers.getToken(tokenizer, ABSTRACT, modifiersToken);
			tokenizer.error("modifier '" + Modifiers.getName(ABSTRACT) + "' is not allowed", token);
			valid = false;
		}

		if (isNative && (allowedMask & NATIVE) == 0) {
			Token token = Modifiers.getToken(tokenizer, NATIVE, modifiersToken);
			tokenizer.error("modifier '" + Modifiers.getName(NATIVE) + "' is not allowed", token);
			valid = false;
		}

		if (isDefault && (allowedMask & DEFAULT) == 0) {
			Token token = Modifiers.getToken(tokenizer, DEFAULT, modifiersToken);
			tokenizer.error("modifier '" + Modifiers.getName(DEFAULT) + "' is not allowed", token);
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
		return -1;
	}

	public static Token getToken(Tokenizer tokenizer, int modifier, Token modifiersToken) {
		if (modifiersToken == null) {
			return null;
		}
		String modifierName = Modifiers.getName(modifier);
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
}
