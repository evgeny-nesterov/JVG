package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeNative;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.List;

public class HiMethod implements Codeable {
	public int argCount;

	public HiClass clazz;

	public NodeAnnotation[] annotations;

	public Modifiers modifiers;

	public Type returnType;

	public String name;

	public NodeArgument[] arguments;

	public Type[] throwsTypes;

	public Node body;

	public HiClass[] argClasses;

	public String[] argNames;

	public HiClass returnClass;

	public MethodSignature signature;

	public Token token;

	private String descr;

	public HiMethod(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, Type returnType, String name, List<NodeArgument> arguments, Type[] throwsTypes, Node body, Token token) {
		NodeArgument[] _arguments = null;
		if (arguments != null) {
			_arguments = new NodeArgument[arguments.size()];
			arguments.toArray(_arguments);
		}
		set(clazz, annotations, modifiers, returnType, name, _arguments, throwsTypes, body, token);
	}

	public HiMethod(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, Type returnType, String name, NodeArgument[] arguments, Type[] throwsTypes, Node body, Token token) {
		set(clazz, annotations, modifiers, returnType, name, arguments, throwsTypes, body, token);
	}

	private void set(HiClass clazz, NodeAnnotation[] annotations, Modifiers modifiers, Type returnType, String name, NodeArgument[] arguments, Type[] throwsTypes, Node body, Token token) {
		this.clazz = clazz;
		this.annotations = annotations;
		this.modifiers = modifiers != null ? modifiers : new Modifiers();
		this.returnType = returnType;
		this.name = name.intern();
		this.arguments = arguments;
		this.throwsTypes = throwsTypes;
		this.body = body;
		this.argCount = arguments != null ? arguments.length : 0;
		this.token = token;
	}

	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(RuntimeContext.METHOD);
		boolean valid = true;
		if (arguments != null) {
			for (NodeArgument argument : arguments) {
				valid &= argument.validate(validationInfo, ctx);
			}
		}
		if (body != null) {
			body.validate(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	public boolean hasVarargs() {
		return argCount > 0 && arguments[argCount - 1].isVarargs();
	}

	public void resolve(RuntimeContext ctx) {
		if (ctx != null && signature == null) {
			if (arguments != null) {
				int length = arguments.length;
				argClasses = new HiClass[length];
				argNames = new String[length];
				for (int i = 0; i < length; i++) {
					argClasses[i] = arguments[i].getType().getClass(ctx);
					argNames[i] = arguments[i].name;
				}
			}
			signature = new MethodSignature(name, argClasses);

			returnClass = returnType.getClass(ctx);

			if (modifiers.isNative()) {
				body = new NodeNative(clazz, returnClass, name, argClasses, argNames);
			}
		}
	}

	public void invoke(RuntimeContext ctx, HiClass type, Object object, HiField<?>[] arguments) {
		if (body != null) {
			if (modifiers.isNative()) {
				ctx.value.valueType = Value.VALUE;
				ctx.value.type = type;
				if (type.isArray()) {
					ctx.value.array = object;
				} else {
					ctx.value.object = (HiObject) object;
				}
			}
			body.execute(ctx);
		}
	}

	@Override
	public String toString() {
		if (descr == null) {
			StringBuilder buf = new StringBuilder();
			buf.append(name);
			buf.append('(');
			for (int i = 0; i < argCount; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				buf.append(arguments[i].getTypeName());
				buf.append(' ');
				buf.append(arguments[i].name);
			}
			buf.append(')');
			descr = buf.toString();
		}
		return descr;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		// do not write class as when method will being read the class will not
		// be yet created
		// os.writeClass(clazz);
		os.writeShortArray(annotations);
		modifiers.code(os);
		os.writeType(returnType);
		os.writeUTF(name);
		os.writeByte(argCount);
		os.writeNullable(arguments);
		os.writeByte(throwsTypes != null ? throwsTypes.length : 0);
		os.writeNullable(throwsTypes);
		os.writeNullable(body);
		os.writeToken(token);
	}

	public static HiMethod decode(DecodeContext os) throws IOException {
		NodeAnnotation[] annotations = os.readShortNodeArray(NodeAnnotation.class);
		Modifiers modifiers = Modifiers.decode(os);
		Type returnType = os.readType();
		String name = os.readUTF();
		NodeArgument[] arguments = os.readNullableNodeArray(NodeArgument.class, os.readByte());
		Type[] throwsTypes = os.readNullableArray(Type.class, os.readByte());
		Node body = os.readNullable(Node.class);
		Token token = os.readToken();
		return new HiMethod(os.getHiClass(), annotations, modifiers, returnType, name, arguments, throwsTypes, body, token);
	}

	public boolean isJava() {
		return false;
	}
}
