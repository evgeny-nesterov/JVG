package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeExpressionSwitch extends Node {
	public NodeExpressionSwitch(Node valueNode) {
		super("switch", TYPE_SWITCH);
		this.valueNode = valueNode;
	}

	public void add(Node[] caseValue, NodeExpression caseBody) {
		if (casesValues == null) {
			casesValues = new ArrayList<>();
			casesNodes = new ArrayList<>();
		}
		casesValues.add(caseValue);
		casesNodes.add(caseBody);
		size++;
	}

	private Node valueNode;

	private int size;

	private List<Node[]> casesValues;

	private List<Node> casesNodes;

	@Override
	public void execute(RuntimeContext ctx) {
		valueNode.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		// TODO support any type
		int value = ctx.value.getInt();
		if (ctx.exitFromBlock()) {
			return;
		}

		int index = -1;
		FOR: for (int i = 0; i < size; i++) {
			Node[] caseValueNodes = casesValues.get(i);
			if (caseValueNodes != null && caseValueNodes.length > 0) {
				for (int j = 0; j < caseValueNodes.length; j++) {
					Node caseValueNode = caseValueNodes[j];
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
						break FOR;
					}
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
						return;
					}
				}
			} finally {
				ctx.exit();
			}
		}
		ctx.throwRuntimeException("no sutable value in the switch");
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(valueNode);
		os.writeShort(size);
		os.writeArraysNullable(casesValues);
		os.writeNullable(casesNodes);
	}

	public static NodeExpressionSwitch decode(DecodeContext os) throws IOException {
		NodeExpressionSwitch node = new NodeExpressionSwitch(os.read(Node.class));
		int size = os.readShort();
		node.casesValues = os.readNullableListArray(Node.class, size);
		node.casesNodes = os.readNullableList(Node.class, size);
		return node;
	}
}
