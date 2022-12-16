package ru.nest.hiscript.tokenizer;

public class AnnotationWordToken extends WordToken {
	public AnnotationWordToken(String word, int line, int offset, int length, int lineOffset) {
		super(word, line, offset, length, lineOffset);
		if (word.equals("interface")) {
			this.type = ANNOTATION_INTERFACE;
		}
	}

	@Override
	public String toString() {
		return "Annotation [" + getWord() + ", " + super.toString() + "]";
	}
}
