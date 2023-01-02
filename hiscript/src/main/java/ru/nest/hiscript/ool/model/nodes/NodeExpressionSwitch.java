package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeExpressionSwitch extends HiNode {
	public NodeExpressionSwitch(HiNode valueNode) {
		super("switch", TYPE_EXPRESSION_SWITCH);
		this.valueNode = valueNode;
	}

	public void add(HiNode[] caseValue, NodeExpression caseBody) {
		if (casesValues == null) {
			casesValues = new ArrayList<>();
			casesNodes = new ArrayList<>();
		}
		casesValues.add(caseValue);
		casesNodes.add(caseBody);
		size++;
	}

	private HiNode valueNode;

	private int size;

	private List<HiNode[]> casesValues;

	private List<HiNode> casesNodes;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		if (size > 0) {
			HiClass topType = null;
			for (int i = 0; i < size; i++) {
				HiClass caseValueType = casesNodes.get(i).getValueClass(validationInfo, ctx);
				topType = caseValueType.getCommonClass(topType);
				if (topType == null) {
					break;
				}
			}
			return topType;
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = valueNode.validate(validationInfo, ctx) && valueNode.expectValue(validationInfo, ctx);
		HiClass topCaseClass = null;
		HiClass topResultClass = null;
		for (int i = 0; i < size; i++) {
			HiNode[] caseValueNodes = casesValues.get(i);
			if (caseValueNodes != null) {
				for (HiNode caseValueNode : caseValueNodes) {
					if (caseValueNode.validate(validationInfo, ctx) && caseValueNode.expectValue(validationInfo, ctx)) {
						HiClass caseValueClass = caseValueNode.getValueClass(validationInfo, ctx);
						if (caseValueClass != null && caseValueClass != HiClassPrimitive.BOOLEAN) {
							HiClass c = caseValueClass.getCommonClass(topCaseClass);
							if (c != null) {
								topCaseClass = c;
							} else {
								validationInfo.error("incompatible switch case types; found " + caseValueClass + ", required " + topCaseClass, caseValueNode.getToken());
								valid = false;
							}
						}
					} else {
						valid = false;
					}
					if (caseValueNode instanceof NodeExpressionNoLS) {
						NodeCastedIdentifier identifier = ((NodeExpressionNoLS) caseValueNode).checkCastedIdentifier();
						if (identifier != null) {
							ctx.initializedNodes.add(identifier.declarationNode);
						}
					}
				}
			}

			HiNode caseNode = casesNodes.get(i);
			if (caseNode.validate(validationInfo, ctx) && caseNode.expectValue(validationInfo, ctx)) {
				HiClass caseNodeClass = caseNode.getValueClass(validationInfo, ctx);
				if (caseNodeClass != null) {
					HiClass c = caseNodeClass.getCommonClass(topResultClass);
					if (c != null) {
						topResultClass = c;
					} else {
						validationInfo.error("incompatible switch values types; found " + caseNodeClass + ", required " + topResultClass, caseNode.getToken());
						valid = false;
					}
				}
			} else {
				valid = false;
			}
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int index = NodeSwitch.getCaseIndex(ctx, valueNode, size, casesValues);
		if (index >= 0) {
			ctx.enter(RuntimeContext.SWITCH, token);
			try {
				for (int i = index; i < size; i++) {
					HiNode caseBody = casesNodes.get(i);
					if (caseBody != null) {
						caseBody.execute(ctx);
						return;
					}
				}
			} finally {
				ctx.exit();
			}
		}
		ctx.throwRuntimeException("no suitable value in the switch");
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
		NodeExpressionSwitch node = new NodeExpressionSwitch(os.read(HiNode.class));
		node.size = os.readShort();
		node.casesValues = os.readNullableListArray(HiNode.class, node.size);
		node.casesNodes = os.readNullableList(HiNode.class, node.size);
		return node;
	}
}
