package script.pol.model;

import java.util.ArrayList;

import script.Breakable;

public class SwitchNode extends Node implements Breakable {
	public SwitchNode(Node value) {
		super("switch");
		this.value = value;
		isBlock = true;

		value.setParent(this);
	}

	private Node value;

	public Node getValue() {
		return value;
	}

	private ArrayList<CaseNode> cases = new ArrayList<CaseNode>();

	public void addCase(CaseNode node) {
		cases.add(node);
		node.setParent(this);
	}

	private boolean isBreaked = false;

	public void Break() {
		isBreaked = true;
	}

	private BlockNode defaultBody = null;

	public BlockNode getDefaultBody() {
		return defaultBody;
	}

	public void setDefault(BlockNode defaultBody) {
		this.defaultBody = defaultBody;
		defaultBody.setParent(this);
	}

	public void compile() throws ExecuteException {
		if (value != null) {
			value.compile();
		}

		for (CaseNode node : cases) {
			node.compile();
		}

		if (defaultBody != null) {
			defaultBody.compile();
		}
	}

	public void execute(RuntimeContext ctx) throws ExecuteException {
		isBreaked = false;

		value.execute(ctx);
		long number = ctx.value.getLong();

		boolean enter = false;
		for (CaseNode node : cases) {
			if (!enter) {
				node.getValue().execute(ctx);
				long caseNumber = ctx.value.getLong();
				if (number == caseNumber) {
					node.execute(ctx);
					enter = true;
				}
			} else {
				node.execute(ctx);
			}

			if (ctx.isExit) {
				return;
			}

			if (isBreaked) {
				break;
			}
		}

		if (!enter && defaultBody != null) {
			defaultBody.execute(ctx);
		}
	}
}
