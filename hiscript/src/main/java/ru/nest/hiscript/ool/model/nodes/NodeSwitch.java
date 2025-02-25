package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeSwitch extends HiNode {
	public NodeSwitch(HiNode valueNode) {
		super("switch", TYPE_SWITCH, true);
		this.valueNode = valueNode;
	}

	public void add(HiNode[] caseValue, NodeBlock caseBody) {
		if (casesValues == null) {
			casesValues = new ArrayList<>();
			casesNodes = new ArrayList<>();
		}
		casesValues.add(caseValue);
		casesNodes.add(caseBody);
		size++;
	}

	public HiNode valueNode;

	private int size;

	private List<HiNode[]> casesValues;

	private List<HiNode> casesNodes;

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		boolean hasDefault = false;
		for (int i = 0; i < size; i++) {
			HiNode caseNode = casesNodes.get(i);
			if (caseNode != null) {
				if (!caseNode.isReturnStatement(label, labels)) {
					return false;
				}
			}
			if (casesValues.get(i) == null) { // default
				hasDefault = true;
			}
		}
		return hasDefault;
	}

	@Override
	public NodeReturn getReturnNode() {
		NodeReturn nodeReturn = null;
		for (int i = 0; i < size; i++) {
			HiNode caseNode = casesNodes.get(i);
			if (caseNode != null) {
				nodeReturn = caseNode.getReturnNode();
				break;
			}
		}
		return nodeReturn;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		NodeValueType valueReturnType = null;
		HiClass valueClass = null;
		if (valueNode != null) {
			if (valueNode.validate(validationInfo, ctx) && valueNode.expectValue(validationInfo, ctx)) {
				valueReturnType = valueNode.getNodeValueType(validationInfo, ctx);
				valueClass = valueReturnType.clazz;
				if (valueClass.isNull()) {
					valueClass = HiClass.OBJECT_CLASS;
				}

				// @autoboxing
				HiClass checkValueClass = valueClass.getAutoboxedPrimitiveClass() != null ? valueClass.getAutoboxedPrimitiveClass() : valueClass;

				if (checkValueClass == HiClassPrimitive.LONG || checkValueClass == HiClassPrimitive.FLOAT || checkValueClass == HiClassPrimitive.DOUBLE || checkValueClass == HiClassPrimitive.BOOLEAN) {
					validationInfo.error("invalid switch value type: '" + valueClass.getNameDescr() + "'", valueNode);
					valueClass = null;
					valid = false;
				}
			} else {
				valid = false;
			}
		} else {
			validationInfo.error("expression expected", getToken());
			valid = false;
		}

		ctx.enter(RuntimeContext.SWITCH, this);
		if (valueClass != null && valueClass.isEnum()) {
			HiClassEnum enumClass = (HiClassEnum) valueClass;
			boolean[] enumProcessedValues = new boolean[enumClass.enumValues.size()];
			for (int i = 0; i < size; i++) {
				HiNode[] caseValueNodes = casesValues.get(i);
				if (caseValueNodes != null) { // not default
					if (caseValueNodes.length == 0) {
						validationInfo.error("expression expected", getToken());
						valid = false;
					}
					for (int j = 0; j < caseValueNodes.length; j++) {
						HiNode caseValueNode = caseValueNodes[j];
						if (caseValueNode instanceof NodeExpressionNoLS) {
							NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) caseValueNode;
							NodeIdentifier identifier = exprCaseValueNode.checkIdentifier();
							if (identifier != null) {
								int enumOrdinal = enumClass.getEnumOrdinal(identifier.getName());
								if (enumOrdinal == -1) {
									validationInfo.error("cannot resolve symbol '" + identifier.getName() + "'", caseValueNode);
									valid = false;
								} else {
									if (enumProcessedValues[enumOrdinal]) {
										validationInfo.error("case enum value '" + identifier.getName() + "' is duplicated", caseValueNode);
										valid = false;
									} else {
										enumProcessedValues[enumOrdinal] = true;
									}
								}
								continue;
							}
						}
						if (valid &= caseValueNode.validate(validationInfo, ctx)) {
							HiClass caseValueClass = caseValueNode.getValueClass(validationInfo, ctx);
							if (caseValueClass != valueClass) {
								validationInfo.error("an enum switch case label must be the unqualified name of an enumeration constant", caseValueNode);
							}
						}
					}
				}

				HiNode caseBodyNode = casesNodes.get(i);
				if (caseBodyNode != null) {
					valid &= caseBodyNode.validate(validationInfo, ctx);
				}
			}
		} else {
			Set<Object> processedValues = new HashSet<>();
			for (int i = 0; i < size; i++) {
				HiNode[] caseValueNodes = casesValues.get(i);
				if (caseValueNodes != null) { // not default
					if (caseValueNodes.length == 0) {
						validationInfo.error("expression expected", getToken());
						valid = false;
					}
					for (int j = 0; j < caseValueNodes.length; j++) {
						HiNode caseValueNode = caseValueNodes[j];
						NodeCastedIdentifier castedIdentifier = caseValueNode.getSingleNode(NodeCastedIdentifier.class);
						boolean isCaseCastedIdentifiers = castedIdentifier != null;
						if (isCaseCastedIdentifiers) {
							if (caseValueNodes.length > 1) {
								validationInfo.error("only one casted identifier is allowed in the case condition", castedIdentifier);
								valid = false;
							}
							ctx.initializedNodes.add(castedIdentifier.declarationNode);
						}

						if (caseValueNode.validate(validationInfo, ctx) && expectCaseValue(validationInfo, ctx, caseValueNode)) {
							HiClass caseValueClass = caseValueNode.getValueClass(validationInfo, ctx);
							if (caseValueClass != null && caseValueClass != HiClassPrimitive.BOOLEAN && caseValueClass.getAutoboxedPrimitiveClass() != HiClassPrimitive.BOOLEAN) {
								Object caseValue = ctx.nodeValueType.getCompileValue();
								if (caseValue != null || caseValueClass.isNull()) {
									if (processedValues.contains(caseValue)) {
										validationInfo.error("case value '" + caseValue + "' is duplicated", caseValueNode);
										valid = false;
									} else {
										processedValues.add(caseValue);
									}
								}
								if (valueClass != null) {
									if (valueClass.isPrimitive()) {
										if (!HiClass.autoCast(ctx, caseValueClass, valueClass, valueReturnType.isCompileValue(), true)) {
											validationInfo.error("incompatible switch case types; found " + caseValueClass.getNameDescr() + ", required " + valueClass.getNameDescr(), caseValueNode);
											valid = false;
										}
									} else if (!caseValueClass.isNull() && !caseValueClass.boxed().isInstanceof(valueClass.boxed())) {
										validationInfo.error("incompatible switch case types; found " + caseValueClass.getNameDescr() + ", required " + valueClass.getNameDescr(), caseValueNode);
										valid = false;
									}
								}
							}
						} else {
							valid = false;
						}
					}
				}

				HiNode caseBodyNode = casesNodes.get(i);
				if (caseBodyNode != null) {
					valid &= caseBodyNode.validate(validationInfo, ctx);
				}

				// after caseNode validation
				if (caseValueNodes != null) { // not default
					for (HiNode caseValueNode : caseValueNodes) {
						NodeCastedIdentifier identifier = ((NodeExpression) caseValueNode).checkCastedIdentifier();
						if (identifier != null) {
							identifier.removeLocalVariables(ctx);
						}
					}
				}
			}
		}
		ctx.exit();
		checkStatementTermination(ctx);
		return valid;
	}

	private boolean expectCaseValue(ValidationInfo validationInfo, CompileClassContext ctx, HiNode caseValueNode) {
		HiClass valueClass = caseValueNode.getValueClass(validationInfo, ctx);
		if ((valueClass == null || valueClass == HiClassPrimitive.VOID) && !(caseValueNode.getExpressionSingleNode() instanceof NodeCastedIdentifier)) {
			validationInfo.error("value or casted identifier is expected", getToken());
			return false;
		}
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.enter(RuntimeContext.SWITCH, token);
		try {
			int index = getCaseIndex(ctx, valueNode, size, casesValues);
			if (index >= 0) {
				for (int i = index; i < size; i++) {
					HiNode caseBody = casesNodes.get(i);
					HiNode[] caseValueNodes = casesValues.get(i);
					NodeCastedIdentifier identifier = null;
					if (caseValueNodes != null && caseValueNodes.length == 1) {
						identifier = caseValueNodes[0].getSingleNode(NodeCastedIdentifier.class);
					}
					try {
						if (caseBody != null) {
							caseBody.execute(ctx);
							if (ctx.exitFromBlock()) {
								return;
							}
							if (ctx.isBreak) {
								break;
							}
						}
					} finally {
						if (identifier != null) {
							identifier.removeLocalVariables(ctx);
						}
					}
				}
			}
		} finally {
			ctx.exit();
		}
	}

	public static int getCaseIndex(RuntimeContext ctx, HiNode valueNode, int size, List<HiNode[]> casesValues) {
		valueNode.execute(ctx);
		if (ctx.exitFromBlock()) {
			return -2;
		}

		HiClass switchValueClass = ctx.value.valueClass;

		// @autoboxing
		if (switchValueClass.getAutoboxedPrimitiveClass() != null && ctx.value.object != null) {
			HiClass primitiveClass = ctx.value.valueClass.getAutoboxedPrimitiveClass();
			if (primitiveClass == HiClassPrimitive.INT || primitiveClass == HiClassPrimitive.BYTE || primitiveClass == HiClassPrimitive.SHORT || primitiveClass == HiClassPrimitive.CHAR || primitiveClass == HiClassPrimitive.BOOLEAN) {
				switchValueClass = primitiveClass;
			}
		}

		if (switchValueClass.isPrimitive()) {
			int value = ctx.value.getInt();
			if (ctx.exitFromBlock()) {
				// TODO delete?
				return -2;
			}
			for (int i = 0; i < size; i++) {
				HiNode[] caseValueNodes = casesValues.get(i);
				if (caseValueNodes != null && caseValueNodes.length > 0) {
					for (int j = 0; j < caseValueNodes.length; j++) {
						HiNode caseValueNode = caseValueNodes[j];
						caseValueNode.execute(ctx);
						if (ctx.exitFromBlock()) {
							return -2;
						}

						if (ctx.value.valueClass == HiClassPrimitive.BOOLEAN || ctx.value.valueClass.getAutoboxedPrimitiveClass() == HiClassPrimitive.BOOLEAN) {
							if (ctx.value.bool) {
								return i;
							}
						} else {
							int caseValue = ctx.value.getInt();
							if (ctx.exitFromBlock()) {
								return -2;
							}
							if (value == caseValue) {
								return i;
							}
						}
					}
				} else {
					// default node
					return i;
				}
			}
		} else if (switchValueClass.isObject() || switchValueClass.isNull()) {
			// enums
			Object object = ctx.value.object;
			HiClass objectClass = null;
			HiObject hiObject = null;
			if (object instanceof HiObject) {
				hiObject = (HiObject) object;
				objectClass = hiObject.clazz;
				if (objectClass.isEnum()) {
					HiClassEnum enumClass = (HiClassEnum) objectClass;
					for (int i = 0; i < size; i++) {
						HiNode[] caseValueNodes = casesValues.get(i);
						if (caseValueNodes != null && caseValueNodes.length > 0) {
							for (int j = 0; j < caseValueNodes.length; j++) {
								NodeIdentifier identifier = caseValueNodes[j].getSingleNode(NodeIdentifier.class);
								if (identifier != null) {
									int enumOrdinal = enumClass.getEnumOrdinal(identifier.getName());
									if (hiObject.getField(ctx, "ordinal").get().equals(enumOrdinal)) {
										return i;
									}
								}
							}
						} else {
							// default node
							return i;
						}
					}
					return -1;
				}
			}
			if (objectClass == null) {
				objectClass = ctx.value.originalValueClass;
			}

			// objects, records, arrays
			for (int i = 0; i < size; i++) {
				HiNode[] caseValueNodes = casesValues.get(i);
				if (caseValueNodes != null && caseValueNodes.length > 0) {
					for (int j = 0; j < caseValueNodes.length; j++) {
						HiNode caseValueNode = caseValueNodes[j];
						caseValueNode.execute(ctx);
						if (ctx.exitFromBlock()) {
							return -2;
						}

						if (ctx.value.valueType == Value.CLASS) {
							HiClass c1 = objectClass;
							HiClass c2 = ctx.value.valueClass;
							if (c1.isInstanceof(c2)) {
								String castedVariableName = ctx.value.castedVariableName;
								HiNode[] castedRecordArguments = ctx.value.castedRecordArguments;
								HiConstructor castedRecordArgumentsConstructor = ctx.value.castedRecordArgumentsConstructor;
								HiNode castedCondition = ctx.value.castedCondition;

								if (!ctx.addCastedVariables(castedVariableName, c2, castedRecordArguments, castedRecordArgumentsConstructor, object, objectClass)) {
									continue;
								}
								if (ctx.exitFromBlock()) {
									return -2;
								}

								if (castedCondition != null) {
									castedCondition.execute(ctx);
									if (ctx.exitFromBlock()) {
										return -2;
									}
									if (!ctx.value.getBoolean()) {
										if (castedVariableName != null) {
											ctx.removeVariable(castedVariableName);
										}
										if (castedRecordArguments != null) {
											for (int recordArgumentIndex = 0; recordArgumentIndex < castedRecordArguments.length; recordArgumentIndex++) {
												NodeVariable castedRecordArgument = (NodeVariable) castedRecordArguments[recordArgumentIndex];
												ctx.removeVariable(castedRecordArgument.getVariableName());
											}
										}
										continue;
									}
								}
								return i;
							}
						} else if (ctx.value.valueClass.isPrimitive() && ctx.value.valueClass == HiClassPrimitive.BOOLEAN) {
							if (ctx.value.getBoolean()) {
								return i;
							}
						} else if (ctx.value.valueClass.isObject()) {
							if (object == null) {
								// TODO delete?
								if (ctx.value.object == null) {
									return i;
								}
							} else if (hiObject.equals(ctx, (HiObject) ctx.value.object)) {
								return i;
							}
							if (ctx.exitFromBlock()) {
								return -2;
							}
						} else if (ctx.value.valueClass.isNull()) {
							if (object == null) {
								return i;
							}
						}
					}
				} else {
					// default node
					return i;
				}
			}
		}
		return -1;
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
		NodeSwitch node = new NodeSwitch(os.read(HiNode.class));
		node.size = os.readShort();
		node.casesValues = os.readNullableListArray(HiNode.class, node.size);
		node.casesNodes = os.readNullableList(HiNode.class, node.size);
		return node;
	}
}
