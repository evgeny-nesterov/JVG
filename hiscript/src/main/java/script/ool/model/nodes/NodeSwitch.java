package script.ool.model.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import script.ool.model.Node;
import script.ool.model.RuntimeContext;

public class NodeSwitch extends Node {
	public NodeSwitch(Node valueNode) {
		super("switch", TYPE_SWITCH);
		this.valueNode = valueNode;
	}

	public void add(NodeExpression caseValue, NodeBlock caseBody) {
		if (casesValues == null) {
			casesValues = new ArrayList<Node>();
			casesNodes = new ArrayList<Node>();
		}
		casesValues.add(caseValue);
		casesNodes.add(caseBody);
		size++;
	}

	private Node valueNode;

	private int size;

	private List<Node> casesValues;

	private List<Node> casesNodes;

	@Override
	public void execute(RuntimeContext ctx) {
		valueNode.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		int value = ctx.value.getInt();
		if (ctx.exitFromBlock()) {
			return;
		}

		int index = -1;
		for (int i = 0; i < size; i++) {
			Node caseValueNode = casesValues.get(i);
			if (caseValueNode != null) {
				caseValueNode.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				int caseValue = ctx.value.getInt();
				if (ctx.exitFromBlock()) {
					return;
				}

				if (value == caseValue) {
					index = i;
					break;
				}
			} else {
				// default node
				index = i;
				break;
			}
		}

		if (index >= 0) {
			ctx.enter(RuntimeContext.SWITCH, line);
			try {
				for (int i = index; i < size; i++) {
					Node caseBody = casesNodes.get(i);
					if (caseBody != null) {
						caseBody.execute(ctx);
						if (ctx.exitFromBlock()) {
							return;
						}

						if (ctx.isBreak) {
							break;
						}
					}
				}
			} finally {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(valueNode);
		os.writeShort(size);
		os.writeNullable(casesValues);
		os.writeNullable(casesNodes);
	}

	public static NodeSwitch decode(DecodeContext os) throws IOException {
		NodeSwitch node = new NodeSwitch(os.read(Node.class));
		int size = os.readShort();
		node.casesValues = os.readNullableList(Node.class, size);
		node.casesNodes = os.readNullableList(Node.class, size);
		return node;
	}
}
