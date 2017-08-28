package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;

public class JRendererLabel extends JLabel {
	private static final long serialVersionUID = -3286049601389254405L;

	public static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public JRendererLabel() {
		setOpaque(true);
		setBorder(noFocusBorder);
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setForeground(null);
		setBackground(null);
	}

	@Override
	public boolean isOpaque() {
		Color back = getBackground();
		Component p = getParent();
		if (p != null) {
			p = p.getParent();
		}

		boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
		return !colorMatch && super.isOpaque();
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void validate() {
	}

	@Override
	public void revalidate() {
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	@Override
	public void repaint(Rectangle r) {
	}

	@Override
	public void repaint() {
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (propertyName == "text" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}

	class UIResource extends DefaultTableCellRenderer implements javax.swing.plaf.UIResource {
		private static final long serialVersionUID = 1L;
	}

	public void setFont(Font font) {
		if (font instanceof FontUIResource)
			font = null;
		super.setFont(font);
	}

	public void setBackground(Color color) {
		if (color instanceof ColorUIResource)
			color = null;
		super.setBackground(color);
	}

	public Dimension getPreferredSize() {
		Dimension retDimension = super.getPreferredSize();
		if (retDimension != null)
			retDimension = new Dimension(retDimension.width + 3, retDimension.height);
		return retDimension;
	}

	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
	}

	public void firePropertyChange(String propertyName, char oldValue, char newValue) {
	}

	public void firePropertyChange(String propertyName, short oldValue, short newValue) {
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
	}

	public void firePropertyChange(String propertyName, long oldValue, long newValue) {
	}

	public void firePropertyChange(String propertyName, float oldValue, float newValue) {
	}

	public void firePropertyChange(String propertyName, double oldValue, double newValue) {
	}
}
