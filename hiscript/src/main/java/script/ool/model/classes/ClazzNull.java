package script.ool.model.classes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.nodes.CodeContext;
import script.ool.model.nodes.DecodeContext;

public class ClazzNull extends Clazz {
	public final static ClazzNull NULL = new ClazzNull();

	private ClazzNull() {
		super((Clazz) null, null, "null", CLASS_TYPE_TOP);
	}

	public boolean isNull() {
		return true;
	}

	public boolean isObject() {
		return false;
	}

	public void code(CodeContext os) throws IOException {
		// write class type
		os.writeByte(Clazz.CLASS_NULL);
	}

	public static Clazz decode(DecodeContext os) throws IOException {
		return NULL;
	}
}
