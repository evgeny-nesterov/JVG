package ru.nest.hiscript.tokenizer;

import java.util.HashMap;
import java.util.Map;

import static ru.nest.hiscript.tokenizer.WordType.*;

public class WordToken extends Token {
	private final static Map<String, WordType> serviceWords = new HashMap<>();

	private final static Map<WordType, String> serviceTypes = new HashMap<>();

	static {
		serviceWords.put("do", DO);
		serviceWords.put("while", WHILE);
		serviceWords.put("if", IF);
		serviceWords.put("else", ELSE);
		serviceWords.put("for", FOR);
		serviceWords.put("break", BREAK);
		serviceWords.put("continue", CONTINUE);
		serviceWords.put("return", RETURN);
		serviceWords.put("switch", SWITCH);
		serviceWords.put("case", CASE);
		serviceWords.put("when", WHEN);
		serviceWords.put("true", TRUE);
		serviceWords.put("false", FALSE);
		serviceWords.put("char", CHAR);
		serviceWords.put("string", STRING);
		serviceWords.put("boolean", BOOLEAN);
		serviceWords.put("byte", BYTE);
		serviceWords.put("short", SHORT);
		serviceWords.put("int", INT);
		serviceWords.put("float", FLOAT);
		serviceWords.put("long", LONG);
		serviceWords.put("double", DOUBLE);
		serviceWords.put("var", VAR);
		serviceWords.put("void", VOID);
		serviceWords.put("try", TRY);
		serviceWords.put("catch", CATCH);
		serviceWords.put("finally", FINALLY);
		serviceWords.put("default", DEFAULT);
		serviceWords.put("synchronized", SYNCHRONIZED);
		serviceWords.put("assert", ASSERT);

		serviceWords.put("interface", INTERFACE);
		serviceWords.put("@interface", ANNOTATION_INTERFACE);
		serviceWords.put("class", CLASS);
		serviceWords.put("enum", ENUM);
		serviceWords.put("record", RECORD);
		serviceWords.put("new", NEW);

		serviceWords.put("package", PACKAGE);
		serviceWords.put("import", IMPORT);

		serviceWords.put("public", PUBLIC);
		serviceWords.put("protected", PROTECTED);
		serviceWords.put("private", PRIVATE);
		serviceWords.put("static", STATIC);
		serviceWords.put("final", FINAL);
		serviceWords.put("native", NATIVE);
		serviceWords.put("abstract", ABSTRACT);
		serviceWords.put("volatile", VOLATILE); // reserved
		serviceWords.put("sealed", SEALED); // reserved

		serviceWords.put("throw", THROW);
		serviceWords.put("throws", THROWS);

		serviceWords.put("instanceof", INSTANCE_OF);
		serviceWords.put("implements", IMPLEMENTS);
		serviceWords.put("extends", EXTENDS);

		serviceWords.put("super", SUPER);
		serviceWords.put("this", THIS);
		serviceWords.put("null", NULL);

		serviceWords.put("_", UNNAMED_VARIABLE);

		for (String word : serviceWords.keySet()) {
			serviceTypes.put(serviceWords.get(word), word);
		}
	}

	public static String getWord(WordType type) {
		return serviceTypes.get(type);
	}

	public static WordType getType(String word) {
		return serviceWords.getOrDefault(word, NOT_SERVICE);
	}

	protected WordType type;

	private String word;

	public WordToken(String word, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		if (serviceWords.containsKey(word)) {
			this.type = serviceWords.get(word);
		} else {
			this.type = NOT_SERVICE;
			this.word = word;
		}
	}

	public WordType getType() {
		return type;
	}

	public boolean isService() {
		return type != NOT_SERVICE;
	}

	public String getWord() {
		if (isService()) {
			return serviceTypes.get(type);
		} else {
			return word;
		}
	}

	@Override
	public String toString() {
		if (isService()) {
			return "Service word [" + getWord() + ", " + super.toString() + "]";
		} else {
			return "Word [" + getWord() + ", " + super.toString() + "]";
		}
	}
}
