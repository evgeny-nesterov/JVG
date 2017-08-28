package ru.nest.jvg.action;

import java.awt.Shape;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

public class FlatPathAction extends JVGAction {
	public FlatPathAction() {
		this(-1);
	}

	public FlatPathAction(double flatness) {
		super("flat-path");
		this.flatness = flatness;
	}

	private double flatness;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPath[] pathes = getPathes(e);
		if (pathes != null && pathes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			double flatness = this.flatness;
			for (JVGPath path : pathes) {
				if (flatness <= 0) {
					flatness = chooseFlatness(pane);
					if (flatness <= 0) {
						return;
					}
				}

				Shape oldShape = path.getShape();
				MutableGeneralPath newShape = new MutableGeneralPath(MutableGeneralPath.WIND_NON_ZERO);
				newShape.append(new MutableGeneralPath(oldShape).getPathIterator(null, flatness), true);
				path.setShape(newShape);
				path.invalidate();

				edit.add(new ShapeChangedUndoRedo(getName(), pane, path, oldShape, newShape));
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			appendMacrosCode(pane, "flatPath(id, %s);", JVGMacrosCode.ARG_ID, flatness);
		}
	}

	private static double FLATNESS = 5;

	public static double chooseFlatness(JVGPane pane) {
		JTextField txt = new JTextField("" + FLATNESS, 6);
		while (true) {
			int option = JOptionPane.showConfirmDialog(pane, txt, "Enter flatness", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (option == JOptionPane.OK_OPTION) {
				try {
					double value = Double.parseDouble(txt.getText());
					if (value > 0) {
						FLATNESS = value;
						return value;
					}
				} catch (Exception exc) {
				}
			} else {
				break;
			}
		}

		return -1;
	}
}
