package javax.swing.pie;

import java.awt.Graphics;

public interface PieRenderer {
	public void paint(Graphics g, Pie pie, int x, int y, int w, int h);
}
