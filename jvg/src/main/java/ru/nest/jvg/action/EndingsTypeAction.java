package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.Endings;
import ru.nest.jvg.shape.paint.EndingsPainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class EndingsTypeAction extends JVGAction {
	public final static int MODIFY_TYPE = 1;

	public final static int MODIFY_DIRECT = 2;

	public final static int MODIFY_FIGURE = 4;

	public final static int MODIFY_FILL = 8;

	public final static int MODIFY_ALL = MODIFY_TYPE | MODIFY_DIRECT | MODIFY_FIGURE | MODIFY_FILL;

	public EndingsTypeAction() {
		super("endings-none");
		endings = new Endings();
		endings.setEndingType(Endings.TYPE_NONE);
	}

	public EndingsTypeAction(int type) {
		this(new Endings(type), MODIFY_TYPE);
	}

	public EndingsTypeAction(Endings endings) {
		this(endings, MODIFY_ALL);
		this.endings = endings;
	}

	public EndingsTypeAction(Endings endings, int modifyType) {
		super("endings");
		this.endings = endings;
		this.modifyType = modifyType;
	}

	private Endings endings;

	private int modifyType;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGShape shape : shapes) {
				setEndings(pane, shape, getName(), edit, endings, modifyType);
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			appendMacrosCode(pane, "setEndings(id, %s);", JVGMacrosCode.ARG_ID, endings.getEndingType());
		}
	}

	public static void setEndings(JVGPane pane, JVGShape shape, String actionName, CompoundUndoRedo edit, Endings endings, int modifyType) {
		Painter painter = null;
		int count = shape.getPaintersCount();
		for (int i = count - 1; i >= 0; i--) {
			Painter p = shape.getPainter(i);
			if (p.getType() == Painter.ENDINGS) {
				painter = p;
				break;
			}
		}

		if (endings.getEndingType() == Endings.TYPE_NONE) {
			if (painter != null) {
				shape.removePainter(painter);

				Class<?>[] p = { Painter.class };
				Object[] o = { painter };
				edit.add(new PropertyUndoRedo(actionName, pane, shape, "addPainter", p, o, "removePainter", p, o));
			}
		} else if (painter == null) {
			EndingsPainter epainter = new EndingsPainter(endings, ColorResource.black);
			shape.addPainter(epainter);

			Class<?>[] p = { Painter.class };
			Object[] o = { epainter };
			edit.add(new PropertyUndoRedo(actionName, pane, shape, "removePainter", p, o, "addPainter", p, o));
		} else if (painter instanceof EndingsPainter) {
			setEndings(pane, (EndingsPainter) painter, actionName, edit, endings, modifyType);
		}
	}

	public static void setEndings(JVGPane pane, EndingsPainter painter, String actionName, CompoundUndoRedo edit, Endings endings, int modifyType) {
		Endings curEndings = painter.getEndings();
		if ((modifyType & MODIFY_TYPE) != 0 && curEndings.getEndingType() != endings.getEndingType()) {
			edit.add(new PropertyUndoRedo(actionName, pane, curEndings, "setEndingType", int.class, curEndings.getEndingType(), endings.getEndingType()));
			curEndings.setEndingType(endings.getEndingType());
		}

		if ((modifyType & MODIFY_DIRECT) != 0 && curEndings.isDirect() != endings.isDirect()) {
			edit.add(new PropertyUndoRedo(actionName, pane, curEndings, "setDirect", boolean.class, curEndings.isDirect(), endings.isDirect()));
			curEndings.setDirect(endings.isDirect());
		}

		if ((modifyType & MODIFY_FIGURE) != 0 && curEndings.getFigure() != endings.getFigure()) {
			edit.add(new PropertyUndoRedo(actionName, pane, curEndings, "setFigure", int.class, curEndings.getFigure(), endings.getFigure()));
			curEndings.setFigure(endings.getFigure());
		}

		if ((modifyType & MODIFY_FILL) != 0 && curEndings.isFill() != endings.isFill()) {
			edit.add(new PropertyUndoRedo(actionName, pane, curEndings, "setFill", boolean.class, curEndings.isFill(), endings.isFill()));
			curEndings.setFill(endings.isFill());
		}
	}
}
