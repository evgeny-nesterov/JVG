package ru.nest.layout;

import java.awt.Insets;
import java.util.Arrays;

public class TableConstraints {
	public TableConstraints(int x, int y, int w, int h) {
		this(x, y, w, h, 0, 0, null);
	}

	public TableConstraints(int x, int y, double WX, double WY) {
		this(x, y, 1, 1, WX, WY, null);
	}

	public TableConstraints(int x, int y, int w, int h, double WX, double WY) {
		this(x, y, w, h, WX, WY, null);
	}

	public TableConstraints(int x, int y, int w, int h, double WX, double WY, Insets insets) {
		if (w < 1) {
			w = 1;
		}

		if (h < 1) {
			h = 1;
		}

		if (WX < 0) {
			WX = 0;
		}

		if (WY < 0) {
			WY = 0;
		}

		double[] wx = new double[w];
		double[] wy = new double[h];
		if (w != 0) {
			Arrays.fill(wx, WX / w);
		}

		if (h != 0) {
			Arrays.fill(wy, WY / h);
		}

		this.x = x;
		this.y = y;
		this.wx = wx;
		this.wy = wy;
		setInsets(insets);
	}

	public TableConstraints(int x, int y) {
		this(x, y, new double[] { 0 }, new double[] { 0 }, null);
	}

	public TableConstraints(int x, int y, double[] wx, double[] wy) {
		this(x, y, wx, wy, null);
	}

	public TableConstraints(int x, int y, double[] wx, double[] wy, Insets insets) {
		if (x < 0) {
			x = 0;
		}

		if (y < 0) {
			y = 0;
		}

		if (wx == null || wx.length == 0) {
			wx = new double[] { 0 };
		}

		if (wy == null || wy.length == 0) {
			wy = new double[] { 0 };
		}

		this.x = x;
		this.y = y;
		this.wx = wx;
		this.wy = wy;
		setInsets(insets);
	}

	public void setInsets(Insets insets) {
		if (insets == null) {
			insets = new Insets(0, 0, 0, 0);
		}

		this.insets = insets;
	}

	public int x;

	public int y;

	public double[] wx;

	public double[] wy;

	public Insets insets;

	public void addWidth(int w, boolean direct) {
		addWidth(new double[w], direct);
	}

	public void addWidth(double[] addWX, boolean direct) {
		if (addWX != null && addWX.length > 0) {
			int addLen = addWX.length, len = wx.length;
			double[] oldWX = wx;
			wx = new double[len + addLen];

			if (direct) {
				System.arraycopy(oldWX, 0, wx, 0, len);
				System.arraycopy(addWX, 0, wx, len, addLen);
			} else {
				System.arraycopy(addWX, 0, wx, 0, addLen);
				System.arraycopy(oldWX, 0, wx, addLen, len);
				x -= addLen;
			}
		}
	}

	public void addHeigth(int h, boolean direct) {
		addHeigth(new double[h], direct);
	}

	public void addHeigth(double[] addWY, boolean direct) {
		if (addWY != null && addWY.length > 0) {
			int addLen = addWY.length, len = wy.length;
			double[] oldWY = wy;
			wy = new double[len + addLen];

			if (direct) {
				System.arraycopy(oldWY, 0, wy, 0, len);
				System.arraycopy(addWY, 0, wy, len, addLen);
			} else {
				System.arraycopy(addWY, 0, wy, 0, addLen);
				System.arraycopy(oldWY, 0, wy, addLen, len);
				y -= addLen;
			}
		}
	}
}
