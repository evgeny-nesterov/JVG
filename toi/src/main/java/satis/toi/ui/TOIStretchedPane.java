package satis.toi.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import satis.toi.TOIPane;

public class TOIStretchedPane extends TOIPane {
	private static final long serialVersionUID = 1L;

	private int fixedWidth;

	private int fixedHeight;

	public TOIStretchedPane(int fixedWidth, int fixedHeight) {
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				fit();
			}
		});
		setMinZoom(0);
		setMaxZoom(Integer.MAX_VALUE);
	}

	public int getFixedWidth() {
		return fixedWidth;
	}

	public int getFixedHeight() {
		return fixedHeight;
	}

	public void setFixedSize(int fixedWidth, int fixedHeight) {
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
	}

	public void fit() {
		double zoom = Math.min(getWidth() / (double) getFixedWidth(), getHeight() / (double) getFixedHeight());
		int w = (int) (getFixedWidth() * zoom);
		int h = (int) (getFixedHeight() * zoom);
		int ix = (int) ((getWidth() - w) / 2 / zoom);
		int iy = (int) ((getHeight() - h) / 2 / zoom);
		setDocumentSize(w, h);
		setDocumentInsets(new Insets(iy, ix, iy, ix));
		setZoom(zoom);
		repaint();
	}

	@Override
	protected void drawBackground(Graphics2D g) {
		double zoom = Math.min(getWidth() / (double) getFixedWidth(), getHeight() / (double) getFixedHeight());
		int w = (int) (getFixedWidth() * zoom);
		int h = (int) (getFixedHeight() * zoom);
		int x = ((getWidth() - w) / 2);
		int y = ((getHeight() - h) / 2);

		g.setColor(new Color(10, 10, 10));
		g.fillRect(0, 0, getWidth() - 1, getHeight());
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth() - 1, getHeight());

		drawBackground(g, x, y, w, h);
	}

	protected void drawBackground(Graphics2D g, int x, int y, int w, int h) {
		g.setColor(getBackground());
		g.fillRect(x, y, w, h);
	}
}
