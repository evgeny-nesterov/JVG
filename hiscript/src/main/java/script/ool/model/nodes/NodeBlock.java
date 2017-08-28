package script.ool.model.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import script.ool.model.Node;
import script.ool.model.NodeInitializer;
import script.ool.model.RuntimeContext;

public class NodeBlock extends Node implements NodeInitializer {
	public NodeBlock() {
		this("block");
	}

	public NodeBlock(String name) {
		super(name, TYPE_BLOCK);
	}

	private List<Node> statements = new ArrayList<Node>(0);

	public void addStatement(Node statement) {
		statements.add(statement);
	}

	private boolean isStatic;

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

	public void execute(RuntimeContext ctx) {
		if (enterType != RuntimeContext.SAME) {
			ctx.enter(enterType, line);
		}

		try {
			int size = statements.size();
			for (int i = 0; i < size; i++) {
				Node statement = statements.get(i);
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
		node.statements = os.readList(Node.class, os.readShort());
		return node;
	}
}
