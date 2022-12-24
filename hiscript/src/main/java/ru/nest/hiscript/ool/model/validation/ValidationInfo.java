package ru.nest.hiscript.ool.model.validation;

import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.tokenizer.Token;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ValidationInfo {
	private HiCompiler compiler;

	public ValidationInfo(HiCompiler compiler) {
		this.compiler = compiler;
	}

	public List<ValidationMessage> messages = new ArrayList<>();

	public void error(String message, Token token) {
		messages.add(new ValidationMessage(ValidationMessage.ValidationLevel.error, message, token));
	}

	public void warning(String message, Token token) {
		messages.add(new ValidationMessage(ValidationMessage.ValidationLevel.warning, message, token));
	}

	public void info(String message, Token token) {
		messages.add(new ValidationMessage(ValidationMessage.ValidationLevel.info, message, token));
	}

	public void throwExceptionIf() throws HiScriptValidationException {
		if (compiler.isVerbose()) {
			for (ValidationMessage message : messages) {
				PrintStream os = message.level == ValidationMessage.ValidationLevel.error ? System.err : System.out;
				os.println("[" + message.level + "] " + message.message + (message.token != null ? " (" + message.token.getLine() + ":" + message.token.getLineOffset() + ")" : ""));
				if (message.token != null && compiler.isPrintInvalidCode()) {
					String codeText = compiler.getTokenLineText(message.token);
					os.println(codeText);
					StringBuilder pointer = new StringBuilder();
					for (int i = 0; i < message.token.getLineOffset(); i++) {
						if (Character.isWhitespace(codeText.charAt(i))) {
							pointer.append(codeText.charAt(i));
						} else {
							pointer.append(" ");
						}
					}
					pointer.append("^");
					os.println(pointer);
				}
			}
		}
		for (ValidationMessage message : messages) {
			if (message.level == ValidationMessage.ValidationLevel.error) {
				throw new HiScriptValidationException(message.message, message.token);
			}
		}
	}

	public HiCompiler getCompiler() {
		return compiler;
	}
}
