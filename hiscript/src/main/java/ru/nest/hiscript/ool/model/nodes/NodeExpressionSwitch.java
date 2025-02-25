package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeExpressionSwitch extends HiNode {
	public NodeExpressionSwitch(HiNode valueNode) {
		super("switch", TYPE_EXPRESSION_SWITCH, true);
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

	private final HiNode valueNode;

	private int size;

	private List<HiNode[]> casesValues;

	private List<HiNode> casesNodes;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = this;
		if (size > 0) {
			if (valueNode != null) {
				NodeValueType valueType = valueNode.getNodeValueType(validationInfo, ctx);
				COMPILE:
				if (valueType.isCompileValue()) {
					Object compileValue = valueType.getCompileValue();
					HiNode matchedCaseResult = null;
					for (int i = 0; i < size; i++) {
						HiNode caseResult = casesNodes.get(i);
						HiNode[] caseConditions = casesValues.get(i);
						if (caseConditions != null) {
							for (HiNode caseCondition : caseConditions) {
								NodeValueType caseValueType = caseCondition.getNodeValueType(validationInfo, ctx);
								if (!caseValueType.isCompileValue()) {
									if (matchedCaseResult != null) {
										validationInfo.warning("case never will be reached", caseCondition);
									}
									break COMPILE;
								}
								if (matchedCaseResult == null) {
									if (caseResult.isCompileValue()) {
										Object caseValue = caseValueType.getCompileValue();
										if (Objects.equals(compileValue, caseValue)) {
											matchedCaseResult = caseResult;
										}
									}
								}
							}
						}
					}
					if (matchedCaseResult != null) {
						NodeValueType resultValueType = matchedCaseResult.getNodeValueType(validationInfo, ctx);
						resultValueType.copyTo(ctx.nodeValueType);
						return resultValueType.clazz;
					}
				}
			}

			HiClass topClass = null;
			for (int i = 0; i < size; i++) {
				HiClass caseValueType = casesNodes.get(i).getValueClass(validationInfo, ctx);
				topClass = caseValueType.getCommonClass(topClass);
				if (topClass == null) {
					break;
				}
			}

			ctx.nodeValueType.enclosingClass = topClass;
			ctx.nodeValueType.enclosingType = Type.getType(topClass);
			ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
			ctx.nodeValueType.type = Type.getType(topClass);
			return topClass;
		} else {
			validationInfo.error("expression switch without cases", getToken());
			return null;
		}
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = false;
		if (valueNode != null) {
			valid = valueNode.validate(validationInfo, ctx) && valueNode.expectValue(validationInfo, ctx);
		} else {
			validationInfo.error("expression expected", getToken());
		}

		HiClass topCaseClass = null;
		HiClass topResultClass = null;
		for (int i = 0; i < size; i++) {
			HiNode[] caseValueNodes = casesValues.get(i);
			if (caseValueNodes != null) {
				if (caseValueNodes.length > 0) {
					for (HiNode caseValueNode : caseValueNodes) {
						if (caseValueNode.validate(validationInfo, ctx) && expectCaseValue(validationInfo, ctx, caseValueNode)) {
							HiClass caseValueClass = caseValueNode.getValueClass(validationInfo, ctx);
							if (caseValueClass != null && caseValueClass != HiClassPrimitive.BOOLEAN) {
								HiClass c = caseValueClass.getCommonClass(topCaseClass);
								if (c != null) {
									topCaseClass = c;
								} else {
									validationInfo.error("incompatible switch case types; found " + caseValueClass + ", required " + topCaseClass, caseValueNode);
									valid = false;
								}
							}
						} else {
							valid = false;
						}
						if (caseValueNode instanceof NodeExpression) {
							NodeCastedIdentifier identifier = ((NodeExpression) caseValueNode).checkCastedIdentifier();
							if (identifier != null) {
								if (caseValueNodes.length > 1) {
									validationInfo.error("only one casted identifier is allowed in the case condition", caseValueNode);
								}
								ctx.initializedNodes.add(identifier.declarationNode);
							}
						}
					}
				} else {
					validationInfo.error("empty case value", getToken());
					valid = false;
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
						validationInfo.error("incompatible switch values types; found " + caseNodeClass + ", required " + topResultClass, caseNode);
						valid = false;
					}
				}
			} else {
				valid = false;
			}

			if (caseValueNodes != null) {
				for (HiNode caseValueNode : caseValueNodes) {
					NodeCastedIdentifier identifier = ((NodeExpressionNoLS) caseValueNode).checkCastedIdentifier();
					if (identifier != null) {
						identifier.removeLocalVariables(ctx);
					}
				}
			}
		}
		return valid;
	}

	private boolean expectCaseValue(ValidationInfo validationInfo, CompileClassContext ctx, HiNode caseValueNode) {
		HiClass valueClass = caseValueNode.getValueClass(validationInfo, ctx);
		if (valueClass == HiClassPrimitive.VOID && !(caseValueNode.getExpressionSingleNode() instanceof NodeCastedIdentifier)) {
			validationInfo.error("value or casted identifier is expected", getToken());
			return false;
		}
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int index = NodeSwitch.getCaseIndex(ctx, valueNode, size, casesValues);
		if (index >= 0) {
			ctx.enter(RuntimeContext.SWITCH, token);
			try {
				for (int i = index; i < size; i++) {
					HiNode caseBody = casesNodes.get(i);
					assert caseBody != null;
					caseBody.execute(ctx);
					return;
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
