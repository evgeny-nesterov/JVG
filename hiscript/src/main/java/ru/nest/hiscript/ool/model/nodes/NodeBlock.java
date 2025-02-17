package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.NodeInitializer;
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

	private int enterType = RuntimeContext.BLOCK;

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

	public void setEnterType(int enterType) {
		this.enterType = enterType;
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
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		if (enterType != RuntimeContext.SAME) {
			ctx.enter(RuntimeContext.BLOCK, this);
		}

		for (HiNode statement : statements) {
			valid &= statement.validate(validationInfo, ctx);
		}

		// TODO check isStatic

		if (enterType != RuntimeContext.SAME) {
			ctx.exit();
		}

		valid &= NodeReturn.validateLambdaReturn(validationInfo, ctx, this, token);
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		if (enterType != RuntimeContext.SAME) {
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
			if (enterType != RuntimeContext.SAME) {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeBoolean(isStatic);
		os.writeByte(enterType);
		os.writeShort(statements.size());
		os.write(statements);
	}

	public static NodeBlock decode(DecodeContext os) throws IOException {
		NodeBlock node = new NodeBlock();
		node.setStatic(os.readBoolean());
		node.setEnterType(os.readByte());
		node.statements = os.readList(HiNode.class, os.readShort());
		return node;
	}
}
