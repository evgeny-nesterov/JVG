package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

public class WComboBox<E> extends JComboBox<E> {
	private int buttonWidth = 11;

	private boolean over = false;

	public WComboBox() {
		super();
		init();
	}

	public WComboBox(E items[]) {
		super(items);
		init();
	}

	public WComboBox(Vector<E> items) {
		super(items);
		init();
	}

	private void init() {
		setBackground(Color.white);
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				repaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				over = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				over = false;
				repaint();
			}
		});

		setUI(new BasicComboBoxUI() {
			@Override
			protected LayoutManager createLayoutManager() {
				return new LayoutManager() {
					@Override
					public void addLayoutComponent(String name, Component comp) {
					}

					@Override
					public void removeLayoutComponent(Component comp) {
					}

					@Override
					public Dimension preferredLayoutSize(Container parent) {
						return parent.getPreferredSize();
					}

					@Override
					public Dimension minimumLayoutSize(Container parent) {
						return parent.getMinimumSize();
					}

					@Override
					public void layoutContainer(Container parent) {
						JComboBox cb = (JComboBox) parent;
						int width = cb.getWidth();
						int height = cb.getHeight();

						Insets insets = getInsets();
						int buttonSize = height - (insets.top + insets.bottom);
						Rectangle cvb;

						if (arrowButton != null) {
							arrowButton.setBounds(width - (insets.right + buttonWidth), insets.top, buttonWidth, buttonSize);
						}

						if (editor != null) {
							cvb = rectangleForCurrentValue();
							editor.setBounds(cvb);
						}
					}
				};
			}

			@Override
			protected Rectangle rectangleForCurrentValue() {
				int width = comboBox.getWidth();
				int height = comboBox.getHeight();
				Insets insets = getInsets();
				return new Rectangle(insets.left, insets.top, width - (insets.left + insets.right + buttonWidth), height - (insets.top + insets.bottom));
			}

			@Override
			protected JButton createArrowButton() {
				return new JButton() {
					{
						addMouseListener(new MouseAdapter() {
							@Override
							public void mouseEntered(MouseEvent e) {
								over = true;
								WComboBox.this.repaint();
							}

							@Override
							public void mouseExited(MouseEvent e) {
								over = false;
								WComboBox.this.repaint();
							}
						});

						setRequestFocusEnabled(false);
						setBackground(Color.white);
						setBorderPainted(false);
						setContentAreaFilled(false);
						setPreferredSize(new Dimension(5, 5));
					}

					@Override
					public void paint(Graphics g) {
						super.paint(g);

						int w = getSize().width;
						int h = getSize().height;
						boolean isPressed = getModel().isPressed();
						boolean isEnabled = isEnabled();
						boolean popupVisible = WComboBox.this.isPopupVisible();

						// background
						g.setColor(isEnabled ? (popupVisible ? Constants.rolloverDarkBackground : ((over || isPressed) ? Constants.rolloverBackground : Constants.iconColor)) : Constants.disabledBackgroundColor);
						g.fillRect(0, 0, w, h);

						// border
						if (isEnabled & (over || popupVisible)) {
							g.setColor(Constants.rolloverOutlineBackground);
							g.drawLine(0, 0, 0, h);
						}

						// draw triangle
						g.setColor((popupVisible || isPressed) ? Color.white : (isEnabled ? Color.black : Color.gray));
						int y = getHeight() / 2;
						g.drawLine(3, y - 1, 7, y - 1);
						g.drawLine(4, y, 6, y);
						g.drawLine(5, y + 1, 5, y + 1);
					}
				};
			}

			@Override
			protected ComboPopup createPopup() {
				ComboPopup popup = super.createPopup();
				popup.getList().setBackground(Color.white);
				popup.getList().setForeground(Color.black);
				popup.getList().setSelectionBackground(Constants.listSelectionColor);
				popup.getList().setSelectionForeground(Color.white);
				return popup;
			}

			@Override
			public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
				ListCellRenderer renderer = comboBox.getRenderer();
				Component c;

				if (hasFocus && !isPopupVisible(comboBox)) {
					c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, true, false);
				} else {
					c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
				}
				c.setFont(comboBox.getFont());
				c.setBackground(Color.white);
				c.setForeground(comboBox.isEnabled() ? Color.black : Color.darkGray);

				boolean shouldValidate = c instanceof JPanel;
				currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, shouldValidate);
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (isEnabled() && (over || isPopupVisible())) {
			g.setColor(Constants.rolloverOutlineBackground);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}
}
