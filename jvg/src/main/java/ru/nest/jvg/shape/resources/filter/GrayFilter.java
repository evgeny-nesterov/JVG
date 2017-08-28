package ru.nest.jvg.shape.resources.filter;

import org.jdom2.Element;

import ru.nest.jvg.parser.JVGParseUtil;

class GrayFilter extends JVGImageFilter {
	public final static String NAME = "gray";

	private boolean brighter = true;

	public boolean isBrighter() {
		return brighter;
	}

	public void setBrighter(boolean brighter) {
		this.brighter = brighter;
	}

	private int percent = 50;

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		int gray = (int) ((0.30 * ((rgb >> 16) & 0xff) + 0.59 * ((rgb >> 8) & 0xff) + 0.11 * (rgb & 0xff)) / 3);

		if (brighter) {
			gray = (255 - ((255 - gray) * (100 - percent) / 100));
		} else {
			gray = (gray * (100 - percent) / 100);
		}

		if (gray < 0) {
			gray = 0;
		}
		if (gray > 255) {
			gray = 255;
		}
		return (rgb & 0xff000000) | (gray << 16) | (gray << 8) | (gray << 0);
	}

	@Override
	public void load(Element e) {
		String brighter = e.getAttributeValue("brighter");
		if (brighter != null) {
			this.brighter = JVGParseUtil.getBoolean(brighter);
		}

		String percent = e.getAttributeValue("percent");
		if (percent != null) {
			this.percent = JVGParseUtil.getInteger(percent, 50);
		}
	}

	@Override
	public void save(Element e) {
		if (!brighter) {
			e.setAttribute("brighter", "no");
		}

		if (percent != 50) {
			e.setAttribute("percent", Integer.toString(percent));
		}
	}
}
