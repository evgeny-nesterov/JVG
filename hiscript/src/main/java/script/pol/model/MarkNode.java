package script.pol.model;

public class MarkNode extends Node {
	public MarkNode(String markName, Node body) {
		super("mark");
		this.markName = markName;
		this.body = body;
		isBlock = true;

		body.setParent(this);
	}

	private String markName;

	public String getMarkName() {
		return markName;
	}

	private Node body;

	public Node getBody() {
		return body;
	}

	@Override
	public void compile() throws ExecuteException {
		if (body != null) {
			body.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		if (body != null) {
			body.execute(ctx);
		}
	}
}
