package ru.nest.jvg.action;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGScriptSupport;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Texture;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.EndingsPainter;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.LinearGradientDraw;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.RadialGradientDraw;
import ru.nest.jvg.shape.paint.ShadowPainter;
import ru.nest.jvg.shape.paint.TextureDraw;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;
import ru.nest.swing.gradient.LinearGradient;
import ru.nest.swing.gradient.RadialGradient;

import javax.swing.event.UndoableEditEvent;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DrawAction extends JVGAction {
	public final static int NONE = 0;

	public final static int SOLID = 1;

	public final static int LINEAR_GRADIENT = 2;

	public final static int RADIAL_GRADIENT = 3;

	public final static int TEXTURE = 4;

	public DrawAction(String name, Resource resource, int painterType) {
		super(name);
		if (resource.getResource() instanceof Color) {
			type = SOLID;
			this.colorResource = resource;
		} else if (resource.getResource() instanceof LinearGradient) {
			type = LINEAR_GRADIENT;
			this.linearGradient = resource;
		} else if (resource.getResource() instanceof RadialGradient) {
			type = RADIAL_GRADIENT;
			this.radialGradient = resource;
		} else if (resource.getResource() instanceof Texture) {
			type = TEXTURE;
			this.textureResource = resource;
		}
		this.painterType = painterType;
	}

	public DrawAction(int painterType) {
		super("no-fill");
		this.painterType = painterType;
		type = NONE;
	}

	private int type;

	private int painterType = Painter.FILL;

	private Resource<Texture> textureResource;

	private Resource<Color> colorResource;

	private Resource<LinearGradient> linearGradient;

	private Resource<RadialGradient> radialGradient;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGShape shape : shapes) {
				processShape(pane, shape, edit);
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();

				switch (type) {
					case SOLID:
						appendMacrosCode(pane, "setSolidDraw(id, %s, %s);", JVGMacrosCode.ARG_ID, painterType, colorResource.getResource().getRGB());
						break;

					case LINEAR_GRADIENT:
						LinearGradient linearGradient = this.linearGradient.getResource();
						appendMacrosCode(pane, "setLinearGradientDraw(id, %s, %s, %s, %s, %s, %s);", JVGMacrosCode.ARG_ID, painterType, linearGradient.getX1(), linearGradient.getY1(), linearGradient.getX2(), linearGradient.getY2(), JVGScriptSupport.getColors(linearGradient.getColors()));
						break;

					case RADIAL_GRADIENT:
						RadialGradient radialGradient = this.radialGradient.getResource();
						appendMacrosCode(pane, "setRadialGradientDraw(id, %s, %s, %s, %s, %s, %s, %s);", JVGMacrosCode.ARG_ID, painterType, radialGradient.getCX(), radialGradient.getCY(), radialGradient.getFX(), radialGradient.getFY(), radialGradient.getR(), JVGScriptSupport.getColors(radialGradient.getColors()));
						break;

					case TEXTURE:
						appendMacrosCode(pane, "setTextureDraw(id, %s, %s, %s, %s, %s, \"%s\");", JVGMacrosCode.ARG_ID, painterType, textureResource.getResource().getAnchorX(), textureResource.getResource().getAnchorY(), textureResource.getResource().getAnchorWidth(), textureResource.getResource().getAnchorHeight(), textureResource.getResource().getImage().getName());
						break;
				}
			}
		}
	}

	private void processShape(JVGPane pane, JVGShape shape, CompoundUndoRedo edit) {
		if (shape instanceof JVGGroup || shape instanceof JVGGroupPath) {
			for (JVGComponent c : shape.getChildren()) {
				if (c instanceof JVGShape) {
					processShape(pane, (JVGShape) c, edit);
				}
			}
			return;
		}

		// find painter
		Painter currentPainter = null;
		int count = shape.getPaintersCount();
		for (int i = count - 1; i >= 0; i--) {
			Painter p = shape.getPainter(i);
			if (p.getType() == painterType) {
				currentPainter = p;
				break;
			}
		}

		// add new painter to shape if painter was not found
		if (currentPainter == null) {
			currentPainter = addPainter(pane, shape, edit);
			if (currentPainter == null) {
				return;
			}
		}

		// update draw
		Draw draw = currentPainter.getPaint();
		int currentDrawType = getDrawType(draw);
		if (type != currentDrawType) {
			setDraw(pane, shape, currentPainter, edit);
		} else {
			changeDraw(pane, shape, edit, currentPainter);
		}
	}

	private Painter addPainter(JVGPane pane, JVGShape shape, CompoundUndoRedo edit) {
		Painter painter = createPainter(painterType);
		if (painter != null) {
			shape.addPainter(0, painter);

			Class<?>[] p = { Painter.class };
			Object[] o = { painter };
			edit.add(new PropertyUndoRedo(getName(), pane, shape, "removePainter", p, o, "addPainter", p, o));

			if (painterType == Painter.FILL) {
				if (!shape.isFill()) {
					shape.setFill(true);
					edit.add(new PropertyUndoRedo(getName(), pane, shape, "setFill", false, true));
				}
			}
		}
		return painter;
	}

	private void removePainter(JVGPane pane, JVGShape shape, CompoundUndoRedo edit, Painter painter) {
		if (painter != null) {
			shape.removePainter(painter);

			Class<?>[] c = { Painter.class };
			Object[] o = { painter };
			edit.add(new PropertyUndoRedo(getName(), pane, shape, "addPainter", c, o, "removePainter", c, o));
		}
	}

	public static int getDrawType(Draw draw) {
		if (draw instanceof ColorDraw) {
			return SOLID;
		} else if (draw instanceof LinearGradientDraw) {
			return LINEAR_GRADIENT;
		} else if (draw instanceof RadialGradientDraw) {
			return RADIAL_GRADIENT;
		} else if (draw instanceof TextureDraw) {
			return TEXTURE;
		} else {
			return NONE;
		}
	}

	private Draw createDraw() {
		switch (type) {
			case SOLID:
				return new ColorDraw(colorResource);
			case LINEAR_GRADIENT:
				return new LinearGradientDraw(linearGradient);
			case RADIAL_GRADIENT:
				return new RadialGradientDraw(radialGradient);
			case TEXTURE:
				return new TextureDraw(textureResource);
		}
		return null;
	}

	public static Painter createPainter(int type) {
		switch (type) {
			case Painter.FILL:
				return new FillPainter();
			case Painter.OUTLINE:
				return new OutlinePainter();
			case Painter.SHADOW:
				return new ShadowPainter();
			case Painter.ENDINGS:
				return new EndingsPainter();
		}
		return null;
	}

	private void setDraw(JVGPane pane, JVGShape shape, Painter painter, CompoundUndoRedo edit) {
		Draw draw = createDraw(); // may be null if type=NONE
		Draw oldDraw = painter.getPaint();
		painter.setPaint(draw);
		edit.add(new PropertyUndoRedo(getName(), pane, painter, "setPaint", Draw.class, oldDraw, draw));
	}

	private void changeDraw(JVGPane pane, JVGShape shape, CompoundUndoRedo edit, Painter painter) {
		Draw<?> draw = painter.getPaint();
		switch (type) {
			case SOLID:
				changeSolidDraw(pane, shape, edit, (ColorDraw) draw);
				break;

			case LINEAR_GRADIENT:
				changeLinearGradientDraw(pane, shape, edit, (LinearGradientDraw) draw);
				break;

			case RADIAL_GRADIENT:
				changeRadialGradientDraw(pane, shape, edit, (RadialGradientDraw) draw);
				break;

			case TEXTURE:
				changeTextureDraw(pane, shape, edit, (TextureDraw) draw);
				break;
		}
	}

	private void changeSolidDraw(JVGPane pane, JVGShape shape, CompoundUndoRedo edit, ColorDraw draw) {
		Resource<Color> oldColor = draw.getResource();
		if (oldColor != colorResource) {
			int oldAlfa = 255;
			if (oldColor != null) {
				oldAlfa = oldColor.getResource().getAlpha();
			}

			// don't change alfa as it may be set separately
			Resource<Color> newColor = colorResource;
			if (colorResource != null && colorResource.getResource() != null && oldAlfa != colorResource.getResource().getAlpha()) {
				newColor = new ColorResource(new Color(newColor.getResource().getRed(), newColor.getResource().getGreen(), newColor.getResource().getBlue(), oldAlfa));
			}

			draw.setResource(newColor);
			edit.add(new PropertyUndoRedo(getName(), pane, draw, "setResource", Resource.class, oldColor, newColor));
		}
	}

	private void changeLinearGradientDraw(JVGPane pane, JVGShape shape, CompoundUndoRedo edit, LinearGradientDraw draw) {
		setDraw(pane, shape, edit, draw, draw.getResource(), linearGradient);
	}

	private void changeRadialGradientDraw(JVGPane pane, JVGShape shape, CompoundUndoRedo edit, RadialGradientDraw draw) {
		setDraw(pane, shape, edit, draw, draw.getResource(), radialGradient);
	}

	private void changeTextureDraw(JVGPane pane, JVGShape shape, CompoundUndoRedo edit, TextureDraw draw) {
		setDraw(pane, shape, edit, draw, draw.getResource(), textureResource);
	}

	private void setDraw(JVGPane pane, JVGShape shape, CompoundUndoRedo edit, Draw draw, Resource oldResource, Resource newResource) {
		Object oldValue = oldResource.getResource();
		Object newValue = newResource.getResource();
		if (oldResource.getName() != null && oldResource.getName().equals(newResource.getName())) {
			// resource is not changed
			if (!oldValue.equals(newValue)) {
				// value changed
				oldResource.setResource(newValue);
				draw.setResource(newResource);
				edit.add(new PropertyUndoRedo(getName(), pane, oldResource, "setResource", Object.class, oldValue, newValue));
				edit.add(new PropertyUndoRedo(getName(), pane, draw, "setResource", Resource.class, oldResource, newResource));
			}
		} else {
			// resource is changed
			draw.setResource(newResource);
			edit.add(new PropertyUndoRedo(getName(), pane, draw, "setResource", Resource.class, oldResource, newResource));
		}
	}
}
