package ru.nest.jvg;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

public class SelectionRotater extends Thread {
	private JVGRoot root;

	public SelectionRotater(JVGRoot root) {
		this.root = root;
	}

	private Repainter repainter;

	public SelectionRotater(JVGRoot root, Repainter repainter) {
		this(root);
		this.repainter = repainter;
	}

	private float selectionPhase = 0f;

	private boolean stop = false;

	public void stopRotate() {
		stop = true;
	}

	private Stroke selectionStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, new float[] { 3f, 3f }, 0f);

	public Stroke getStroke() {
		return selectionStroke;
	}

	private Map<Float, Stroke> strokes = new HashMap<>();

	@Override
	public void run() {
		while (!stop) {
			try {
				sleep(100);
			} catch (InterruptedException exc) {
				System.err.println("SelectionRotater: exit with error " + exc);
				return;
			}

			selectionPhase = (selectionPhase + 1f) % 6f;
			selectionStroke = strokes.get(selectionPhase);
			if (selectionStroke == null) {
				selectionStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, new float[] { 3f, 3f }, selectionPhase);
				strokes.put(selectionPhase, selectionStroke);
			}
			repaint();
		}
	}

	public void repaint() {
		if (repainter != null) {
			repainter.repaint(root);
		} else {
			root.repaint();
		}
	}
}
