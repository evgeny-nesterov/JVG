package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionChooser;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ru.nest.jvg.action.JVGAction;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;

public abstract class AbstractChooseColorButton extends ActionChooser {
	protected JVGLocaleManager lm = JVGLocaleManager.getInstance();

	public AbstractChooseColorButton() {
	}

	public AbstractChooseColorButton(JVGAction defaultAction, Icon icon, Draw draw) {
		init(defaultAction, icon, draw);
	}

	public final static ColorResource[][] colors = { { ColorResource.white, ColorResource.lightgray, ColorResource.gray, ColorResource.darkgray, ColorResource.black }, { ColorResource.yellow, ColorResource.cyan, ColorResource.orange, ColorResource.pink, ColorResource.magenta }, { new ColorResource(new Color(255, 200, 200)), new ColorResource(new Color(255, 0, 0)), new ColorResource(new Color(155, 100, 100)), new ColorResource(new Color(155, 0, 0)), new ColorResource(new Color(55, 0, 0)) }, { new ColorResource(new Color(200, 255, 200)), new ColorResource(new Color(0, 255, 0)), new ColorResource(new Color(100, 155, 100)), new ColorResource(new Color(0, 155, 0)), new ColorResource(new Color(0, 55, 0)) },
			{ new ColorResource(new Color(200, 200, 255)), new ColorResource(new Color(0, 0, 255)), new ColorResource(new Color(100, 100, 155)), new ColorResource(new Color(0, 0, 155)), new ColorResource(new Color(0, 0, 55)) } };

	protected void init(JVGAction defaultAction, Icon icon, Draw draw) {
		this.draw = draw;
		if (draw instanceof ColorDraw) {
			lastColor = ((ColorDraw) draw).getResource().getResource();
		}

		JPopupMenu popup = predefinedColorPanel.getPopupMenu();
		popup.setLayout(new GridBagLayout());

		init(icon, defaultAction, predefinedColorPanel);

		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < colors[i].length; j++) {
				popup.add(createColorButton(new ColorDraw(colors[i][j])), new GridBagConstraints(j, i + 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			}
		}

		AbstractButton btnMore = createChooseButton();
		btnMore.setText(lm.getValue("chooser.color.more", "More..."));
		btnMore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				choose();
			}
		});
		btnMore.setMargin(new Insets(0, 0, 0, 0));

		popup.add(btnMore, new GridBagConstraints(0, colors.length + 2, colors[0].length + 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	private Color lastColor = Color.black;

	public void choose() {
		JColorChooser colorChooser = new JColorChooser(lastColor);
		Color c = JColorChooser.showDialog(null, lm.getValue("chooser.color.more.choose", "Choose Color"), lastColor);
		if (c != null) {
			ColorDraw draw = new ColorDraw(c);
			setFiller(draw.getResource());
			setDraw(draw);
			lastColor = c;
		}
	}

	public abstract void setFiller(Resource color);

	private JMenu predefinedColorPanel = new JMenu();

	public JPopupMenu getPopup() {
		return predefinedColorPanel.getPopupMenu();
	}

	protected Draw draw;

	public Draw getDraw() {
		return draw;
	}

	public void setCurrentAction(Action action, Draw draw) {
		this.draw = draw;
		setAction(action);
		repaint();
	}

	protected abstract AbstractButton createChooseButton();

	protected abstract AbstractButton createColorButton(Draw draw);

	private JVGShape shape = new JVGShape() {
	};

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (draw != null) {
			Graphics2D g2d = (Graphics2D) g;
			Rectangle rect = new Rectangle(3, getHeight() - 6, getWidth() - space - 6, 4);
			shape.setShape(rect, false);
			g2d.setPaint(draw.getPaint(shape, rect, null));
			g2d.fill(rect);
		}
	}

	public void setDraw(Draw draw) {
		this.draw = draw;
	}

	class ColorButton extends JMenuItem implements ActionListener {
		protected Draw draw;

		protected Action action;

		public ColorButton(Draw draw, Action action) {
			this.draw = draw;
			this.action = action;

			setPreferredSize(new Dimension(16, 16));
			setOpaque(false);
			setBorderPainted(false);
			setRequestFocusEnabled(false);

			addActionListener(action);
			addActionListener(this);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;

			Rectangle rect = new Rectangle(2, 2, getWidth() - 5, getHeight() - 5);
			g2d.setPaint(draw.getPaint(null, rect, null));
			g2d.fill(rect);

			g2d.setColor(Color.gray);
			g2d.draw(rect);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setCurrentAction(action, draw);
		}
	}
}
