package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeDeclaration extends HiNode implements NodeVariable, PrimitiveTypes {
	public NodeDeclaration(Type type, String name, HiNode initialization, Modifiers modifiers, NodeAnnotation[] annotations) {
		super("declaration", TYPE_DECLARATION);
		this.type = type;
		this.name = name.intern();
		this.initialization = initialization;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}

	public NodeDeclaration(String typeName, String name) {
		super("declaration", TYPE_DECLARATION);
		this.type = Type.getTypeByFullName(typeName);
		this.name = name.intern();
	}

	public Type type;

	public String name;

	public HiNode initialization;

	public Modifiers modifiers;

	public NodeAnnotation[] annotations;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass clazz;
		if (type == Type.varType) {
			// assume initialization is not null
			clazz = initialization.getValueClass(validationInfo, ctx);
		} else {
			clazz = type.getClass(ctx);
		}
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (initialization != null) {
			HiClass variableType = getValueClass(validationInfo, ctx);
			if (type == Type.varType) {
				type = Type.getType(variableType);
				// do not check cast
			} else {
				valid = initialization.validate(validationInfo, ctx);
				if (valid) {
					NodeValueType initializationValueType = initialization.getValueType(validationInfo, ctx);
					HiClass initializationType = initializationValueType.type;
					boolean canBeCasted = true;
					if (initializationValueType.isValue) {
						canBeCasted = initializationValueType.autoCastValue(variableType);
					} else {
						canBeCasted = HiClass.autoCast(initializationType, variableType, false);
					}
					if (!canBeCasted) {
						validationInfo.error("incompatible types: " + initializationType.fullName + " cannot be converted to " + variableType.fullName, initialization.getToken());
						valid = false;
					}
				}
			}
			ctx.initializedNodes.add(this);
		}
		// TODO check name, modifiers, annotations
		valid &= ctx.addLocalVariable(this);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// TODO keep in field only runtime annotations
		HiField<?> field = HiField.getField(type, name, initialization);
		field.setModifiers(modifiers);

		try {
			field.execute(ctx);
			ctx.addVariable(field);
		} finally {
			if (ctx.exitFromBlock()) {
				return;
			}
			field.initialized = initialization != null;
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
		os.writeUTF(name);
		os.writeNullable(initialization);
		modifiers.code(os);
		os.writeShortArray(annotations);
	}

	public static NodeDeclaration decode(DecodeContext os) throws IOException {
		return new NodeDeclaration(os.readType(), os.readUTF(), os.readNullable(HiNode.class), Modifiers.decode(os), os.readShortNodeArray(NodeAnnotation.class));
	}

	@Override
	public String getVariableName() {
		return name;
	}

	@Override
	public String getVariableType() {
		return type.name;
	}
}
