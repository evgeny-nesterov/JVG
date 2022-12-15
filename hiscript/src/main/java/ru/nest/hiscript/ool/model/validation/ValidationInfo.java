package ru.nest.hiscript.ool.model.validation;

import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.tokenizer.Token;

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

	public void throwExceptionIf() throws ValidationException {
		for (ValidationMessage message : messages) {
			if (message.level == ValidationMessage.ValidationLevel.error) {
				throw new ValidationException(message.message, message.token);
			}
		}
	}

	public HiCompiler getCompiler() {
		return compiler;
	}
}
