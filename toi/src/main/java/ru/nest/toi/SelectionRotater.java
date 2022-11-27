package ru.nest.toi;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

public class SelectionRotater extends Thread {
	private TOIPane pane;

	private float selectionPhase = 0f;

	private boolean stop = false;

	private Stroke selectionStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, new float[] { 3f, 3f }, 0f);

	private Map<Float, Stroke> strokes = new HashMap<>();

	public SelectionRotater(TOIPane pane) {
		this.pane = pane;
	}

	public void stopRotate() {
		stop = true;
	}

	public Stroke getStroke() {
		return selectionStroke;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				sleep(100);
			} catch (InterruptedException exc) {
				System.err.println("SelectionRotater: exit with error " + exc.toString());
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
		pane.repaint();
	}
}
