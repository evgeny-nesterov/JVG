package ru.nest.jvg.editor;

import ru.nest.jvg.action.EndingsTypeAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.paint.Endings;
import ru.nest.jvg.shape.paint.EndingsPainter;
import ru.nest.swing.ActionChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EndingsPatternChooser extends ActionChooser {
	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	public EndingsPatternChooser() {
		JMenu menu = new JMenu();

		int width = 80;
		final MutableGeneralPath path = new MutableGeneralPath();
		path.moveTo(5.0, 0.0);
		path.lineTo(width / 2.0, 0.0);
		path.lineTo(width - 5.0, 0.0);

		Action selectedAction = null;
		for (int figure = Endings.FIGURE_CIRCLE; figure <= Endings.FIGURE_ROMB; figure++) {
			for (int type = figure == Endings.FIGURE_CIRCLE ? Endings.TYPE_NONE : Endings.TYPE_NONE + 1; type <= Endings.TYPE_ALL_ENDINGS; type++) {
				final Endings endings = new Endings();
				endings.setEndingType(type);
				endings.setFigure(figure);

				final Action action = type == Endings.TYPE_NONE ? new EndingsTypeAction() : new EndingsTypeAction(new Endings(endings));
				if (selectedAction == null) {
					selectedAction = action;
				}

				final JMenuItem item = new JMenuItem() {
					@Override
					public void paint(Graphics g) {
						super.paint(g);

						Graphics2D g2d = (Graphics2D) g;
						g2d.setColor(Color.black);

						int y = getHeight() / 2;
						g2d.translate(0, y);

						g2d.draw(path);
						EndingsPainter.drawEndings(g2d, path, endings);
					}
				};
				item.addActionListener(action);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						EndingsPatternChooser.this.type = endings.getEndingType();
						setAction(action);
					}
				});
				item.setPreferredSize(new Dimension(width, 16));
				menu.add(item);
			}
		}

		init(new ImageIcon(JVGEditor.class.getResource("img/endings_pattern.png")), selectedAction, menu);

		setToolTipText(lm.getValue("chooser.endings.pattern.tooltip", "Endings Pattern"));
		setFocusable(false);
		setRequestFocusEnabled(false);
	}

	private int type;

	public int getType() {
		return type;
	}
}
