package ru.nest.jvg.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.ActionChooser;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ru.nest.jvg.action.BasicStrokeAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;

public class OutlinePatternChooser extends ActionChooser {
	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	private float[][] values = { {}, { 16f, 10f }, { 8f, 6f }, { 4f, 3f }, { 8f, 6f, 4f, 6f }, { 8f, 6f, 4f, 6f, 4f, 6f }, { 20f, 8f, 8f, 8f }, { 2, 4 }, { 2, 2 }, { 1, 3 }, { 1, 1 } };

	private HashMap<float[], BasicStroke> strokes = new HashMap<float[], BasicStroke>();

	public OutlinePatternChooser() {
		JMenu menu = new JMenu();

		final Action actionNoLine = new BasicStrokeAction();

		JMenuItem itemNoLine = new JMenuItem(lm.getValue("chooser.outline.pattern.noline", "No Line"));
		itemNoLine.setHorizontalAlignment(SwingConstants.CENTER);
		itemNoLine.setFont(itemNoLine.getFont().deriveFont(Font.PLAIN, 12));
		itemNoLine.addActionListener(actionNoLine);
		itemNoLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setAction(actionNoLine);
			}
		});
		itemNoLine.setPreferredSize(new Dimension(150, 20));
		menu.add(itemNoLine);

		Action selectedAction = null;
		for (int i = 0; i < values.length; i++) {
			final Action action = new BasicStrokeAction(i > 0 ? values[i] : null);
			if (i == 0) {
				selectedAction = action;
			}

			strokes.put(values[i], new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, i > 0 ? values[i] : null, 0f));

			final int index = i;
			final JMenuItem item = new JMenuItem() {
				@Override
				public void paint(Graphics g) {
					super.paint(g);

					Graphics2D g2d = (Graphics2D) g;
					g2d.setColor(Color.black);
					g2d.setStroke(strokes.get(values[index]));
					int y = getHeight() / 2;
					g2d.drawLine(5, y, getWidth() - 10, y);
				}
			};
			item.addActionListener(action);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dash_array = strokes.get(values[index]).getDashArray();
					setAction(action);
				}
			});
			item.setPreferredSize(new Dimension(150, 20));
			menu.add(item);
		}

		JMenuItem menuOther = new JMenuItem(lm.getValue("chooser.outline.pattern.other", "Other"));
		menuOther.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField txt = new JTextField(30);
				int option = JOptionPane.showConfirmDialog(null, txt, lm.getValue("chooser.outline.pattern.message-enter-dash-array", "Enter dash array value"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (option == JOptionPane.OK_OPTION) {
					try {
						String[] s = txt.getText().split(",");
						float[] dash = new float[s.length];
						for (int i = 0; i < s.length; i++) {
							dash[i] = Float.parseFloat(s[i]);
						}
						if (dash.length == 0) {
							dash = null;
						}
						Action action = new BasicStrokeAction(dash);
						setAction(action);
						action.actionPerformed(e);
						dash_array = dash;
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		});
		menu.add(menuOther);

		init(new ImageIcon(JVGEditor.class.getResource("img/line_pattern.png")), selectedAction, menu);

		setToolTipText(lm.getValue("chooser.outline.pattern.tooltip", "Outline Pattern"));
		setFocusable(false);
		setRequestFocusEnabled(false);
	}

	private float[] dash_array;

	public float[] getDashArray() {
		return dash_array;
	}
}
