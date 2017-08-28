package javax.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.text.View;

public class VerticalLabelUI extends BasicLabelUI {
	static {
		labelUI = new VerticalLabelUI(false);
	}

	protected boolean clockwise;

	public VerticalLabelUI(boolean clockwise) {
		super();
		this.clockwise = clockwise;
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension dim = super.getPreferredSize(c);
		return new Dimension(dim.height, dim.width);
	}

	private static Rectangle paintIconR = new Rectangle();

	private static Rectangle paintTextR = new Rectangle();

	private static Rectangle paintViewR = new Rectangle();

	private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

	@Override
	public void paint(Graphics g, JComponent c) {
		JLabel label = (JLabel) c;
		String text = label.getText();
		Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

		if ((icon == null) && (text == null)) {
			return;
		}

		FontMetrics fm = label.getGraphics().getFontMetrics();
		Insets insets = c.getInsets(paintViewInsets);

		paintViewR.x = insets.left;
		paintViewR.y = insets.top;
		paintViewR.height = c.getWidth() - (insets.left + insets.right);
		paintViewR.width = c.getHeight() - (insets.top + insets.bottom);

		paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

		String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

		Graphics2D g2 = (Graphics2D) g;
		AffineTransform tr = g2.getTransform();
		if (clockwise) {
			g2.rotate(Math.PI / 2);
			g2.translate(0, -c.getWidth());
		} else {
			g2.rotate(-Math.PI / 2);
			g2.translate(-c.getHeight(), 0);
		}

		if (icon != null) {
			icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
		}

		if (text != null) {
			View v = (View) c.getClientProperty(BasicHTML.propertyKey);
			if (v != null) {
				v.paint(g, paintTextR);
			} else {
				int textX = paintTextR.x;
				int textY = paintTextR.y + fm.getAscent();

				if (label.isEnabled()) {
					paintEnabledText(label, g, clippedText, textX, textY);
				} else {
					paintDisabledText(label, g, clippedText, textX, textY);
				}
			}
		}

		g2.setTransform(tr);
	}

	public static void main(String[] arg) {
		JFrame f = new JFrame();

		JLabel lbl = new JLabel("123456");
		lbl.setOpaque(true);
		lbl.setBackground(Color.white);
		lbl.setUI(BasicLabelUI.labelUI);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(lbl, BorderLayout.WEST);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocation(200, 200);
		f.pack();
		f.setVisible(true);
	}
}
