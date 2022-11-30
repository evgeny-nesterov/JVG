package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeSwitch extends Node {
	public NodeSwitch(Node valueNode) {
		super("switch", TYPE_SWITCH);
		this.valueNode = valueNode;
	}

	public void add(Node[] caseValue, NodeBlock caseBody) {
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

		int index = -1;
		if (ctx.value.type.isPrimitive()) {
			int value = ctx.value.getInt();
			if (ctx.exitFromBlock()) {
				return;
			}
			FOR:
			for (int i = 0; i < size; i++) {
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
		} else if (ctx.value.type.isObject()) {
			HiObject object = ctx.value.object;
			if (object.clazz.isEnum()) {
				HiClassEnum enumClass = (HiClassEnum) object.clazz;
				FOR:
				for (int i = 0; i < size; i++) {
					Node[] caseValueNodes = casesValues.get(i);
					if (caseValueNodes != null && caseValueNodes.length > 0) {
						for (int j = 0; j < caseValueNodes.length; j++) {
							Node caseValueNode = caseValueNodes[j];
							if (caseValueNode instanceof NodeExpressionNoLS) {
								NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) caseValueNode;
								NodeIdentifier identifier = exprCaseValueNode.checkIdentifier();
								if (identifier != null) {
									int enumOrdinal = enumClass.getEnumOrdinal(identifier.getName());
									if (enumOrdinal == -1) {
										ctx.throwException("RuntimeException", "Cannot resolve symbol '" + identifier.getName() + "'");
										return;
									}

									if (object.getField("ordinal").get().equals(enumOrdinal)) {
										index = i;
										break FOR;
									}
									continue;
								}
							}

							ctx.throwException("RuntimeException", "An enum switch case label must be the unqualified name of an enumeration constant");
							return;
						}
					} else {
						// default node
						index = i;
						break;
					}
				}
			} else {
				FOR:
				for (int i = 0; i < size; i++) {
					Node[] caseValueNodes = casesValues.get(i);
					if (caseValueNodes != null && caseValueNodes.length > 0) {
						for (int j = 0; j < caseValueNodes.length; j++) {
							Node caseValueNode = caseValueNodes[j];
							caseValueNode.execute(ctx);
							if (ctx.exitFromBlock()) {
								return;
							}

							if (ctx.value.type.isObject()) {
								if (object.equals(ctx, ctx.value.object)) {
									index = i;
									break FOR;
								}
								if (ctx.exitFromBlock()) {
									return;
								}
							}
						}
					} else {
						// default node
						index = i;
						break;
					}
				}
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
		os.writeArraysNullable(casesValues);
		os.writeNullable(casesNodes);
	}

	public static NodeSwitch decode(DecodeContext os) throws IOException {
		NodeSwitch node = new NodeSwitch(os.read(Node.class));
		node.size = os.readShort();
		node.casesValues = os.readNullableListArray(Node.class, node.size);
		node.casesNodes = os.readNullableList(Node.class, node.size);
		return node;
	}
}
