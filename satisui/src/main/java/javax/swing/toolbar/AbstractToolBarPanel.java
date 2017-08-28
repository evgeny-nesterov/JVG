package javax.swing.toolbar;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;

import javax.swing.GUIUtils;
import javax.swing.JPanel;

public abstract class AbstractToolBarPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** the light gradient colour 1 */
	private Color colour1;

	/** the dark gradient colour 2 */
	private Color colour2;

	/** whether to fill a gradient background */
	private boolean fillGradient;

	/**
	 * Creates a new panel with a double buffer and a flow layout.
	 */
	public AbstractToolBarPanel() {
		this(true);
	}

	/**
	 * Creates a new panel with <code>FlowLayout</code> and the specified buffering strategy. If <code>isDoubleBuffered</code> is true, the
	 * <code>JPanel</code> will use a double buffer.
	 * 
	 * @param isDoubleBuffered
	 *            a boolean, true for double-buffering, which uses additional memory space to achieve fast, flicker-free updates
	 */
	public AbstractToolBarPanel(boolean isDoubleBuffered) {
		this(new FlowLayout(), isDoubleBuffered);
	}

	/**
	 * Create a new buffered panel with the specified layout manager
	 * 
	 * @param layout
	 *            the LayoutManager to use
	 */
	public AbstractToolBarPanel(LayoutManager layout) {
		this(layout, true);
	}

	/**
	 * Creates a new panel with the specified layout manager and buffering strategy.
	 * 
	 * @param layout
	 *            the LayoutManager to use
	 * @param isDoubleBuffered
	 *            a boolean, true for double-buffering, which uses additional memory space to achieve fast, flicker-free updates
	 */
	public AbstractToolBarPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		colour1 = getBackground();
		colour2 = GUIUtils.getDarker(colour1, 0.85);
	}

	@Override
	public boolean isOpaque() {
		return !fillGradient;
	}

	@Override
	public void paintComponent(Graphics g) {
		if (fillGradient) {
			int width = getWidth();
			int height = getHeight();

			Graphics2D g2 = (Graphics2D) g;
			Paint originalPaint = g2.getPaint();
			GradientPaint fade = new GradientPaint(0, height, colour2, 0, (int) (height * 0.5), colour1);

			g2.setPaint(fade);
			g2.fillRect(0, 0, width, height);

			g2.setPaint(originalPaint);
		}
		super.paintComponent(g);
	}

}
