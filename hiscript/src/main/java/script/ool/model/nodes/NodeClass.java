package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.ClassLoadListener;
import script.ool.model.Clazz;
import script.ool.model.NoClassException;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;

public class NodeClass extends Node {
	public NodeClass(Clazz clazz) {
		super("class", TYPE_CLASS);
		this.clazz = clazz;

		if (clazz == null) {
			throw new RuntimeException("class is null");
		}
	}

	private NodeClass() {
		super("class", TYPE_CLASS);
	}

	private Clazz clazz;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.addClass(clazz);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeClass(clazz);
	}

	public static NodeClass decode(DecodeContext os) throws IOException {
		try {
			return new NodeClass(os.readClass());
		} catch (NoClassException exc) {
			final NodeClass node = new NodeClass();
			os.addClassLoadListener(new ClassLoadListener() {
				@Override
				public void classLoaded(Clazz clazz) {
					node.clazz = clazz;
				}
			}, exc.getIndex());
			return node;
		}
	}
}
