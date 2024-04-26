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
		super("declaration", TYPE_DECLARATION, true);
		this.type = type;
		this.name = name.intern();
		this.initialization = initialization;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}

	public NodeDeclaration(String typeName, String name) {
		super("declaration", TYPE_DECLARATION, true);
		this.type = Type.getTypeByFullName(typeName);
		this.name = name.intern();
	}

	public Type type;

	public String name;

	public HiNode initialization;

	public Modifiers modifiers;

	public NodeAnnotation[] annotations;

	private HiClass clazz;

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass clazz;
		if (type == Type.varType) {
			// assume initialization is not null
			if (initialization != null) {
				clazz = initialization.getValueClass(validationInfo, ctx);
				if (clazz.isNull()) {
					clazz = HiClass.OBJECT_CLASS;
					validationInfo.error("invalid var initialization", initialization.getToken());
				}
			} else {
				clazz = HiClass.OBJECT_CLASS;
				validationInfo.error("var is not initialized", getToken());
			}
		} else {
			clazz = type.getClass(ctx);
		}
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		ctx.nodeValueType.type = type;
		return clazz;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = HiNode.validateAnnotations(validationInfo, ctx, annotations);
		valid &= ctx.level.checkUnreachable(validationInfo, getToken());
		clazz = getValueClass(validationInfo, ctx);
		if (type.parameters != null) {
			if (type.parameters.length > 0) {
				valid &= type.validateClass(clazz, validationInfo, ctx, getToken());
			} else {
				validationInfo.error("type parameter expected", getToken());
				valid = false;
			}
		}
		if (initialization != null && ctx.nodeValueType.valid) {
			if (type == Type.varType) {
				type = Type.getType(clazz);
				// do not check cast
			} else {
				ctx.level.variableClass = clazz;
				ctx.level.variableNode = this;
				NodeValueType initializationValueType = initialization.getNodeValueType(validationInfo, ctx);
				if (initializationValueType.valid) {
					boolean canBeCasted = false;
					if (initializationValueType.isCompileValue()) {
						canBeCasted = initializationValueType.autoCastValue(clazz);
					} else if (initializationValueType.returnType != NodeValueType.NodeValueReturnType.classValue) {
						canBeCasted = HiClass.autoCast(ctx, initializationValueType.clazz, clazz, false, true);
					}
					if (!canBeCasted) {
						validationInfo.error("incompatible types: " + initializationValueType.clazz.getNameDescr() + " cannot be converted to " + clazz.getNameDescr(), initialization.getToken());
						valid = false;
					}
				}
			}

			ctx.level.enclosingClass = clazz;
			ctx.level.enclosingType = type;
			valid &= initialization.validate(validationInfo, ctx);

			ctx.level.enclosingClass = null;
			ctx.level.enclosingType = null;

			ctx.initializedNodes.add(this);

			NodeConstructor newNode = initialization.getSingleNode(NodeConstructor.class);
			if (newNode != null) {
				valid &= newNode.validateDeclarationGenericType(type, validationInfo, ctx);
			}
		}

		// TODO check name, modifiers, annotations
		// TODO keep in field only runtime annotations
		valid &= ctx.addLocalVariable(this);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		HiField<?> field = clazz != null ? HiField.getField(clazz, name, initialization, token) : HiField.getField(type, name, initialization, token);
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
