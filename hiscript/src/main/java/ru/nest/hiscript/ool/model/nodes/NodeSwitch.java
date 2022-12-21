package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeSwitch extends HiNode {
	public NodeSwitch(HiNode valueNode) {
		super("switch", TYPE_SWITCH);
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
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = valueNode.validate(validationInfo, ctx) && valueNode.expectValue(validationInfo, ctx);
		HiClass valueClass = valueNode.getValueType(validationInfo, ctx);
		ctx.enter(RuntimeContext.SWITCH, this);
		if (valueClass.isEnum()) {
			HiClassEnum enumClass = (HiClassEnum) valueClass;
			for (int i = 0; i < size; i++) {
				HiNode[] caseValueNodes = casesValues.get(i);
				if (caseValueNodes != null) { // not default
					for (int j = 0; j < caseValueNodes.length; j++) {
						HiNode caseValueNode = caseValueNodes[j];
						if (caseValueNode instanceof NodeExpressionNoLS) {
							NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) caseValueNode;
							NodeIdentifier identifier = exprCaseValueNode.checkIdentifier();
							if (identifier != null) {
								int enumOrdinal = enumClass.getEnumOrdinal(identifier.getName());
								if (enumOrdinal == -1) {
									validationInfo.error("Cannot resolve symbol '" + identifier.getName() + "'", caseValueNode.getToken());
									valid = false;
								}
							}
						}
					}
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				HiNode[] caseValueNodes = casesValues.get(i);
				if (caseValueNodes != null) { // not default
					for (int j = 0; j < caseValueNodes.length; j++) {
						HiNode caseValueNode = caseValueNodes[j];
						valid &= caseValueNode.validate(validationInfo, ctx) && caseValueNode.expectValue(validationInfo, ctx);
					}
				}
			}
		}
		for (int i = 0; i < size; i++) {
			valid &= casesNodes.get(i).validate(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		int index = getCaseIndex(ctx, valueNode, size, casesValues);
		if (index >= 0) {
			ctx.enter(RuntimeContext.SWITCH, token);
			try {
				for (int i = index; i < size; i++) {
					HiNode caseBody = casesNodes.get(i);
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

	public static int getCaseIndex(RuntimeContext ctx, HiNode valueNode, int size, List<HiNode[]> casesValues) {
		valueNode.execute(ctx);
		if (ctx.exitFromBlock()) {
			return -2;
		}

		if (ctx.value.type.isPrimitive()) {
			int value = ctx.value.getInt();
			if (ctx.exitFromBlock()) {
				return -2;
			}
			FOR:
			for (int i = 0; i < size; i++) {
				HiNode[] caseValueNodes = casesValues.get(i);
				if (caseValueNodes != null && caseValueNodes.length > 0) {
					for (int j = 0; j < caseValueNodes.length; j++) {
						HiNode caseValueNode = caseValueNodes[j];
						caseValueNode.execute(ctx);
						if (ctx.exitFromBlock()) {
							return -2;
						}

						int caseValue = ctx.value.getInt();
						if (ctx.exitFromBlock()) {
							return -2;
						}

						if (value == caseValue) {
							return i;
						}
					}
				} else {
					// default node
					return i;
				}
			}
		} else if (ctx.value.type.isObject()) {
			HiObject object = ctx.value.object;
			if (object.clazz.isEnum()) {
				HiClassEnum enumClass = (HiClassEnum) object.clazz;
				FOR:
				for (int i = 0; i < size; i++) {
					HiNode[] caseValueNodes = casesValues.get(i);
					if (caseValueNodes != null && caseValueNodes.length > 0) {
						for (int j = 0; j < caseValueNodes.length; j++) {
							HiNode caseValueNode = caseValueNodes[j];
							if (caseValueNode instanceof NodeExpressionNoLS) {
								NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) caseValueNode;
								NodeIdentifier identifier = exprCaseValueNode.checkIdentifier();
								if (identifier != null) {
									int enumOrdinal = enumClass.getEnumOrdinal(identifier.getName());
									if (enumOrdinal == -1) {
										ctx.throwException("RuntimeException", "Cannot resolve symbol '" + identifier.getName() + "'");
										return -2;
									}

									if (object.getField(ctx, "ordinal").get().equals(enumOrdinal)) {
										return i;
									}
									continue;
								}
							}

							ctx.throwException("RuntimeException", "An enum switch case label must be the unqualified name of an enumeration constant");
							return -2;
						}
					} else {
						// default node
						return i;
					}
				}
			} else {
				FOR:
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
								HiClass c1 = object.clazz;
								HiClass c2 = ctx.value.type;
								boolean isInstanceof = c1.isInstanceof(c2);
								if (isInstanceof) {
									if (ctx.value.castedVariableName != null) {
										if (ctx.getVariable(ctx.value.castedVariableName) != null) {
											ctx.throwRuntimeException("Variable '" + ctx.value.castedVariableName + "' is already defined in the scope");
											return -2;
										}

										HiFieldObject castedField = (HiFieldObject) HiField.getField(Type.getType(c2), ctx.value.castedVariableName);
										castedField.set(object);
										ctx.addVariable(castedField);
									}
									if (ctx.value.castedRecordArguments != null) {
										if (!c2.isRecord()) {
											ctx.throwRuntimeException("Inconvertible types; cannot cast " + c2.fullName + " to Record");
											return -2;
										}
										for (NodeArgument castedRecordArgument : ctx.value.castedRecordArguments) {
											HiField castedField = object.getField(ctx, castedRecordArgument.name, c2);
											ctx.addVariable(castedField);
										}
									}
									if (ctx.value.castedCondition != null) {
										ctx.value.castedCondition.execute(ctx);
										if (!ctx.value.getBoolean()) {
											continue;
										}
									}
									return i;
								}
							} else if (ctx.value.type.isPrimitive() && ctx.value.type.name.equals("boolean")) {
								if (ctx.value.getBoolean()) {
									return i;
								}
							} else if (ctx.value.type.isObject()) {
								if (object.equals(ctx, ctx.value.object)) {
									return i;
								}
								if (ctx.exitFromBlock()) {
									return -2;
								}
							}
						}
					} else {
						// default node
						return i;
					}
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
