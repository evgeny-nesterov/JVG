package script.ool.model;

import java.io.IOException;
import java.util.List;

import script.ool.model.nodes.CodeContext;
import script.ool.model.nodes.DecodeContext;
import script.ool.model.nodes.NodeArgument;
import script.ool.model.nodes.NodeNative;

public class Method implements Codable {
	public int argCount;

	public Clazz clazz;

	public Modifiers modifiers;

	public Type returnType;

	public String name;

	public NodeArgument[] arguments;

	public Node body;

	public Clazz[] argClasses;

	public String[] argNames;

	public Clazz returnClass;

	public MethodSignature signature;

	private String descr;

	public Method(Clazz clazz, Modifiers modifiers, Type returnType, String name, List<NodeArgument> arguments, Node body) {
		NodeArgument[] _arguments = new NodeArgument[arguments != null ? arguments.size() : 0];
		if (arguments != null) {
			arguments.toArray(_arguments);
		}
		set(clazz, modifiers, returnType, name, _arguments, body);
	}

	public Method(Clazz clazz, Modifiers modifiers, Type returnType, String name, NodeArgument[] arguments, Node body) {
		set(clazz, modifiers, returnType, name, arguments, body);
	}

	private void set(Clazz clazz, Modifiers modifiers, Type returnType, String name, NodeArgument[] arguments, Node body) {
		this.clazz = clazz;
		this.modifiers = modifiers != null ? modifiers : new Modifiers();
		this.returnType = returnType;
		this.name = name.intern();
		this.arguments = arguments;
		this.body = body;

		argCount = arguments != null ? arguments.length : 0;
	}

	public void resolve(RuntimeContext ctx) {
		if (ctx != null && signature == null) {
			if (arguments != null) {
				int length = arguments.length;
				argClasses = new Clazz[length];
				argNames = new String[length];
				for (int i = 0; i < length; i++) {
					argClasses[i] = arguments[i].type.getClazz(ctx);
					argNames[i] = arguments[i].name;
				}
			}
			signature = new MethodSignature(name, argClasses);

			returnClass = returnType.getClazz(ctx);

			if (modifiers.isNative()) {
				body = new NodeNative(clazz, returnClass, name, argClasses, argNames);
			}
		}
	}

	public void invoke(RuntimeContext ctx, Clazz type, Object object, Field<?>[] arguments) {
		if (body != null) {
			if (modifiers.isNative()) {
				ctx.value.valueType = Value.VALUE;
				ctx.value.type = type;
				if (type.isArray()) {
					ctx.value.array = object;
				} else {
					ctx.value.object = (Obj) object;
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
				buf.append(arguments[i].type.name);
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
		modifiers.code(os);
		os.writeType(returnType);
		os.writeUTF(name);

		int count = arguments != null ? arguments.length : 0;
		os.writeByte(count);
		for (int i = 0; i < count; i++) {
			arguments[i].code(os);
		}

		os.writeNullable(body);
	}

	public static Method decode(DecodeContext os) throws IOException {
		Modifiers modifiers = Modifiers.decode(os);

		Type returnType = os.readType();
		String name = os.readUTF();

		int count = os.readByte();
		NodeArgument[] arguments = new NodeArgument[count];
		for (int i = 0; i < count; i++) {
			arguments[i] = NodeArgument.decode(os);
		}

		return new Method(os.getClazz(), modifiers, returnType, name, arguments, os.readNullable(Node.class));
	}
}
