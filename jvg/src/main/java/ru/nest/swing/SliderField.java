package ru.nest.swing;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SliderField extends JPanel {
	private IntegerTextField field = new IntegerTextField();

	private JSlider slider = new JSlider();

	private boolean ignoreEvent = false;

	private JPopupMenu popup = new JPopupMenu();

	private JLabel btn = new JLabel() {
		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int y = getHeight() / 2 - 1;
			int x = getWidth() / 2;
			g.setColor(Color.black);
			g.drawLine(x - 2, y, x + 2, y);
			g.drawLine(x - 1, y + 1, x + 1, y + 1);
			g.drawLine(x, y + 2, x, y + 2);
		}
	};

	public SliderField(int min, int max, int value) {
		slider.setMinimum(min);
		slider.setMaximum(max);

		popup.setLayout(new BorderLayout());
		popup.add(slider, BorderLayout.CENTER);
		popup.pack();

		btn.setPreferredSize(new Dimension(7, 7));
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					showPopup();

					try {
						Point location = slider.getLocationOnScreen();
						Dimension size = slider.getSize();
						int value = slider.getValue();
						int x = (int) ((size.getWidth() - 10) * value / (slider.getMaximum() - slider.getMinimum())) + 5;

						Robot r = new Robot();
						r.mouseRelease(InputEvent.BUTTON1_MASK);
						r.mouseMove(location.x + x, location.y + size.height / 2);
						r.mousePress(InputEvent.BUTTON1_MASK);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		});

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!ignoreEvent) {
					ignoreEvent = true;
					field.setText(Integer.toString(slider.getValue()));
					ignoreEvent = false;
				}
			}
		});
		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					hidePopup();
				}
			}
		});

		field.setHorizontalAlignment(SwingConstants.RIGHT);
		field.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ignoreEvent) {
					validateField();
				}
			}
		});
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ignoreEvent) {
					validateField();
				}
			}
		});

		setLayout(new BorderLayout());
		add(field, BorderLayout.CENTER);
		add(btn, BorderLayout.EAST);

		setValue(value);
	}

	private void validateField() {
		ignoreEvent = true;
		try {
			String s = field.getText();
			int value = Integer.parseInt(s);
			slider.setValue(value);
		} catch (Exception exc) {
			field.setText(Integer.toString(slider.getValue()));
		}
		ignoreEvent = false;
	}

	public void showPopup() {
		popup.show(this, 0, getHeight());
	}

	public void hidePopup() {
		popup.setVisible(false);
	}

	public void setValue(int value) {
		slider.setValue(value);
	}

	public int getValue() {
		return slider.getValue();
	}

	public JSlider getSlider() {
		return slider;
	}

	@Override
	public void setRequestFocusEnabled(boolean requestFocusEnabled) {
		super.setRequestFocusEnabled(requestFocusEnabled);
		slider.setRequestFocusEnabled(requestFocusEnabled);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		field.setEnabled(enabled);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(200, 200, 200, 200);
		f.getContentPane().setLayout(new FlowLayout());
		SliderField s = new SliderField(0, 100, 25);
		s.setPreferredSize(new Dimension(30, 18));
		f.getContentPane().add(s);
		f.setVisible(true);
	}
}
