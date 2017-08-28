package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class JRendererCheckBox extends JCheckBox {
	private static final long serialVersionUID = 1L;
	public static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public JRendererCheckBox() {
		super();
		setOpaque(true);
		setBorder(noFocusBorder);
		setHorizontalAlignment(SwingConstants.CENTER);
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

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}

	class UIResource extends DefaultTableCellRenderer implements javax.swing.plaf.UIResource {

		private static final long serialVersionUID = 1L;
	}
}
