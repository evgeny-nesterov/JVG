package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Node;
import script.ool.model.Obj;
import script.ool.model.Operation;
import script.ool.model.Operations;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeInvocation extends Node {
	public NodeInvocation(String name, boolean innerInvocation, Node[] arguments) {
		this(name, arguments);
		setInner(innerInvocation);
	}

	public NodeInvocation(String name, Node[] arguments) {
		super("invocation", TYPE_INVOCATION);
		this.name = name.intern();
		this.arguments = arguments;
	}

	private String name;

	private Node[] arguments;

	private boolean innerInvocation;

	public void setInner(boolean innerInvocation) {
		this.innerInvocation = innerInvocation;
	}

	public void execute(RuntimeContext ctx) {
		if (!innerInvocation) {
			ctx.value.valueType = Value.METHOD;
			ctx.value.name = name;
			ctx.value.arguments = arguments;
		} else {
			Value[] vs = ctx.getValues(1);
			try {
				// v1 - contains value as object
				ctx.value.object = ctx.level.object;
				ctx.value.type = ctx.level.clazz;
				if (ctx.value.object != null) {
					ctx.value.valueType = Value.VALUE;
				} else {
					// static context
					ctx.value.valueType = Value.CLASS;
				}

				if (ctx.value.type == null) {
					// TODO: error
				}

				// v2 - contains method attributes (name, arguments)
				Value v = vs[0];
				v.valueType = Value.METHOD;
				v.name = name;
				v.arguments = arguments;

				Operation o = Operations.getOperation(Operations.INVOCATION);
				o.doOperation(ctx, ctx.value, v);
			} finally {
				ctx.putValues(vs);
			}
		}
	}

	// TODO: do more usable
	public static void invoke(RuntimeContext ctx, Obj object, String methodName, Node... arguments) {
		Value[] vs = ctx.getValues(1);
		try {
			// v1 - contains value as object
			ctx.value.valueType = Value.VALUE;
			ctx.value.object = object;
			ctx.value.type = object.clazz;

			// v2 - contains method attributes (name, arguments)
			Value v = vs[0];
			v.valueType = Value.METHOD;
			v.name = methodName;
			v.arguments = arguments;

			Operation o = Operations.getOperation(Operations.INVOCATION);
			o.doOperation(ctx, ctx.value, v);
		} finally {
			ctx.putValues(vs);
		}
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeUTF(name);
		os.writeBoolean(innerInvocation);
		os.writeByte(arguments != null ? arguments.length : 0);
		os.write(arguments);
	}

	public static NodeInvocation decode(DecodeContext os) throws IOException {
		return new NodeInvocation(os.readUTF(), os.readBoolean(), os.readArray(Node.class, os.readByte()));
	}
}
