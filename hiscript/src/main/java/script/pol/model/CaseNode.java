package script.pol.model;

public class CaseNode extends Node {
	public CaseNode(Node value, BlockNode body) {
		super("case");
		this.value = value;
		this.body = body;

		value.setParent(this);
		body.setParent(this);
	}

	private Node value;

	public Node getValue() {
		return value;
	}

	private BlockNode body;

	public BlockNode getBody() {
		return body;
	}

	@Override
	public void compile() throws ExecuteException {
		value.compile();

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
