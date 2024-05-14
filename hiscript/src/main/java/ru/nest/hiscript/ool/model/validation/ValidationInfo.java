package ru.nest.hiscript.ool.model.validation;

import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.tokenizer.Token;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ValidationInfo {
	private final HiCompiler compiler;

	private boolean valid = true;

	public ValidationInfo(HiCompiler compiler) {
		this.compiler = compiler;
		if (compiler.getValidationInfo() == null) {
			compiler.setValidationInfo(this);
		}
	}

	public List<ValidationMessage> messages = new ArrayList<>();

	public boolean hasMessage(String messageText) {
		for (ValidationMessage message : messages) {
			if (message.message.indexOf(messageText) != -1) {
				return true;
			}
		}
		return false;
	}

	public void error(String message, Token token) {
		ValidationMessage validationMessage = new ValidationMessage(ValidationMessage.ValidationLevel.error, message, token);
		if (!messages.contains(validationMessage)) {
			messages.add(validationMessage);
		}
		valid = false;
	}

	public void warning(String message, Token token) {
		messages.add(new ValidationMessage(ValidationMessage.ValidationLevel.warning, message, token));
	}

	public void info(String message, Token token) {
		messages.add(new ValidationMessage(ValidationMessage.ValidationLevel.info, message, token));
	}

	public void printError() {
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

	public void throwExceptionIf() throws HiScriptValidationException {
		if (compiler.isVerbose()) {
			printError();
		}
		for (ValidationMessage message : messages) {
			if (message.level == ValidationMessage.ValidationLevel.error) {
				throw new HiScriptValidationException(message.message, message.token).setValidationInfo(this);
			}
		}
	}

	public HiCompiler getCompiler() {
		return compiler;
	}

	public boolean isValid() {
		return valid;
	}
}
