package ru.nest.jvg.macros;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import ru.nest.jvg.JVGPane;

public class JVGMacros {
	private List<JVGMacrosCode> codeList = new ArrayList<JVGMacrosCode>();

	private boolean active = true;

	public JVGMacros() {
	}

	public void appendCode(JVGMacrosCode code) {
		if (active) {
			codeList.add(code);

			System.out.print(getCode().toString());
			System.out.println("===================================");
		}
	}

	public void removeCode(JVGMacrosCode code) {
		if (active) {
			codeList.remove(code);
		}
	}

	public void removeLastCode() {
		if (active && codeList.size() > 0) {
			codeList.remove(codeList.size() - 1);
		}
	}

	public JVGMacrosCode getCode() {
		JVGMacrosCode code = new JVGMacrosCode(JVGMacrosCode.ARG_NONE);
		code.append("long[] ids = getSelection();\n");

		int lastType = JVGMacrosCode.ARG_NONE;
		for (JVGMacrosCode c : codeList) {
			if (c.getArgType() != lastType) {
				if (c.getArgType() == JVGMacrosCode.ARG_ID) {
					code.append("for(int i = 0; i < ids.length; i++) {\n");
					code.append("  long id = ids[i];\n");
				}
			}

			if (c.getArgType() == JVGMacrosCode.ARG_ID) {
				code.append("  ");
			}

			code.append(c);

			if (c.getArgType() != lastType) {
				if (lastType == JVGMacrosCode.ARG_ID) {
					code.append("}\n\n");
				}
			}

			lastType = c.getArgType();
		}

		if (lastType == JVGMacrosCode.ARG_ID) {
			code.append("}\n\n");
		}

		return code;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public static void appendTransform(JVGPane pane, AffineTransform transform) {
		if (pane.isMacrosActive()) {
			double[] matrix = new double[6];
			transform.getMatrix(matrix);
			JVGMacrosCode code = new JVGMacrosCode(JVGMacrosCode.ARG_ID, "transform(id, int[]{%s, %s, %s, %s, %s, %s});\n", matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
			pane.appendMacrosCode(code);
		}
	}

	public static void appendComponentOperation(JVGPane pane, String format, Object... args) {
		if (pane.isMacrosActive()) {
			JVGMacrosCode code = new JVGMacrosCode(JVGMacrosCode.ARG_ID, format + "\n", args);
			pane.appendMacrosCode(code);
		}
	}
}
