package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class TryCatchNode extends Node {
	public TryCatchNode(Node tryBody, Node catchBody, Node finallyBody, String errorVariableName) {
		super("try-catch");
		this.tryBody = tryBody;
		this.catchBody = catchBody;
		this.finallyBody = finallyBody;
		this.errorVariableName = errorVariableName;
		isBlock = true;

		if (tryBody != null) {
			tryBody.setParent(this);
		}

		if (catchBody != null) {
			catchBody.setParent(this);
		}

		if (finallyBody != null) {
			finallyBody.setParent(this);
		}
	}

	private Node tryBody;

	public Node getTryBody() {
		return tryBody;
	}

	private Node catchBody;

	public Node getCatchBody() {
		return catchBody;
	}

	private Node finallyBody;

	public Node getFinallyBody() {
		return finallyBody;
	}

	private String errorVariableName;

	public String getErrorVariableName() {
		return errorVariableName;
	}

	private Variable errorVariable;

	@Override
	public void compile() throws ExecuteException {
		if (tryBody != null) {
			tryBody.compile();
		}

		if (catchBody != null) {
			catchBody.compile();
			errorVariable = new Variable(null, errorVariableName, Words.STRING, 0);
		}

		if (finallyBody != null) {
			finallyBody.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		try {
			if (tryBody != null) {
				tryBody.execute(ctx);
			}
		} catch (Throwable exc) {
			if (catchBody != null) {
				Variable error = catchBody.addVariable(errorVariable);
				error.getValue().setValue(exc.getMessage(), Words.STRING);
				catchBody.execute(ctx);
			}
		} finally {
			if (finallyBody != null) {
				finallyBody.execute(ctx);
			}
		}
	}
}
