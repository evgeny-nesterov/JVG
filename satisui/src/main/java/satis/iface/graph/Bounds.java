package satis.iface.graph;

public class Bounds {
	public Bounds() {

	}

	public Bounds(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public void setBounds(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public double x = 0;

	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}

	public double y = 0;

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}

	public double w = 0;

	public void setW(double w) {
		this.w = w;
	}

	public double getW() {
		return w;
	}

	public double h = 0;

	public void setH(double h) {
		this.h = h;
	}

	public double getH() {
		return h;
	}

	public boolean contains(double X, double Y) {
		return X >= x && Y >= y && X <= x + w && Y <= y + h;
	}
}
