package ru.nest.jvg.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WComboBox;

import ru.nest.jvg.action.BasicStrokeAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;

public class OutlineWidthChooser extends WComboBox {
	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private float[] values = new float[] { 0.5f, 1f, 1.5f, 2f, 3f, 4, 5, 6, 7, 8, 9, 10, 12, 15 };

	private Action[] actions;

	private Map<Float, Stroke> strokes = new HashMap<>();

	private float currentWidth = 1f;

	public OutlineWidthChooser() {
		actions = new Action[values.length];
		for (int i = 0; i < values.length; i++) {
			actions[i] = new BasicStrokeAction(values[i]);
			strokes.put(values[i], new BasicStroke(values[i]));
		}

		setToolTipText(lm.getValue("chooser.outline.width.tooltip", "Outline Width"));
		setFocusable(false);
		setRequestFocusEnabled(false);
		setPreferredSize(new Dimension(60, 20));

		setModel(new DefaultComboBoxModel() {
			@Override
			public int getSize() {
				return values.length + 1;
			}

			@Override
			public Object getElementAt(int index) {
				return index < values.length ? values[index] : -1;
			}
		});

		setRenderer(new DefaultListCellRenderer() {
			private int index;

			private Dimension preferredSize = new Dimension(0, 20);

			private float width = 1f;

			private JLabel lblOther = new JLabel(lm.getValue("chooser.outline.width.other", "Other"));

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				this.index = index;

				if (index == values.length) {
					return lblOther;
				}

				if (value != null) {
					width = (Float) value;
				} else {
					width = 0f;
				}

				value = "";
				JComponent c = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				c.setPreferredSize(preferredSize);
				c.setOpaque(true);
				if (isSelected) {
					c.setBackground(list.getSelectionBackground());
				} else {
					c.setBackground(list.getBackground());
				}
				return c;
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);

				if (index == -1) {
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
				} else {
					g.setColor(Color.white);
					g.fillRect(2, 2, getWidth() - 4, getHeight() - 5);
				}

				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(Color.black);
				if (strokes.containsKey(width)) {
					g2d.setStroke(strokes.get(width));
				}
				int y = getHeight() / 2;
				g2d.drawLine(5, y, getWidth() - 10, y);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		});

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = getSelectedIndex();
				if (index >= 0 && index < actions.length) {
					actions[index].actionPerformed(e);
					currentWidth = values[index];
				} else if (index == actions.length) {
					JTextField txt = new JTextField();
					int option = JOptionPane.showConfirmDialog(null, txt, JVGLocaleManager.getInstance().getValue("chooser.outline.width.other.title", "Enter width"), JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						try {
							float value = Float.parseFloat(txt.getText());
							new BasicStrokeAction(value).actionPerformed(e);
							currentWidth = value;
						} catch (NumberFormatException exc) {
							exc.printStackTrace();
						}
					}
				}
			}
		});

		setSelectedItem(1f);
	}

	public float getSelectedWidth() {
		return currentWidth;
	}
}
