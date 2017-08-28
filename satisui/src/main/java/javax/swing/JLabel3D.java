package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.border.LineBorder;

public class JLabel3D extends JLabel {
	private static final long serialVersionUID = 1L;

	private Color color;

	private Color colorLight;

	private Color colorBorder;

	private double factor;

	public JLabel3D(Color color, double factor) {
		this("", null, color, factor, 20);
	}

	public JLabel3D(String text, Icon icon, Color color) {
		this(text, icon, color, 1.05);
	}

	public JLabel3D(String text, Icon icon, Color color, double factor) {
		this(text, icon, color, factor, 20);
	}

	public JLabel3D(String text, Icon icon, Color color, double factor, int height) {
		super(text);
		set(icon, color, factor, height);
	}

	public JLabel3D(String text, Color color, Color colorLight) {
		super(text);
		set(color, colorLight);
	}

	public void set(Icon icon, Color color, double factor, int height) {
		this.factor = factor;
		setOpaque(false);
		setPreferredSize(new Dimension(100, height));
		this.color = color;
		this.colorLight = new Color(lighter(color.getRed()), lighter(color.getGreen()), lighter(color.getBlue()));
		this.colorBorder = new Color(darker(color.getRed()), darker(color.getGreen()), darker(color.getBlue()));
		setIcon(icon != null ? icon : new BlankIcon(5));
		setBorder(new LineBorder(this.colorBorder));
	}

	public void set(Color color, Color colorLight) {
		setOpaque(false);
		setPreferredSize(new Dimension(100, 22));
		this.color = color;
		this.colorLight = colorLight;
		this.colorBorder = new Color(darker(color.getRed()), darker(color.getGreen()), darker(color.getBlue()));
		setIcon(new BlankIcon(5));
		setBorder(new LineBorder(this.colorBorder));
	}

	private int lighter(int c) {
		return Math.min(255, (int) (c * factor));
	}

	private int darker(int c) {
		return Math.max(0, (int) (0.8 * c));
	}

	@Override
	public void paintComponent(Graphics g) {
		int h = getHeight() * 2 / 5;

		g.setColor(colorLight);
		g.fillRect(0, 0, getWidth(), h);

		g.setColor(color);
		g.fillRect(1, h, getWidth() - 1, getHeight() - h);

		super.paintComponent(g);
	}

	public static JLabel3D getGrayLabel(String text) {
		return new JLabel3D(text, null, new Color(220, 220, 220), 1.1);
	}

	public static JLabel3D getRedLabel(String text) {
		return new JLabel3D(text, null, new Color(255, 220, 220), 1.07);
	}

	public static JLabel3D getGreenLabel(String text) {
		return new JLabel3D(text, null, new Color(210, 255, 210), 1.1);
	}

	public static JLabel3D getBlueLabel(String text) {
		return new JLabel3D(text, null, new Color(210, 220, 230), 1.07);
	}

	public void setHeight(int height) {
		setPreferredSize(new Dimension(height, 30));
	}
}
