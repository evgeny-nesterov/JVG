package javax.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import ru.nest.jvg.editor.Util;

public class SwitchPanel extends JPanel {
	private boolean isSingleSelection = true;

	private int minHeight = 50;

	private JPanel panel;

	public SwitchPanel() {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	private int tabsCount = 0;

	public void addTab(String name, JComponent c) {
		c.setVisible(tabsCount == 0);
		c.setMinimumSize(new Dimension(20, minHeight));

		panel.add(new TabPanel(name, c, tabsCount), new GridBagConstraints(0, 2 * tabsCount, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(1, 1, 0, 1), 0, 0));
		panel.add(c, new GridBagConstraints(0, 2 * tabsCount + 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		tabsCount++;
	}

	public boolean isSingleSelection() {
		return isSingleSelection;
	}

	public void hideTabs() {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component c = panel.getComponent(i);
			if (c instanceof TabPanel) {
				TabPanel tabPanel = (TabPanel) c;
				tabPanel.getComponent().setVisible(false);
			}
		}
		panel.revalidate();
		panel.repaint();
	}

	public void setSingleSelection(boolean isSingleSelection) {
		this.isSingleSelection = isSingleSelection;
	}

	public int getSelectedIndex() {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component c = panel.getComponent(i);
			if (c instanceof TabPanel) {
				TabPanel tabPanel = (TabPanel) c;
				if (tabPanel.getComponent().isVisible()) {
					return tabPanel.getIndex();
				}
			}
		}
		return -1;
	}

	class TabPanel extends JPanel {
		private String name;

		private JLabel label;

		private JComponent c;

		private int index;

		private boolean fullScreen = false;

		private ImageIcon iconExpand = new ImageIcon(SwitchPanel.class.getResource("img/maximize.gif"));

		private ImageIcon iconRestore = new ImageIcon(SwitchPanel.class.getResource("img/restore.gif"));

		private IconButton btnExpand = new IconButton(iconExpand);

		public TabPanel(String name, final JComponent c, int index) {
			this.name = name;
			this.c = c;
			this.index = index;

			label = new JLabel(name) {
				@Override
				public void paintComponent(Graphics g) {
					Util.drawGlassRect(g, getWidth(), getHeight());
					super.paintComponent(g);
				}
			};
			label.setFont(label.getFont().deriveFont(Font.BOLD, 10));
			label.setBorder(BorderFactory.createLineBorder(new Color(205, 205, 205)));
			label.setPreferredSize(new Dimension(50, 16));
			label.setOpaque(false);
			label.setBackground(new Color(220, 220, 220));
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (isSingleSelection()) {
						showComponent();
					} else {
						if (!c.isVisible()) {
							showComponent();
						} else {
							hideComponent();
						}
					}
				}
			});
			label.setLayout(new BorderLayout());
			label.add(btnExpand, BorderLayout.EAST);

			btnExpand.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setFullScreen(!fullScreen);
				}
			});

			setLayout(new BorderLayout());
			add(label, BorderLayout.NORTH);
		}

		public void showComponent() {
			if (isSingleSelection()) {
				hideTabs();
			}
			c.setVisible(true);
			panel.revalidate();
			panel.repaint();
		}

		public void hideComponent() {
			c.setVisible(false);
			panel.revalidate();
			panel.repaint();
		}

		@Override
		public String getName() {
			return name;
		}

		public JLabel getLabel() {
			return label;
		}

		public JComponent getComponent() {
			return c;
		}

		public int getIndex() {
			return index;
		}

		public boolean isFullScreen() {
			return fullScreen;
		}

		public void setFullScreen(boolean fullScreen) {
			this.fullScreen = fullScreen;
			btnExpand.setIcon(fullScreen ? iconRestore : iconExpand);

			if (fullScreen) {
				for (int i = 0; i < panel.getComponentCount(); i++) {
					Component c = panel.getComponent(i);
					if (c instanceof TabPanel) {
						TabPanel tabPanel = (TabPanel) c;
						tabPanel.setVisible(c == this);
						tabPanel.getComponent().setVisible(c == this);
					}
				}
			} else {
				for (int i = 0; i < panel.getComponentCount(); i++) {
					Component c = panel.getComponent(i);
					if (c instanceof TabPanel) {
						TabPanel tabPanel = (TabPanel) c;
						tabPanel.setVisible(true);
						tabPanel.getComponent().setVisible(c == this);
					}
				}
			}

			panel.revalidate();
			panel.repaint();
		}
	}
}
