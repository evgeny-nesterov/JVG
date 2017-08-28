package javax.swing.pie;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class PieFlatRenderer implements PieRenderer {
	@Override
	public void paint(Graphics g, Pie pie, int x, int y, int w, int h) {
		PieModel model = pie.getModel();
		int count = model.getCount();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double sum = 0;
		for (int i = 0; i < count; i++) {
			double value = model.getValue(i);
			if (value > 0.0) {
				sum += value;
			}
		}

		if (sum == 0) {
			sum = 1;
		}

		double from = -pie.getStartAngle();
		for (int i = 0; i < count; i++) {
			if (model.getValue(i) >= 0) {
				double alfa = 360 * model.getValue(i) / sum;

				g2d.setColor(model.getColor(i));
				g2d.fillArc(x, y, w, h, (int) from, (int) alfa);

				g2d.setColor(Color.gray);
				g2d.drawArc(x, y, w, h, (int) from, (int) alfa);

				from += alfa;
			}
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}
