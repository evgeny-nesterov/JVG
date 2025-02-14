package ru.nest.jvg.editor;

import ru.nest.fonts.Fonts;
import ru.nest.swing.IconButton;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SliderChooser extends IconButton {
	private NumberFormat format = new DecimalFormat("##.#");

	private Font font = new Font("Dialog", Font.PLAIN, 10);

	private double value;

	private double scale;

	private JPopupMenu popup;

	public SliderChooser(Icon icon, final double min, final double max) {
		this(icon, 10, min, max);
	}

	public SliderChooser(Icon icon, final double scale, final double min, final double max) {
		super(icon);
		this.scale = scale;

		setPreferredSize(new Dimension(24, 24));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!isEnabled()) {
					return;
				}

				final double oldValue = getValue();
				final JSlider slider = new JSlider();
				slider.setMinimum((int) (scale * min));
				slider.setMaximum((int) (scale * max));
				slider.setValue((int) (scale * getValue()));
				slider.setPreferredSize(new Dimension(500, 18));
				slider.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						double value = slider.getValue() / scale;
						setValue(value);
						repaint();
					}
				});

				popup = new JPopupMenu();
				popup.setLayout(new BorderLayout());
				popup.add(slider, BorderLayout.CENTER);
				popup.pack();
				popup.addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					}

					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					}

					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
						if (oldValue != value) {
							commit(oldValue, value);
						}
					}
				});
				popup.show(SliderChooser.this, 0, getHeight());
				repaint();
			}
		});
	}

	public void commit(double oldValue, double newValue) {
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		font = Fonts.getFont("CordiaUPC").deriveFont(14f);
		g.setFont(font);

		if (popup != null && popup.isVisible()) {
			g.setColor(Color.gray);
			g.fillRect(2, 13, getWidth() - 4, getHeight() - 15);
			g.setColor(Color.white);
			g.drawString(format.format(value), 3, getHeight() - 3);
		} else {
			g.setColor(Color.white);
			g.drawString(format.format(value), 2, getHeight() - 4);
			g.setColor(isEnabled() ? Color.darkGray : Color.lightGray);
			g.drawString(format.format(value), 3, getHeight() - 3);
		}
	}

	public NumberFormat getFormat() {
		return format;
	}

	public void setFormat(NumberFormat format) {
		this.format = format;
	}
}
