package ru.nest.jvg.action;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.GradientDraw;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class FillerTransparencyAction extends JVGAction {
	public FillerTransparencyAction(int alfa, int painterType) {
		super("filler-transparency");
		this.painterType = painterType;
		this.alfa = alfa;
	}

	private int alfa;

	private int painterType;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGShape shape : shapes) {
				setFillerAlfa(pane, edit, shape, alfa, painterType);
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			appendMacrosCode(pane, "setFillerTransparency(id, %s, %s);", JVGMacrosCode.ARG_ID, alfa, painterType);
		}
	}

	public static void setFillerAlfa(JVGPane pane, CompoundUndoRedo edit, JVGShape shape, int alfa, int painterType) {
		Painter painter = null;
		int count = shape.getPaintersCount();
		for (int i = count - 1; i >= 0; i--) {
			Painter p = shape.getPainter(i);
			if (p.getType() == painterType) {
				painter = p;
				break;
			}
		}

		if (painter != null) {
			Draw draw = painter.getPaint();
			if (draw instanceof ColorDraw) {
				setAlfaForSolid(pane, edit, (ColorDraw) draw, alfa);
			} else if (draw instanceof GradientDraw) {
				setAlfaForGradient(pane, edit, (GradientDraw) draw, alfa);
			}
		}
	}

	public static void setAlfaForSolid(JVGPane pane, CompoundUndoRedo edit, ColorDraw draw, int alfa) {
		Resource<Color> oldColor = draw.getResource();
		int red = 255, green = 255, blue = 255, oldAlfa = 255;
		if (oldColor != null) {
			red = oldColor.getResource().getRed();
			green = oldColor.getResource().getGreen();
			blue = oldColor.getResource().getBlue();
			oldAlfa = oldColor.getResource().getAlpha();
		}

		if (alfa != oldAlfa) {
			Resource<Color> color = new ColorResource(new Color(red, green, blue, alfa));
			draw.setResource(color);
			if (edit != null) {
				edit.add(new PropertyUndoRedo("filler-transparency", pane, draw, "setResource", Resource.class, oldColor, color));
			}
		}
	}

	public static void setAlfaForGradient(JVGPane pane, CompoundUndoRedo edit, GradientDraw draw, int alfa) {
		Resource<Color>[] oldColors = draw.getColors();

		boolean changed = false;
		Resource<Color>[] newColors = new Resource[oldColors.length];
		for (int i = 0; i < oldColors.length; i++) {
			Resource<Color> oldColor = oldColors[i];
			int red = 255, green = 255, blue = 255, oldAlfa = 255;
			if (oldColor != null) {
				red = oldColor.getResource().getRed();
				green = oldColor.getResource().getGreen();
				blue = oldColor.getResource().getBlue();
				oldAlfa = oldColor.getResource().getAlpha();
			}

			if (alfa != oldAlfa) {
				newColors[i] = new ColorResource(new Color(red, green, blue, alfa));
				changed = true;
			}
		}

		if (changed) {
			draw.setColors(newColors);
			if (edit != null) {
				edit.add(new PropertyUndoRedo("filler-transparency", pane, draw, "setColors", oldColors, newColors));
			}
		}
	}
}
