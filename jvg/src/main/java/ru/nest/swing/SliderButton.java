package ru.nest.swing;

import javax.swing.*;

public class SliderButton extends JPanel {
	private JTextField txtValue = new IntegerTextField();

	private JSlider slider = new JSlider();

	private WComboBox cmb = new WComboBox();

	public SliderButton(int min, int max) {
		setLimits(min, max);
	}

	private int min;

	public int getMin() {
		return min;
	}

	private int max;

	public int getMax() {
		return max;
	}

	public void setLimits(int min, int max) {
		this.min = min;
		this.max = max;

		slider.setMinimum(min);
		slider.setMaximum(max);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setBounds(300, 100, 200, 30);
		f.setContentPane(new SliderButton(0, 100));
		f.setVisible(true);
	}
}
