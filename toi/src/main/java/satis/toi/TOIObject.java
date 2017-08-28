package satis.toi;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public abstract class TOIObject {
	private Long id;

	private String text;

	private Font font;

	protected boolean valid = false;

	private boolean selected;

	private boolean focused;

	protected Shape shape;

	protected Shape bounds;

	protected Rectangle2D originalBounds;

	private Color color;

	protected AffineTransform transform = new AffineTransform();

	public abstract void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx);

	public AffineTransform getTransform() {
		return transform;
	}

	public void transform(AffineTransform transform) {
		if (transform != null && !transform.isIdentity()) {
			this.transform.preConcatenate(transform);
			invalidate();
		}
	}

	public void paintControls(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		if (ctx.getLayer() == TOIPaintContext.MAIN_LAYER && isSelected() && isFocused()) {
			for (TOIObjectControl c : controls) {
				if (ctx.getControlFilter() == null || ctx.getControlFilter().pass(c)) {
					c.paint(g, gt, ctx);
				}
			}
		}
	}

	public TOIObjectControl getControlAt(TOIPane pane, double x, double y) {
		if (isSelected() && isFocused()) {
			for (int i = controls.size() - 1; i >= 0; i--) {
				TOIObjectControl c = controls.get(i);
				if (c.contains(pane, x, y)) {
					return c;
				}
			}
		}
		return null;
	}

	protected List<TOIObjectControl> controls = new ArrayList<TOIObjectControl>();

	public List<TOIObjectControl> getControls() {
		return controls;
	}

	public void addControl(TOIObjectControl control) {
		controls.add(control);
	}

	public void removeControl(TOIObjectControl control) {
		controls.remove(control);
	}

	public void translate(double dx, double dy) {
		transform(AffineTransform.getTranslateInstance(dx, dy));
	}

	public boolean contains(double x, double y) {
		return getShape().contains(x, y);
	}

	public Shape getShape() {
		validate();
		return shape;
	}

	public Shape getBounds() {
		validate();
		return bounds;
	}

	public boolean isValid() {
		return valid;
	}

	public void validate() {
		valid = true;
	}

	public void invalidate() {
		valid = false;
		shape = null;
		bounds = null;
		originalBounds = null;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		invalidate();
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		invalidate();
	}

	public Rectangle2D getOriginalBounds() {
		validate();
		return originalBounds;
	}

	public void paintSelection(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		if (isFocused()) {
			paintFocus(g, gt, ctx);
		} else {
			Stroke oldStroke = g.getStroke();
			Shape transformedPath = ctx.getTransform().createTransformedShape(getBounds());
			g.setStroke(TOIPane.selectedStroke1);
			g.setColor(new Color(220, 220, 220));
			g.draw(transformedPath);
			g.setStroke(TOIPane.selectedStroke2);
			g.setColor(Color.black);
			g.draw(transformedPath);
			g.setStroke(oldStroke);
		}
	}

	public void paintFocus(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		Stroke oldStroke = g.getStroke();
		Shape transformedBounds = getTransform().createTransformedShape(getOriginalBounds());
		transformedBounds = ctx.getTransform().createTransformedShape(transformedBounds);
		g.setStroke(TOIPane.selectedStroke1);
		g.setColor(new Color(220, 220, 220));
		g.draw(transformedBounds);
		g.setStroke(TOIPane.selectedStroke2);
		g.setColor(Color.black);
		g.draw(transformedBounds);
		g.setStroke(oldStroke);
	}

	public void setColorDeep(Color color) {
		setColor(color);
	}

	public void setFontDeep(Font font) {
		setFont(font);
	}
}
