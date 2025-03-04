package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NodeBlock extends HiNode implements NodeInitializer {
	public NodeBlock() {
		this("block");
	}

	public NodeBlock(String name) {
		super(name, TYPE_BLOCK, true);
	}

	public NodeBlock(HiNode statement) {
		this();
		addStatement(statement);
	}

	public List<HiNode> statements = new ArrayList<>(0);

	private boolean isStatic;

	private ContextType enterType = ContextType.BLOCK;

	public void addStatement(HiNode statement) {
		statements.add(statement);
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public void setEnterType(ContextType enterType) {
		this.enterType = enterType;
	}

	public <N extends HiNodeIF> N getSingleStatement() {
		if (statements != null && statements.size() == 1) {
			return (N) statements.get(0);
		}
		return null;
	}

	public <N extends HiNodeIF> N getSingleStatement(Class<N> clazz) {
		HiNodeIF singleStatement = getSingleStatement();
		if (singleStatement != null && clazz.isAssignableFrom(singleStatement.getClass())) {
			return (N) singleStatement;
		}
		return null;
	}

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		for (HiNode statement : statements) {
			if (statement.isReturnStatement(label, labels)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public NodeReturn getReturnNode() {
		for (HiNode statement : statements) {
			NodeReturn returnNode = statement.getReturnNode();
			if (returnNode != null) {
				return returnNode;
			}
		}
		return null;
	}

	@Override
	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		NodeReturn returnNode = getReturnNode();
		HiClass returnClass;
		if (returnNode != null) {
			returnClass = returnNode.getValueClass(validationInfo, ctx);
			ctx.nodeValueType.type = Type.getType(returnClass);
		} else {
			returnClass = HiClassPrimitive.VOID;
			ctx.nodeValueType.type = Type.voidType;
		}
		ctx.nodeValueType.clazz = returnClass;
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		return returnClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		if (enterType != ContextType.SAME) {
			ctx.enter(ContextType.BLOCK, this);
		}

		for (HiNode statement : statements) {
			valid &= statement.validate(validationInfo, ctx);
		}

		// TODO check isStatic

		if (enterType != ContextType.SAME) {
			ctx.exit();
		}

		valid &= NodeReturn.validateLambdaReturn(validationInfo, ctx, this, token);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		if (enterType != ContextType.SAME) {
			ctx.enter(enterType, token);
		}

		try {
			int size = statements.size();
			for (int i = 0; i < size; i++) {
				statements.get(i).execute(ctx);

				if (ctx.exitFromBlock()) {
					return;
				}
				if (ctx.isBreak || ctx.isContinue) {
					break;
				}
			}
		} finally {
			if (enterType != ContextType.SAME) {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeBoolean(isStatic);
		os.writeEnum(enterType);
		os.writeShort(statements.size());
		os.write(statements);
	}

	public static NodeBlock decode(DecodeContext os) throws IOException {
		NodeBlock node = new NodeBlock();
		node.setStatic(os.readBoolean());
		node.setEnterType(ContextType.values()[os.readByte()]);
		node.statements = os.readList(HiNode.class, os.readShort());
		return node;
	}
}
