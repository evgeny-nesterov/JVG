package ru.nest.jvg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class JVGStretchedPane extends JVGPane {
	private int fixedWidth;

	private int fixedHeight;

	private Color documentColor = Color.black;

	public JVGStretchedPane(int fixedWidth, int fixedHeight) {
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				fit();
			}
		});
		setOpaque(true);
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
		setDocumentSize(new Dimension(w, h));
		setDocumentInsets(new Insets(iy, ix, iy, ix));
		setZoom(zoom);
		repaint();
	}

	@Override
	public void paintBeforeTransformed(Graphics2D g) {
		double zoom = Math.min(getWidth() / (double) getFixedWidth(), getHeight() / (double) getFixedHeight());
		int w = (int) (getFixedWidth() * zoom);
		int h = (int) (getFixedHeight() * zoom);
		int x = ((getWidth() - w) / 2);
		int y = ((getHeight() - h) / 2);
		g.clipRect(x, y, w, h);
		super.paintBeforeTransformed(g);
	}

	@Override
	protected void paintBackground(Graphics2D g) {
		super.paintBackground(g);

		double zoom = Math.min(getWidth() / (double) getFixedWidth(), getHeight() / (double) getFixedHeight());
		int w = (int) (getFixedWidth() * zoom);
		int h = (int) (getFixedHeight() * zoom);
		int x = ((getWidth() - w) / 2);
		int y = ((getHeight() - h) / 2);

		g.setColor(documentColor);
		g.fillRect(0, 0, getWidth() - 1, getHeight());
		g.setColor(documentColor.darker());
		g.drawRect(0, 0, getWidth() - 1, getHeight());

		drawBackground(g, x, y, w, h);
	}

	protected void drawBackground(Graphics2D g, int x, int y, int w, int h) {
		g.setColor(getBackground());
		g.fillRect(x, y, w, h);
	}

	public Color getDocumentColor() {
		return documentColor;
	}

	public void setDocumentColor(Color documentColor) {
		this.documentColor = documentColor;
	}
}
