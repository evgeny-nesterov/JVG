package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.NodeInitializer;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	public void addStatement(HiNode statement) {
		statements.add(statement);
	}

	private boolean isStatic;

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	private int enterType = RuntimeContext.BLOCK;

	public void setEnterType(int enterType) {
		this.enterType = enterType;
	}

	public NodeReturn getReturnNode(ValidationInfo validationInfo, CompileClassContext ctx) {
		NodeReturn firstReturnNode = null;
		for (HiNode statement : statements) {
			if (statement instanceof NodeReturn) {
				NodeReturn returnNode = (NodeReturn) statement;
				if (firstReturnNode == null) {
					firstReturnNode = returnNode;
				} else {
					validationInfo.error("unreachable statement", returnNode.getToken());
					break;
				}
			} else if (statement instanceof NodeBlock) {
				NodeBlock block = (NodeBlock) statement;
				NodeReturn returnNode = block.getReturnNode(validationInfo, ctx);
				if (returnNode != null) {
					if (firstReturnNode == null) {
						firstReturnNode = returnNode;
					} else {
						validationInfo.error("unreachable statement", returnNode.getToken());
						break;
					}
				}
			}
		}
		return firstReturnNode;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (enterType != RuntimeContext.SAME) {
			ctx.enter(RuntimeContext.BLOCK, this);
		}

		boolean valid = true;
		// TODO check isStatic
		boolean terminated = false;
		boolean isUnreachable = false;
		for (HiNode statement : statements) {
			valid &= statement.validate(validationInfo, ctx);
			if (terminated && !isUnreachable) {
				validationInfo.error("unreachable statement", statement.getToken());
				isUnreachable = true;
				valid = false;
			}
			if (statement.isTerminal()) {
				terminated = true;
			}
		}

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
				HiNode statement = statements.get(i);
				statement.execute(ctx);

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
