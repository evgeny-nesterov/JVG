package ru.nest.jvg.shape.paint;

public class Endings implements Cloneable {
	public final static int TYPE_NONE = 0;

	public final static int TYPE_LAST_ENDING = 1;

	public final static int TYPE_FIRST_ENDING = 2;

	public final static int TYPE_BOTH_ENDING = 3;

	public final static int TYPE_ALL_ENDINGS = 4;

	public final static int FIGURE_CIRCLE = 0;

	public final static int FIGURE_SQUARE = 1;

	public final static int FIGURE_ARROW = 2;

	public final static int FIGURE_ROMB = 3;

	public Endings() {
	}

	public Endings(int type) {
		setEndingType(type);
	}

	public Endings(int type, boolean direct, int figure, boolean fill) {
		set(type, direct, figure, fill);
	}

	public Endings(Endings endings) {
		set(endings.getEndingType(), endings.isDirect(), endings.getFigure(), endings.isFill());
	}

	public void set(int type, boolean direct, int figure, boolean fill) {
		setEndingType(type);
		setDirect(direct);
		setFigure(figure);
		setFill(fill);
	}

	private int type = TYPE_LAST_ENDING;

	public int getEndingType() {
		return type;
	}

	public void setEndingType(int type) {
		this.type = type;
	}

	private boolean direct = true;

	public boolean isDirect() {
		return direct;
	}

	public void setDirect(boolean direct) {
		this.direct = direct;
	}

	private int figure = FIGURE_ARROW;

	public int getFigure() {
		return figure;
	}

	public void setFigure(int figure) {
		this.figure = figure;
	}

	private boolean fill = true;

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException exc) {
			exc.printStackTrace();
			return null;
		}
	}
}
