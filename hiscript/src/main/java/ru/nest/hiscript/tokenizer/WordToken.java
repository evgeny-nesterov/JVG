package ru.nest.hiscript.tokenizer;

import java.util.HashMap;

public class WordToken extends Token implements Words {
	private final static HashMap<String, Integer> service_words = new HashMap<>();

	private final static HashMap<Integer, String> service_types = new HashMap<>();

	static {
		service_words.put("do", DO);
		service_words.put("while", WHILE);
		service_words.put("if", IF);
		service_words.put("else", ELSE);
		service_words.put("for", FOR);
		service_words.put("break", BREAK);
		service_words.put("continue", CONTINUE);
		service_words.put("return", RETURN);
		service_words.put("switch", SWITCH);
		service_words.put("case", CASE);
		service_words.put("true", TRUE);
		service_words.put("false", FALSE);
		service_words.put("char", CHAR);
		service_words.put("string", STRING);
		service_words.put("boolean", BOOLEAN);
		service_words.put("byte", BYTE);
		service_words.put("short", SHORT);
		service_words.put("int", INT);
		service_words.put("float", FLOAT);
		service_words.put("long", LONG);
		service_words.put("double", DOUBLE);
		service_words.put("void", VOID);
		service_words.put("try", TRY);
		service_words.put("catch", CATCH);
		service_words.put("finally", FINALLY);
		service_words.put("default", DEFAULT);
		service_words.put("synchronized", SYNCHRONIZED);
		service_words.put("assert", ASSERT);

		service_words.put("class", CLASS);
		service_words.put("new", NEW);

		service_words.put("public", PUBLIC);
		service_words.put("protected", PROTECTED);
		service_words.put("private", PRIVATE);
		service_words.put("static", STATIC);
		service_words.put("final", FINAL);
		service_words.put("native", NATIVE);
		service_words.put("abstract", ABSTRACT);

		service_words.put("throw", THROW);
		service_words.put("throws", THROWS);

		service_words.put("interface", INTERFACE);
		service_words.put("instanceof", INSTANCEOF);
		service_words.put("implements", IMPLEMENS);
		service_words.put("extends", EXTENDS);

		service_words.put("super", SUPER);
		service_words.put("this", THIS);
		service_words.put("null", NULL);

		for (String word : service_words.keySet()) {
			service_types.put(service_words.get(word), word);
		}
	}

	public static String getWord(int type) {
		return service_types.get(type);
	}

	public static int getType(String word) {
		return service_words.getOrDefault(word, NOT_SERVICE);
	}

	public WordToken(String word, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);

		if (service_words.containsKey(word)) {
			type = service_words.get(word);
		} else {
			type = NOT_SERVICE;
			this.word = word;
		}
	}

	private int type;

	public int getType() {
		return type;
	}

	public boolean isService() {
		return type != NOT_SERVICE;
	}

	private String word;

	public String getWord() {
		if (isService()) {
			return service_types.get(type);
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
