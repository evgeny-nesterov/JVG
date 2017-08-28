package javax.swing.toolbar;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.RolloverButton;

public class PanelToolBar extends AbstractToolBarPanel {
	private static final long serialVersionUID = 1L;

	public PanelToolBar() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
	}

	public JButton addButton(ActionListener action, String actionCommand, ImageIcon icon, String toolTip, boolean enabled) {
		JButton button = new RolloverButton();

		if (icon != null) {
			button.setIcon(icon);
		}

		button.setText(null);
		button.setToolTipText(toolTip);
		button.setActionCommand(actionCommand);
		button.addActionListener(action);
		button.setEnabled(enabled);
		add(button);
		return button;
	}

	public JButton addButton(ActionListener action, String actionCommand, ImageIcon icon, String toolTip) {
		return addButton(action, actionCommand, icon, toolTip, true);
	}

	public void addSeparator() {
		add(new PanelToolBarSeparator());
	}

	public void addLabel(String text) {
		add(new JLabel(text));
	}

	public void addTextField(JTextField textField) {
		add(textField);
	}

	public void addComboBox(JComboBox<?> comboBox) {
		add(comboBox);
	}

	public void addButton(JButton button) {
		add(button);
	}

	private class PanelToolBarSeparator extends JLabel {
		private static final long serialVersionUID = 1L;
		private int preferredWidth;

		public PanelToolBarSeparator() {
			this(4);
		}

		public PanelToolBarSeparator(int preferredWidth) {
			this.preferredWidth = preferredWidth;
		}

		@Override
		public boolean isOpaque() {
			return true;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(preferredWidth, 1);
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
	}
}
