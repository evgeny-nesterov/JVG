package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
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
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (size > 0) {
			HiClass topType = casesNodes.get(0).getValueType(validationInfo, ctx);
			for (int i = 1; i < size && topType != null; i++) {
				HiClass caseValueType = casesNodes.get(i).getValueType(validationInfo, ctx);
				if (caseValueType != null) {
					topType = topType.getCommonClass(caseValueType);
				} else {
					topType = null;
				}
			}
			return topType;
		}
		return null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = valueNode.validate(validationInfo, ctx) && valueNode.expectValue(validationInfo, ctx);
		for (int i = 0; i < size; i++) {
			HiNode[] caseValues = casesValues.get(i);
			if (caseValues != null) {
				for (HiNode caseValue : caseValues) {
					valid &= caseValue.validate(validationInfo, ctx) && caseValue.expectValue(validationInfo, ctx);
				}
			}
			valid &= casesNodes.get(i).validate(validationInfo, ctx) && casesNodes.get(i).expectValue(validationInfo, ctx);
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
