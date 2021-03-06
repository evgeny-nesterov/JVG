package ru.nest.hiscript.ool.model;

import java.io.IOException;

import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.tokenizer.Words;

public class Modifiers implements ModifiersIF, Codable {
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
		}
		return -1;
	}

	public Modifiers() {
	}

	public Modifiers(int modifiers) {
		setModifiers(modifiers);
	}

	private int access = ACCESS_DEFAULT;

	public int getAccess() {
		return access;
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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
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

		return modifiers;
	}

	public void setModifiers(int code) {
		setStatic((code & STATIC) != 0);
		setFinal((code & FINAL) != 0);
		setNative((code & NATIVE) != 0);
		setAbstract((code & ABSTRACT) != 0);

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

	public int check(int... allowed) {
		if (allowed.length > 0) {
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
		}
		return -1;
	}
}
