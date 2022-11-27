package ru.nest.jvg.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.WComboBox;

import ru.nest.jvg.action.FillerTransparencyAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.shape.paint.Painter;

public class FillerTransparencyChooser extends WComboBox {
	private float[] values = new float[11];

	private Action[] actions;

	private Map<Float, Color> colors = new HashMap<>();

	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	public FillerTransparencyChooser() {
		for (int i = 0; i < 11; i++) {
			values[i] = i * 25.5f;
		}

		actions = new Action[values.length];
		for (int i = 0; i < values.length; i++) {
			actions[i] = new FillerTransparencyAction((int) values[i], Painter.FILL);
			colors.put(values[i], new Color(0, 0, 0, (int) values[i]));
		}

		setToolTipText(lm.getValue("chooser.fill.transparency.tooltip", "Fill Transparency"));
		setFocusable(false);
		setRequestFocusEnabled(false);
		setPreferredSize(new Dimension(80, 20));
		setFont(getFont().deriveFont(Font.PLAIN, 12));

		setModel(new DefaultComboBoxModel() {
			@Override
			public int getSize() {
				return values.length;
			}

			@Override
			public Object getElementAt(int index) {
				return values[index];
			}
		});

		setRenderer(new DefaultListCellRenderer() {
			private float transparency = 255;

			private Dimension preferredSize = new Dimension(0, 20);

			private int index;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				this.index = index;
				if (value != null) {
					transparency = (Float) value;
					value = "";
				} else {
					transparency = 255;
					value = lm.getValue("chooser.fill.transparency.no", "No Transparency");
				}

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

			private Stroke stroke = new BasicStroke(3.f);

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d = (Graphics2D) g;

				if (index == -1) {
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
				} else {
					g.setColor(Color.white);
					g.fillRect(2, 2, getWidth() - 4, getHeight() - 5);
				}

				Stroke oldStroke = g2d.getStroke();
				Shape oldClip = g2d.getClip();
				g2d.setColor(Color.blue);
				g2d.setStroke(stroke);
				g2d.setClip(35, 2, getWidth() - 37, getHeight() - 5);
				int endPos = getWidth() + getHeight();
				for (int i = 39; i < endPos; i += 7) {
					g.drawLine(i, 0, 0, i);
				}
				g2d.setStroke(oldStroke);
				g2d.setClip(oldClip);

				g.setColor(colors.get(transparency));
				g.fillRect(35, 2, getWidth() - 37, getHeight() - 5);
				g.setColor(Color.darkGray);
				g.drawRect(35, 2, getWidth() - 37, getHeight() - 5);

				g.setColor(Color.black);
				g.drawString((int) (transparency / 2.55) + "%", 2, (g.getFontMetrics().getHeight() + 18) / 2);
			}
		});

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = getSelectedIndex();
				if (index >= 0) {
					actions[index].actionPerformed(e);
				}
			}
		});

		setSelectedItem(255);
	}
}
