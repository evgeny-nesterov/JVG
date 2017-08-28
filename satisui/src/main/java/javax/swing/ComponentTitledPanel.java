package javax.swing;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Rectangle;

public class ComponentTitledPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	protected ComponentTitledBorder border;

	protected JComponent component;

	protected JPanel panel;

	public ComponentTitledPanel() {
		this(null);
	}

	public ComponentTitledPanel(JComponent component) {
		super(new BorderLayout());
		this.component = component;
		border = new ComponentTitledBorder(component);
		setBorder(border);
		panel = new JPanel();

		if (component != null) {
			add(component);
		}
		add(panel);
	}

	public JComponent getTitleComponent() {
		return component;
	}

	public void setTitleComponent(JComponent newComponent) {
		if (component != null) {
			remove(component);
		}
		add(newComponent);
		border.setTitleComponent(newComponent);
		component = newComponent;
	}

	public JPanel getContentPane() {
		return panel;
	}

	@Override
	public void doLayout() {
		Insets insets = getInsets();
		Rectangle rect = getBounds();

		rect.x = 0;
		rect.y = 0;

		Rectangle compR = border.getComponentRect(rect, insets);
		component.setBounds(compR);
		rect.x += insets.left;
		rect.y += insets.top;
		rect.width -= insets.left + insets.right;
		rect.height -= insets.top + insets.bottom;
		panel.setBounds(rect);
	}
}
